package com.xingyao.platform.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingyao.platform.module.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "JOIN sys_user_role ur ON ur.role_id = rm.role_id " +
            "WHERE ur.user_id = #{userId} AND m.visible = 1 ORDER BY m.sort, m.id")
    List<SysMenu> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT r.role_code FROM sys_role r JOIN sys_user_role ur ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<String> selectRoleCodes(@Param("userId") Long userId);
}
