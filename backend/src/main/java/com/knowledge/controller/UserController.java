package com.knowledge.controller;

import com.knowledge.common.Result;
import com.knowledge.dto.PasswordDTO;
import com.knowledge.entity.User;
import com.knowledge.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private PasswordEncoder passwordEncoder;

    // 获取当前用户信息
    @GetMapping("/info")
    public Result<?> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        user.setPassword(null);
        return Result.success(user);
    }

    // 更新个人信息（含用户名、头像）
    @PutMapping("/info")
    public Result<?> updateUserInfo(@RequestBody User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User dbUser = userService.getById(userId);
        // 修改用户名时检查是否重复
        if (user.getUsername() != null && !user.getUsername().equals(dbUser.getUsername())) {
            if (userService.getByUsername(user.getUsername()) != null) {
                return Result.error("用户名已被占用");
            }
            dbUser.setUsername(user.getUsername());
        }
        dbUser.setNickname(user.getNickname());
        if (user.getEmail() != null && !user.getEmail().isEmpty()
                && !user.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            return Result.error("邮箱格式不正确");
        }
        dbUser.setEmail(user.getEmail());
        dbUser.setAvatar(user.getAvatar());
        userService.updateById(dbUser);
        dbUser.setPassword(null);
        return Result.success(dbUser);
    }

    // 修改密码
    @PutMapping("/password")
    public Result<?> updatePassword(@Validated @RequestBody PasswordDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return Result.error("旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userService.updateById(user);
        return Result.success("密码修改成功");
    }
}
