package com.knowledge.controller;

import com.knowledge.common.JwtUtil;
import com.knowledge.common.Result;
import com.knowledge.dto.LoginDTO;
import com.knowledge.dto.RegisterDTO;
import com.knowledge.entity.User;
import com.knowledge.service.OperationLogService;
import com.knowledge.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private UserService userService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private OperationLogService logService;

    // 登录
    @PostMapping("/login")
    public Result<?> login(@Validated @RequestBody LoginDTO dto, HttpServletRequest request) {
        User user = userService.getByUsername(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return Result.error("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("role", user.getRole());
        data.put("avatar", user.getAvatar());

        logService.log(user.getId(), user.getUsername(), "用户登录", "POST /api/auth/login", null, request.getRemoteAddr(), 1, null);
        return Result.success(data);
    }

    // 注册
    @PostMapping("/register")
    public Result<?> register(@Validated @RequestBody RegisterDTO dto, HttpServletRequest request) {
        if (userService.getByUsername(dto.getUsername()) != null) {
            return Result.error("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setEmail(dto.getEmail() != null ? dto.getEmail() : "");
        user.setRole("USER");
        user.setStatus(1);
        userService.save(user);

        logService.log(user.getId(), user.getUsername(), "用户注册", "POST /api/auth/register", null, request.getRemoteAddr(), 1, null);
        return Result.success("注册成功");
    }
}
