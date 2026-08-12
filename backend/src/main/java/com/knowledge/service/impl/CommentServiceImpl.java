package com.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.entity.Comment;
import com.knowledge.entity.User;
import com.knowledge.mapper.CommentMapper;
import com.knowledge.mapper.UserMapper;
import com.knowledge.service.CommentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Resource
    private UserMapper userMapper;

    @Override
    public IPage<Map<String, Object>> pageCommentsByNoteId(Long noteId, int page, int size) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getNoteId, noteId);
        wrapper.orderByDesc(Comment::getCreateTime);
        IPage<Comment> commentPage = page(new Page<>(page, size), wrapper);

        IPage<Map<String, Object>> result = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        List<Comment> comments = commentPage.getRecords();
        if (comments.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        Set<Long> userIds = comments.stream().map(Comment::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(u -> userMap.put(u.getId(), u));
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (Comment c : comments) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("noteId", c.getNoteId());
            map.put("userId", c.getUserId());
            map.put("content", c.getContent());
            map.put("createTime", c.getCreateTime());
            User user = userMap.get(c.getUserId());
            if (user != null) {
                map.put("nickname", user.getNickname() != null && !user.getNickname().isEmpty() ? user.getNickname() : user.getUsername());
                map.put("avatar", user.getAvatar());
            } else {
                map.put("nickname", "未知用户");
                map.put("avatar", null);
            }
            records.add(map);
        }
        result.setRecords(records);
        return result;
    }
}
