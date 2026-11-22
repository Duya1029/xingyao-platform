package com.xingyao.platform.module.mes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xingyao.platform.common.R;
import com.xingyao.platform.module.mes.entity.MesTask;
import com.xingyao.platform.module.mes.entity.MesTaskMember;
import com.xingyao.platform.module.mes.service.MesTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mes/tasks")
@RequiredArgsConstructor
public class MesTaskController {

    private final MesTaskService taskService;

    @GetMapping
    public R<IPage<MesTask>> page(@RequestParam(defaultValue = "1") int pageNum,
                                  @RequestParam(defaultValue = "10") int pageSize,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String assignType,
                                  @RequestParam(required = false) Integer status) {
        return R.ok(taskService.page(pageNum, pageSize, keyword, assignType, status));
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        MesTask task = taskService.getById(id);
        List<MesTaskMember> members = taskService.members(id);
        Map<String, Object> map = new HashMap<>();
        map.put("task", task);
        map.put("members", members);
        return R.ok(map);
    }

    @PostMapping
    public R<Void> create(@RequestBody MesTask task) {
        task.setId(null);
        taskService.save(task);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody MesTask task) {
        task.setId(id);
        taskService.save(task);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return R.ok();
    }
}
