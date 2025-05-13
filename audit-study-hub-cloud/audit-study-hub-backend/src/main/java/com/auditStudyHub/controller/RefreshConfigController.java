package com.auditStudyHub.controller;

import com.auditStudyHub.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.bus.endpoint.RefreshBusEndpoint;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * 配置刷新控制器
 * 
 * @author AuditStudyHub
 */
@RestController
@RequestMapping("/config")
@RefreshScope
@RequiredArgsConstructor
public class RefreshConfigController {

    private final ContextRefresher contextRefresher;
    private final RefreshBusEndpoint refreshBusEndpoint;

    /**
     * 刷新当前实例的配置
     * 
     * @return 刷新结果
     */
    @PostMapping("/refresh")
    public Result<Set<String>> refreshConfig() {
        Set<String> refreshedKeys = contextRefresher.refresh();
        return Result.success("配置刷新成功，共刷新了 " + refreshedKeys.size() + " 个配置项", refreshedKeys);
    }

    /**
     * 刷新所有实例的配置（集群范围）
     * 
     * @return 刷新结果
     */
    @PostMapping("/refresh/bus")
    public Result<String> refreshBus() {
        refreshBusEndpoint.busRefresh();
        return Result.success("集群配置刷新指令已发送");
    }
} 