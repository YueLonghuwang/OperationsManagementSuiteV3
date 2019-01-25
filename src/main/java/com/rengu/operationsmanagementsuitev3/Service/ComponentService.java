package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ProjectEntity;
import com.rengu.operationsmanagementsuitev3.Repository.ComponentRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import com.rengu.operationsmanagementsuitev3.Utils.FormatUtils;
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

import java.io.IOException;
import java.util.List;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-24 14:38
 **/

@Slf4j
@Service
@Transactional
public class ComponentService {

    private final ComponentRepository componentRepository;
    private final ComponentFileService componentFileService;
    private final ComponentHistoryService componentHistoryService;
    @Autowired
    private DeploymentDesignDetailService deploymentDesignDetailService;

    @Autowired
    public ComponentService(ComponentRepository componentRepository, ComponentFileService componentFileService, ComponentHistoryService componentHistoryService) {
        this.componentRepository = componentRepository;
        this.componentFileService = componentFileService;
        this.componentHistoryService = componentHistoryService;
    }

    // 根据工程保存组件
    @CachePut(value = " Component_Cache", key = "#componentEntity.id")
    public ComponentEntity saveComponentByProject(ProjectEntity projectEntity, ComponentEntity componentEntity) {
        if (StringUtils.isEmpty(componentEntity.getName())) {
            throw new RuntimeException(ApplicationMessages.COMPONENT_NAME_ARGS_NOT_FOUND);
        }
        if (StringUtils.isEmpty(componentEntity.getVersion())) {
            throw new RuntimeException(ApplicationMessages.COMPONENT_VERSION_ARGS_NOT_FOUND);
        }
        if (hasComponentByNameAndVersionAndDeletedAndProject(componentEntity.getName(), componentEntity.getVersion(), false, projectEntity)) {
            throw new RuntimeException(ApplicationMessages.COMPONENT_NAME_AND_VERSION_EXISTED + componentEntity.getName() + "-" + componentEntity.getVersion());
        }
        if (StringUtils.isEmpty(componentEntity.getRelativePath())) {
            throw new RuntimeException(ApplicationMessages.COMPONENT_RELATIVE_PATH_ARGS_NOT_FOUND);
        }
        componentEntity.setRelativePath(FormatUtils.formatPath(componentEntity.getRelativePath()));
        componentEntity.setProjectEntity(projectEntity);
        return componentRepository.save(componentEntity);
    }

    // 根据id复制组件
    public ComponentEntity copyComponentById(ComponentEntity componentArgs) {
        ComponentEntity componentEntity = new ComponentEntity();
        BeanUtils.copyProperties(componentArgs, componentEntity, "id", "createTime");
        componentEntity.setName(getComponentName(componentArgs));
        componentRepository.save(componentEntity);
        componentFileService.copyComponentFileByComponent(componentArgs, componentEntity);
        return componentEntity;
    }

    // 根据Id删除组件
    @CacheEvict(value = " Component_Cache", key = "#componentId")
    public ComponentEntity deleteComponentById(String componentId) {
        ComponentEntity componentEntity = getComponentById(componentId);
        componentEntity.setDeleted(true);
        return componentRepository.save(componentEntity);
    }

    // 根据Id撤销删除组件
    @CachePut(value = " Component_Cache", key = "#componentId")
    public ComponentEntity restoreComponentById(String componentId) {
        ComponentEntity componentEntity = getComponentById(componentId);
        componentEntity.setName(getComponentName(componentEntity));
        componentEntity.setDeleted(false);
        return componentRepository.save(componentEntity);
    }

    // 根据Id清除组件
    @CacheEvict(value = " Component_Cache", key = "#componentId")
    public ComponentEntity cleanComponentById(String componentId) throws IOException {
        ComponentEntity componentEntity = getComponentById(componentId);
        deploymentDesignDetailService.deleteDeploymentDesignDetailByComponent(componentEntity);
        componentHistoryService.deleteComponentHistoryByComponent(componentEntity);
        componentFileService.deleteComponentFileByComponent(componentEntity);
        componentRepository.delete(componentEntity);
        return componentEntity;
    }

    @CacheEvict(value = " Component_Cache", allEntries = true)
    public List<ComponentEntity> deleteComponentByProject(ProjectEntity projectEntity) throws IOException {
        List<ComponentEntity> componentEntityList = getComponentsByProject(projectEntity);
        for (ComponentEntity componentEntity : componentEntityList) {
            cleanComponentById(componentEntity.getId());
        }
        return componentEntityList;
    }

