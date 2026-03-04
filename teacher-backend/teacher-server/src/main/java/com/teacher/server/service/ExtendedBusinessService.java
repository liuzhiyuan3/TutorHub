package com.teacher.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.common.exception.BusinessException;
import com.teacher.common.model.PageResult;
import com.teacher.common.security.LoginUser;
import com.teacher.common.util.IdUtil;
import com.teacher.pojo.entity.AppointmentEntity;
import com.teacher.pojo.entity.DispatchRecordEntity;
import com.teacher.pojo.entity.FavoriteTeacherEntity;
import com.teacher.server.mapper.AppointmentMapper;
import com.teacher.server.mapper.DispatchRecordMapper;
import com.teacher.server.mapper.FavoriteTeacherMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ExtendedBusinessService {
    private final AppointmentMapper appointmentMapper;
    private final FavoriteTeacherMapper favoriteTeacherMapper;
    private final DispatchRecordMapper dispatchRecordMapper;
    private final AuthService authService;

    public ExtendedBusinessService(AppointmentMapper appointmentMapper, FavoriteTeacherMapper favoriteTeacherMapper, DispatchRecordMapper dispatchRecordMapper, AuthService authService) {
        this.appointmentMapper = appointmentMapper;
        this.favoriteTeacherMapper = favoriteTeacherMapper;
        this.dispatchRecordMapper = dispatchRecordMapper;
        this.authService = authService;
    }

    public AppointmentEntity createAppointment(AppointmentEntity entity) {
        LoginUser loginUser = authService.currentLoginUser();
        if (loginUser.getUserType() == null || loginUser.getUserType() != 0) {
            throw new BusinessException("仅家长可预约");
        }
        entity.setId(IdUtil.uuid32());
        entity.setParentId(loginUser.getId());
        entity.setAppointmentStatus(entity.getAppointmentStatus() == null ? 0 : entity.getAppointmentStatus());
        entity.setAppointmentDeleteStatus(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        appointmentMapper.insert(entity);
        return entity;
    }

    public PageResult<AppointmentEntity> myAppointments(long pageNo, long pageSize) {
        LoginUser loginUser = authService.currentLoginUser();
        Page<AppointmentEntity> page = new Page<>(pageNo, pageSize);
        Page<AppointmentEntity> result = appointmentMapper.selectPage(page, new LambdaQueryWrapper<AppointmentEntity>()
                .eq(AppointmentEntity::getParentId, loginUser.getId())
                .eq(AppointmentEntity::getAppointmentDeleteStatus, 0)
                .orderByDesc(AppointmentEntity::getCreateTime));
        return new PageResult<>(result.getTotal(), pageNo, pageSize, result.getRecords());
    }

    public FavoriteTeacherEntity addFavorite(String teacherId) {
        LoginUser loginUser = authService.currentLoginUser();
        FavoriteTeacherEntity db = favoriteTeacherMapper.selectOne(new LambdaQueryWrapper<FavoriteTeacherEntity>()
                .eq(FavoriteTeacherEntity::getParentId, loginUser.getId())
                .eq(FavoriteTeacherEntity::getTeacherId, teacherId)
                .last("limit 1"));
        if (db != null) {
            return db;
        }
        FavoriteTeacherEntity entity = new FavoriteTeacherEntity();
        entity.setId(IdUtil.uuid32());
        entity.setParentId(loginUser.getId());
        entity.setTeacherId(teacherId);
        entity.setCreateTime(LocalDateTime.now());
        favoriteTeacherMapper.insert(entity);
        return entity;
    }

    public PageResult<FavoriteTeacherEntity> myFavorites(long pageNo, long pageSize) {
        LoginUser loginUser = authService.currentLoginUser();
        Page<FavoriteTeacherEntity> page = new Page<>(pageNo, pageSize);
        Page<FavoriteTeacherEntity> result = favoriteTeacherMapper.selectPage(page, new LambdaQueryWrapper<FavoriteTeacherEntity>()
                .eq(FavoriteTeacherEntity::getParentId, loginUser.getId())
                .orderByDesc(FavoriteTeacherEntity::getCreateTime));
        return new PageResult<>(result.getTotal(), pageNo, pageSize, result.getRecords());
    }

    public PageResult<DispatchRecordEntity> dispatchPage(long pageNo, long pageSize) {
        Page<DispatchRecordEntity> page = new Page<>(pageNo, pageSize);
        Page<DispatchRecordEntity> result = dispatchRecordMapper.selectPage(page, new LambdaQueryWrapper<DispatchRecordEntity>()
                .orderByDesc(DispatchRecordEntity::getDispatchTime));
        return new PageResult<>(result.getTotal(), pageNo, pageSize, result.getRecords());
    }

    public PageResult<AppointmentEntity> adminAppointmentPage(long pageNo, long pageSize) {
        Page<AppointmentEntity> page = new Page<>(pageNo, pageSize);
        Page<AppointmentEntity> result = appointmentMapper.selectPage(page, new LambdaQueryWrapper<AppointmentEntity>()
                .eq(AppointmentEntity::getAppointmentDeleteStatus, 0)
                .orderByDesc(AppointmentEntity::getCreateTime));
        return new PageResult<>(result.getTotal(), pageNo, pageSize, result.getRecords());
    }

    public PageResult<FavoriteTeacherEntity> adminFavoritePage(long pageNo, long pageSize) {
        Page<FavoriteTeacherEntity> page = new Page<>(pageNo, pageSize);
        Page<FavoriteTeacherEntity> result = favoriteTeacherMapper.selectPage(page, new LambdaQueryWrapper<FavoriteTeacherEntity>()
                .orderByDesc(FavoriteTeacherEntity::getCreateTime));
        return new PageResult<>(result.getTotal(), pageNo, pageSize, result.getRecords());
    }
}
