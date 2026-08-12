package com.knowledge.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.knowledge.common.Result;
import com.knowledge.entity.Tag;
import com.knowledge.service.TagService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/tag")
public class TagController {

    @Resource
    private TagService tagService;

    // 查询当前用户的标签列表
    @GetMapping("/list")
    public Result<?> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Tag> list = tagService.list(
                new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getUserId, userId)
                        .orderByDesc(Tag::getCreateTime));
        return Result.success(list);
    }

    // 新增标签
    @PostMapping
    public Result<?> create(@RequestBody Tag tag, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        tag.setUserId(userId);
        tagService.save(tag);
        return Result.success("创建成功");
    }

    // 更新标签
    @PutMapping
    public Result<?> update(@RequestBody Tag tag, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Tag db = tagService.getById(tag.getId());
        if (db == null || !db.getUserId().equals(userId)) {
            return Result.error("标签不存在或无权限");
        }
        db.setName(tag.getName());
        db.setColor(tag.getColor());
        tagService.updateById(db);
        return Result.success("更新成功");
    }

    // 删除标签
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Tag db = tagService.getById(id);
        if (db == null || !db.getUserId().equals(userId)) {
            return Result.error("标签不存在或无权限");
        }
        tagService.removeById(id);
        return Result.success("删除成功");
    }
}
