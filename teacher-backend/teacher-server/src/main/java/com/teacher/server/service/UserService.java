package com.teacher.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.common.exception.BusinessException;
import com.teacher.common.model.PageResult;
import com.teacher.common.security.LoginUser;
import com.teacher.pojo.entity.UserEntity;
import com.teacher.server.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final AuthService authService;

    public UserService(UserMapper userMapper, AuthService authService) {
        this.userMapper = userMapper;
        this.authService = authService;
    }

    public UserEntity me() {
        LoginUser loginUser = authService.currentLoginUser();
        UserEntity user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setUserPassword(null);
        return user;
    }

    public UserEntity updateMe(UserEntity request) {
        LoginUser loginUser = authService.currentLoginUser();
        UserEntity db = userMapper.selectById(loginUser.getId());
        if (db == null) {
            throw new BusinessException("用户不存在");
        }
        db.setUserName(request.getUserName());
        db.setUserEmail(request.getUserEmail());
        db.setUserGender(request.getUserGender());
        db.setUserPortrait(request.getUserPortrait());
        db.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(db);
        db.setUserPassword(null);
        return db;
    }

    public PageResult<UserEntity> adminPage(long pageNo, long pageSize, String keyword) {
        Page<UserEntity> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUserDeleteStatus, 0)
                .orderByDesc(UserEntity::getCreateTime);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(UserEntity::getUserName, keyword)
                    .or()
                    .like(UserEntity::getUserAccount, keyword)
                    .or()
                    .like(UserEntity::getUserPhone, keyword));
        }
        Page<UserEntity> result = userMapper.selectPage(page, wrapper);
        result.getRecords().forEach(u -> u.setUserPassword(null));
        return new PageResult<>(result.getTotal(), pageNo, pageSize, result.getRecords());
    }
}
