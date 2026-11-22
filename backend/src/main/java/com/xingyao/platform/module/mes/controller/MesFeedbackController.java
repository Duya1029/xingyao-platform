package com.xingyao.platform.module.mes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xingyao.platform.common.R;
import com.xingyao.platform.common.UserContext;
import com.xingyao.platform.module.mes.entity.MesFeedback;
import com.xingyao.platform.module.mes.service.MesFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/mes/feedback")
@RequiredArgsConstructor
public class MesFeedbackController {

    private final MesFeedbackService feedbackService;

    @GetMapping
    public R<IPage<MesFeedback>> page(@RequestParam(defaultValue = "1") int pageNum,
                                      @RequestParam(defaultValue = "10") int pageSize,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(required = false) Long taskId,
                                      @RequestParam(required = false) String keyword) {
        return R.ok(feedbackService.page(pageNum, pageSize, status, taskId, keyword));
    }

    @PostMapping
    public R<MesFeedback> create(@RequestBody Map<String, Object> body) {
        Long taskId = Long.valueOf(String.valueOf(body.get("taskId")));
        BigDecimal qty = new BigDecimal(String.valueOf(body.get("qty")));
        String remark = body.get("remark") == null ? null : String.valueOf(body.get("remark"));
        return R.ok(feedbackService.create(taskId, qty, remark, UserContext.get()));
    }

    @PostMapping("/{id}/approve")
    public R<Void> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        feedbackService.approve(id, body == null ? null : body.get("remark"), UserContext.get());
        return R.ok();
    }

    @PostMapping("/{id}/reject")
    public R<Void> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        feedbackService.reject(id, body == null ? null : body.get("remark"), UserContext.get());
        return R.ok();
    }

    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        return R.ok(feedbackService.stats());
    }
}
