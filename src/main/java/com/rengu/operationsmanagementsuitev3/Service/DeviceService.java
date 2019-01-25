package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.*;
import com.rengu.operationsmanagementsuitev3.Repository.DeviceRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import com.rengu.operationsmanagementsuitev3.Utils.FormatUtils;
import com.rengu.operationsmanagementsuitev3.Utils.IPUtils;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-23 11:05
 **/

@Slf4j
@Service
@Transactional
public class DeviceService {

    public enum OS_TYPE {
        UNKNOWN, WINDOWS, LINUX, VXWORKS
    }

    public static final Map<String, HeartbeatEntity> ONLINE_HOST_ADRESS = new ConcurrentHashMap<>();

    private final DeviceRepository deviceRepository;
    private final OrderService orderService;
    private final ScanHandlerService scanHandlerService;
    private final DeploymentDesignNodeService deploymentDesignNodeService;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository, OrderService orderService, ScanHandlerService scanHandlerService, DeploymentDesignNodeService deploymentDesignNodeService) {
        this.deviceRepository = deviceRepository;
        this.orderService = orderService;
        this.scanHandlerService = scanHandlerService;
        this.deploymentDesignNodeService = deploymentDesignNodeService;
    }

    // 根据工程创建设备
    @CachePut(value = "Device_Cache", key = "#deviceEntity.id")
    public DeviceEntity saveDeviceByProject(ProjectEntity projectEntity, DeviceEntity deviceEntity) {
        if (StringUtils.isEmpty(deviceEntity.getName())) {
            throw new RuntimeException(ApplicationMessages.DEVICE_NAME_ARGS_NOT_FOUND);
        }
        if (StringUtils.isEmpty(deviceEntity.getHostAddress()) || !IPUtils.isIPv4Address(deviceEntity.getHostAddress())) {
            throw new RuntimeException(ApplicationMessages.DEVICE_HOST_ADDRESS_ARGS_NOT_FOUND);
        }
        if (StringUtils.isEmpty(deviceEntity.getDeployPath())) {
            throw new RuntimeException(ApplicationMessages.DEVICE_DEPLOY_PATH_ARGS_NOT_FOUND);
        }
        if (hasDeviceByHostAddressAndDeletedAndProject(deviceEntity.getHostAddress(), false, projectEntity)) {
            throw new RuntimeException(ApplicationMessages.DEVICE_HOST_ADDRESS_EXISTED + deviceEntity.getHostAddress());
        }
        deviceEntity.setDeployPath(FormatUtils.formatPath(deviceEntity.getDeployPath()));
        deviceEntity.setProjectEntity(projectEntity);
        return deviceRepository.save(deviceEntity);
    }

    // 根据Id复制设备
    public DeviceEntity copyDeviceById(String deviceId) {
        DeviceEntity deviceArgs = getDeviceById(deviceId);
        DeviceEntity deviceEntity = new DeviceEntity();
        BeanUtils.copyProperties(deviceArgs, deviceEntity, "id", "createTime", "hostAddress");
        deviceEntity.setHostAddress(getHostAddress(deviceArgs.getHostAddress(), deviceArgs.getProjectEntity()));
        return deviceRepository.save(deviceEntity);
    }

    // 根据Id删除设备
    @CacheEvict(value = "Device_Cache", key = "#deviceId")
    public DeviceEntity deleteDeviceById(String deviceId) {
        DeviceEntity deviceEntity = getDeviceById(deviceId);
        deviceEntity.setDeleted(true);
        return deviceRepository.save(deviceEntity);
    }

    @CacheEvict(value = "Device_Cache", allEntries = true)
    public List<DeviceEntity> deleteDeviceByProject(ProjectEntity projectEntity) {
        List<DeviceEntity> deviceEntityList = getDevicesByProject(projectEntity);
        for (DeviceEntity deviceEntity : deviceEntityList) {
            cleanDeviceById(deviceEntity.getId());
        }
        return deviceEntityList;
    }

    // 根据Id撤销删除设备
    @CachePut(value = "Device_Cache", key = "#deviceId")
    public DeviceEntity restoreDeviceById(String deviceId) {
        DeviceEntity deviceEntity = getDeviceById(deviceId);
        if (hasDeviceByHostAddressAndDeletedAndProject(deviceEntity.getHostAddress(), false, deviceEntity.getProjectEntity())) {
            throw new RuntimeException(ApplicationMessages.DEVICE_HOST_ADDRESS_EXISTED + deviceEntity.getHostAddress());
        }
        deviceEntity.setDeleted(false);
        return deviceRepository.save(deviceEntity);
    }

    // 根据Id清除设备
    @CacheEvict(value = "Device_Cache", key = "#deviceId")
    public DeviceEntity cleanDeviceById(String deviceId) {
        DeviceEntity deviceEntity = getDeviceById(deviceId);
        deploymentDesignNodeService.deleteDeploymentDesignNodeByDevice(deviceEntity);
        deviceRepository.delete(deviceEntity);
        return deviceEntity;
    }

    // 根据Id修改设备
    @CachePut(value = "Device_Cache", key = "#deviceId")
    public DeviceEntity updateDeviceById(String deviceId, DeviceEntity deviceArgs) {
        DeviceEntity deviceEntity = getDeviceById(deviceId);
        if (!StringUtils.isEmpty(deviceArgs.getName()) && !deviceEntity.getName().equals(deviceArgs.getName())) {
            deviceEntity.setName(deviceArgs.getName());
        }
        if (!StringUtils.isEmpty(deviceArgs.getDeployPath()) && !deviceEntity.getDeployPath().equals(deviceArgs.getDeployPath())) {
            deviceEntity.setDeployPath(deviceArgs.getDeployPath());
        }
        if (deviceArgs.getDescription() != null && !deviceEntity.getDescription().equals(deviceArgs.getDescription())) {
            deviceEntity.setDescription(deviceArgs.getDescription());
        }
        if (!StringUtils.isEmpty(deviceArgs.getHostAddress()) && !deviceEntity.getHostAddress().equals(deviceArgs.getHostAddress())) {
            if (!IPUtils.isIPv4Address(deviceArgs.getHostAddress())) {
                throw new RuntimeException(ApplicationMessages.DEVICE_HOST_ADDRESS_NOT_FOUND);
            }
            if (hasDeviceByHostAddressAndDeletedAndProject(deviceArgs.getHostAddress(), false, deviceEntity.getProjectEntity())) {
                throw new RuntimeException(ApplicationMessages.DEVICE_HOST_ADDRESS_EXISTED + deviceArgs.getHostAddress());
            }
            deviceEntity.setHostAddress(deviceArgs.getHostAddress());
        }
        return deviceRepository.save(deviceEntity);
    }


    // 根据IP、是否删除及工程查询设备是否存在
    public boolean hasDeviceByHostAddressAndDeletedAndProject(String hostAddress, boolean deleted, ProjectEntity projectEntity) {
        if (StringUtils.isEmpty(hostAddress) || !IPUtils.isIPv4Address(hostAddress)) {
            return false;
        }
        return deviceRepository.existsByHostAddressAndDeletedAndProjectEntity(hostAddress, deleted, projectEntity);
    }

    // 根据Id判断设备是否存在
    public boolean hasDeviceById(String deviceId) {
        if (StringUtils.isEmpty(deviceId)) {
            return false;
        }
        return deviceRepository.existsById(deviceId);
    }

    // 根据Id查询设备
    @Cacheable(value = "Device_Cache", key = "#deviceId")
    public DeviceEntity getDeviceById(String deviceId) {
        if (!hasDeviceById(deviceId)) {
            throw new RuntimeException(ApplicationMessages.DEVICE_ID_NOT_FOUND + deviceId);
        }
        return deviceRepository.findById(deviceId).get();
    }

    // 查询所有设备
    public Page<DeviceEntity> getDevices(Pageable pageable) {
        return deviceRepository.findAll(pageable);
    }

    public List<DeviceEntity> getDevicesByProject(ProjectEntity projectEntity) {
        return deviceRepository.findAllByProjectEntity(projectEntity);
    }

    // 根据是否删除及工程查询设备
    public Page<DeviceEntity> getDevicesByDeletedAndProject(Pageable pageable, boolean deleted, ProjectEntity projectEntity) {
        return deviceRepository.findByDeletedAndProjectEntity(pageable, deleted, projectEntity);
    }

    // 根据是否删除及工程查询设备
    public List<DeviceEntity> getDevicesByDeletedAndProject(boolean deleted, ProjectEntity projectEntity) {
        return deviceRepository.findByDeletedAndProjectEntity(deleted, projectEntity);
    }

    // 根据是否删除及工程查询设备数量
    public long countDevicesByDeletedAndProject(boolean deleted, ProjectEntity projectEntity) {
        return deviceRepository.countByDeletedAndProjectEntity(deleted, projectEntity);
    }

    // 根据id扫描设备磁盘信息
    public List<ProcessScanResultEntity> getProcessById(String deviceId) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        DeviceEntity deviceEntity = getDeviceById(deviceId);
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setTag(OrderService.PROCESS_SCAN_TAG);
        orderEntity.setTargetDevice(deviceEntity);
        orderService.sendProcessScanOrderByUDP(orderEntity);
        return scanHandlerService.processScanHandler(orderEntity).get(10, TimeUnit.SECONDS);
    }

    // 根据id扫描设备磁盘信息
    public List<DiskScanResultEntity> getDisksById(String deviceId) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        DeviceEntity deviceEntity = getDeviceById(deviceId);
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setTag(OrderService.DISK_SCAN_TAG);
        orderEntity.setTargetDevice(deviceEntity);
        orderService.sendDiskScanOrderByUDP(orderEntity);
        return scanHandlerService.diskScanHandler(orderEntity).get(10, TimeUnit.SECONDS);
    }

    // 生成不重复的设备IP地址
    public String getHostAddress(String hostAddress, ProjectEntity projectEntity) {
        while (hasDeviceByHostAddressAndDeletedAndProject(hostAddress, false, projectEntity)) {
            hostAddress = IPUtils.longToIP(IPUtils.ipToLong(hostAddress) + 1);
        }
        return hostAddress;
    }
}
