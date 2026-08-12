package com.knowledge.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledge.common.Result;
import com.knowledge.entity.OperationLog;
import com.knowledge.service.OperationLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/admin/log")
@PreAuthorize("hasRole('ADMIN')")
public class OperationLogController {

    @Resource
    private OperationLogService operationLogService;

    // 分页查询操作日志
    @GetMapping("/page")
    public Result<?> page(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "15") int size,
                          @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(OperationLog::getOperation, keyword)
                    .or().like(OperationLog::getUsername, keyword);
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);
        return Result.success(operationLogService.page(new Page<>(page, size), wrapper));
    }

    // 清空日志
    @DeleteMapping("/clear")
    public Result<?> clear() {
        operationLogService.remove(null);
        return Result.success("清空成功");
    }
}
