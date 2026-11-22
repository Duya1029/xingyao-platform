package com.xingyao.platform.module.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyao.platform.common.BizException;
import com.xingyao.platform.common.LoginUser;
import com.xingyao.platform.module.system.entity.SysLoginLog;
import com.xingyao.platform.module.system.entity.SysMenu;
import com.xingyao.platform.module.system.entity.SysRole;
import com.xingyao.platform.module.system.entity.SysUser;
import com.xingyao.platform.module.system.mapper.SysLoginLogMapper;
import com.xingyao.platform.module.system.mapper.SysMenuMapper;
import com.xingyao.platform.module.system.mapper.SysRoleMapper;
import com.xingyao.platform.module.system.mapper.SysUserMapper;
import com.xingyao.platform.module.system.mapper.SysUserRoleMapper;
import com.xingyao.platform.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final SysMenuMapper menuMapper;
    private final SysLoginLogMapper loginLogMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @org.springframework.beans.factory.annotation.Value("${xingyao.captcha.enabled:true}")
    private boolean captchaEnabled;

    private final Map<String, CaptchaItem> captchaStore = new ConcurrentHashMap<>();
    private final Map<Long, LoginUser> userCache = new ConcurrentHashMap<>();

    private record CaptchaItem(String code, long expireAt) {
    }

    public boolean isCaptchaEnabled() {
        return captchaEnabled;
    }

    public Map<String, Object> captcha() {
        String code = randomCode();
        String key = UUID.randomUUID().toString().replace("-", "");
        captchaStore.put(key, new CaptchaItem(code, System.currentTimeMillis() + 120_000));
        captchaStore.entrySet().removeIf(e -> e.getValue().expireAt() < System.currentTimeMillis());
        Map<String, Object> res = new HashMap<>();
        res.put("key", key);
        res.put("image", drawImage(code));
        return res;
    }

    private String randomCode() {
        String chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String drawImage(String code) {
        int w = 120, h = 40;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(245, 247, 250));
        g.fillRect(0, 0, w, h);
        Random random = new Random();
        g.setColor(new Color(220, 226, 235));
        for (int i = 0; i < 6; i++) {
            g.drawLine(random.nextInt(w), random.nextInt(h), random.nextInt(w), random.nextInt(h));
        }
        g.setFont(new Font("Arial", Font.BOLD, 26));
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(30 + random.nextInt(80), 80 + random.nextInt(80), 150 + random.nextInt(80)));
            g.drawString(String.valueOf(code.charAt(i)), 12 + i * 25, 30 + random.nextInt(5));
        }
        g.dispose();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", bos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (Exception e) {
            throw new BizException("验证码生成失败");
        }
    }

    private void checkCaptcha(String key, String code) {
        CaptchaItem item = key == null ? null : captchaStore.remove(key);
        if (item == null) {
            throw new BizException(400, "验证码已失效，请刷新");
        }
        if (item.expireAt() < System.currentTimeMillis()) {
            throw new BizException(400, "验证码已过期，请刷新");
        }
        if (code == null || !item.code().equalsIgnoreCase(code.trim())) {
            throw new BizException(400, "验证码错误");
        }
    }

    public Map<String, Object> login(String username, String password, String captchaKey, String captchaCode, String ip) {
        if (captchaEnabled) {
            checkCaptcha(captchaKey, captchaCode);
        }
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            log(username, ip, 0, "用户不存在");
            throw new BizException(400, "账号或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            log(username, ip, 0, "账号已停用");
            throw new BizException(400, "账号已停用，请联系管理员");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log(username, ip, 0, "密码错误");
            throw new BizException(400, "账号或密码错误");
        }
        log(username, ip, 1, "登录成功");
        LoginUser loginUser = loadLoginUser(user);
        userCache.put(user.getId(), loginUser);
        Map<String, Object> res = new HashMap<>();
        res.put("token", jwtUtil.create(user.getId(), user.getUsername()));
        res.put("user", toUserMap(user, loginUser));
        return res;
    }

    @org.springframework.transaction.annotation.Transactional
    public void register(String username, String password, String realName, String phone) {
        if (username == null || !username.trim().matches("[A-Za-z0-9_]{3,20}")) {
            throw new BizException(400, "账号需为 3~20 位字母、数字或下划线");
        }
        if (password == null || password.trim().length() < 6) {
            throw new BizException(400, "密码至少 6 位");
        }
        if (realName == null || realName.isBlank()) {
            throw new BizException(400, "请填写姓名");
        }
        String name = username.trim();
        if (userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, name)) > 0) {
            throw new BizException(400, "账号已存在，请更换");
        }
        SysRole viewer = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, "VIEWER"));
        if (viewer == null) {
            throw new BizException(500, "系统缺少默认角色 VIEWER");
        }
        SysUser user = new SysUser();
        user.setUsername(name);
        user.setPassword(passwordEncoder.encode(password.trim()));
        user.setRealName(realName.trim());
        user.setPhone(phone);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        userRoleMapper.insertOne(user.getId(), viewer.getId());
    }

    private void log(String username, String ip, int success, String message) {
        SysLoginLog log = new SysLoginLog();
        log.setUsername(username);
        log.setIp(ip);
        log.setSuccess(success);
        log.setMessage(message);
        log.setCreateTime(LocalDateTime.now());
        loginLogMapper.insert(log);
    }

    public LoginUser loadLoginUser(Long userId) {
        LoginUser cached = userCache.get(userId);
        if (cached != null) {
            return cached;
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(401, "用户不存在");
        }
        LoginUser loginUser = loadLoginUser(user);
        userCache.put(userId, loginUser);
        return loginUser;
    }

    private LoginUser loadLoginUser(SysUser user) {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setRealName(user.getRealName());
        loginUser.setRoles(menuMapper.selectRoleCodes(user.getId()));
        Set<String> perms = new HashSet<>();
        for (SysMenu menu : menuMapper.selectByUserId(user.getId())) {
            if (menu.getPerms() != null && !menu.getPerms().isBlank()) {
                perms.add(menu.getPerms());
            }
        }
        loginUser.setPerms(perms);
        return loginUser;
    }

    public Map<String, Object> userInfo(LoginUser loginUser) {
        SysUser user = userMapper.selectById(loginUser.getId());
        return toUserMap(user, loginUser);
    }

    private Map<String, Object> toUserMap(SysUser user, LoginUser loginUser) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("realName", user.getRealName());
        map.put("deptName", user.getDeptName());
        map.put("roles", loginUser.getRoles());
        map.put("perms", loginUser.getPerms());
        return map;
    }

    public List<Map<String, Object>> menuTree(LoginUser loginUser) {
        List<SysMenu> menus = menuMapper.selectByUserId(loginUser.getId());
        return buildTree(menus, 0L);
    }

    private List<Map<String, Object>> buildTree(List<SysMenu> menus, Long parentId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (Objects.equals(menu.getParentId(), parentId)) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", menu.getId());
                node.put("name", menu.getName());
                node.put("path", menu.getPath());
                node.put("component", menu.getComponent());
                node.put("icon", menu.getIcon());
                node.put("type", menu.getType());
                node.put("perms", menu.getPerms());
                List<Map<String, Object>> children = buildTree(menus, menu.getId());
                if (!children.isEmpty()) {
                    node.put("children", children);
                }
                result.add(node);
            }
        }
        return result;
    }

    public void evictCache(Long userId) {
        userCache.remove(userId);
    }
}
