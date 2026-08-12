package com.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.dto.NoteDTO;
import com.knowledge.entity.*;
import com.knowledge.mapper.*;
import com.knowledge.service.NoteService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

    @Resource
    private NoteTagMapper noteTagMapper;
    @Resource
    private TagMapper tagMapper;
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional
    public void createNote(NoteDTO dto, Long userId) {
        Note note = new Note();
        BeanUtils.copyProperties(dto, note);
        note.setUserId(userId);
        note.setViewCount(0);
        note.setCreateTime(LocalDateTime.now());
        note.setUpdateTime(LocalDateTime.now());
        save(note);
        // 保存标签关联
        saveNoteTags(note.getId(), dto.getTagIds());
    }

    @Override
    @Transactional
    public void updateNote(NoteDTO dto, Long userId) {
        Note note = getById(dto.getId());
        if (note == null || !note.getUserId().equals(userId)) {
            throw new RuntimeException("笔记不存在或无权限");
        }
        BeanUtils.copyProperties(dto, note);
        note.setUpdateTime(LocalDateTime.now());
        updateById(note);
        // 先删除旧标签关联，再保存新的
        noteTagMapper.delete(new LambdaQueryWrapper<NoteTag>().eq(NoteTag::getNoteId, note.getId()));
        saveNoteTags(note.getId(), dto.getTagIds());
    }

    @Override
    public IPage<Map<String, Object>> pageNotes(Long userId, String keyword, Long categoryId, Long tagId, int page, int size) {
        // 如果按标签筛选，先查出符合条件的笔记ID
        List<Long> noteIdsByTag = null;
        if (tagId != null) {
            noteIdsByTag = noteTagMapper.selectList(
                    new LambdaQueryWrapper<NoteTag>().eq(NoteTag::getTagId, tagId)
            ).stream().map(NoteTag::getNoteId).collect(Collectors.toList());
            if (noteIdsByTag.isEmpty()) {
                return new Page<>(page, size);
            }
        }

        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Note::getTitle, keyword).or().like(Note::getContent, keyword));
        }
        if (categoryId != null) {
            wrapper.eq(Note::getCategoryId, categoryId);
        }
        if (noteIdsByTag != null) {
            wrapper.in(Note::getId, noteIdsByTag);
        }
        wrapper.orderByDesc(Note::getIsTop).orderByDesc(Note::getUpdateTime);

        IPage<Note> notePage = page(new Page<>(page, size), wrapper);
        return convertNotePageToMap(notePage);
    }

    @Override
    public Map<String, Object> getNoteDetail(Long noteId, Long userId) {
        Note note = getById(noteId);
        if (note == null) {
            throw new RuntimeException("笔记不存在");
        }
        // 更新查看次数和最近查看时间
        note.setViewCount(note.getViewCount() + 1);
        note.setLastViewTime(LocalDateTime.now());
        updateById(note);

        Map<String, Object> result = new HashMap<>();
        result.put("note", note);
        // 查询关联标签
        List<NoteTag> noteTags = noteTagMapper.selectList(
                new LambdaQueryWrapper<NoteTag>().eq(NoteTag::getNoteId, noteId));
        if (!noteTags.isEmpty()) {
            List<Long> tagIds = noteTags.stream().map(NoteTag::getTagId).collect(Collectors.toList());
            result.put("tags", tagMapper.selectBatchIds(tagIds));
            result.put("tagIds", tagIds);
        } else {
            result.put("tags", Collections.emptyList());
            result.put("tagIds", Collections.emptyList());
        }
        // 查询分类名称
        if (note.getCategoryId() != null) {
            Category category = categoryMapper.selectById(note.getCategoryId());
            result.put("categoryName", category != null ? category.getName() : "");
        }
        return result;
    }

    @Override
    public List<Note> recentViewed(Long userId, int limit) {
        return list(new LambdaQueryWrapper<Note>()
                .eq(Note::getUserId, userId)
                .isNotNull(Note::getLastViewTime)
                .orderByDesc(Note::getLastViewTime)
                .last("LIMIT " + limit));
    }

    @Override
    public IPage<Map<String, Object>> adminPageNotes(String keyword, int page, int size) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Note::getTitle, keyword);
        }
        wrapper.orderByDesc(Note::getCreateTime);
        IPage<Note> notePage = page(new Page<>(page, size), wrapper);
        return convertNotePageToMap(notePage);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userMapper.selectCount(null));
        stats.put("totalNotes", count());
        stats.put("totalCategories", categoryMapper.selectCount(null));
        stats.put("totalTags", tagMapper.selectCount(null));

        // 最近7天每天新增笔记数（一次查询）
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(6).withHour(0).withMinute(0).withSecond(0);
        List<Map<String, Object>> dbResult = baseMapper.countByDay(sevenDaysAgo);
        Map<String, Long> dayCountMap = new HashMap<>();
        for (Map<String, Object> row : dbResult) {
            dayCountMap.put(row.get("date").toString(), ((Number) row.get("count")).longValue());
        }
        List<Map<String, Object>> dailyNotes = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String dateStr = LocalDateTime.now().minusDays(i).toLocalDate().toString();
            Map<String, Object> day = new HashMap<>();
            day.put("date", dateStr);
            day.put("count", dayCountMap.getOrDefault(dateStr, 0L));
            dailyNotes.add(day);
        }
        stats.put("dailyNotes", dailyNotes);
        return stats;
    }

    // 保存笔记-标签关联
    private void saveNoteTags(Long noteId, List<Long> tagIds) {
        if (tagIds != null && !tagIds.isEmpty()) {
            List<NoteTag> list = tagIds.stream().map(tagId -> {
                NoteTag nt = new NoteTag();
                nt.setNoteId(noteId);
                nt.setTagId(tagId);
                return nt;
            }).collect(Collectors.toList());
            noteTagMapper.batchInsert(list);
        }
    }

    // 将笔记分页结果转为包含额外信息的Map（批量查询，避免 N+1）
    private IPage<Map<String, Object>> convertNotePageToMap(IPage<Note> notePage) {
        IPage<Map<String, Object>> result = new Page<>(notePage.getCurrent(), notePage.getSize(), notePage.getTotal());
        List<Note> notes = notePage.getRecords();
        if (notes.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        // 批量查询分类
        Set<Long> categoryIds = notes.stream()
                .map(Note::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> categoryNameMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            categoryMapper.selectBatchIds(categoryIds)
                    .forEach(cat -> categoryNameMap.put(cat.getId(), cat.getName()));
        }

        // 批量查询笔记-标签关联
        List<Long> noteIds = notes.stream().map(Note::getId).collect(Collectors.toList());
        List<NoteTag> allNoteTags = noteTagMapper.selectList(
                new LambdaQueryWrapper<NoteTag>().in(NoteTag::getNoteId, noteIds));
        Map<Long, List<Long>> noteTagMap = allNoteTags.stream()
                .collect(Collectors.groupingBy(NoteTag::getNoteId,
                        Collectors.mapping(NoteTag::getTagId, Collectors.toList())));

        // 批量查询标签
        Set<Long> allTagIds = allNoteTags.stream().map(NoteTag::getTagId).collect(Collectors.toSet());
        Map<Long, Tag> tagMap = new HashMap<>();
        if (!allTagIds.isEmpty()) {
            tagMapper.selectBatchIds(allTagIds).forEach(tag -> tagMap.put(tag.getId(), tag));
        }

        // 组装结果
        List<Map<String, Object>> records = new ArrayList<>();
        for (Note note : notes) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", note.getId());
            map.put("title", note.getTitle());
            map.put("summary", note.getSummary());
            map.put("categoryId", note.getCategoryId());
            map.put("userId", note.getUserId());
            map.put("viewCount", note.getViewCount());
            map.put("isTop", note.getIsTop());
            map.put("status", note.getStatus());
            map.put("shareStatus", note.getShareStatus());
            map.put("createTime", note.getCreateTime());
            map.put("updateTime", note.getUpdateTime());
            map.put("categoryName", categoryNameMap.getOrDefault(note.getCategoryId(), ""));
            List<Long> tagIds = noteTagMap.getOrDefault(note.getId(), Collections.emptyList());
            map.put("tags", tagIds.stream().map(tagMap::get).filter(Objects::nonNull).collect(Collectors.toList()));
            records.add(map);
        }
        result.setRecords(records);
        return result;
    }

    // 添加作者昵称到结果集
    private void appendAuthorNames(IPage<Note> notePage, IPage<Map<String, Object>> result) {
        Set<Long> userIds = notePage.getRecords().stream()
                .map(Note::getUserId).collect(Collectors.toSet());
        Map<Long, String> nicknameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds)
                    .forEach(u -> nicknameMap.put(u.getId(),
                            u.getNickname() != null && !u.getNickname().isEmpty() ? u.getNickname() : u.getUsername()));
        }
        for (Map<String, Object> record : result.getRecords()) {
            Long uid = (Long) record.get("userId");
            record.put("authorName", nicknameMap.getOrDefault(uid, "未知用户"));
        }
    }

    @Override
    public void submitToShare(Long noteId, Long userId) {
        Note note = getById(noteId);
        if (note == null || !note.getUserId().equals(userId)) {
            throw new RuntimeException("笔记不存在或无权限");
        }
        if (note.getStatus() != 1) {
            throw new RuntimeException("只能分享已发布的笔记");
        }
        if (note.getShareStatus() != null && note.getShareStatus() == 1) {
            throw new RuntimeException("该笔记已在审核中");
        }
        note.setShareStatus(1);
        updateById(note);
    }

    @Override
    public IPage<Map<String, Object>> pageCommunityNotes(String keyword, int page, int size) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getShareStatus, 2);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Note::getTitle, keyword).or().like(Note::getSummary, keyword));
        }
        wrapper.orderByDesc(Note::getUpdateTime);
        IPage<Note> notePage = page(new Page<>(page, size), wrapper);
        IPage<Map<String, Object>> result = convertNotePageToMap(notePage);
        appendAuthorNames(notePage, result);
        return result;
    }

    @Override
    public IPage<Map<String, Object>> adminPageShareNotes(int page, int size) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getShareStatus, 1);
        wrapper.orderByAsc(Note::getUpdateTime);
        IPage<Note> notePage = page(new Page<>(page, size), wrapper);
        IPage<Map<String, Object>> result = convertNotePageToMap(notePage);
        appendAuthorNames(notePage, result);
        return result;
    }

    @Override
    public void reviewShare(Long noteId, Integer shareStatus) {
        if (shareStatus != 2 && shareStatus != 3) {
            throw new RuntimeException("无效的审核状态");
        }
        Note note = getById(noteId);
        if (note == null) {
            throw new RuntimeException("笔记不存在");
        }
        if (note.getShareStatus() != 1) {
            throw new RuntimeException("该笔记不在待审核状态");
        }
        note.setShareStatus(shareStatus);
        updateById(note);
    }
}
