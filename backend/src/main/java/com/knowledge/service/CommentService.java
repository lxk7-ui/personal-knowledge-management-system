package com.knowledge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.knowledge.entity.Comment;

import java.util.Map;

public interface CommentService extends IService<Comment> {
    IPage<Map<String, Object>> pageCommentsByNoteId(Long noteId, int page, int size);
}
