package com.knowledge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.knowledge.entity.User;

public interface UserService extends IService<User> {
    User getByUsername(String username);
}
