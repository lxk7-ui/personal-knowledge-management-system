package com.knowledge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.knowledge.entity.OperationLog;

public interface OperationLogService extends IService<OperationLog> {
    // 记录操作日志
    void log(Long userId, String username, String operation, String method, String params, String ip, Integer status, String errorMsg);
}
