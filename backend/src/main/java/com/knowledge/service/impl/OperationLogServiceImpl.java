package com.knowledge.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.entity.OperationLog;
import com.knowledge.mapper.OperationLogMapper;
import com.knowledge.service.OperationLogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    @Async
    public void log(Long userId, String username, String operation, String method, String params, String ip, Integer status, String errorMsg) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setOperation(operation);
        log.setMethod(method);
        log.setParams(params);
        log.setIp(ip);
        log.setStatus(status);
        log.setErrorMsg(errorMsg);
        log.setCreateTime(LocalDateTime.now());
        save(log);
    }
}
