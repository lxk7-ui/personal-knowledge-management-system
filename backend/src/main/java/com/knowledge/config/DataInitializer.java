package com.knowledge.config;

import com.knowledge.entity.User;
import com.knowledge.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 数据初始化：首次启动时按环境变量创建管理员账号。
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Resource
    private UserService userService;
    @Resource
    private PasswordEncoder passwordEncoder;

    @Value("${app.initializer.admin-username:admin}")
    private String adminUsername;

    @Value("${app.initializer.admin-password:}")
    private String adminPassword;

    @Value("${app.initializer.admin-nickname:管理员}")
    private String adminNickname;

    @Override
    public void run(String... args) {
        if (adminPassword == null || adminPassword.trim().isEmpty()) {
            System.out.println(">>> 未设置 INITIAL_ADMIN_PASSWORD，已跳过初始管理员创建");
            return;
        }

        if (userService.getByUsername(adminUsername) == null) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setNickname(adminNickname);
            admin.setEmail("");
            admin.setAvatar("");
            admin.setRole("ADMIN");
            admin.setStatus(1);
            userService.save(admin);
            System.out.println(">>> 已创建初始管理员账号：" + adminUsername);
        }
    }
}
