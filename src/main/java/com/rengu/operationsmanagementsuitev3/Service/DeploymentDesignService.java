package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignNodeEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ProjectEntity;
import com.rengu.operationsmanagementsuitev3.Repository.DeploymentDesignRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-03 17:31
 **/

@Slf4j
@Service
@Transactional
public class DeploymentDesignService {


    private final DeploymentDesignRepository deploymentDesignRepository;
    private final DeploymentDesignNodeService deploymentDesignNodeService;

    @Autowired
    public DeploymentDesignService(DeploymentDesignRepository deploymentDesignRepository, DeploymentDesignNodeService deploymentDesignNodeService) {
        this.deploymentDesignRepository = deploymentDesignRepository;
        this.deploymentDesignNodeService = deploymentDesignNodeService;
    }

    // 根据工程保存部署设计
    @CachePut(value = "DeploymentDesign_Cache", key = "#deploymentDesignEntity.id")
    public DeploymentDesignEntity saveDeploymentDesignByProject(ProjectEntity projectEntity, DeploymentDesignEntity deploymentDesignEntity) {
        if (StringUtils.isEmpty(deploymentDesignEntity.getName())) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NAME_ARGS_NOT_FOUND);
        }
        if (hasDeploymentDesignByNameAndDeletedAndProject(deploymentDesignEntity.getName(), false, projectEntity)) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NAME_EXISTED + deploymentDesignEntity.getName());
        }
        deploymentDesignEntity.setProjectEntity(projectEntity);
        return deploymentDesignRepository.save(deploymentDesignEntity);
    }

    // 根据Id复制部署设计
    public DeploymentDesignEntity copyDeploymentDesignById(String deploymentDesignId) {
        DeploymentDesignEntity deploymentDesignArgs = getDeploymentDesignById(deploymentDesignId);
        DeploymentDesignEntity deploymentDesignEntity = new DeploymentDesignEntity();
        BeanUtils.copyProperties(deploymentDesignArgs, deploymentDesignEntity, "id", "createTime", "name", "source");
        deploymentDesignEntity.setName(getDeploymentDesignName(deploymentDesignArgs));
        return deploymentDesignRepository.save(deploymentDesignEntity);
    }

    // 根据Id复制部署设计
    public DeploymentDesignEntity baselineDeploymentDesignById(String deploymentDesignId, String createMessage) {
        DeploymentDesignEntity deploymentDesignArgs = getDeploymentDesignById(deploymentDesignId);
        DeploymentDesignEntity deploymentDesignEntity = new DeploymentDesignEntity();
        BeanUtils.copyProperties(deploymentDesignArgs, deploymentDesignEntity, "id", "createTime", "name", "source");
        deploymentDesignEntity.setName(getDeploymentDesignName(deploymentDesignArgs));
        deploymentDesignEntity.setBaseline(true);
        deploymentDesignEntity.setCreateMessage(createMessage);
        deploymentDesignEntity.setSource(deploymentDesignArgs);
        return deploymentDesignRepository.save(deploymentDesignEntity);
    }

    // 根据Id删除部署设计
    @CacheEvict(value = "DeploymentDesign_Cache", key = "#deploymentDesignId")
    public DeploymentDesignEntity deleteDeploymentDesignById(String deploymentDesignId) {
        DeploymentDesignEntity deploymentDesignEntity = getDeploymentDesignById(deploymentDesignId);
        deploymentDesignEntity.setDeleted(true);
        return deploymentDesignRepository.save(deploymentDesignEntity);
    }

    // 根据id撤销删除部署设计
    @CachePut(value = "DeploymentDesign_Cache", key = "#deploymentDesignId")
    public DeploymentDesignEntity restoreDeploymentDesignById(String deploymentDesignId) {
        DeploymentDesignEntity deploymentDesignEntity = getDeploymentDesignById(deploymentDesignId);
        deploymentDesignEntity.setDeleted(false);
        deploymentDesignEntity.setName(getDeploymentDesignName(deploymentDesignEntity));
        return deploymentDesignRepository.save(deploymentDesignEntity);
    }

    // 根据id清除部署设计
    @CacheEvict(value = "DeploymentDesign_Cache", key = "#deploymentDesignId")
    public DeploymentDesignEntity cleanDeploymentDesignById(String deploymentDesignId) {
        DeploymentDesignEntity deploymentDesignEntity = getDeploymentDesignById(deploymentDesignId);
        return cleanDeploymentDesignById(deploymentDesignEntity);
    }

    @CacheEvict(value = "DeploymentDesign_Cache", key = "#deploymentDesignEntity.id")
    public DeploymentDesignEntity cleanDeploymentDesignById(DeploymentDesignEntity deploymentDesignEntity) {
        for (DeploymentDesignNodeEntity deploymentDesignNodeEntity : deploymentDesignNodeService.getDeploymentDesignNodesByDeploymentDesign(deploymentDesignEntity)) {
            deploymentDesignNodeService.deleteDeploymentDesignNodeById(deploymentDesignNodeEntity);
        }
        deploymentDesignRepository.delete(deploymentDesignEntity);
        return deploymentDesignEntity;
    }

    @CacheEvict(value = "DeploymentDesign_Cache", allEntries = true)
    public void deleteDeploymentDesignByProject(ProjectEntity projectEntity) {
        for (DeploymentDesignEntity deploymentDesignEntity : getDeploymentDesignsByProject(projectEntity)) {
            cleanDeploymentDesignById(deploymentDesignEntity);
        }
    }

    // 根据id修改部署设计
    @CachePut(value = "DeploymentDesign_Cache", key = "#deploymentDesignId")
    public DeploymentDesignEntity updateDeploymentDesignById(String deploymentDesignId, DeploymentDesignEntity deploymentDesignArgs) {
        DeploymentDesignEntity deploymentDesignEntity = getDeploymentDesignById(deploymentDesignId);
        if (!StringUtils.isEmpty(deploymentDesignArgs.getName()) && !deploymentDesignEntity.getName().equals(deploymentDesignArgs.getName())) {
            if (hasDeploymentDesignByNameAndDeletedAndProject(deploymentDesignArgs.getName(), false, deploymentDesignEntity.getProjectEntity())) {
                throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NAME_EXISTED + deploymentDesignArgs.getName());
            }
            deploymentDesignEntity.setName(deploymentDesignArgs.getName());
        }
        if (deploymentDesignArgs.getDescription() != null && !deploymentDesignEntity.getDescription().equals(deploymentDesignArgs.getDescription())) {
            deploymentDesignEntity.setDescription(deploymentDesignArgs.getDescription());
        }
        return deploymentDesignRepository.save(deploymentDesignEntity);
    }

    // 根据工程、名称、是否被删除判断部署设计是否存在
    public boolean hasDeploymentDesignByNameAndDeletedAndProject(String name, boolean deleted, ProjectEntity projectEntity) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        return deploymentDesignRepository.existsByNameAndDeletedAndProjectEntity(name, deleted, projectEntity);
    }

    // 根据Id判断部署设计是否存在
    public boolean hasDeploymentDesignById(String deploymentDesignId) {
        if (StringUtils.isEmpty(deploymentDesignId)) {
            return false;
        }
        return deploymentDesignRepository.existsById(deploymentDesignId);
    }

    // 根据Id查询部署设计
    @Cacheable(value = "DeploymentDesign_Cache", key = "#deploymentDesignId")
    public DeploymentDesignEntity getDeploymentDesignById(String deploymentDesignId) {
        if (!hasDeploymentDesignById(deploymentDesignId)) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_ID_NOT_FOUND + deploymentDesignId);
        }
        return deploymentDesignRepository.findById(deploymentDesignId).get();
    }

    // 根据工程和是否删除查询部署设计-分页
    public Page<DeploymentDesignEntity> getDeploymentDesignsByDeletedAndProject(Pageable pageable, boolean deleted, ProjectEntity projectEntity) {
        return deploymentDesignRepository.findAllByDeletedAndProjectEntity(pageable, deleted, projectEntity);
    }

    // 根据工程查询部署设计
    public List<DeploymentDesignEntity> getDeploymentDesignsByProject(ProjectEntity projectEntity) {
        return deploymentDesignRepository.findAllByProjectEntity(projectEntity);
    }

    // 查询未删除的部署设计数量
    public long countDeploymentDesignsByDeletedAndProject(boolean deleted, ProjectEntity projectEntity) {
        return deploymentDesignRepository.countAllByDeletedAndProjectEntity(deleted, projectEntity);
    }

    // 生成不重复的部署设计名称
    private String getDeploymentDesignName(DeploymentDesignEntity deploymentDesignEntity) {
        String name = deploymentDesignEntity.getName();
        if (hasDeploymentDesignByNameAndDeletedAndProject(name, false, deploymentDesignEntity.getProjectEntity())) {
            int index = 0;
            String tempName = name;
            if (name.contains("@")) {
                tempName = name.substring(0, name.lastIndexOf("@"));
                index = Integer.parseInt(name.substring(name.lastIndexOf("@") + 1)) + 1;
                name = tempName + "@" + index;
            }
            while (hasDeploymentDesignByNameAndDeletedAndProject(name, false, deploymentDesignEntity.getProjectEntity())) {
                name = tempName + "@" + index;
                index = index + 1;
            }
            return name;
        } else {
            return name;
        }
    }
}
