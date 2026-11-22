package com.xingyao.platform.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyao.platform.common.BizException;
import com.xingyao.platform.common.R;
import com.xingyao.platform.module.system.entity.SysLoginLog;
import com.xingyao.platform.module.system.entity.SysRole;
import com.xingyao.platform.module.system.entity.SysUser;
import com.xingyao.platform.module.system.mapper.SysLoginLogMapper;
import com.xingyao.platform.module.system.mapper.SysRoleMapper;
import com.xingyao.platform.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysLoginLogMapper loginLogMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @GetMapping("/users")
    public R<IPage<SysUser>> users(@RequestParam(defaultValue = "1") int pageNum,
                                   @RequestParam(defaultValue = "10") int pageSize,
                                   @RequestParam(required = false) String keyword) {
        IPage<SysUser> page = userMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysUser>()
                        .and(StringUtils.hasText(keyword), w -> w.like(SysUser::getUsername, keyword)
                                .or().like(SysUser::getRealName, keyword))
                        .orderByAsc(SysUser::getId));
        page.getRecords().forEach(u -> u.setPassword(null));
        return R.ok(page);
    }

    @GetMapping("/users/all")
    public R<List<SysUser>> allUsers() {
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStatus, 1).orderByAsc(SysUser::getId));
        users.forEach(u -> u.setPassword(null));
        return R.ok(users);
    }

    @PostMapping("/users")
    public R<Void> createUser(@RequestBody SysUser user) {
        if (!StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getRealName())) {
            throw new BizException("账号与姓名不能为空");
        }
        if (userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, user.getUsername())) > 0) {
            throw new BizException("账号已存在：" + user.getUsername());
        }
        String raw = StringUtils.hasText(user.getPassword()) ? user.getPassword() : "123456";
        user.setPassword(encoder.encode(raw));
        user.setStatus(user.getStatus() == null ? 1 : user.getStatus());
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        return R.ok();
    }

    @PutMapping("/users/{id}")
    public R<Void> updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(encoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return R.ok();
    }

    @DeleteMapping("/users/{id}")
    public R<Void> deleteUser(@PathVariable Long id) {
        userMapper.deleteById(id);
        return R.ok();
    }

    @GetMapping("/roles")
    public R<List<SysRole>> roles() {
        return R.ok(roleMapper.selectList(null));
    }

    @GetMapping("/login-logs")
    public R<IPage<SysLoginLog>> loginLogs(@RequestParam(defaultValue = "1") int pageNum,
                                           @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(loginLogMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysLoginLog>().orderByDesc(SysLoginLog::getId)));
    }
}
