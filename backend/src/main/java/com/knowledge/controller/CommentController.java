package com.knowledge.controller;

import com.knowledge.common.Result;
import com.knowledge.entity.Comment;
import com.knowledge.entity.Note;
import com.knowledge.service.CommentService;
import com.knowledge.service.NoteService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Resource
    private CommentService commentService;
    @Resource
    private NoteService noteService;

    @PostMapping
    public Result<?> create(@RequestBody Comment comment, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (comment.getNoteId() == null || comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            return Result.error("留言内容不能为空");
        }
        if (comment.getContent().trim().length() > 1000) {
            return Result.error("留言内容不能超过1000个字符");
        }
        Note note = noteService.getById(comment.getNoteId());
        if (note == null || note.getShareStatus() == null || note.getShareStatus() != 2) {
            return Result.error("该帖子不存在或未通过审核");
        }
        comment.setUserId(userId);
        comment.setContent(comment.getContent().trim().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
        commentService.save(comment);
        return Result.success("留言成功");
    }

    @GetMapping("/list")
    public Result<?> list(@RequestParam Long noteId,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentService.pageCommentsByNoteId(noteId, page, size));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Comment comment = commentService.getById(id);
        if (comment == null || !comment.getUserId().equals(userId)) {
            return Result.error("留言不存在或无权限");
        }
        commentService.removeById(id);
        return Result.success("删除成功");
    }
}