    // 根据Id修改组件
    @CachePut(value = " Component_Cache", key = "#componentId")
    public ComponentEntity updateComponentById(String componentId, ComponentEntity componentArgs) {
        boolean isModifiedName = false;
        boolean isModifiedVersion = false;
        ComponentEntity componentEntity = getComponentById(componentId);
        if (!StringUtils.isEmpty(componentArgs.getName()) && !componentEntity.getName().equals(componentArgs.getName())) {
            isModifiedName = true;
        }
        if (!StringUtils.isEmpty(componentArgs.getVersion()) && !componentEntity.getVersion().equals(componentArgs.getVersion())) {
            isModifiedVersion = true;
        }
        if ((isModifiedName || isModifiedVersion) && hasComponentByNameAndVersionAndDeletedAndProject(componentArgs.getName(), componentArgs.getVersion(), false, componentEntity.getProjectEntity())) {
            throw new RuntimeException(ApplicationMessages.COMPONENT_NAME_AND_VERSION_EXISTED + componentArgs.getName() + "-" + componentArgs.getVersion());
        }
        if (!StringUtils.isEmpty(componentArgs.getRelativePath()) && !componentEntity.getRelativePath().equals(componentArgs.getRelativePath())) {
            componentEntity.setRelativePath(componentArgs.getRelativePath());
        }
        if (componentArgs.getDescription() != null && !componentEntity.getDescription().equals(componentArgs.getDescription())) {
            componentEntity.setDescription(componentArgs.getDescription());
        }
        if (isModifiedName) {
            componentEntity.setName(componentArgs.getName());
        }
        if (isModifiedVersion) {
            componentEntity.setVersion(componentArgs.getVersion());
        }
        return componentRepository.save(componentEntity);
    }

    // 根据组件名称、版本、是否删除及工程查询组件是否存在
    public boolean hasComponentByNameAndVersionAndDeletedAndProject(String name, String version, boolean deleted, ProjectEntity projectEntity) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(version)) {
            return false;
        }
        return componentRepository.existsByNameAndVersionAndDeletedAndProjectEntity(name, version, deleted, projectEntity);
    }

    // 根据Id查询组件是否存在
    public boolean hasComponentById(String componentId) {
        if (StringUtils.isEmpty(componentId)) {
            return false;
        }
        return componentRepository.existsById(componentId);
    }

    // 查询所有组件
    public Page<ComponentEntity> getComponents(Pageable pageable) {
        return componentRepository.findAll(pageable);
    }

    // 根据Id查询组件
    @Cacheable(value = " Component_Cache", key = "#componentId")
    public ComponentEntity getComponentById(String componentId) {
        if (!hasComponentById(componentId)) {
            throw new RuntimeException(ApplicationMessages.COMPONENT_ID_NOT_FOUND + componentId);
        }
        return componentRepository.findById(componentId).get();
    }

    // 根据工程查询组件
    public Page<ComponentEntity> getComponentsByDeletedAndProject(Pageable pageable, boolean deleted, ProjectEntity projectEntity) {
        return componentRepository.findByDeletedAndProjectEntity(pageable, deleted, projectEntity);
    }

    // 根据工程查询组件
    public List<ComponentEntity> getComponentsByProject(ProjectEntity projectEntity) {
        return componentRepository.findAllByProjectEntity(projectEntity);
    }

    // 根据工程查询组件
    public List<ComponentEntity> getComponentsByDeletedAndProject(boolean deleted, ProjectEntity projectEntity) {
        return componentRepository.findByDeletedAndProjectEntity(deleted, projectEntity);
    }

    // 根据工程查询组件数量
    public long countComponentsByDeletedAndProject(boolean deleted, ProjectEntity projectEntity) {
        return componentRepository.countByDeletedAndProjectEntity(deleted, projectEntity);
    }

    // 生成不重复的组件名称
    private String getComponentName(ComponentEntity componentEntity) {
        String name = componentEntity.getName();
        String version = componentEntity.getVersion();
        if (hasComponentByNameAndVersionAndDeletedAndProject(name, version, false, componentEntity.getProjectEntity())) {
            int index = 0;
            String tempName = name;
            if (name.contains("@")) {
                tempName = name.substring(0, name.lastIndexOf("@"));
                index = Integer.parseInt(name.substring(name.lastIndexOf("@") + 1)) + 1;
                name = tempName + "@" + index;
            }
            while (hasComponentByNameAndVersionAndDeletedAndProject(name, version, false, componentEntity.getProjectEntity())) {
                name = tempName + "@" + index;
                index = index + 1;
            }
            return name;
        } else {
            return name;
        }
    }
}
