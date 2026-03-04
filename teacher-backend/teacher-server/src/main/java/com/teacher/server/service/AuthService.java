package com.teacher.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teacher.common.exception.BusinessException;
import com.teacher.common.security.JwtUtils;
import com.teacher.common.security.LoginUser;
import com.teacher.common.security.LoginUserContext;
import com.teacher.common.util.IdUtil;
import com.teacher.pojo.dto.LoginRequest;
import com.teacher.pojo.dto.RegisterRequest;
import com.teacher.pojo.entity.AdminEntity;
import com.teacher.pojo.entity.UserEntity;
import com.teacher.pojo.vo.LoginVO;
import com.teacher.server.mapper.AdminMapper;
import com.teacher.server.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final AdminMapper adminMapper;
    private final JwtUtils jwtUtils;

    public AuthService(UserMapper userMapper, AdminMapper adminMapper, JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.adminMapper = adminMapper;
        this.jwtUtils = jwtUtils;
    }

    public LoginVO userLogin(LoginRequest request) {
        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUserAccount, request.getAccount())
                .eq(UserEntity::getUserDeleteStatus, 0)
                .last("limit 1"));
        if (user == null || !user.getUserPassword().equals(request.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        String token = jwtUtils.generateToken(new LoginUser(user.getId(), user.getUserAccount(), user.getUserType(), false));
        return new LoginVO(token, user.getId(), user.getUserAccount(), user.getUserName(), user.getUserType(), false);
    }

    public LoginVO adminLogin(LoginRequest request) {
        AdminEntity admin = adminMapper.selectOne(new LambdaQueryWrapper<AdminEntity>()
                .eq(AdminEntity::getAdminAccount, request.getAccount())
                .eq(AdminEntity::getAdminDeleteStatus, 0)
                .last("limit 1"));
        if (admin == null || !admin.getAdminPassword().equals(request.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        String token = jwtUtils.generateToken(new LoginUser(admin.getId(), admin.getAdminAccount(), -1, true));
        return new LoginVO(token, admin.getId(), admin.getAdminAccount(), admin.getAdminName(), -1, true);
    }

    public LoginVO register(RegisterRequest request) {
        UserEntity exist = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUserAccount, request.getAccount())
                .last("limit 1"));
        if (exist != null) {
            throw new BusinessException("账号已存在");
        }
        UserEntity phoneExist = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUserPhone, request.getPhone())
                .last("limit 1"));
        if (phoneExist != null) {
            throw new BusinessException("手机号已存在");
        }
        LocalDateTime now = LocalDateTime.now();
        UserEntity user = new UserEntity();
        user.setId(IdUtil.uuid32());
        user.setUserAccount(request.getAccount());
        user.setUserPassword(request.getPassword());
        user.setUserName(request.getName());
        user.setUserPhone(request.getPhone());
        user.setUserType(request.getUserType());
        user.setUserStatus(1);
        user.setUserDeleteStatus(0);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userMapper.insert(user);
        String token = jwtUtils.generateToken(new LoginUser(user.getId(), user.getUserAccount(), user.getUserType(), false));
        return new LoginVO(token, user.getId(), user.getUserAccount(), user.getUserName(), user.getUserType(), false);
    }

    public LoginUser currentLoginUser() {
        LoginUser loginUser = LoginUserContext.get();
        if (loginUser == null) {
            throw new BusinessException("请先登录");
        }
        return loginUser;
    }

    public LoginUser requireAdmin() {
        LoginUser loginUser = currentLoginUser();
        if (!loginUser.isAdmin()) {
            throw new BusinessException("仅管理员可操作");
        }
        return loginUser;
    }
}
