package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignDetailEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignNodeEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Service.*;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-04 10:49
 **/

@RestController
@RequestMapping(value = "/deploymentdesignnodes")
public class DeploymentDesignNodeController {

    private final DeploymentDesignNodeService deploymentDesignNodeService;
    private final DeviceService deviceService;
    private final ComponentHistoryService componentHistoryService;
    private final DeploymentDesignDetailService deploymentDesignDetailService;
    private final ComponentService componentService;

    @Autowired
    public DeploymentDesignNodeController(DeploymentDesignNodeService deploymentDesignNodeService, DeviceService deviceService, ComponentHistoryService componentHistoryService, DeploymentDesignDetailService deploymentDesignDetailService, ComponentService componentService) {
        this.deploymentDesignNodeService = deploymentDesignNodeService;
        this.deviceService = deviceService;
        this.componentHistoryService = componentHistoryService;
        this.deploymentDesignDetailService = deploymentDesignDetailService;
        this.componentService = componentService;
    }

    // 根据Id挂载设备
    @PostMapping(value = "/{deploymentDesignNodeId}/device/{deviceId}/bind")
    public ResultEntity bindDeviceById(@PathVariable(value = "deploymentDesignNodeId") String deploymentDesignNodeId, @PathVariable(value = "deviceId") String deviceId) {
        return ResultUtils.build(deploymentDesignNodeService.bindDeviceById(deploymentDesignNodeId, deviceService.getDeviceById(deviceId)));
    }

    // 根据Id删除部署设计节点
    @DeleteMapping(value = "/{deploymentDesignNodeId}")
    public ResultEntity bindDeviceById(@PathVariable(value = "deploymentDesignNodeId") String deploymentDesignNodeId) {
        return ResultUtils.build(deploymentDesignNodeService.deleteDeploymentDesignNodeById(deploymentDesignNodeService.getDeploymentDesignNodeById(deploymentDesignNodeId)));
    }

    // 根据id解绑设备
    @DeleteMapping(value = "/{deploymentDesignNodeId}/unbind")
    public ResultEntity unbindDeviceById(@PathVariable(value = "deploymentDesignNodeId") String deploymentDesignNodeId) {
        return ResultUtils.build(deploymentDesignNodeService.unbindDeviceById(deploymentDesignNodeId));
    }

    // 根据Id挂载设备
    @PostMapping(value = "/{deploymentDesignNodeId}/componenthistory/{componentHistoryId}/bind")
    public ResultEntity bindComponentHistoryById(@PathVariable(value = "deploymentDesignNodeId") String deploymentDesignNodeId, @PathVariable(value = "componentHistoryId") String componentHistoryId, @RequestParam(value = "keepLatest", defaultValue = "false") boolean keepLatest) {
        return ResultUtils.build(deploymentDesignDetailService.bindComponentHistoryByDeploymentDesignNode(deploymentDesignNodeService.getDeploymentDesignNodeById(deploymentDesignNodeId), componentHistoryService.getComponentHistoryById(componentHistoryId), keepLatest));
    }

    // 根据节点查询部署设计详情
    @GetMapping(value = "/{deploymentDesignNodeId}/deploymentdesigndetails")
    public ResultEntity getDeploymentDesignDetailsById(@PathVariable(value = "deploymentDesignNodeId") String deploymentDesignNodeId) {
        return ResultUtils.build(deploymentDesignDetailService.getDeploymentDesignDetailsByDeploymentDesignNode(deploymentDesignNodeService.getDeploymentDesignNodeById(deploymentDesignNodeId)));
    }

    // 查询可绑定的组件
    @GetMapping(value = "/{deploymentDesignNodeId}/components")
    public ResultEntity getAvailableComponentsById(@PathVariable(value = "deploymentDesignNodeId") String deploymentDesignNodeId) {
        DeploymentDesignNodeEntity deploymentDesignNodeEntity = deploymentDesignNodeService.getDeploymentDesignNodeById(deploymentDesignNodeId);
        List<DeploymentDesignDetailEntity> deploymentDesignDetailEntityList = deploymentDesignDetailService.getDeploymentDesignDetailsByDeploymentDesignNode(deploymentDesignNodeEntity);
        List<ComponentEntity> componentEntityList = componentService.getComponentsByDeletedAndProject(false, deploymentDesignNodeEntity.getDeploymentDesignEntity().getProjectEntity());
        for (DeploymentDesignDetailEntity deploymentDesignDetailEntity : deploymentDesignDetailEntityList) {
            if (deploymentDesignDetailEntity.getComponentEntity() != null) {
                componentEntityList.remove(deploymentDesignDetailEntity.getComponentEntity());
            }
        }
        return ResultUtils.build(componentEntityList);
    }

    // 下发整个节点
    @PutMapping(value = "/{deploymentDesignNodeId}/deploy")
    public void deployDeploymentDesignNodeById(@PathVariable(value = "deploymentDesignNodeId") String deploymentDesignNodeId) {
        deploymentDesignDetailService.deployDeploymentDesignDetailByDeploymentDesignNode(deploymentDesignNodeService.getDeploymentDesignNodeById(deploymentDesignNodeId));
    }

    // 根据部署设计Id及设备Id进行扫描
    @GetMapping(value = "/{deploymentDesignNodeId}/scan")
    public ResultEntity scanDeploymentDesignDetailsById(@PathVariable(value = "deploymentDesignNodeId") String deploymentDesignNodeId, @RequestParam(value = "extensions", required = false, defaultValue = "") String... extensions) throws InterruptedException, ExecutionException, IOException {
        return ResultUtils.build(deploymentDesignDetailService.scanDeploymentDesignDetailsByDeploymentDesignNode(deploymentDesignNodeService.getDeploymentDesignNodeById(deploymentDesignNodeId), extensions));
    }
}
