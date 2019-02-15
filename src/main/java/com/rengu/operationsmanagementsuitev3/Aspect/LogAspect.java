package com.rengu.operationsmanagementsuitev3.Aspect;

import com.rengu.operationsmanagementsuitev3.Controller.ComponentController;
import com.rengu.operationsmanagementsuitev3.Controller.DeviceController;
import com.rengu.operationsmanagementsuitev3.Controller.ProjectController;
import com.rengu.operationsmanagementsuitev3.Controller.UserController;
import com.rengu.operationsmanagementsuitev3.Entity.*;
import com.rengu.operationsmanagementsuitev3.Service.UserActionLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-22 17:15
 **/


@Slf4j
@Aspect
@Component
public class LogAspect {

    private final UserActionLogService userActionLogService;

    @Autowired
    public LogAspect(UserActionLogService userActionLogService) {
        this.userActionLogService = userActionLogService;
    }

    @Pointcut(value = "execution(public * com.rengu.operationsmanagementsuitev3.Controller..*(..))")
    private void requestPonitCut() {
    }

    @Before(value = "requestPonitCut()")
    public void doBefore(JoinPoint joinPoint) {
        // 获取Http请求对象
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
        // 记录文件日志
        LogEntity logEntity = new LogEntity();
        logEntity.setUsername(httpServletRequest.getUserPrincipal() == null ? "未知用户" : httpServletRequest.getUserPrincipal().getName());
        logEntity.setHostAddress(httpServletRequest.getRemoteAddr());
        logEntity.setRquestMethod(httpServletRequest.getMethod());
        logEntity.setUrl(httpServletRequest.getRequestURI());
        logEntity.setUserAgent(httpServletRequest.getHeader("User-Agent"));
        logEntity.setCallMethod(joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
//        log.info(logEntity.toString());
    }

    @AfterReturning(pointcut = "requestPonitCut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, ResultEntity result) {
        // 获取Http请求对象
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
        if (httpServletRequest.getUserPrincipal() != null) {
            String username = httpServletRequest.getUserPrincipal().getName();
            int object = UserActionLogService.ERROR_OBJECT;
            int type = UserActionLogService.ERROR_TYPE;
            String description = "";
            // 用户接口
            if (joinPoint.getTarget().getClass().equals(UserController.class)) {
                switch (joinPoint.getSignature().getName()) {
                    case "saveAdminUser": {
                        UserEntity userEntity = (UserEntity) result.getData();
                        object = UserActionLogService.USER_OBJECT;
                        type = UserActionLogService.CREATE_TYPE;
                        description = "用户：" + username + "，创建管理员用户：" + userEntity.getUsername();
                        break;
                    }
                    case "deleteUserById": {
                        UserEntity userEntity = (UserEntity) result.getData();
                        object = UserActionLogService.USER_OBJECT;
                        type = UserActionLogService.DELETE_TYPE;
                        description = "用户：" + username + "，根据Id删除用户：" + userEntity.getUsername();
                        break;
                    }
                    case "updatePasswordById": {
                        UserEntity userEntity = (UserEntity) result.getData();
                        object = UserActionLogService.USER_OBJECT;
                        type = UserActionLogService.UPDATE_TYPE;
                        description = "用户：" + username + "，根据Id修改用户密码：" + userEntity.getUsername();
                        break;
                    }
                    case "userUpgradeById": {
                        UserEntity userEntity = (UserEntity) result.getData();
                        object = UserActionLogService.USER_OBJECT;
                        type = UserActionLogService.UPDATE_TYPE;
                        description = "用户：" + username + "，升级用户：" + userEntity.getUsername() + "为管理员";
                        break;
                    }
                    case "userDegradeById": {
                        UserEntity userEntity = (UserEntity) result.getData();
                        object = UserActionLogService.USER_OBJECT;
                        type = UserActionLogService.UPDATE_TYPE;
                        description = "用户：" + username + "，降级管理员：" + userEntity.getUsername() + "为普通用户";
                        break;
                    }
                    case "saveProjectByUser": {
                        ProjectEntity projectEntity = (ProjectEntity) result.getData();
                        object = UserActionLogService.PROJECT_OBJECT;
                        type = UserActionLogService.CREATE_TYPE;
                        description = "用户：" + username + "，创建工程：" + projectEntity.getName();
                        break;
                    }
                    default:
                }
            }
            // 工程接口
            if (joinPoint.getTarget().getClass().equals(ProjectController.class)) {
                switch (joinPoint.getSignature().getName()) {
                    case "deleteProjectById": {
                        ProjectEntity projectEntity = (ProjectEntity) result.getData();
                        object = UserActionLogService.PROJECT_OBJECT;
                        type = UserActionLogService.DELETE_TYPE;
                        description = "用户：" + username + "，删除工程：" + projectEntity.getName();
                        break;
                    }
                    case "restoreProjectById": {
                        ProjectEntity projectEntity = (ProjectEntity) result.getData();
                        object = UserActionLogService.PROJECT_OBJECT;
                        type = UserActionLogService.RESTORE_TYPE;
                        description = "用户：" + username + "，撤销删除工程：" + projectEntity.getName();
                        break;
                    }
                    case "cleanProjectById": {
                        ProjectEntity projectEntity = (ProjectEntity) result.getData();
                        object = UserActionLogService.PROJECT_OBJECT;
                        type = UserActionLogService.CLEAN_TYPE;
                        description = "用户：" + username + "，清除工程：" + projectEntity.getName();
                        break;
                    }
                    case "updateProjectById": {
                        ProjectEntity projectEntity = (ProjectEntity) result.getData();
                        object = UserActionLogService.PROJECT_OBJECT;
                        type = UserActionLogService.UPDATE_TYPE;
                        description = "用户：" + username + "，修改工程：" + projectEntity.getName() + "信息。";
                        break;
                    }
                    case "saveDeviceByProject": {
                        DeviceEntity deviceEntity = (DeviceEntity) result.getData();
                        object = UserActionLogService.DEVICE_OBJECT;
                        type = UserActionLogService.CREATE_TYPE;
                        description = "用户：" + username + "，保存设备：" + deviceEntity.getName();
                        break;
                    }
                    case "saveComponentByProject": {
                        ComponentEntity componentEntity = (ComponentEntity) result.getData();
                        object = UserActionLogService.COMPONENT_OBJECT;
                        type = UserActionLogService.CREATE_TYPE;
                        description = "用户：" + username + "，保存组件：" + componentEntity.getName();
                        break;
                    }
                    case "saveDeploymentDesignByProject": {
                        DeploymentDesignEntity deploymentDesignEntity = (DeploymentDesignEntity) result.getData();
                        object = UserActionLogService.DEPLOYMENT_DESIGN_OBJECT;
                        type = UserActionLogService.CREATE_TYPE;
                        description = "用户：" + username + "，保存部署设计：" + deploymentDesignEntity.getName();
                        break;
                    }
                    default:
                }
            }
            // 设备接口
            if (joinPoint.getTarget().getClass().equals(DeviceController.class)) {
                switch (joinPoint.getSignature().getName()) {
                    case "copyDeviceById": {
                        DeviceEntity deviceEntity = (DeviceEntity) result.getData();
                        object = UserActionLogService.DEVICE_OBJECT;
                        type = UserActionLogService.COPY_TYPE;
                        description = "用户：" + username + "，复制设备：" + deviceEntity.getName();
                        break;
                    }
                    case "deleteDeviceById": {
                        DeviceEntity deviceEntity = (DeviceEntity) result.getData();
                        object = UserActionLogService.DEVICE_OBJECT;
                        type = UserActionLogService.DELETE_TYPE;
                        description = "用户：" + username + "，删除设备：" + deviceEntity.getName();
                        break;
                    }
                    case "restoreDeviceById": {
                        DeviceEntity deviceEntity = (DeviceEntity) result.getData();
                        object = UserActionLogService.DEVICE_OBJECT;
                        type = UserActionLogService.RESTORE_TYPE;
                        description = "用户：" + username + "，撤销删除设备：" + deviceEntity.getName();
                        break;
                    }
                    case "cleanDeviceById": {
                        DeviceEntity deviceEntity = (DeviceEntity) result.getData();
                        object = UserActionLogService.DEVICE_OBJECT;
                        type = UserActionLogService.CLEAN_TYPE;
                        description = "用户：" + username + "，清除设备：" + deviceEntity.getName();
                        break;
                    }
                    case "updateDeviceById": {
                        DeviceEntity deviceEntity = (DeviceEntity) result.getData();
                        object = UserActionLogService.DEVICE_OBJECT;
                        type = UserActionLogService.UPDATE_TYPE;
                        description = "用户：" + username + "，更新设备：" + deviceEntity.getName() + "信息。";
                        break;
                    }
                    case "getProcessById": {
                        object = UserActionLogService.DEVICE_OBJECT;
                        type = UserActionLogService.SCAN_TYPE;
                        description = "用户：" + username + "，获取设备进程信息";
                        break;
                    }
                    case "getDisksById": {
                        object = UserActionLogService.DEVICE_OBJECT;
                        type = UserActionLogService.SCAN_TYPE;
                        description = "用户：" + username + "，获取设备磁盘信息";
                        break;
                    }
                    default:
                }
            }
            // 组件接口
            if (joinPoint.getTarget().getClass().equals(ComponentController.class)) {
                switch (joinPoint.getSignature().getName()) {
                    case "copyComponentById": {
                        ComponentEntity componentEntity = (ComponentEntity) result.getData();
                        object = UserActionLogService.COMPONENT_OBJECT;
                        type = UserActionLogService.COPY_TYPE;
                        description = "用户：" + username + "，复制组件：" + componentEntity.getName();
                        break;
                    }
                    case "deleteComponentById": {
                        ComponentEntity componentEntity = (ComponentEntity) result.getData();
                        object = UserActionLogService.COMPONENT_OBJECT;
                        type = UserActionLogService.DELETE_TYPE;
                        description = "用户：" + username + "，删除组件：" + componentEntity.getName();
                        break;
                    }
                    case "restoreComponentById": {
                        ComponentEntity componentEntity = (ComponentEntity) result.getData();
                        object = UserActionLogService.COMPONENT_OBJECT;
                        type = UserActionLogService.RESTORE_TYPE;
                        description = "用户：" + username + "，撤销删除组件：" + componentEntity.getName();
                        break;
                    }
                    case "cleanComponentById": {
                        ComponentEntity componentEntity = (ComponentEntity) result.getData();
                        object = UserActionLogService.COMPONENT_OBJECT;
                        type = UserActionLogService.CLEAN_TYPE;
                        description = "用户：" + username + "，清除组件：" + componentEntity.getName();
                        break;
                    }
                    case "updateComponentById": {
                        ComponentEntity componentEntity = (ComponentEntity) result.getData();
                        object = UserActionLogService.COMPONENT_OBJECT;
                        type = UserActionLogService.UPDATE_TYPE;
                        description = "用户：" + username + "，更新组件：" + componentEntity.getName() + "信息";
                        break;
                    }
                    case "exportComponentFileByComponent": {
                        ComponentEntity componentEntity = (ComponentEntity) result.getData();
                        object = UserActionLogService.COMPONENT_OBJECT;
                        type = UserActionLogService.EXPORT_TYPE;
                        description = "用户：" + username + "，下载组件：" + componentEntity.getName() + "文件";
                        break;
                    }
                    default:
                }
            }
            if (object != UserActionLogService.ERROR_OBJECT || type != UserActionLogService.ERROR_TYPE || !StringUtils.isEmpty(description)) {
                UserActionLogEntity userActionLogEntity = new UserActionLogEntity();
                userActionLogEntity.setUsername(username);
                userActionLogEntity.setObject(object);
                userActionLogEntity.setType(type);
                userActionLogEntity.setDescription(description);
                userActionLogService.saveUserActionLog(userActionLogEntity);
            }
        }
    }

    @AfterThrowing(pointcut = "requestPonitCut()", throwing = "exception")
    public void doAfterThrowing(JoinPoint joinPoint, RuntimeException exception) {
    }

    @After(value = "requestPonitCut()")
    public void doAfter(JoinPoint joinPoint) {
    }
}
