package com.teacher.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.common.exception.BusinessException;
import com.teacher.common.model.PageResult;
import com.teacher.common.security.LoginUser;
import com.teacher.common.util.IdUtil;
import com.teacher.pojo.dto.RequirementCreateRequest;
import com.teacher.pojo.entity.RequirementEntity;
import com.teacher.server.mapper.RequirementMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RequirementService {
    private final RequirementMapper requirementMapper;
    private final AuthService authService;

    public RequirementService(RequirementMapper requirementMapper, AuthService authService) {
        this.requirementMapper = requirementMapper;
        this.authService = authService;
    }

    public RequirementEntity create(RequirementCreateRequest request) {
        LoginUser loginUser = authService.currentLoginUser();
        if (loginUser.getUserType() == null || loginUser.getUserType() != 0) {
            throw new BusinessException("仅家长可发布需求");
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
        entity.setRequirementTutoringMethod(request.getRequirementTutoringMethod());
        entity.setRequirementFrequency(request.getRequirementFrequency());
        entity.setRequirementSalary(request.getRequirementSalary());
        entity.setRequirementOther(request.getRequirementOther());
        entity.setRequirementStatus(0);
        entity.setRequirementAuditStatus(1);
        entity.setRequirementDeleteStatus(0);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        requirementMapper.insert(entity);
        return entity;
    }

    public PageResult<RequirementEntity> pageOpen(long pageNo, long pageSize, String subjectId, String regionId) {
        Page<RequirementEntity> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<RequirementEntity> wrapper = new LambdaQueryWrapper<RequirementEntity>()
                .eq(RequirementEntity::getRequirementDeleteStatus, 0)
                .eq(RequirementEntity::getRequirementAuditStatus, 1)
                .eq(RequirementEntity::getRequirementStatus, 0)
                .orderByDesc(RequirementEntity::getCreateTime);
        if (subjectId != null && !subjectId.isBlank()) {
            wrapper.eq(RequirementEntity::getSubjectId, subjectId);
        }
        if (regionId != null && !regionId.isBlank()) {
            wrapper.eq(RequirementEntity::getRegionId, regionId);
        }
        Page<RequirementEntity> result = requirementMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), pageNo, pageSize, result.getRecords());
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

    public RequirementEntity detail(String id) {
        RequirementEntity entity = requirementMapper.selectById(id);
        if (entity == null || entity.getRequirementDeleteStatus() == 1) {
            throw new BusinessException("需求不存在");
        }
        return entity;
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
}
