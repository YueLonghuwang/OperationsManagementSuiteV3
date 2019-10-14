package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-24 10:17
 **/

@Repository
public interface FileRepository extends JpaRepository<FileEntity, String> {

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    boolean existsByMD5(String md5);

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    Optional<FileEntity> findByMD5(String md5);
}
