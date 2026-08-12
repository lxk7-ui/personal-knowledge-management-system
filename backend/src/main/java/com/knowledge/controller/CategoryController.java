package com.knowledge.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.knowledge.common.Result;
import com.knowledge.entity.Category;
import com.knowledge.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    // 查询当前用户的分类列表
    @GetMapping("/list")
    public Result<?> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Category> list = categoryService.list(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getUserId, userId)
                        .orderByAsc(Category::getSortOrder));
        return Result.success(list);
    }

    // 新增分类
    @PostMapping
    public Result<?> create(@RequestBody Category category, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        category.setUserId(userId);
        categoryService.save(category);
        return Result.success("创建成功");
    }

    // 更新分类
    @PutMapping
    public Result<?> update(@RequestBody Category category, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Category db = categoryService.getById(category.getId());
        if (db == null || !db.getUserId().equals(userId)) {
            return Result.error("分类不存在或无权限");
        }
        db.setName(category.getName());
        db.setSortOrder(category.getSortOrder());
        categoryService.updateById(db);
        return Result.success("更新成功");
    }

    // 删除分类
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Category db = categoryService.getById(id);
        if (db == null || !db.getUserId().equals(userId)) {
            return Result.error("分类不存在或无权限");
        }
        categoryService.removeById(id);
        return Result.success("删除成功");
    }
}
