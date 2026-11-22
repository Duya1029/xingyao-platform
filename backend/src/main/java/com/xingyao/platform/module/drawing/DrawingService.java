package com.xingyao.platform.module.drawing;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyao.platform.common.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 图纸批量识别：批量入口 + 队列 + 结果回写。
 * 识别引擎为占位实现，后续接入真实 OCR/CAD 解析时只替换 recognize()。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrawingService {

    private final DrwTaskMapper taskMapper;
    private final DrwFileMapper fileMapper;

    @Value("${xingyao.upload.dir}")
    private String uploadDir;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    private static final List<String> ALLOWED = List.of("pdf", "png", "jpg", "jpeg", "dwg", "dxf");

    public Long createBatch(List<MultipartFile> files, String operator) {
        if (files == null || files.isEmpty()) {
            throw new BizException("请至少选择一份图纸");
        }
        if (files.size() > 50) {
            throw new BizException("单次最多上传 50 份图纸");
        }
        for (MultipartFile file : files) {
            String ext = ext(file.getOriginalFilename());
            if (!ALLOWED.contains(ext)) {
                throw new BizException("不支持的文件类型：" + file.getOriginalFilename() + "（支持 " + String.join("/", ALLOWED) + "）");
            }
        }
        DrwTask task = new DrwTask();
        task.setTaskNo("DRW" + System.currentTimeMillis());
        task.setFileCount(files.size());
        task.setSuccessCount(0);
        task.setFailCount(0);
        task.setStatus("RUNNING");
        task.setCreateUser(operator);
        task.setCreateTime(LocalDateTime.now());
        taskMapper.insert(task);

        File dir = new File(uploadDir, task.getTaskNo());
        if (!dir.exists() && !dir.mkdirs()) {
            log.warn("创建上传目录失败: {}", dir.getAbsolutePath());
        }
        for (MultipartFile file : files) {
            DrwFile df = new DrwFile();
            df.setTaskId(task.getId());
            df.setFileName(file.getOriginalFilename());
            df.setFileSize(file.getSize());
            df.setStatus("PENDING");
            df.setRetryCount(0);
            fileMapper.insert(df);
            try {
                file.transferTo(new File(dir, df.getId() + "_" + file.getOriginalFilename()));
            } catch (Exception e) {
                log.warn("保存图纸失败: {}", e.getMessage());
            }
        }
        executor.submit(() -> processTask(task.getId()));
        return task.getId();
    }

    public void processTask(Long taskId) {
        List<DrwFile> files = fileMapper.selectList(new LambdaQueryWrapper<DrwFile>()
                .eq(DrwFile::getTaskId, taskId).eq(DrwFile::getStatus, "PENDING"));
        for (DrwFile file : files) {
            recognize(file);
            fileMapper.updateById(file);
        }
        refreshTask(taskId);
    }

    public void retry(Long fileId) {
        DrwFile file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new BizException("图纸不存在");
        }
        file.setStatus("PENDING");
        file.setRetryCount((file.getRetryCount() == null ? 0 : file.getRetryCount()) + 1);
        file.setFailReason(null);
        fileMapper.updateById(file);
        executor.submit(() -> {
            DrwFile latest = fileMapper.selectById(fileId);
            recognize(latest);
            fileMapper.updateById(latest);
            refreshTask(latest.getTaskId());
        });
    }

    private void recognize(DrwFile file) {
        try {
            Thread.sleep(600);
            String name = file.getFileName() == null ? "" : file.getFileName();
            String base = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
            // 占位识别结果：真实环境替换为 OCR/CAD 解析服务
            String result = String.format(
                    "{\"partNo\":\"%s\",\"material\":\"45#\",\"spec\":\"DN100\",\"qty\":1,\"confidence\":0.92}",
                    base.replace("\"", ""));
            file.setRecognizeResult(result);
            file.setStatus("SUCCESS");
            file.setFailReason(null);
        } catch (Exception e) {
            file.setStatus("FAILED");
            file.setFailReason("识别失败：" + e.getMessage());
        }
    }

    private void refreshTask(Long taskId) {
        List<DrwFile> files = fileMapper.selectList(new LambdaQueryWrapper<DrwFile>()
                .eq(DrwFile::getTaskId, taskId));
        long pending = files.stream().filter(f -> "PENDING".equals(f.getStatus())).count();
        int success = (int) files.stream().filter(f -> "SUCCESS".equals(f.getStatus())).count();
        int fail = (int) files.stream().filter(f -> "FAILED".equals(f.getStatus())).count();
        DrwTask task = taskMapper.selectById(taskId);
        task.setSuccessCount(success);
        task.setFailCount(fail);
        task.setStatus(pending > 0 ? "RUNNING" : (fail > 0 && success == 0 ? "FAILED" : "DONE"));
        taskMapper.updateById(task);
    }

    private String ext(String name) {
        if (name == null || !name.contains(".")) {
            return "";
        }
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
