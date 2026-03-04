package com.teacher.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.common.exception.BusinessException;
import com.teacher.common.model.PageResult;
import com.teacher.common.security.LoginUser;
import com.teacher.common.util.IdUtil;
import com.teacher.pojo.dto.AuditRequest;
import com.teacher.pojo.dto.TeacherProfileRequest;
import com.teacher.pojo.entity.TeacherAuditEntity;
import com.teacher.pojo.entity.TeacherInfoEntity;
import com.teacher.pojo.entity.UserEntity;
import com.teacher.server.mapper.TeacherAuditMapper;
import com.teacher.server.mapper.TeacherInfoMapper;
import com.teacher.server.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TeacherService {
    private final TeacherInfoMapper teacherInfoMapper;
    private final TeacherAuditMapper teacherAuditMapper;
    private final UserMapper userMapper;
    private final AuthService authService;

    public TeacherService(TeacherInfoMapper teacherInfoMapper, TeacherAuditMapper teacherAuditMapper, UserMapper userMapper, AuthService authService) {
        this.teacherInfoMapper = teacherInfoMapper;
        this.teacherAuditMapper = teacherAuditMapper;
        this.userMapper = userMapper;
        this.authService = authService;
    }

    public TeacherInfoEntity saveMine(TeacherProfileRequest request) {
        LoginUser loginUser = authService.currentLoginUser();
        UserEntity user = userMapper.selectById(loginUser.getId());
        if (user == null || user.getUserType() == null || user.getUserType() != 1) {
            throw new BusinessException("仅教员可操作");
        }
        TeacherInfoEntity db = teacherInfoMapper.selectOne(new LambdaQueryWrapper<TeacherInfoEntity>()
                .eq(TeacherInfoEntity::getUserId, loginUser.getId())
                .eq(TeacherInfoEntity::getTeacherDeleteStatus, 0)
                .last("limit 1"));
        LocalDateTime now = LocalDateTime.now();
        if (db == null) {
            db = new TeacherInfoEntity();
            db.setId(IdUtil.uuid32());
            db.setUserId(loginUser.getId());
            db.setTeacherSuccessCount(0);
            db.setTeacherViewCount(0);
            db.setTeacherAuditStatus(0);
            db.setTeacherEnableStatus(1);
            db.setTeacherDeleteStatus(0);
            db.setCreateTime(now);
        }
        db.setTeacherIdentity(request.getTeacherIdentity());
        db.setTeacherTutoringMethod(request.getTeacherTutoringMethod());
        db.setTeacherTeachingYears(request.getTeacherTeachingYears() == null ? 0 : request.getTeacherTeachingYears());
        db.setTeacherSchool(request.getTeacherSchool());
        db.setTeacherMajor(request.getTeacherMajor());
        db.setTeacherEducation(request.getTeacherEducation());
        db.setTeacherExperience(request.getTeacherExperience());
        db.setTeacherSelfDescription(request.getTeacherSelfDescription());
        db.setUpdateTime(now);
        if (teacherInfoMapper.selectById(db.getId()) == null) {
            teacherInfoMapper.insert(db);
        } else {
            teacherInfoMapper.updateById(db);
        }
        return db;
    }

    public TeacherInfoEntity mine() {
        LoginUser loginUser = authService.currentLoginUser();
        return teacherInfoMapper.selectOne(new LambdaQueryWrapper<TeacherInfoEntity>()
                .eq(TeacherInfoEntity::getUserId, loginUser.getId())
                .eq(TeacherInfoEntity::getTeacherDeleteStatus, 0)
                .last("limit 1"));
    }

    public PageResult<TeacherInfoEntity> page(long pageNo, long pageSize, String keyword, Integer auditStatus) {
        Page<TeacherInfoEntity> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<TeacherInfoEntity> wrapper = new LambdaQueryWrapper<TeacherInfoEntity>()
                .eq(TeacherInfoEntity::getTeacherDeleteStatus, 0)
                .orderByDesc(TeacherInfoEntity::getCreateTime);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(TeacherInfoEntity::getTeacherIdentity, keyword)
                    .or()
                    .like(TeacherInfoEntity::getTeacherSchool, keyword)
                    .or()
                    .like(TeacherInfoEntity::getTeacherMajor, keyword));
        }
        if (auditStatus != null) {
            wrapper.eq(TeacherInfoEntity::getTeacherAuditStatus, auditStatus);
        }
        Page<TeacherInfoEntity> result = teacherInfoMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), pageNo, pageSize, result.getRecords());
    }

    public void audit(String id, AuditRequest request) {
        TeacherInfoEntity db = teacherInfoMapper.selectById(id);
        if (db == null) {
            throw new BusinessException("教员不存在");
        }
        db.setTeacherAuditStatus(request.getAuditStatus());
        db.setUpdateTime(LocalDateTime.now());
        teacherInfoMapper.updateById(db);

        TeacherAuditEntity audit = new TeacherAuditEntity();
        audit.setId(IdUtil.uuid32());
        audit.setTeacherId(id);
        audit.setAuditStatus(request.getAuditStatus());
        audit.setAuditReason(request.getReason());
        audit.setAuditTime(LocalDateTime.now());
        audit.setCreateTime(LocalDateTime.now());
        audit.setUpdateTime(LocalDateTime.now());
        teacherAuditMapper.insert(audit);
    }
}
