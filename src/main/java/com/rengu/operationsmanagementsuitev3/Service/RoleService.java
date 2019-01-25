package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.RoleEntity;
import com.rengu.operationsmanagementsuitev3.Repository.RoleRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-22 17:03
 **/

@Slf4j
@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    // 保存角色
    @CachePut(value = "Role_Cache", key = "#roleEntity.name")
    public RoleEntity saveRole(RoleEntity roleEntity) {
        if (roleEntity == null) {
            throw new RuntimeException(ApplicationMessages.ROLE_ARGS_NOT_FOUND);
        }
        if (StringUtils.isEmpty(roleEntity.getName())) {
            throw new RuntimeException(ApplicationMessages.ROLE_NAME_NOT_FOUND + roleEntity.getName());
        }
        if (hasRoleByName(roleEntity.getName())) {
            throw new RuntimeException(ApplicationMessages.ROLE_NAME_EXISTED + roleEntity.getName());
        }
        return roleRepository.save(roleEntity);
    }

    // 根据名称检查角色是否存在
    public boolean hasRoleByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        return roleRepository.existsByName(name);
    }

    // 根据名称查询角色
    @Cacheable(value = "Role_Cache", key = "#name")
    public RoleEntity getRoleByName(String name) {
        if (!hasRoleByName(name)) {
            throw new RuntimeException(ApplicationMessages.ROLE_NAME_NOT_FOUND);
        }
        return roleRepository.findByName(name).get();
    }
}
