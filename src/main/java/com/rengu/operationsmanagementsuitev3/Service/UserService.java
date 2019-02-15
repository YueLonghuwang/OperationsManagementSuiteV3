package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.RoleEntity;
import com.rengu.operationsmanagementsuitev3.Entity.UserEntity;
import com.rengu.operationsmanagementsuitev3.Repository.UserRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-22 17:05
 **/

@Slf4j
@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleService roleService;

    @Autowired
    public UserService(UserRepository userRepository, RoleService roleService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUserByUsername(username);
    }

    //保存普通用户
    public UserEntity saveDefaultUser(UserEntity userEntity) {
        return saveUser(userEntity, roleService.getRoleByName(ApplicationConfig.DEFAULT_USER_ROLE_NAME));
    }

    //保存管理员用户
    public UserEntity saveAdminUser(UserEntity userEntity) {
        return saveUser(userEntity, roleService.getRoleByName(ApplicationConfig.DEFAULT_USER_ROLE_NAME), roleService.getRoleByName(ApplicationConfig.DEFAULT_ADMIN_ROLE_NAME));
    }

    // 保存用户
    @CachePut(value = "User_Cache", key = "#userEntity.id")
    public UserEntity saveUser(UserEntity userEntity, RoleEntity... roleEntities) {
        if (userEntity == null) {
            throw new RuntimeException(ApplicationMessages.USER_ARGS_NOT_FOUND);
        }
        if (StringUtils.isEmpty(userEntity.getUsername())) {
            throw new RuntimeException(ApplicationMessages.USER_USERNAME_ARGS_NOT_FOUND);
        }
        if (hasUserByUsername(userEntity.getUsername())) {
            throw new RuntimeException(ApplicationMessages.USER_USERNAME_EXISTED + userEntity.getUsername());
        }
        if (StringUtils.isEmpty(userEntity.getPassword())) {
            throw new RuntimeException(ApplicationMessages.USER_PASSWORD_ARGS_NOT_FOUND);
        }
        userEntity.setPassword(new BCryptPasswordEncoder().encode(userEntity.getPassword()));
        Set<RoleEntity> roleEntitySet = userEntity.getRoleEntities() == null ? new HashSet<>() : userEntity.getRoleEntities();
        roleEntitySet.addAll(Arrays.asList(roleEntities));
        userEntity.setRoleEntities(roleEntitySet);
        return userRepository.save(userEntity);
    }

    // 根据Id删除用户
    @CacheEvict(value = "User_Cache", key = "#userId")
    public UserEntity deleteUserById(String userId) {
        UserEntity userEntity = getUserById(userId);
        userRepository.delete(userEntity);
        return userEntity;
    }

    // 根据Id修改用户密码
    @CachePut(value = "User_Cache", key = "#userId")
    public UserEntity updateUserPasswordById(String userId, String password) {
        if (StringUtils.isEmpty(password)) {
            throw new RuntimeException(ApplicationMessages.USER_PASSWORD_ARGS_NOT_FOUND);
        }
        UserEntity userEntity = getUserById(userId);
        userEntity.setPassword(new BCryptPasswordEncoder().encode(password));
        return userRepository.save(userEntity);
    }

    // 根据Id升级用户
    @CachePut(value = "User_Cache", key = "#userId")
    public UserEntity userUpgradeById(String userId) {
        UserEntity userEntity = getUserById(userId);
        if (hasAdminAuthorityByUser(userEntity)) {
            throw new RuntimeException(ApplicationMessages.USER_HAS_ADMIN_ROLE);
        }
        userEntity.getRoleEntities().add(roleService.getRoleByName(ApplicationConfig.DEFAULT_ADMIN_ROLE_NAME));
        return userRepository.save(userEntity);
    }

    // 根据Id降级用户
    @CachePut(value = "User_Cache", key = "#userId")
    public UserEntity userDegradeById(String userId) {
        UserEntity userEntity = getUserById(userId);
        if (!hasAdminAuthorityByUser(userEntity)) {
            throw new RuntimeException(ApplicationMessages.USER_NOT_HAS_ADMIN_ROLE);
        }
        userEntity.getRoleEntities().remove(roleService.getRoleByName(ApplicationConfig.DEFAULT_ADMIN_ROLE_NAME));
        return userRepository.save(userEntity);
    }

    // 根据用户名查询用户是否存在
    public boolean hasUserByUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            return false;
        }
        return userRepository.existsByUsername(username);
    }

    // 根据Id查询用户是否存在
    public boolean hasUserById(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return false;
        }
        return userRepository.existsById(userId);
    }

    // 检查用户是否具有管理员权限
    public boolean hasAdminAuthorityByUser(UserEntity userEntity) {
        return userEntity.getRoleEntities().contains(roleService.getRoleByName(ApplicationConfig.DEFAULT_ADMIN_ROLE_NAME));
    }

    // 根据用户名查询用户
    public UserEntity getUserByUsername(String username) {
        if (!hasUserByUsername(username)) {
            throw new UsernameNotFoundException(ApplicationMessages.USER_USERNAME_NOT_FOUND + username);
        }
        return userRepository.findByUsername(username).get();
    }

    // 根据用户Id查询用户
    @Cacheable(value = "User_Cache", key = "#userId")
    public UserEntity getUserById(String userId) {
        if (!hasUserById(userId)) {
            throw new RuntimeException(ApplicationMessages.USER_ID_NOT_FOUND + userId);
        }
        return userRepository.findById(userId).get();
    }

    // 查询所有用户
    public Page<UserEntity> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}
