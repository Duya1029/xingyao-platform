package com.xingyao.platform.module.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyao.platform.common.BizException;
import com.xingyao.platform.common.R;
import com.xingyao.platform.module.mes.entity.MesStationMember;
import com.xingyao.platform.module.mes.entity.MesWorkshop;
import com.xingyao.platform.module.mes.entity.MesWorkstation;
import com.xingyao.platform.module.mes.mapper.MesStationMemberMapper;
import com.xingyao.platform.module.mes.mapper.MesWorkshopMapper;
import com.xingyao.platform.module.mes.mapper.MesWorkstationMapper;
import com.xingyao.platform.module.system.entity.SysUser;
import com.xingyao.platform.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mes")
@RequiredArgsConstructor
public class MesWorkstationController {

    private final MesWorkstationMapper workstationMapper;
    private final MesWorkshopMapper workshopMapper;
    private final MesStationMemberMapper memberMapper;
    private final SysUserMapper userMapper;

    @GetMapping("/workstations")
    public R<List<MesWorkstation>> list(@RequestParam(required = false) String keyword) {
        List<MesWorkstation> list = workstationMapper.selectList(new LambdaQueryWrapper<MesWorkstation>()
                .like(StringUtils.hasText(keyword), MesWorkstation::getName, keyword)
                .orderByAsc(MesWorkstation::getId));
        Map<Long, MesWorkshop> workshopMap = workshopMapper.selectList(null).stream()
                .collect(Collectors.toMap(MesWorkshop::getId, Function.identity()));
        for (MesWorkstation ws : list) {
            MesWorkshop workshop = workshopMap.get(ws.getWorkshopId());
            ws.setWorkshopName(workshop == null ? null : workshop.getName());
            ws.setMemberCount(Math.toIntExact(memberMapper.selectCount(new LambdaQueryWrapper<MesStationMember>()
                    .eq(MesStationMember::getStationId, ws.getId()))));
        }
        return R.ok(list);
    }

    @GetMapping("/workshops")
    public R<List<MesWorkshop>> workshops() {
        return R.ok(workshopMapper.selectList(null));
    }

    @GetMapping("/workstations/{id}/members")
    public R<List<SysUser>> members(@PathVariable Long id) {
        List<Long> userIds = memberMapper.selectList(new LambdaQueryWrapper<MesStationMember>()
                        .eq(MesStationMember::getStationId, id))
                .stream().map(MesStationMember::getUserId).toList();
        if (userIds.isEmpty()) {
            return R.ok(List.of());
        }
        return R.ok(userMapper.selectBatchIds(userIds));
    }

    @PostMapping("/workstations/{id}/members")
    @Transactional
    public R<Void> saveMembers(@PathVariable Long id, @RequestBody List<Long> userIds) {
        memberMapper.delete(new LambdaQueryWrapper<MesStationMember>().eq(MesStationMember::getStationId, id));
        for (Long userId : userIds) {
            MesStationMember m = new MesStationMember();
            m.setStationId(id);
            m.setUserId(userId);
            memberMapper.insert(m);
        }
        return R.ok();
    }

    @PostMapping("/workstations")
    public R<Void> create(@RequestBody MesWorkstation ws) {
        if (!StringUtils.hasText(ws.getCode()) || !StringUtils.hasText(ws.getName())) {
            throw new BizException("工作站编码与名称不能为空");
        }
        workstationMapper.insert(ws);
        return R.ok();
    }

    @PutMapping("/workstations/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody MesWorkstation ws) {
        ws.setId(id);
        workstationMapper.updateById(ws);
        return R.ok();
    }

    @DeleteMapping("/workstations/{id}")
    public R<Void> delete(@PathVariable Long id) {
        memberMapper.delete(new LambdaQueryWrapper<MesStationMember>().eq(MesStationMember::getStationId, id));
        workstationMapper.deleteById(id);
        return R.ok();
    }
}
