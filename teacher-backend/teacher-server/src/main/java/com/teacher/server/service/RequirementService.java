package com.teacher.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.common.exception.BusinessException;
import com.teacher.common.model.PageResult;
import com.teacher.common.security.LoginUser;
import com.teacher.common.security.LoginUserContext;
import com.teacher.common.util.IdUtil;
import com.teacher.pojo.dto.AuditRequest;
import com.teacher.pojo.dto.RequirementCreateRequest;
import com.teacher.pojo.entity.RegionEntity;
import com.teacher.pojo.entity.RequirementEntity;
import com.teacher.pojo.entity.SubjectEntity;
import com.teacher.pojo.entity.TeacherInfoEntity;
import com.teacher.pojo.enums.AuditStatusEnum;
import com.teacher.pojo.enums.RequirementStatusEnum;
import com.teacher.pojo.vo.PublicRequirementListItemVO;
import com.teacher.pojo.vo.RequirementPublicDetailVO;
import com.teacher.server.mapper.RegionMapper;
import com.teacher.server.mapper.RequirementMapper;
import com.teacher.server.mapper.SubjectMapper;
import com.teacher.server.mapper.TeacherInfoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RequirementService {
    private final RequirementMapper requirementMapper;
    private final SubjectMapper subjectMapper;
    private final RegionMapper regionMapper;
    private final TeacherInfoMapper teacherInfoMapper;
    private final AuthService authService;

    public RequirementService(RequirementMapper requirementMapper, SubjectMapper subjectMapper, RegionMapper regionMapper,
                              TeacherInfoMapper teacherInfoMapper, AuthService authService) {
        this.requirementMapper = requirementMapper;
        this.subjectMapper = subjectMapper;
        this.regionMapper = regionMapper;
        this.teacherInfoMapper = teacherInfoMapper;
        this.authService = authService;
    }

    public RequirementEntity create(RequirementCreateRequest request) {
        LoginUser loginUser = authService.currentLoginUser();
        if (loginUser.getUserType() == null || loginUser.getUserType() != 0) {
            throw new BusinessException("Only parent can create requirement");
        }
        LocalDateTime now = LocalDateTime.now();
        RequirementEntity entity = new RequirementEntity();
        entity.setId(IdUtil.uuid32());
        entity.setParentId(loginUser.getId());
        entity.setRequirementTitle(request.getRequirementTitle());
        entity.setRequirementDescription(request.getRequirementDescription());
        entity.setSubjectId(request.getSubjectId());
        entity.setRequirementGrade(request.getRequirementGrade());
        entity.setRegionId(request.getRegionId());
        entity.setRequirementAddress(request.getRequirementAddress());
        entity.setRequirementLongitude(request.getRequirementLongitude());
        entity.setRequirementLatitude(request.getRequirementLatitude());
        entity.setRequirementTutoringMethod(request.getRequirementTutoringMethod());
        entity.setRequirementFrequency(request.getRequirementFrequency());
        entity.setRequirementSalary(request.getRequirementSalary());
        entity.setStudentGender(request.getStudentGender());
        entity.setSalaryText(request.getSalaryText());
        entity.setCrossStreet(request.getCrossStreet());
        entity.setStudentDetail(request.getStudentDetail());
        entity.setTeacherQualification(request.getTeacherQualification());
        entity.setTeacherGenderPreference(request.getTeacherGenderPreference());
        entity.setTeacherRequirementText(request.getTeacherRequirementText());
        entity.setRequirementOther(request.getRequirementOther());
        entity.setRequirementImages(request.getRequirementImages());
        entity.setRequirementStatus(RequirementStatusEnum.WAITING.getCode());
        entity.setRequirementAuditStatus(AuditStatusEnum.PENDING.getCode());
        entity.setRequirementDeleteStatus(0);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        requirementMapper.insert(entity);
        return entity;
    }

    public PageResult<PublicRequirementListItemVO> pageOpen(long pageNo, long pageSize, String subjectId, String regionId) {
        Page<RequirementEntity> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<RequirementEntity> wrapper = new LambdaQueryWrapper<RequirementEntity>()
                .eq(RequirementEntity::getRequirementDeleteStatus, 0)
                .eq(RequirementEntity::getRequirementAuditStatus, AuditStatusEnum.APPROVED.getCode())
                .eq(RequirementEntity::getRequirementStatus, RequirementStatusEnum.WAITING.getCode())
                .orderByDesc(RequirementEntity::getCreateTime);
        if (subjectId != null && !subjectId.isBlank()) {
            wrapper.eq(RequirementEntity::getSubjectId, subjectId);
        }
        if (regionId != null && !regionId.isBlank()) {
            wrapper.eq(RequirementEntity::getRegionId, regionId);
        }
        Page<RequirementEntity> result = requirementMapper.selectPage(page, wrapper);
        List<RequirementEntity> records = result.getRecords();
        if (records.isEmpty()) {
            return new PageResult<>(result.getTotal(), pageNo, pageSize, Collections.emptyList());
        }
        List<String> subjectIds = records.stream().map(RequirementEntity::getSubjectId).distinct().collect(Collectors.toList());
        List<String> regionIds = records.stream().map(RequirementEntity::getRegionId).distinct().collect(Collectors.toList());
        Map<String, String> subjectNameMap = subjectMapper.selectBatchIds(subjectIds).stream()
                .collect(Collectors.toMap(SubjectEntity::getId, SubjectEntity::getSubjectName, (a, b) -> a));
        Map<String, String> regionNameMap = regionMapper.selectBatchIds(regionIds).stream()
                .collect(Collectors.toMap(RegionEntity::getId, RegionEntity::getRegionName, (a, b) -> a));
        List<PublicRequirementListItemVO> voList = records.stream().map(item -> {
            PublicRequirementListItemVO vo = new PublicRequirementListItemVO();
            vo.setId(item.getId());
            vo.setRequirementTitle(item.getRequirementTitle());
            vo.setRequirementGrade(item.getRequirementGrade());
            vo.setRequirementAddress(item.getRequirementAddress());
            vo.setRequirementTutoringMethod(item.getRequirementTutoringMethod());
            vo.setRequirementSalary(item.getRequirementSalary());
            vo.setSubjectId(item.getSubjectId());
            vo.setSubjectName(subjectNameMap.get(item.getSubjectId()));
            vo.setRegionId(item.getRegionId());
            vo.setRegionName(regionNameMap.get(item.getRegionId()));
            vo.setRequirementStatus(item.getRequirementStatus());
            vo.setRequirementStatusText(requirementStatusText(item.getRequirementStatus()));
            vo.setTeacherProfileVisibility(resolveTeacherProfileVisibility());
            vo.setCreateTime(item.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), pageNo, pageSize, voList);
    }

    public PageResult<RequirementEntity> myPage(long pageNo, long pageSize) {
        LoginUser loginUser = authService.currentLoginUser();
        Page<RequirementEntity> page = new Page<>(pageNo, pageSize);
        Page<RequirementEntity> result = requirementMapper.selectPage(page,
                new LambdaQueryWrapper<RequirementEntity>()
                        .eq(RequirementEntity::getParentId, loginUser.getId())
                        .eq(RequirementEntity::getRequirementDeleteStatus, 0)
                        .orderByDesc(RequirementEntity::getCreateTime));
        return new PageResult<>(result.getTotal(), pageNo, pageSize, result.getRecords());
    }

    public RequirementPublicDetailVO detail(String id) {
        RequirementEntity entity = requirementMapper.selectById(id);
        if (entity == null || entity.getRequirementDeleteStatus() == 1) {
            throw new BusinessException("Requirement not found");
        }
        RequirementPublicDetailVO vo = new RequirementPublicDetailVO();
        vo.setId(entity.getId());
        vo.setRequirementTitle(entity.getRequirementTitle());
        vo.setRequirementDescription(entity.getRequirementDescription());
        vo.setRequirementGrade(entity.getRequirementGrade());
        vo.setRequirementAddress(entity.getRequirementAddress());
        vo.setRequirementTutoringMethod(entity.getRequirementTutoringMethod());
        vo.setRequirementFrequency(entity.getRequirementFrequency());
        vo.setRequirementSalary(entity.getRequirementSalary());
        vo.setStudentGender(entity.getStudentGender());
        vo.setSalaryText(entity.getSalaryText());
        vo.setCrossStreet(entity.getCrossStreet());
        vo.setStudentDetail(entity.getStudentDetail());
        vo.setTeacherQualification(entity.getTeacherQualification());
        vo.setTeacherGenderPreference(entity.getTeacherGenderPreference());
        vo.setTeacherRequirementText(entity.getTeacherRequirementText());
        vo.setRequirementOther(entity.getRequirementOther());
        vo.setRequirementImages(entity.getRequirementImages());
        vo.setSubjectId(entity.getSubjectId());
        vo.setRegionId(entity.getRegionId());
        SubjectEntity subject = subjectMapper.selectById(entity.getSubjectId());
        RegionEntity region = regionMapper.selectById(entity.getRegionId());
        vo.setSubjectName(subject == null ? null : subject.getSubjectName());
        vo.setRegionName(region == null ? null : region.getRegionName());
        vo.setCreateTime(entity.getCreateTime());
        return vo;
    }

    public PageResult<RequirementEntity> adminPage(long pageNo, long pageSize, Integer status) {
        Page<RequirementEntity> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<RequirementEntity> wrapper = new LambdaQueryWrapper<RequirementEntity>()
                .eq(RequirementEntity::getRequirementDeleteStatus, 0)
                .orderByDesc(RequirementEntity::getCreateTime);
        if (status != null) {
            wrapper.eq(RequirementEntity::getRequirementStatus, status);
        }
        Page<RequirementEntity> result = requirementMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), pageNo, pageSize, result.getRecords());
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminAudit(String id, AuditRequest request) {
        if (!AuditStatusEnum.isValid(request.getAuditStatus())) {
            throw new BusinessException("Invalid audit status");
        }
        RequirementEntity db = requirementMapper.selectById(id);
        if (db == null || db.getRequirementDeleteStatus() == 1) {
            throw new BusinessException("Requirement not found");
        }
        if (request.getAuditStatus() != null && request.getAuditStatus() == AuditStatusEnum.PENDING.getCode()) {
            throw new BusinessException("Invalid audit status");
        }
        db.setRequirementAuditStatus(request.getAuditStatus());
        db.setUpdateTime(LocalDateTime.now());
        if (request.getAuditStatus() != null && request.getAuditStatus() == AuditStatusEnum.REJECTED.getCode()
                && db.getRequirementStatus() != null && db.getRequirementStatus() == RequirementStatusEnum.WAITING.getCode()) {
            db.setRequirementStatus(RequirementStatusEnum.CANCELED.getCode());
        }
        requirementMapper.updateById(db);
    }

    @Transactional(rollbackFor = Exception.class)
    public RequirementEntity cancelMyRequirement(String id) {
        LoginUser loginUser = authService.currentLoginUser();
        RequirementEntity db = requirementMapper.selectById(id);
        if (db == null || db.getRequirementDeleteStatus() == 1) {
            throw new BusinessException("Requirement not found");
        }
        if (!loginUser.getId().equals(db.getParentId())) {
            throw new BusinessException("No permission to cancel this requirement");
        }
        if (!RequirementStatusEnum.canTransit(db.getRequirementStatus(), RequirementStatusEnum.CANCELED.getCode())) {
            throw new BusinessException("Current status cannot be canceled");
        }
        int updated = requirementMapper.update(
                null,
                new LambdaUpdateWrapper<RequirementEntity>()
                        .set(RequirementEntity::getRequirementStatus, RequirementStatusEnum.CANCELED.getCode())
                        .set(RequirementEntity::getUpdateTime, LocalDateTime.now())
                        .eq(RequirementEntity::getId, id)
                        .eq(RequirementEntity::getParentId, loginUser.getId())
                        .eq(RequirementEntity::getRequirementDeleteStatus, 0)
        );
        if (updated == 0) {
            throw new BusinessException("Cancel failed, please retry");
        }
        return requirementMapper.selectById(id);
    }

    private String requirementStatusText(Integer requirementStatus) {
        if (requirementStatus == null) {
            return "UNKNOWN";
        }
        if (requirementStatus == RequirementStatusEnum.WAITING.getCode()) {
            return "WAITING";
        }
        if (requirementStatus == RequirementStatusEnum.RECEIVED.getCode()) {
            return "RECEIVED";
        }
        if (requirementStatus == RequirementStatusEnum.FINISHED.getCode()) {
            return "FINISHED";
        }
        if (requirementStatus == RequirementStatusEnum.CANCELED.getCode()) {
            return "CANCELED";
        }
        return "UNKNOWN";
    }

    private String resolveTeacherProfileVisibility() {
        LoginUser loginUser = LoginUserContext.get();
        if (loginUser == null || loginUser.getUserType() == null || loginUser.getUserType() != 1) {
            return "HIDDEN";
        }
        TeacherInfoEntity teacher = teacherInfoMapper.selectOne(new LambdaQueryWrapper<TeacherInfoEntity>()
                .eq(TeacherInfoEntity::getUserId, loginUser.getId())
                .eq(TeacherInfoEntity::getTeacherDeleteStatus, 0)
                .last("limit 1"));
        if (teacher == null || teacher.getTeacherAuditStatus() == null) {
            return "PENDING";
        }
        return teacher.getTeacherAuditStatus() == AuditStatusEnum.APPROVED.getCode() ? "VISIBLE" : "PENDING";
    }
}



