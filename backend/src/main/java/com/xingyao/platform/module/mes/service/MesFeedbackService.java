package com.xingyao.platform.module.mes.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyao.platform.common.BizException;
import com.xingyao.platform.common.LoginUser;
import com.xingyao.platform.module.mes.entity.*;
import com.xingyao.platform.module.mes.mapper.*;
import com.xingyao.platform.module.system.entity.SysUser;
import com.xingyao.platform.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MesFeedbackService {

    private final MesFeedbackMapper feedbackMapper;
    private final MesTaskMapper taskMapper;
    private final MesTaskMemberMapper memberMapper;
    private final MesStationMemberMapper stationMemberMapper;
    private final SysUserMapper userMapper;

    public IPage<MesFeedback> page(int pageNum, int pageSize, String status, Long taskId, String keyword) {
        LambdaQueryWrapper<MesFeedback> qw = new LambdaQueryWrapper<>();
        qw.eq(StringUtils.hasText(status), MesFeedback::getStatus, status);
        qw.eq(taskId != null, MesFeedback::getTaskId, taskId);
        qw.like(StringUtils.hasText(keyword), MesFeedback::getFeedbackNo, keyword);
        qw.orderByDesc(MesFeedback::getId);
        IPage<MesFeedback> result = feedbackMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        enrich(result.getRecords());
        return result;
    }

    public void enrich(List<MesFeedback> records) {
        if (records.isEmpty()) {
            return;
        }
        Set<Long> taskIds = records.stream().map(MesFeedback::getTaskId).collect(Collectors.toSet());
        Set<Long> userIds = records.stream().map(MesFeedback::getUserId).collect(Collectors.toSet());
        Map<Long, MesTask> taskMap = taskMapper.selectBatchIds(taskIds).stream()
                .collect(Collectors.toMap(MesTask::getId, Function.identity()));
        Map<Long, SysUser> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
        for (MesFeedback fb : records) {
            MesTask task = taskMap.get(fb.getTaskId());
            if (task != null) {
                fb.setTaskNo(task.getTaskNo());
                fb.setPlanQty(task.getPlanQty());
                fb.setFinishedQty(task.getFinishedQty());
            }
            SysUser user = userMap.get(fb.getUserId());
            fb.setUserName(user == null ? null : user.getRealName());
        }
    }

    /** 报工：JB-4 累计上限校验 + JB-5 关联人员校验 */
    @Transactional
    public MesFeedback create(Long taskId, BigDecimal qty, String remark, LoginUser currentUser) {
        MesTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BizException("排产任务不存在");
        }
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("报工数量必须大于 0");
        }
        if (task.getStatus() != null && task.getStatus() == 2) {
            throw new BizException("任务已完工，不能继续报工");
        }
        checkRelatedUser(task, currentUser.getId());

        BigDecimal reported = nvl(taskMapper.sumReportedQty(taskId));
        BigDecimal remain = task.getPlanQty().subtract(reported);
        if (remain.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("累计报工数量已达排产数量，不允许继续报工");
        }
        if (qty.compareTo(remain) > 0) {
            throw new BizException("本次报工数量超过剩余可报数量（剩余 " + remain.stripTrailingZeros().toPlainString() + "）");
        }

        MesFeedback fb = new MesFeedback();
        fb.setFeedbackNo("FB" + System.currentTimeMillis());
        fb.setTaskId(taskId);
        fb.setUserId(currentUser.getId());
        fb.setQty(qty);
        fb.setStatus("PENDING");
        fb.setCreateTime(LocalDateTime.now());
        fb.setAuditRemark(remark);
        feedbackMapper.insert(fb);

        if (task.getStatus() == null || task.getStatus() == 0) {
            task.setStatus(1);
            taskMapper.updateById(task);
        }
        return fb;
    }

    /** JB-5：非本订单关联人员不允许报工 */
    private void checkRelatedUser(MesTask task, Long userId) {
        boolean related = memberMapper.selectCount(new LambdaQueryWrapper<MesTaskMember>()
                .eq(MesTaskMember::getTaskId, task.getId())
                .eq(MesTaskMember::getUserId, userId)) > 0;
        if (!related && "PERSON".equals(task.getAssignType()) && userId.equals(task.getAssigneeId())) {
            related = true;
        }
        if (!related && task.getWorkstationId() != null) {
            related = stationMemberMapper.selectCount(new LambdaQueryWrapper<MesStationMember>()
                    .eq(MesStationMember::getStationId, task.getWorkstationId())
                    .eq(MesStationMember::getUserId, userId)) > 0;
        }
        if (!related) {
            throw new BizException(403, "您不是本订单关联人员，不允许报工");
        }
    }

    /** 审核通过：JB-3 回写排产已生产数量，JB-4 后端强校验 */
    @Transactional
    public void approve(Long id, String remark, LoginUser auditor) {
        MesFeedback fb = feedbackMapper.selectById(id);
        if (fb == null) {
            throw new BizException("报工单不存在");
        }
        if (!"PENDING".equals(fb.getStatus())) {
            throw new BizException("该报工单已审核，不能重复操作");
        }
        MesTask task = taskMapper.selectById(fb.getTaskId());
        BigDecimal approved = nvl(feedbackMapper.sumApprovedQty(fb.getTaskId(), fb.getId()));
        if (approved.add(fb.getQty()).compareTo(task.getPlanQty()) > 0) {
            throw new BizException("审核后已生产数量将超过排产数量，请调整报工数量");
        }
        fb.setStatus("APPROVED");
        fb.setAuditUserId(auditor.getId());
        fb.setAuditTime(LocalDateTime.now());
        fb.setAuditRemark(remark);
        feedbackMapper.updateById(fb);

        BigDecimal totalApproved = nvl(feedbackMapper.sumApprovedQty(fb.getTaskId(), null));
        task.setFinishedQty(totalApproved);
        task.setStatus(totalApproved.compareTo(task.getPlanQty()) >= 0 ? 2 : 1);
        taskMapper.updateById(task);
    }

    @Transactional
    public void reject(Long id, String remark, LoginUser auditor) {
        MesFeedback fb = feedbackMapper.selectById(id);
        if (fb == null) {
            throw new BizException("报工单不存在");
        }
        if (!"PENDING".equals(fb.getStatus())) {
            throw new BizException("该报工单已审核，不能重复操作");
        }
        fb.setStatus("REJECTED");
        fb.setAuditUserId(auditor.getId());
        fb.setAuditTime(LocalDateTime.now());
        fb.setAuditRemark(remark);
        feedbackMapper.updateById(fb);
    }

    public Map<String, Object> stats() {
        Map<String, Object> map = new HashMap<>();
        map.put("pending", feedbackMapper.selectCount(new LambdaQueryWrapper<MesFeedback>()
                .eq(MesFeedback::getStatus, "PENDING")));
        map.put("approved", feedbackMapper.selectCount(new LambdaQueryWrapper<MesFeedback>()
                .eq(MesFeedback::getStatus, "APPROVED")));
        map.put("rejected", feedbackMapper.selectCount(new LambdaQueryWrapper<MesFeedback>()
                .eq(MesFeedback::getStatus, "REJECTED")));
        return map;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
