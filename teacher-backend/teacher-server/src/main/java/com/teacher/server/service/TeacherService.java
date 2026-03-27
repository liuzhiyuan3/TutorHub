package com.teacher.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.common.exception.BusinessException;
import com.teacher.common.model.PageResult;
import com.teacher.common.security.LoginUser;
import com.teacher.common.util.IdUtil;
import com.teacher.pojo.dto.AuditRequest;
import com.teacher.pojo.dto.TeacherProfileRequest;
import com.teacher.pojo.entity.OrderEntity;
import com.teacher.pojo.entity.RegionEntity;
import com.teacher.pojo.entity.SubjectEntity;
import com.teacher.pojo.entity.TeacherAuditEntity;
import com.teacher.pojo.entity.TeacherInfoEntity;
import com.teacher.pojo.entity.TeacherRegionEntity;
import com.teacher.pojo.entity.TeacherSubjectEntity;
import com.teacher.pojo.entity.TeacherSuccessRecordEntity;
import com.teacher.pojo.entity.UserEntity;
import com.teacher.pojo.enums.AuditStatusEnum;
import com.teacher.pojo.vo.TeacherPublicDetailVO;
import com.teacher.pojo.vo.TeacherSuccessRecordVO;
import com.teacher.pojo.vo.admin.AdminTeacherProfileVO;
import com.teacher.server.mapper.OrderMapper;
import com.teacher.server.mapper.RegionMapper;
import com.teacher.server.mapper.SubjectMapper;
import com.teacher.server.mapper.TeacherAuditMapper;
import com.teacher.server.mapper.TeacherInfoMapper;
import com.teacher.server.mapper.TeacherRegionMapper;
import com.teacher.server.mapper.TeacherSubjectMapper;
import com.teacher.server.mapper.TeacherSuccessRecordMapper;
import com.teacher.server.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherService {
    private final TeacherInfoMapper teacherInfoMapper;
    private final TeacherAuditMapper teacherAuditMapper;
    private final TeacherSubjectMapper teacherSubjectMapper;
    private final TeacherRegionMapper teacherRegionMapper;
    private final TeacherSuccessRecordMapper teacherSuccessRecordMapper;
    private final SubjectMapper subjectMapper;
    private final RegionMapper regionMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final AuthService authService;
    private final TeacherOrderStatsService teacherOrderStatsService;
    private final MediaUrlService mediaUrlService;

    public TeacherService(TeacherInfoMapper teacherInfoMapper,
                          TeacherAuditMapper teacherAuditMapper,
                          TeacherSubjectMapper teacherSubjectMapper,
                          TeacherRegionMapper teacherRegionMapper,
                          TeacherSuccessRecordMapper teacherSuccessRecordMapper,
                          SubjectMapper subjectMapper,
                          RegionMapper regionMapper,
                          OrderMapper orderMapper,
                          UserMapper userMapper,
                          AuthService authService,
                          TeacherOrderStatsService teacherOrderStatsService,
                          MediaUrlService mediaUrlService) {
        this.teacherInfoMapper = teacherInfoMapper;
        this.teacherAuditMapper = teacherAuditMapper;
        this.teacherSubjectMapper = teacherSubjectMapper;
        this.teacherRegionMapper = teacherRegionMapper;
        this.teacherSuccessRecordMapper = teacherSuccessRecordMapper;
        this.subjectMapper = subjectMapper;
        this.regionMapper = regionMapper;
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.authService = authService;
        this.teacherOrderStatsService = teacherOrderStatsService;
        this.mediaUrlService = mediaUrlService;
    }

    public TeacherInfoEntity saveMine(TeacherProfileRequest request) {
        LoginUser loginUser = authService.currentLoginUser();
        UserEntity user = userMapper.selectById(loginUser.getId());
        if (user == null || user.getUserType() == null || user.getUserType() != 1) {
            throw new BusinessException("Only teacher can operate");
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
        db.setTeacherPhoto(mediaUrlService.normalize(request.getTeacherPhoto()));
        db.setTeacherExperience(request.getTeacherExperience());
        db.setTeacherSelfDescription(request.getTeacherSelfDescription());
        db.setTeacherProfileCompleted(1);
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
        result.getRecords().forEach(item -> item.setTeacherPhoto(mediaUrlService.normalize(item.getTeacherPhoto())));
        return new PageResult<>(result.getTotal(), pageNo, pageSize, result.getRecords());
    }

    public void audit(String id, AuditRequest request) {
        if (!AuditStatusEnum.isValid(request.getAuditStatus())) {
            throw new BusinessException("Invalid audit status");
        }
        if (request.getAuditStatus() != null
                && request.getAuditStatus() == AuditStatusEnum.REJECTED.getCode()
                && (request.getReason() == null || request.getReason().isBlank())) {
            throw new BusinessException("Reject reason is required");
        }
        TeacherInfoEntity db = teacherInfoMapper.selectById(id);
        if (db == null) {
            throw new BusinessException("Teacher not found");
        }
        if (request.getAuditStatus() != null && request.getAuditStatus() == AuditStatusEnum.PENDING.getCode()) {
            throw new BusinessException("Teacher not found");
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

    public AdminTeacherProfileVO adminProfile(String teacherId) {
        TeacherInfoEntity teacherInfo = teacherInfoMapper.selectById(teacherId);
        if (teacherInfo == null || teacherInfo.getTeacherDeleteStatus() == 1) {
            throw new BusinessException("Teacher not found");
        }
        UserEntity user = userMapper.selectById(teacherInfo.getUserId());
        if (user != null) {
            user.setUserPassword(null);
        }

        AdminTeacherProfileVO profile = new AdminTeacherProfileVO();
        profile.setTeacherInfo(teacherInfo);
        profile.setUser(user);

        List<String> subjectIds = teacherSubjectMapper.selectList(new LambdaQueryWrapper<TeacherSubjectEntity>()
                        .eq(TeacherSubjectEntity::getTeacherId, teacherId))
                .stream()
                .map(TeacherSubjectEntity::getSubjectId)
                .collect(Collectors.toList());
        if (!subjectIds.isEmpty()) {
            List<String> subjectNames = subjectMapper.selectBatchIds(subjectIds)
                    .stream()
                    .filter(s -> s.getSubjectDeleteStatus() == null || s.getSubjectDeleteStatus() == 0)
                    .map(SubjectEntity::getSubjectName)
                    .collect(Collectors.toList());
            profile.setSubjectNames(subjectNames);
        }

        List<String> regionIds = teacherRegionMapper.selectList(new LambdaQueryWrapper<TeacherRegionEntity>()
                        .eq(TeacherRegionEntity::getTeacherId, teacherId))
                .stream()
                .map(TeacherRegionEntity::getRegionId)
                .collect(Collectors.toList());
        if (!regionIds.isEmpty()) {
            List<String> regionNames = regionMapper.selectBatchIds(regionIds)
                    .stream()
                    .filter(r -> r.getRegionDeleteStatus() == null || r.getRegionDeleteStatus() == 0)
                    .map(RegionEntity::getRegionName)
                    .collect(Collectors.toList());
            profile.setRegionNames(regionNames);
        }

        List<TeacherSuccessRecordEntity> successRecords = teacherSuccessRecordMapper.selectList(
                new LambdaQueryWrapper<TeacherSuccessRecordEntity>()
                        .eq(TeacherSuccessRecordEntity::getTeacherId, teacherId)
                        .eq(TeacherSuccessRecordEntity::getSuccessDeleteStatus, 0)
                        .orderByDesc(TeacherSuccessRecordEntity::getSuccessOrderDate)
                        .last("limit 20"));
        profile.setSuccessRecords(successRecords);

        List<TeacherAuditEntity> auditRecords = teacherAuditMapper.selectList(
                new LambdaQueryWrapper<TeacherAuditEntity>()
                        .eq(TeacherAuditEntity::getTeacherId, teacherId)
                        .orderByDesc(TeacherAuditEntity::getCreateTime)
                        .last("limit 20"));
        profile.setAuditRecords(auditRecords);

        List<OrderEntity> recentOrders = orderMapper.selectList(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getTeacherId, teacherId)
                .eq(OrderEntity::getOrderDeleteStatus, 0)
                .orderByDesc(OrderEntity::getCreateTime)
                .last("limit 20"));
        profile.setRecentOrders(recentOrders);
        return profile;
    }

    public TeacherPublicDetailVO publicDetail(String teacherId) {
        TeacherInfoEntity teacherInfo = teacherInfoMapper.selectOne(new LambdaQueryWrapper<TeacherInfoEntity>()
                .eq(TeacherInfoEntity::getId, teacherId)
                .eq(TeacherInfoEntity::getTeacherDeleteStatus, 0)
                .last("limit 1"));
        if (teacherInfo == null) {
            throw new BusinessException("Teacher not found");
        }
        if (teacherInfo.getTeacherEnableStatus() == null || teacherInfo.getTeacherEnableStatus() != 1) {
            throw new BusinessException("Teacher profile is not visible");
        }
        if (teacherInfo.getTeacherAuditStatus() == null || teacherInfo.getTeacherAuditStatus() != AuditStatusEnum.APPROVED.getCode()) {
            throw new BusinessException("Teacher profile is under review");
        }
        teacherInfoMapper.update(
                null,
                new LambdaUpdateWrapper<TeacherInfoEntity>()
                        .setSql("teacher_view_count = ifnull(teacher_view_count, 0) + 1")
                        .set(TeacherInfoEntity::getUpdateTime, LocalDateTime.now())
                        .eq(TeacherInfoEntity::getId, teacherId)
        );
        teacherInfo.setTeacherViewCount((teacherInfo.getTeacherViewCount() == null ? 0 : teacherInfo.getTeacherViewCount()) + 1);
        UserEntity user = userMapper.selectById(teacherInfo.getUserId());

        TeacherPublicDetailVO vo = new TeacherPublicDetailVO();
        vo.setTeacherId(teacherInfo.getId());
        vo.setUserId(teacherInfo.getUserId());
        vo.setUserName(user == null ? null : user.getUserName());
        vo.setTeacherPhoto(mediaUrlService.normalize(teacherInfo.getTeacherPhoto()));
        vo.setTeacherIdentity(teacherInfo.getTeacherIdentity());
        vo.setTeacherTutoringMethod(teacherInfo.getTeacherTutoringMethod());
        vo.setTeacherTeachingYears(teacherInfo.getTeacherTeachingYears());
        vo.setTeacherSchool(teacherInfo.getTeacherSchool());
        vo.setTeacherMajor(teacherInfo.getTeacherMajor());
        vo.setTeacherEducation(teacherInfo.getTeacherEducation());
        vo.setTeacherExperience(teacherInfo.getTeacherExperience());
        vo.setTeacherSelfDescription(teacherInfo.getTeacherSelfDescription());
        vo.setTeacherSuccessCount(teacherInfo.getTeacherSuccessCount());
        vo.setTeacherViewCount(teacherInfo.getTeacherViewCount());
        TeacherOrderStatsService.TeacherOrderStats stats = teacherOrderStatsService.buildSingleTeacherOrderStats(teacherId);
        vo.setHistoryDealCount(stats.getHistoryDealCount());
        vo.setHireCount(stats.getHireCount());

        List<String> subjectIds = teacherSubjectMapper.selectList(new LambdaQueryWrapper<TeacherSubjectEntity>()
                        .eq(TeacherSubjectEntity::getTeacherId, teacherId))
                .stream()
                .map(TeacherSubjectEntity::getSubjectId)
                .distinct()
                .collect(Collectors.toList());
        if (!subjectIds.isEmpty()) {
            vo.setSubjectNames(subjectMapper.selectBatchIds(subjectIds)
                    .stream()
                    .filter(s -> s.getSubjectDeleteStatus() == null || s.getSubjectDeleteStatus() == 0)
                    .map(SubjectEntity::getSubjectName)
                    .collect(Collectors.toList()));
        } else {
            vo.setSubjectNames(Collections.emptyList());
        }

        List<String> regionIds = teacherRegionMapper.selectList(new LambdaQueryWrapper<TeacherRegionEntity>()
                        .eq(TeacherRegionEntity::getTeacherId, teacherId))
                .stream()
                .map(TeacherRegionEntity::getRegionId)
                .distinct()
                .collect(Collectors.toList());
        if (!regionIds.isEmpty()) {
            vo.setRegionNames(regionMapper.selectBatchIds(regionIds)
                    .stream()
                    .filter(r -> r.getRegionDeleteStatus() == null || r.getRegionDeleteStatus() == 0)
                    .map(RegionEntity::getRegionName)
                    .collect(Collectors.toList()));
        } else {
            vo.setRegionNames(Collections.emptyList());
        }

        List<TeacherSuccessRecordVO> successRecords = teacherSuccessRecordMapper.selectList(
                        new LambdaQueryWrapper<TeacherSuccessRecordEntity>()
                                .eq(TeacherSuccessRecordEntity::getTeacherId, teacherId)
                                .eq(TeacherSuccessRecordEntity::getSuccessDeleteStatus, 0)
                                .orderByDesc(TeacherSuccessRecordEntity::getSuccessOrderDate)
                                .last("limit 20"))
                .stream()
                .map(item -> {
                    TeacherSuccessRecordVO record = new TeacherSuccessRecordVO();
                    record.setSuccessGrade(item.getSuccessGrade());
                    record.setSuccessOrderDate(item.getSuccessOrderDate());
                    record.setSuccessDescription(item.getSuccessDescription());
                    return record;
                })
                .collect(Collectors.toList());
        vo.setSuccessRecords(successRecords);
        return vo;
    }
}




