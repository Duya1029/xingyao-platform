package com.xingyao.platform.module.dashboard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyao.platform.common.R;
import com.xingyao.platform.module.mes.entity.MesFeedback;
import com.xingyao.platform.module.mes.entity.MesTask;
import com.xingyao.platform.module.mes.mapper.MesFeedbackMapper;
import com.xingyao.platform.module.mes.mapper.MesTaskMapper;
import com.xingyao.platform.module.mes.service.MesTaskService;
import com.xingyao.platform.module.wms.entity.WmsItem;
import com.xingyao.platform.module.wms.mapper.WmsItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final MesTaskMapper taskMapper;
    private final MesFeedbackMapper feedbackMapper;
    private final WmsItemMapper itemMapper;
    private final MesTaskService taskService;

    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        Map<String, Object> map = new HashMap<>();
        map.put("taskTotal", taskMapper.selectCount(null));
        map.put("taskRunning", taskMapper.selectCount(new LambdaQueryWrapper<MesTask>().eq(MesTask::getStatus, 1)));
        map.put("taskFinished", taskMapper.selectCount(new LambdaQueryWrapper<MesTask>().eq(MesTask::getStatus, 2)));
        map.put("feedbackPending", feedbackMapper.selectCount(new LambdaQueryWrapper<MesFeedback>()
                .eq(MesFeedback::getStatus, "PENDING")));
        map.put("itemTotal", itemMapper.selectCount(null));

        List<MesTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<MesTask>()
                .orderByDesc(MesTask::getId).last("OFFSET 0 ROWS FETCH NEXT 5 ROWS ONLY"));
        taskService.enrich(tasks);
        map.put("recentTasks", tasks);

        BigDecimal planTotal = tasks.stream().map(MesTask::getPlanQty).reduce(BigDecimal.ZERO, BigDecimal::add);
        map.put("recentPlanQty", planTotal);
        return R.ok(map);
    }

    @GetMapping("/todo")
    public R<IPage<MesFeedback>> todo() {
        IPage<MesFeedback> page = feedbackMapper.selectPage(new Page<>(1, 5),
                new LambdaQueryWrapper<MesFeedback>().eq(MesFeedback::getStatus, "PENDING")
                        .orderByAsc(MesFeedback::getId));
        return R.ok(page);
    }
}
