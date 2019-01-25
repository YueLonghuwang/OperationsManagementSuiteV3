package com.rengu.operationsmanagementsuitev3.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-04 09:07
 **/

@Data
@Entity
public class DeploymentDesignDetailEntity implements Serializable {

    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private boolean keepLatest;
    @ManyToOne
    private ComponentHistoryEntity componentHistoryEntity;
    @ManyToOne
    private ComponentEntity componentEntity;
    @ManyToOne
    private DeploymentDesignNodeEntity deploymentDesignNodeEntity;
    @ManyToOne
    private DeploymentDesignEntity deploymentDesignEntity;
}