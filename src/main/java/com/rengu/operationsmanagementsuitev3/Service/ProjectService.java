package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.ProjectEntity;
import com.rengu.operationsmanagementsuitev3.Entity.UserEntity;
import com.rengu.operationsmanagementsuitev3.Repository.ProjectRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import lombok.extern.slf4j.Slf4j;
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

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-22 17:34
 **/


@Slf4j
@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final DeviceService deviceService;
    private final ComponentService componentService;
    private final DeploymentDesignService deploymentDesignService;
    private final DeployLogService deployLogService;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, DeviceService deviceService, ComponentService componentService, DeploymentDesignService deploymentDesignService, DeployLogService deployLogService) {
        this.projectRepository = projectRepository;
        this.deviceService = deviceService;
        this.componentService = componentService;
        this.deploymentDesignService = deploymentDesignService;
        this.deployLogService = deployLogService;
    }

    // 根据用户创建工程
    @CachePut(value = "Project_Cache", key = "#projectEntity.id")
    public ProjectEntity saveProjectByUser(ProjectEntity projectEntity, UserEntity userEntity) {
        if (StringUtils.isEmpty(projectEntity.getName())) {
            throw new RuntimeException(ApplicationMessages.PROJECT_NAME_ARGS_NOT_FOUND);
        }
        if (hasProjectByNameAndDeletedAndUser(projectEntity.getName(), false, userEntity)) {
            throw new RuntimeException(ApplicationMessages.PROJECT_NAME_EXISTED + projectEntity.getName());
        }
        projectEntity.setUserEntity(userEntity);
        return projectRepository.save(projectEntity);
    }

    // 根据Id删除工程
    @CacheEvict(value = "Project_Cache", key = "#projectId")
    public ProjectEntity deleteProjectById(String projectId) {
        ProjectEntity projectEntity = getProjectById(projectId);
        projectEntity.setDeleted(true);
        return projectRepository.save(projectEntity);
    }

    // 根据Id还原工程
    @CachePut(value = "Project_Cache", key = "#projectId")
    public ProjectEntity restoreProjectById(String projectId) {
        ProjectEntity projectEntity = getProjectById(projectId);
        projectEntity.setName(getProjectName(projectEntity));
        projectEntity.setDeleted(false);
        return projectRepository.save(projectEntity);
    }

    // 根据Id彻底删除工程
    @CacheEvict(value = "Project_Cache", key = "#projectId")
    public ProjectEntity cleanProjectById(String projectId) throws IOException {
        ProjectEntity projectEntity = getProjectById(projectId);
        deployLogService.deleteDeployLogByProject(projectEntity);
        deviceService.deleteDeviceByProject(projectEntity);
        componentService.deleteComponentByProject(projectEntity);
        deploymentDesignService.deleteDeploymentDesignByProject(projectEntity);
        projectRepository.delete(projectEntity);
        return projectEntity;
    }

    // 根据Id修改工程
    @CachePut(value = "Project_Cache", key = "#projectId")
    public ProjectEntity updateProjectById(String projectId, ProjectEntity projectArgs) {
        ProjectEntity projectEntity = getProjectById(projectId);
        if (!StringUtils.isEmpty(projectArgs.getName()) && !projectEntity.getName().equals(projectArgs.getName())) {
            if (hasProjectByNameAndDeletedAndUser(projectArgs.getName(), false, projectEntity.getUserEntity())) {
                throw new RuntimeException(ApplicationMessages.PROJECT_NAME_EXISTED + projectArgs.getName());
            }
            projectEntity.setName(projectArgs.getName());
        }
        if (projectArgs.getDescription() != null && !projectEntity.getDescription().equals(projectArgs.getDescription())) {
            projectEntity.setDescription(projectArgs.getDescription());
        }
        return projectRepository.save(projectEntity);
    }

    // 添加或删除星标
    @CachePut(value = "Project_Cache", key = "#projectId")
    public ProjectEntity starProjectById(String projectId, boolean hasStar) {
        ProjectEntity projectEntity = getProjectById(projectId);
        if (projectEntity.isHasStar() != hasStar) {
            projectEntity.setHasStar(hasStar);
            return projectRepository.save(projectEntity);
        }
        return projectEntity;
    }


    // 根据名称、是否删除及用户判断工程是否存在
    public boolean hasProjectByNameAndDeletedAndUser(String name, boolean deleted, UserEntity userEntity) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        return projectRepository.existsByNameAndDeletedAndUserEntity(name, deleted, userEntity);
    }

    // 根据id判断工程是否存在
    public boolean hasProjectById(String projectId) {
        if (StringUtils.isEmpty(projectId)) {
            return false;
        }
        return projectRepository.existsById(projectId);
    }

    // 根据Id查询工程
    @Cacheable(value = "Project_Cache", key = "#projectId")
    public ProjectEntity getProjectById(String projectId) {
        if (!hasProjectById(projectId)) {
            throw new RuntimeException(ApplicationMessages.PROJECT_ID_NOT_FOUND + projectId);
        }
        return projectRepository.findById(projectId).get();
    }

    // 根据是否删除及用户查询工程
    public Page<ProjectEntity> getProjectsByDeletedAndUser(Pageable pageable, boolean deleted, UserEntity userEntity) {
        return projectRepository.findByDeletedAndUserEntity(pageable, deleted, userEntity);
    }

    // 查询所有工程
    public Page<ProjectEntity> getProjects(Pageable pageable) {
        return projectRepository.findAll(pageable);
    }

    // 移交工程管理用户
    public ProjectEntity transferProjectByUser(String projectId, UserEntity userEntity) {
        ProjectEntity projectEntity = getProjectById(projectId);
        projectEntity.setUserEntity(userEntity);
        return projectRepository.save(projectEntity);
    }

    // 生成不重复的工程名称
    private String getProjectName(ProjectEntity projectEntity) {
        String name = projectEntity.getName();
        if (hasProjectByNameAndDeletedAndUser(name, false, projectEntity.getUserEntity())) {
            int index = 0;
            String tempName = name;
            if (name.contains("@")) {
                tempName = name.substring(0, name.lastIndexOf("@"));
                index = Integer.parseInt(name.substring(name.lastIndexOf("@") + 1)) + 1;
                name = tempName + "@" + index;
            }
            while (hasProjectByNameAndDeletedAndUser(name, false, projectEntity.getUserEntity())) {
                name = tempName + "@" + index;
                index = index + 1;
            }
            return name;
        } else {
            return name;
        }
    }
}
