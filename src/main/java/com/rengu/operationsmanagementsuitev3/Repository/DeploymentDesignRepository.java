package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-03 17:30
 **/

@Repository
public interface DeploymentDesignRepository extends JpaRepository<DeploymentDesignEntity, String> {

    boolean existsByNameAndDeletedAndProjectEntity(String name, boolean deleted, ProjectEntity projectEntity);

    Page<DeploymentDesignEntity> findAllByDeletedAndProjectEntity(Pageable pageable, boolean deleted, ProjectEntity projectEntity);

    long countAllByDeletedAndProjectEntity(boolean deleted, ProjectEntity projectEntity);

    List<DeploymentDesignEntity> findAllByProjectEntity(ProjectEntity projectEntity);
}
