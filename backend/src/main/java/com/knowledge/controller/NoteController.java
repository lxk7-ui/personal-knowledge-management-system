package com.knowledge.controller;

import com.knowledge.common.Result;
import com.knowledge.dto.NoteDTO;
import com.knowledge.entity.Note;
import com.knowledge.service.NoteService;
import com.knowledge.service.OperationLogService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/note")
public class NoteController {

    @Resource
    private NoteService noteService;
    @Resource
    private OperationLogService logService;

    // 创建笔记
    @PostMapping
    public Result<?> create(@Validated @RequestBody NoteDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        noteService.createNote(dto, userId);
        logService.log(userId, (String) request.getAttribute("username"), "创建笔记：" + dto.getTitle(),
                "POST /api/note", null, request.getRemoteAddr(), 1, null);
        return Result.success("创建成功");
    }

    // 更新笔记
    @PutMapping
    public Result<?> update(@Validated @RequestBody NoteDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        noteService.updateNote(dto, userId);
        logService.log(userId, (String) request.getAttribute("username"), "更新笔记：" + dto.getTitle(),
                "PUT /api/note", null, request.getRemoteAddr(), 1, null);
        return Result.success("更新成功");
    }

    // 删除笔记
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Note note = noteService.getById(id);
        if (note == null || !note.getUserId().equals(userId)) {
            return Result.error("笔记不存在或无权限");
        }
        noteService.removeById(id);
        logService.log(userId, (String) request.getAttribute("username"), "删除笔记：" + note.getTitle(),
                "DELETE /api/note/" + id, null, request.getRemoteAddr(), 1, null);
        return Result.success("删除成功");
    }

    // 笔记详情
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(noteService.getNoteDetail(id, userId));
    }

    // 分页查询笔记
    @GetMapping("/page")
    public Result<?> page(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Long categoryId,
                          @RequestParam(required = false) Long tagId,
                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(noteService.pageNotes(userId, keyword, categoryId, tagId, page, size));
    }

    // 最近查看的笔记
    @GetMapping("/recent")
    public Result<?> recent(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(noteService.recentViewed(userId, 10));
    }

    // 导出笔记为 TXT
    @GetMapping("/export/{id}")
    public Result<?> export(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Note note = noteService.getById(id);
        if (note == null || !note.getUserId().equals(userId)) {
            return Result.error("笔记不存在或无权限");
        }
        // 去除 HTML 标签，返回纯文本
        String text = note.getContent() != null ? note.getContent().replaceAll("<[^>]+>", "") : "";
        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("title", note.getTitle());
        data.put("content", text);
        return Result.success(data);
    }

    // 提交笔记到社区分享
    @PostMapping("/share/{id}")
    public Result<?> submitShare(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        noteService.submitToShare(id, userId);
        logService.log(userId, (String) request.getAttribute("username"),
                "提交笔记分享审核，笔记ID：" + id,
                "POST /api/note/share/" + id, null, request.getRemoteAddr(), 1, null);
        return Result.success("已提交审核");
    }

    // 社区笔记分页浏览
    @GetMapping("/community")
    public Result<?> communityPage(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String keyword) {
        return Result.success(noteService.pageCommunityNotes(keyword, page, size));
    }
}
