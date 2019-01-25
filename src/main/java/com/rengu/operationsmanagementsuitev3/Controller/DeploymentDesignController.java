package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignNodeEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeviceEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Service.DeploymentDesignDetailService;
import com.rengu.operationsmanagementsuitev3.Service.DeploymentDesignNodeService;
import com.rengu.operationsmanagementsuitev3.Service.DeploymentDesignService;
import com.rengu.operationsmanagementsuitev3.Service.DeviceService;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-04 09:44
 **/

@RestController
@RequestMapping(value = "/deploymentdesigns")
public class DeploymentDesignController {

    private final DeploymentDesignService deploymentDesignService;
    private final DeploymentDesignNodeService deploymentDesignNodeService;
    private final DeploymentDesignDetailService deploymentDesignDetailService;
    private final DeviceService deviceService;

    @Autowired
    public DeploymentDesignController(DeploymentDesignService deploymentDesignService, DeploymentDesignNodeService deploymentDesignNodeService, DeploymentDesignDetailService deploymentDesignDetailService, DeviceService deviceService) {
        this.deploymentDesignService = deploymentDesignService;
        this.deploymentDesignNodeService = deploymentDesignNodeService;
        this.deploymentDesignDetailService = deploymentDesignDetailService;
        this.deviceService = deviceService;
    }

    @PostMapping(value = "/{deploymentDesignId}/copy")
    public ResultEntity copyDeploymentDesignById(@PathVariable(value = "deploymentDesignId") String deploymentDesignId) {
        DeploymentDesignEntity deploymentDesignEntity = deploymentDesignService.copyDeploymentDesignById(deploymentDesignId);
        deploymentDesignNodeService.copyDeploymentDesignNodesByDeploymentDesign(deploymentDesignService.getDeploymentDesignById(deploymentDesignId), deploymentDesignEntity);
        return ResultUtils.build(deploymentDesignEntity);
    }

    @PostMapping(value = "/{deploymentDesignId}/baseline")
    public ResultEntity baselineDeploymentDesignById(@PathVariable(value = "deploymentDesignId") String deploymentDesignId, @RequestParam(value = "createMessage") String createMessage) {
        DeploymentDesignEntity deploymentDesignEntity = deploymentDesignService.baselineDeploymentDesignById(deploymentDesignId, createMessage);
        deploymentDesignNodeService.copyDeploymentDesignNodesByDeploymentDesign(deploymentDesignService.getDeploymentDesignById(deploymentDesignId), deploymentDesignEntity);
        return ResultUtils.build(deploymentDesignEntity);
    }

    @DeleteMapping(value = "/{deploymentDesignId}")
    public ResultEntity deleteDeploymentDesignById(@PathVariable(value = "deploymentDesignId") String deploymentDesignId) {
        return ResultUtils.build(deploymentDesignService.deleteDeploymentDesignById(deploymentDesignId));
    }

    // 根据Id撤销删除部署设计
    @PatchMapping(value = "/{deploymentDesignId}/restore")
    public ResultEntity restoreDeploymentDesignById(@PathVariable(value = "deploymentDesignId") String deploymentDesignId) {
        return ResultUtils.build(deploymentDesignService.restoreDeploymentDesignById(deploymentDesignId));
    }

    // 根据id清除部署设计
    @DeleteMapping(value = "/{deploymentDesignId}/clean")
    public ResultEntity cleanDeploymentDesignById(@PathVariable(value = "deploymentDesignId") String deploymentDesignId) {
        return ResultUtils.build(deploymentDesignService.cleanDeploymentDesignById(deploymentDesignId));
    }

    // 根据Id修改部署设计
    @PatchMapping(value = "/{deploymentDesignId}")
    public ResultEntity updateDeploymentDesignById(@PathVariable(value = "deploymentDesignId") String deploymentDesignId, DeploymentDesignEntity deploymentDesignArgs) {
        return ResultUtils.build(deploymentDesignService.updateDeploymentDesignById(deploymentDesignId, deploymentDesignArgs));
    }

    // 根据Id查询部署设计
    @GetMapping(value = "/{deploymentDesignId}")
    public ResultEntity getDeploymentDesignById(@PathVariable(value = "deploymentDesignId") String deploymentDesignId) {
        return ResultUtils.build(deploymentDesignService.getDeploymentDesignById(deploymentDesignId));
    }

    // 根据Id建立部署设计节点
    @PostMapping(value = "/{deploymentDesignId}/deploymentdesignnode")
    public ResultEntity saveDeploymentDesignNodeById(@PathVariable(value = "deploymentDesignId") String deploymentDesignId, DeploymentDesignNodeEntity deploymentDesignNodeEntity) {
        return ResultUtils.build(deploymentDesignNodeService.saveDeploymentDesignNodeByDeploymentDesign(deploymentDesignService.getDeploymentDesignById(deploymentDesignId), deploymentDesignNodeEntity));
    }

    // 根据Id查询部署设计节点
    @GetMapping(value = "/{deploymentDesignId}/deploymentdesignnodes")
    public ResultEntity getDeploymentDesignNodesById(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable, @PathVariable(value = "deploymentDesignId") String deploymentDesignId) {
        return ResultUtils.build(deploymentDesignNodeService.getDeploymentDesignNodesByDeploymentDesign(pageable, deploymentDesignService.getDeploymentDesignById(deploymentDesignId)));
    }

    // 查询当前部署设计下可用于绑定的设备
    @GetMapping(value = "/{deploymentDesignId}/devices")
    public ResultEntity getAvailableDevicesById(@PathVariable(value = "deploymentDesignId") String deploymentDesignId) {
        DeploymentDesignEntity deploymentDesignEntity = deploymentDesignService.getDeploymentDesignById(deploymentDesignId);
        List<DeviceEntity> deviceEntityList = deviceService.getDevicesByDeletedAndProject(false, deploymentDesignEntity.getProjectEntity());
        for (DeploymentDesignNodeEntity deploymentDesignNodeEntity : deploymentDesignNodeService.getDeploymentDesignNodesByDeploymentDesign(deploymentDesignEntity)) {
            if (deploymentDesignNodeEntity.getDeviceEntity() != null) {
                deviceEntityList.remove(deploymentDesignNodeEntity.getDeviceEntity());
            }
        }
        return ResultUtils.build(deviceEntityList);
    }

    // 下发整个部署设计
    @PutMapping(value = "/{deploymentDesignId}/deploy")
    public void deployDeploymentDesignById(@PathVariable(value = "deploymentDesignId") String deploymentDesignId) {
        List<DeploymentDesignNodeEntity> deploymentDesignNodeEntityList = deploymentDesignNodeService.getDeploymentDesignNodesByDeploymentDesign(deploymentDesignService.getDeploymentDesignById(deploymentDesignId));
        for (DeploymentDesignNodeEntity deploymentDesignNodeEntity : deploymentDesignNodeEntityList) {
            deploymentDesignDetailService.deployDeploymentDesignDetailByDeploymentDesignNode(deploymentDesignNodeEntity);
        }
    }
}
