package com.xingyao.platform.module.mes.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyao.platform.common.BizException;
import com.xingyao.platform.module.mes.entity.*;
import com.xingyao.platform.module.mes.mapper.*;
import com.xingyao.platform.module.system.entity.SysUser;
import com.xingyao.platform.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MesTaskService {

    private final MesTaskMapper taskMapper;
    private final MesTaskMemberMapper memberMapper;
    private final MesStationMemberMapper stationMemberMapper;
    private final MesWorkstationMapper workstationMapper;
    private final MesRouteMapper routeMapper;
    private final MesWorkOrderMapper workOrderMapper;
    private final SysUserMapper userMapper;

    public IPage<MesTask> page(int pageNum, int pageSize, String keyword, String assignType, Integer status) {
        LambdaQueryWrapper<MesTask> qw = new LambdaQueryWrapper<>();
        qw.like(StringUtils.hasText(keyword), MesTask::getTaskNo, keyword);
        qw.eq(StringUtils.hasText(assignType), MesTask::getAssignType, assignType);
        qw.eq(status != null, MesTask::getStatus, status);
        qw.orderByDesc(MesTask::getId);
        IPage<MesTask> result = taskMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        enrich(result.getRecords());
        return result;
    }

    public void enrich(List<MesTask> records) {
        if (records.isEmpty()) {
            return;
        }
        Set<Long> orderIds = records.stream().map(MesTask::getWorkOrderId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> routeIds = records.stream().map(MesTask::getRouteId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> stationIds = records.stream().map(MesTask::getWorkstationId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> userIds = records.stream()
                .filter(t -> "PERSON".equals(t.getAssignType()))
                .map(MesTask::getAssigneeId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, MesWorkOrder> orderMap = orderIds.isEmpty() ? Map.of()
                : workOrderMapper.selectBatchIds(orderIds).stream().collect(Collectors.toMap(MesWorkOrder::getId, Function.identity()));
        Map<Long, MesRoute> routeMap = routeIds.isEmpty() ? Map.of()
                : routeMapper.selectBatchIds(routeIds).stream().collect(Collectors.toMap(MesRoute::getId, Function.identity()));
        Map<Long, MesWorkstation> stationMap = stationIds.isEmpty() ? Map.of()
                : workstationMapper.selectBatchIds(stationIds).stream().collect(Collectors.toMap(MesWorkstation::getId, Function.identity()));
        Map<Long, SysUser> userMap = userIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(SysUser::getId, Function.identity()));

        for (MesTask task : records) {
            MesWorkOrder order = orderMap.get(task.getWorkOrderId());
            if (order != null) {
                task.setOrderNo(order.getOrderNo());
                task.setProductName(order.getProductName());
            }
            MesRoute route = routeMap.get(task.getRouteId());
            if (route != null) {
                task.setRouteName(route.getName());
            }
            MesWorkstation station = stationMap.get(task.getWorkstationId());
            if (station != null) {
                task.setWorkstationName(station.getName());
            }
            if ("PERSON".equals(task.getAssignType())) {
                SysUser user = userMap.get(task.getAssigneeId());
                task.setAssigneeName(user == null ? null : user.getRealName());
            } else {
                MesWorkstation assignStation = stationMap.get(task.getAssigneeId());
                task.setAssigneeName(assignStation == null ? task.getWorkstationName() : assignStation.getName());
            }
            BigDecimal reported = taskMapper.sumReportedQty(task.getId());
            task.setReportedQty(reported);
            task.setRemainQty(task.getPlanQty().subtract(reported));
        }
    }

    @Transactional
    public void save(MesTask task) {
        validateAssign(task);
        if (task.getId() == null) {
            if (!StringUtils.hasText(task.getTaskNo())) {
                task.setTaskNo("TK" + System.currentTimeMillis());
            }
            if (task.getFinishedQty() == null) {
                task.setFinishedQty(BigDecimal.ZERO);
            }
            if (task.getStatus() == null) {
                task.setStatus(0);
            }
            task.setCreateTime(java.time.LocalDateTime.now());
            taskMapper.insert(task);
        } else {
            taskMapper.updateById(task);
        }
        rebuildMembers(task.getId());
    }

    /** JB-1：指派方式只能是「个人」或「工作站」二选一 */
    private void validateAssign(MesTask task) {
        String type = task.getAssignType();
        if (!"PERSON".equals(type) && !"STATION".equals(type)) {
            throw new BizException("指派方式只能选择个人或工作站");
        }
        if (task.getAssigneeId() == null) {
            throw new BizException("PERSON".equals(type) ? "请选择指派人员" : "请选择指派工作站");
        }
        if ("PERSON".equals(type)) {
            if (userMapper.selectById(task.getAssigneeId()) == null) {
                throw new BizException("指派人员不存在");
            }
        } else {
            MesWorkstation station = workstationMapper.selectById(task.getAssigneeId());
            if (station == null) {
                throw new BizException("指派工作站不存在");
            }
            task.setWorkstationId(station.getId());
        }
        if (task.getPlanQty() == null || task.getPlanQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("排产数量必须大于 0");
        }
    }

    /** 重建订单关联人员（JB-5 判定依据） */
    @Transactional
    public void rebuildMembers(Long taskId) {
        MesTask task = taskMapper.selectById(taskId);
        memberMapper.delete(new LambdaQueryWrapper<MesTaskMember>().eq(MesTaskMember::getTaskId, taskId));
        Set<Long> userIds = new LinkedHashSet<>();
        if ("PERSON".equals(task.getAssignType())) {
            userIds.add(task.getAssigneeId());
        } else if (task.getWorkstationId() != null) {
            stationMemberMapper.selectList(new LambdaQueryWrapper<MesStationMember>()
                            .eq(MesStationMember::getStationId, task.getWorkstationId()))
                    .forEach(m -> userIds.add(m.getUserId()));
        }
        for (Long userId : userIds) {
            MesTaskMember member = new MesTaskMember();
            member.setTaskId(taskId);
            member.setUserId(userId);
            member.setSource("PERSON".equals(task.getAssignType()) ? "ASSIGNEE" : "STATION");
            memberMapper.insert(member);
        }
    }

    public MesTask getById(Long id) {
        MesTask task = taskMapper.selectById(id);
        if (task != null) {
            enrich(List.of(task));
        }
        return task;
    }

    public List<MesTaskMember> members(Long taskId) {
        return memberMapper.selectList(new LambdaQueryWrapper<MesTaskMember>().eq(MesTaskMember::getTaskId, taskId));
    }

    @Transactional
    public void delete(Long id) {
        memberMapper.delete(new LambdaQueryWrapper<MesTaskMember>().eq(MesTaskMember::getTaskId, id));
        taskMapper.deleteById(id);
    }
}
