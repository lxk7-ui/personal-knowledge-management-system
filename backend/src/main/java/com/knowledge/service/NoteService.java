package com.knowledge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.knowledge.dto.NoteDTO;
import com.knowledge.entity.Note;

import java.util.List;
import java.util.Map;

public interface NoteService extends IService<Note> {
    // 创建笔记（含标签关联）
    void createNote(NoteDTO dto, Long userId);
    // 更新笔记（含标签关联）
    void updateNote(NoteDTO dto, Long userId);
    // 分页查询笔记
    IPage<Map<String, Object>> pageNotes(Long userId, String keyword, Long categoryId, Long tagId, int page, int size);
    // 获取笔记详情（含标签）
    Map<String, Object> getNoteDetail(Long noteId, Long userId);
    // 最近查看的笔记
    List<Note> recentViewed(Long userId, int limit);
    // 管理员分页查询所有笔记
    IPage<Map<String, Object>> adminPageNotes(String keyword, int page, int size);
    // 统计数据
    Map<String, Object> getStatistics();
    // 提交笔记到社区分享
    void submitToShare(Long noteId, Long userId);
    // 社区笔记分页
    IPage<Map<String, Object>> pageCommunityNotes(String keyword, int page, int size);
    // 管理员查看待审核分享
    IPage<Map<String, Object>> adminPageShareNotes(int page, int size);
    // 管理员审核分享
    void reviewShare(Long noteId, Integer shareStatus);
}
