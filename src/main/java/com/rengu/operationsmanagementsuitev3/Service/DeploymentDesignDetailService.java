package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.*;
import com.rengu.operationsmanagementsuitev3.Repository.DeploymentDesignDetailRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-04 11:25
 **/

@Slf4j
@Service
@Transactional
public class DeploymentDesignDetailService {

    private final DeploymentDesignDetailRepository deploymentDesignDetailRepository;
    private final DeployMetaService deployMetaService;

    @Autowired
    public DeploymentDesignDetailService(DeploymentDesignDetailRepository deploymentDesignDetailRepository, DeployMetaService deployMetaService) {
        this.deploymentDesignDetailRepository = deploymentDesignDetailRepository;
        this.deployMetaService = deployMetaService;
    }

    // 部署设计节点绑定组件历史
    public DeploymentDesignDetailEntity bindComponentHistoryByDeploymentDesignNode(DeploymentDesignNodeEntity deploymentDesignNodeEntity, ComponentHistoryEntity componentHistoryEntity, boolean keepLatest) {
        if (hasDeploymentDesignDetailByDeploymentDesignNodeAndComponent(deploymentDesignNodeEntity, componentHistoryEntity.getComponentEntity())) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_DETAIL_COMPONENT_EXISTED + componentHistoryEntity.getComponentEntity().getName() + "-" + componentHistoryEntity.getComponentEntity().getVersion());
        }
        DeploymentDesignDetailEntity deploymentDesignDetailEntity = new DeploymentDesignDetailEntity();
        deploymentDesignDetailEntity.setKeepLatest(keepLatest);
        deploymentDesignDetailEntity.setComponentHistoryEntity(componentHistoryEntity);
        deploymentDesignDetailEntity.setComponentEntity(componentHistoryEntity.getComponentEntity());
        deploymentDesignDetailEntity.setDeploymentDesignNodeEntity(deploymentDesignNodeEntity);
        deploymentDesignDetailEntity.setDeploymentDesignEntity(deploymentDesignNodeEntity.getDeploymentDesignEntity());
        return deploymentDesignDetailRepository.save(deploymentDesignDetailEntity);
    }

    // 根据部署设计节点复制部署设计详情
    public List<DeploymentDesignDetailEntity> copyDeploymentDesignDetailsByDeploymentDesignNode(DeploymentDesignNodeEntity sourceDeploymentDesignNode, DeploymentDesignNodeEntity targetDeploymentDesignNode) {
        List<DeploymentDesignDetailEntity> deploymentDesignDetailEntityList = new ArrayList<>();
        for (DeploymentDesignDetailEntity sourceDeploymentDesignDetailEntity : getDeploymentDesignDetailsByDeploymentDesignNode(sourceDeploymentDesignNode)) {
            DeploymentDesignDetailEntity deploymentDesignDetailEntity = new DeploymentDesignDetailEntity();
            BeanUtils.copyProperties(sourceDeploymentDesignDetailEntity, deploymentDesignDetailEntity, "id", "createTime", "deploymentDesignNodeEntity", "deploymentDesignEntity");
            deploymentDesignDetailEntity.setDeploymentDesignNodeEntity(targetDeploymentDesignNode);
            deploymentDesignDetailEntity.setDeploymentDesignEntity(targetDeploymentDesignNode.getDeploymentDesignEntity());
            deploymentDesignDetailEntityList.add(deploymentDesignDetailRepository.save(deploymentDesignDetailEntity));
        }
        return deploymentDesignDetailEntityList;
    }

    // 根据Id删除部署设计详情
    public DeploymentDesignDetailEntity deleteDeploymentDesignDetailById(String deploymentDesignDetailId) {
        DeploymentDesignDetailEntity deploymentDesignDetailEntity = getDeploymentDesignDetailById(deploymentDesignDetailId);
        return deleteDeploymentDesignDetailById(deploymentDesignDetailEntity);
    }

    public DeploymentDesignDetailEntity deleteDeploymentDesignDetailById(DeploymentDesignDetailEntity deploymentDesignDetailEntity) {
        // todo 添加删除部署设计扫描历史逻辑
        deploymentDesignDetailRepository.delete(deploymentDesignDetailEntity);
        return deploymentDesignDetailEntity;
    }

    public void deleteDeploymentDesignDetailByComponent(ComponentEntity componentEntity) {
        for (DeploymentDesignDetailEntity deploymentDesignDetailEntity : getDeploymentDesignDetailsByComponent(componentEntity)) {
            deleteDeploymentDesignDetailById(deploymentDesignDetailEntity);
        }
    }

    // 更新绑定组件历史版本
    public DeploymentDesignDetailEntity updateComponentHistoryById(String deploymentDesignDetailId, ComponentHistoryEntity componentHistoryEntity) {
        DeploymentDesignDetailEntity deploymentDesignDetailEntity = getDeploymentDesignDetailById(deploymentDesignDetailId);
        if (!deploymentDesignDetailEntity.getComponentHistoryEntity().equals(componentHistoryEntity)) {
            deploymentDesignDetailEntity.setComponentHistoryEntity(componentHistoryEntity);
            deploymentDesignDetailEntity.setComponentEntity(componentHistoryEntity.getComponentEntity());
        }
        return deploymentDesignDetailRepository.save(deploymentDesignDetailEntity);
    }

    // 更新是否保持最新版本状态
    public DeploymentDesignDetailEntity updateKeepLatestById(String deploymentDesignDetailId, boolean keepLatest) {
        DeploymentDesignDetailEntity deploymentDesignDetailEntity = getDeploymentDesignDetailById(deploymentDesignDetailId);
        if (deploymentDesignDetailEntity.isKeepLatest() != keepLatest) {
            deploymentDesignDetailEntity.setKeepLatest(keepLatest);
        }
        return deploymentDesignDetailRepository.save(deploymentDesignDetailEntity);
    }

    // 根据组件和部署设计节点判断是否存在
    public boolean hasDeploymentDesignDetailByDeploymentDesignNodeAndComponent(DeploymentDesignNodeEntity deploymentDesignNodeEntity, ComponentEntity componentEntity) {
        return deploymentDesignDetailRepository.existsByDeploymentDesignNodeEntityAndComponentEntity(deploymentDesignNodeEntity, componentEntity);
    }

    // 根据id查询部署设计详情是否存在
    public boolean hasDeploymentDesignDetailById(String deploymentDesignDetailId) {
        if (StringUtils.isEmpty(deploymentDesignDetailId)) {
            return false;
        }
        return deploymentDesignDetailRepository.existsById(deploymentDesignDetailId);
    }

    // 根据Id查询部署设计详情
    public DeploymentDesignDetailEntity getDeploymentDesignDetailById(String deploymentDesignDetailId) {
        if (!hasDeploymentDesignDetailById(deploymentDesignDetailId)) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_DETAIL_ID_NOT_FOUND + deploymentDesignDetailId);
        }
        return deploymentDesignDetailRepository.findById(deploymentDesignDetailId).get();
    }

    // 根据部署设计节点查询部署设计详情
    public List<DeploymentDesignDetailEntity> getDeploymentDesignDetailsByDeploymentDesignNode(DeploymentDesignNodeEntity deploymentDesignNodeEntity) {
        return deploymentDesignDetailRepository.findAllByDeploymentDesignNodeEntity(deploymentDesignNodeEntity);
    }

    public List<DeploymentDesignDetailEntity> getDeploymentDesignDetailsByComponent(ComponentEntity componentEntity) {
        return deploymentDesignDetailRepository.findAllByComponentEntity(componentEntity);
    }

    // 部署单个组件
    @Async
    public void deployDeploymentDesignDetailById(String deploymentDesignDetailId) {
        DeploymentDesignDetailEntity deploymentDesignDetailEntity = getDeploymentDesignDetailById(deploymentDesignDetailId);
        DeploymentDesignNodeEntity deploymentDesignNodeEntity = deploymentDesignDetailEntity.getDeploymentDesignNodeEntity();
        if (deploymentDesignNodeEntity.getDeviceEntity() == null) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NODE_DEVICE_ARGS_NOT_FOUND);
        }
        DeviceEntity deviceEntity = deploymentDesignNodeEntity.getDeviceEntity();
        if (!DeviceService.ONLINE_HOST_ADRESS.containsKey(deviceEntity.getHostAddress())) {
            throw new RuntimeException(ApplicationMessages.DEVICE_NOT_ONLINE + deviceEntity.getHostAddress());
        }
        List<DeployMetaEntity> deployMetaEntityList = deployMetaService.createDeployMeta(deploymentDesignDetailEntity);
        deployMetaService.deployMeta(deploymentDesignNodeEntity.getDeploymentDesignEntity(), deviceEntity, deployMetaEntityList);
    }

    // 部署单个节点
    @Async
    public void deployDeploymentDesignDetailByDeploymentDesignNode(DeploymentDesignNodeEntity deploymentDesignNodeEntity) {
        List<DeploymentDesignDetailEntity> deploymentDesignDetailEntityList = getDeploymentDesignDetailsByDeploymentDesignNode(deploymentDesignNodeEntity);
        if (deploymentDesignNodeEntity.getDeviceEntity() == null) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NODE_DEVICE_ARGS_NOT_FOUND);
        }
        DeviceEntity deviceEntity = deploymentDesignNodeEntity.getDeviceEntity();
        if (!DeviceService.ONLINE_HOST_ADRESS.containsKey(deviceEntity.getHostAddress())) {
            throw new RuntimeException(ApplicationMessages.DEVICE_NOT_ONLINE + deviceEntity.getHostAddress());
        }
        List<DeployMetaEntity> deployMetaEntityList = deployMetaService.createDeployMeta(deploymentDesignDetailEntityList.toArray(new DeploymentDesignDetailEntity[deploymentDesignDetailEntityList.size()]));
        deployMetaService.deployMeta(deploymentDesignNodeEntity.getDeploymentDesignEntity(), deviceEntity, deployMetaEntityList);
    }
}