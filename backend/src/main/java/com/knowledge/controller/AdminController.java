package com.knowledge.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledge.common.Result;
import com.knowledge.entity.*;
import com.knowledge.service.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Resource
    private UserService userService;
    @Resource
    private NoteService noteService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private TagService tagService;
    @Resource
    private OperationLogService logService;
    @Resource
    private CommentService commentService;

    // ========== 用户管理 ==========

    @GetMapping("/user/page")
    public Result<?> userPage(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword).or().like(User::getNickname, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userService.page(new Page<>(page, size), wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }

    @PutMapping("/user/status/{id}")
    public Result<?> toggleUserStatus(@PathVariable Long id, @RequestParam Integer status, HttpServletRequest request) {
        User user = userService.getById(id);
        if (user == null) return Result.error("用户不存在");
        user.setStatus(status);
        userService.updateById(user);
        logService.log((Long) request.getAttribute("userId"), (String) request.getAttribute("username"),
                (status == 1 ? "启用" : "禁用") + "用户：" + user.getUsername(),
                "PUT /api/admin/user/status/" + id, null, request.getRemoteAddr(), 1, null);
        return Result.success("操作成功");
    }

    @DeleteMapping("/user/{id}")
    public Result<?> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        User user = userService.getById(id);
        if (user == null) return Result.error("用户不存在");
        userService.removeById(id);
        logService.log((Long) request.getAttribute("userId"), (String) request.getAttribute("username"),
                "删除用户：" + user.getUsername(),
                "DELETE /api/admin/user/" + id, null, request.getRemoteAddr(), 1, null);
        return Result.success("删除成功");
    }

    // ========== 笔记管理 ==========

    @GetMapping("/note/page")
    public Result<?> notePage(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String keyword) {
        return Result.success(noteService.adminPageNotes(keyword, page, size));
    }

    @DeleteMapping("/note/{id}")
    public Result<?> deleteNote(@PathVariable Long id, HttpServletRequest request) {
        Note note = noteService.getById(id);
        if (note == null) return Result.error("笔记不存在");
        noteService.removeById(id);
        logService.log((Long) request.getAttribute("userId"), (String) request.getAttribute("username"),
                "管理员删除笔记：" + note.getTitle(),
                "DELETE /api/admin/note/" + id, null, request.getRemoteAddr(), 1, null);
        return Result.success("删除成功");
    }

    // ========== 分类管理 ==========

    @GetMapping("/category/page")
    public Result<?> categoryPage(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        return Result.success(categoryService.page(new Page<>(page, size),
                new LambdaQueryWrapper<Category>().orderByDesc(Category::getCreateTime)));
    }

    @DeleteMapping("/category/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.success("删除成功");
    }

    // ========== 标签管理 ==========

    @GetMapping("/tag/page")
    public Result<?> tagPage(@RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int size) {
        return Result.success(tagService.page(new Page<>(page, size),
                new LambdaQueryWrapper<Tag>().orderByDesc(Tag::getCreateTime)));
    }

    @DeleteMapping("/tag/{id}")
    public Result<?> deleteTag(@PathVariable Long id) {
        tagService.removeById(id);
        return Result.success("删除成功");
    }

    // ========== 数据统计 ==========

    @GetMapping("/statistics")
    public Result<?> statistics() {
        return Result.success(noteService.getStatistics());
    }

    // ========== 社区分享审核 ==========

    @GetMapping("/share/page")
    public Result<?> sharePage(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size) {
        return Result.success(noteService.adminPageShareNotes(page, size));
    }

    @PutMapping("/share/review/{id}")
    public Result<?> reviewShare(@PathVariable Long id,
                                 @RequestParam Integer shareStatus,
                                 HttpServletRequest request) {
        noteService.reviewShare(id, shareStatus);
        String action = shareStatus == 2 ? "通过" : "拒绝";
        logService.log((Long) request.getAttribute("userId"),
                (String) request.getAttribute("username"),
                action + "笔记分享，笔记ID：" + id,
                "PUT /api/admin/share/review/" + id, null, request.getRemoteAddr(), 1, null);
        return Result.success("审核完成");
    }

    // ========== 留言管理 ==========

    @GetMapping("/comment/page")
    public Result<?> commentPage(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Comment::getContent, keyword);
        }
        wrapper.orderByDesc(Comment::getCreateTime);
        return Result.success(commentService.page(new Page<>(page, size), wrapper));
    }

    @PutMapping("/comment/{id}")
    public Result<?> updateComment(@PathVariable Long id, @RequestBody Comment comment, HttpServletRequest request) {
        Comment db = commentService.getById(id);
        if (db == null) return Result.error("留言不存在");
        db.setContent(comment.getContent());
        commentService.updateById(db);
        logService.log((Long) request.getAttribute("userId"), (String) request.getAttribute("username"),
                "修改留言，留言ID：" + id,
                "PUT /api/admin/comment/" + id, null, request.getRemoteAddr(), 1, null);
        return Result.success("修改成功");
    }

    @DeleteMapping("/comment/{id}")
    public Result<?> deleteComment(@PathVariable Long id, HttpServletRequest request) {
        Comment comment = commentService.getById(id);
        if (comment == null) return Result.error("留言不存在");
        commentService.removeById(id);
        logService.log((Long) request.getAttribute("userId"), (String) request.getAttribute("username"),
                "删除留言，留言ID：" + id,
                "DELETE /api/admin/comment/" + id, null, request.getRemoteAddr(), 1, null);
        return Result.success("删除成功");
    }
}