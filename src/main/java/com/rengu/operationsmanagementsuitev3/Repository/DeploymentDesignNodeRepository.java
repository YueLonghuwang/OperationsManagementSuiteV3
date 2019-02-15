package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignNodeEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeviceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-04 10:00
 **/

@Repository
public interface DeploymentDesignNodeRepository extends JpaRepository<DeploymentDesignNodeEntity, String> {

    Page<DeploymentDesignNodeEntity> findAllByDeploymentDesignEntity(Pageable pageable, DeploymentDesignEntity deploymentDesignEntity);

    List<DeploymentDesignNodeEntity> findAllByDeploymentDesignEntity(DeploymentDesignEntity deploymentDesignEntity);

    List<DeploymentDesignNodeEntity> findAllByDeviceEntity(DeviceEntity deviceEntity);

    boolean existsByDeviceEntityAndDeploymentDesignEntity(DeviceEntity deviceEntity, DeploymentDesignEntity deploymentDesignEntity);
}
