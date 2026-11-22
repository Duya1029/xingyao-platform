package com.xingyao.platform.common;

import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class LoginUser {
    private Long id;
    private String username;
    private String realName;
    private List<String> roles;
    private Set<String> perms = new HashSet<>();
}
