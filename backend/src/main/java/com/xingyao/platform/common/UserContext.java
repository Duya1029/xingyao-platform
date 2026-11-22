package com.xingyao.platform.common;

public class UserContext {
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long userId() {
        LoginUser u = HOLDER.get();
        if (u == null) {
            throw new BizException(401, "未登录或登录已过期");
        }
        return u.getId();
    }

    public static String username() {
        LoginUser u = HOLDER.get();
        return u == null ? "anonymous" : u.getUsername();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
