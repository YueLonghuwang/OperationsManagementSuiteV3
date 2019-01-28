package com.rengu.operationsmanagementsuitev3.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-30 15:19
 **/

@Data
public class OrderEntity implements Serializable {

    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private String tag;
    private DeploymentDesignNodeEntity deploymentDesignNodeEntity;
    private DeploymentDesignDetailEntity deploymentDesignDetailEntity;
    private String extension;
    private String targetPath;
    private DeviceEntity targetDevice;
}
