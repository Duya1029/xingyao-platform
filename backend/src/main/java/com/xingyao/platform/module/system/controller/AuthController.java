package com.xingyao.platform.module.system.controller;

import com.xingyao.platform.common.LoginUser;
import com.xingyao.platform.common.R;
import com.xingyao.platform.common.UserContext;
import com.xingyao.platform.module.system.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/config")
    public R<Map<String, Object>> config() {
        return R.ok(Map.of("captchaEnabled", authService.isCaptchaEnabled()));
    }

    @GetMapping("/captcha")
    public R<Map<String, Object>> captcha() {
        return R.ok(authService.captcha());
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return R.ok(authService.login(body.get("username"), body.get("password"),
                body.get("captchaKey"), body.get("captchaCode"), ip));
    }

    @PostMapping("/register")
    public R<Void> register(@RequestBody Map<String, String> body) {
        authService.register(body.get("username"), body.get("password"),
                body.get("realName"), body.get("phone"));
        return R.ok();
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        return R.ok();
    }

    @GetMapping("/info")
    public R<Map<String, Object>> info() {
        LoginUser user = UserContext.get();
        return R.ok(authService.userInfo(user));
    }

    @GetMapping("/menus")
    public R<List<Map<String, Object>>> menus() {
        return R.ok(authService.menuTree(UserContext.get()));
    }
}
