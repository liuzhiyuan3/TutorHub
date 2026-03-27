package com.teacher.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.common.exception.BusinessException;
import com.teacher.common.model.PageResult;
import com.teacher.common.security.LoginUser;
import com.teacher.common.security.LoginUserContext;
import com.teacher.common.util.IdUtil;
import com.teacher.pojo.dto.AppointmentCreateRequest;
import com.teacher.pojo.entity.AppointmentEntity;
import com.teacher.pojo.entity.DispatchRecordEntity;
import com.teacher.pojo.entity.FavoriteTeacherEntity;
import com.teacher.pojo.entity.OrderEntity;
import com.teacher.pojo.entity.RegionEntity;
import com.teacher.pojo.entity.RequirementEntity;
import com.teacher.pojo.entity.SubjectEntity;
import com.teacher.pojo.entity.TeacherInfoEntity;
import com.teacher.pojo.enums.AppointmentStatusEnum;
import com.teacher.pojo.vo.DispatchPublicDetailVO;
import com.teacher.server.mapper.AppointmentMapper;
import com.teacher.server.mapper.DispatchRecordMapper;
import com.teacher.server.mapper.FavoriteTeacherMapper;
import com.teacher.server.mapper.OrderMapper;
import com.teacher.server.mapper.RegionMapper;
import com.teacher.server.mapper.RequirementMapper;
import com.teacher.server.mapper.SubjectMapper;
import com.teacher.server.mapper.TeacherInfoMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ExtendedBusinessService {
    private final AppointmentMapper appointmentMapper;
    private final FavoriteTeacherMapper favoriteTeacherMapper;
    private final DispatchRecordMapper dispatchRecordMapper;
    private final TeacherInfoMapper teacherInfoMapper;
    private final OrderMapper orderMapper;
    private final RequirementMapper requirementMapper;
    private final SubjectMapper subjectMapper;
    private final RegionMapper regionMapper;
    private final AuthService authService;

    public ExtendedBusinessService(AppointmentMapper appointmentMapper, FavoriteTeacherMapper favoriteTeacherMapper,
                                   DispatchRecordMapper dispatchRecordMapper, TeacherInfoMapper teacherInfoMapper,
                                   OrderMapper orderMapper, RequirementMapper requirementMapper, SubjectMapper subjectMapper,
                                   RegionMapper regionMapper, AuthService authService) {
        this.appointmentMapper = appointmentMapper;
        this.favoriteTeacherMapper = favoriteTeacherMapper;
        this.dispatchRecordMapper = dispatchRecordMapper;
        this.teacherInfoMapper = teacherInfoMapper;
        this.orderMapper = orderMapper;
        this.requirementMapper = requirementMapper;
        this.subjectMapper = subjectMapper;
        this.regionMapper = regionMapper;
        this.authService = authService;
    }

    public AppointmentEntity createAppointment(AppointmentCreateRequest request) {
        LoginUser loginUser = authService.currentLoginUser();
        if (loginUser.getUserType() == null || loginUser.getUserType() != 0) {
            throw new BusinessException("Only parent can create appointment");
        }
        if (request.getTeacherId() == null || request.getTeacherId().isBlank()) {
            throw new BusinessException("Teacher id is required");
        }
        TeacherInfoEntity teacherInfo = teacherInfoMapper.selectById(request.getTeacherId());
        if (teacherInfo == null || teacherInfo.getTeacherDeleteStatus() == 1) {
            throw new BusinessException("Teacher not found");
        }
        if (request.getAppointmentTime() == null || request.getAppointmentTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Appointment time must be in the future");
        }
        AppointmentEntity entity = new AppointmentEntity();
        entity.setId(IdUtil.uuid32());
        entity.setParentId(loginUser.getId());
        entity.setTeacherId(request.getTeacherId());
        entity.setAppointmentSubject(request.getAppointmentSubject());
        entity.setAppointmentGrade(request.getAppointmentGrade());
        entity.setAppointmentAddress(request.getAppointmentAddress());
        entity.setAppointmentTime(request.getAppointmentTime());
        entity.setAppointmentRemark(request.getAppointmentRemark());
        entity.setAppointmentStatus(AppointmentStatusEnum.WAITING_CONFIRM.getCode());
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
        if (teacherId == null || teacherId.isBlank()) {
            throw new BusinessException("Teacher id is required");
        }
        LoginUser loginUser = authService.currentLoginUser();
        TeacherInfoEntity teacherInfo = teacherInfoMapper.selectById(teacherId);
        if (teacherInfo == null || teacherInfo.getTeacherDeleteStatus() == 1) {
            throw new BusinessException("Teacher not found");
        }
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

    public DispatchPublicDetailVO publicDispatchDetail(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new BusinessException("Order id is required");
        }
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null || (order.getOrderDeleteStatus() != null && order.getOrderDeleteStatus() == 1)) {
            throw new BusinessException("Order not found");
        }
        RequirementEntity requirement = requirementMapper.selectById(order.getRequirementId());
        if (requirement == null || (requirement.getRequirementDeleteStatus() != null && requirement.getRequirementDeleteStatus() == 1)) {
            throw new BusinessException("Requirement not found");
        }
        DispatchRecordEntity dispatch = dispatchRecordMapper.selectOne(new LambdaQueryWrapper<DispatchRecordEntity>()
                .eq(DispatchRecordEntity::getOrderId, orderId)
                .orderByDesc(DispatchRecordEntity::getDispatchTime)
                .last("limit 1"));

        SubjectEntity subject = subjectMapper.selectById(requirement.getSubjectId());
        RegionEntity region = regionMapper.selectById(requirement.getRegionId());
        boolean fullVisible = LoginUserContext.get() != null;

        DispatchPublicDetailVO vo = new DispatchPublicDetailVO();
        vo.setOrderId(order.getId());
        vo.setOrderNumber(order.getOrderNumber());
        vo.setSubjectName(subject == null ? "-" : subject.getSubjectName());
        vo.setRequirementGrade(requirement.getRequirementGrade());
        vo.setStudentGender(fullVisible ? requirement.getStudentGender() : maskText(requirement.getStudentGender()));
        vo.setSalaryText(fullVisible ? safeSalaryText(requirement) : maskText(safeSalaryText(requirement)));
        vo.setRegionName(region == null ? "-" : region.getRegionName());
        vo.setRequirementAddress(fullVisible ? requirement.getRequirementAddress() : maskAddress(requirement.getRequirementAddress()));
        vo.setCrossStreet(fullVisible ? requirement.getCrossStreet() : maskText(requirement.getCrossStreet()));
        vo.setRequirementFrequency(fullVisible ? requirement.getRequirementFrequency() : maskText(requirement.getRequirementFrequency()));
        vo.setStudentDetail(fullVisible ? requirement.getStudentDetail() : "登录后查看");
        vo.setTeacherQualification(fullVisible ? requirement.getTeacherQualification() : maskText(requirement.getTeacherQualification()));
        vo.setRequirementTutoringMethodText(tutoringMethodText(requirement.getRequirementTutoringMethod()));
        vo.setTeacherGenderPreference(fullVisible ? requirement.getTeacherGenderPreference() : maskText(requirement.getTeacherGenderPreference()));
        vo.setTeacherRequirementText(fullVisible ? requirement.getTeacherRequirementText() : "登录后查看");
        vo.setSummary(requirement.getRequirementTitle());
        vo.setDispatchTime(dispatch == null ? order.getCreateTime() : dispatch.getDispatchTime());
        vo.setFullVisible(fullVisible);
        return vo;
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

    public void removeFavorite(String teacherId) {
        LoginUser loginUser = authService.currentLoginUser();
        favoriteTeacherMapper.delete(new LambdaQueryWrapper<FavoriteTeacherEntity>()
                .eq(FavoriteTeacherEntity::getParentId, loginUser.getId())
                .eq(FavoriteTeacherEntity::getTeacherId, teacherId));
    }

    public AppointmentEntity updateAppointmentStatus(String appointmentId, Integer newStatus) {
        LoginUser loginUser = authService.currentLoginUser();
        AppointmentEntity db = appointmentMapper.selectById(appointmentId);
        if (db == null || db.getAppointmentDeleteStatus() == 1) {
            throw new BusinessException("Appointment not found");
        }
        if (!AppointmentStatusEnum.canTransit(db.getAppointmentStatus(), newStatus)) {
            throw new BusinessException("Invalid appointment status transition");
        }
        if (loginUser.getUserType() != null && loginUser.getUserType() == 0) {
            if (!loginUser.getId().equals(db.getParentId())) {
                throw new BusinessException("No permission");
            }
            if (newStatus == null || newStatus != AppointmentStatusEnum.CANCELED.getCode()) {
                throw new BusinessException("Parent can only cancel appointment");
            }
        } else if (!loginUser.isAdmin()) {
            TeacherInfoEntity teacherInfo = teacherInfoMapper.selectOne(new LambdaQueryWrapper<TeacherInfoEntity>()
                    .eq(TeacherInfoEntity::getUserId, loginUser.getId())
                    .eq(TeacherInfoEntity::getTeacherDeleteStatus, 0)
                    .last("limit 1"));
            if (teacherInfo == null || !teacherInfo.getId().equals(db.getTeacherId())) {
                throw new BusinessException("No permission");
            }
            if (!(newStatus != null && (newStatus == AppointmentStatusEnum.CONFIRMED.getCode()
                    || newStatus == AppointmentStatusEnum.REJECTED.getCode()))) {
                throw new BusinessException("Teacher can only confirm or reject");
            }
        }
        int updated = appointmentMapper.update(
                null,
                new LambdaUpdateWrapper<AppointmentEntity>()
                        .set(AppointmentEntity::getAppointmentStatus, newStatus)
                        .set(AppointmentEntity::getUpdateTime, LocalDateTime.now())
                        .eq(AppointmentEntity::getId, appointmentId)
                        .eq(AppointmentEntity::getAppointmentDeleteStatus, 0)
        );
        if (updated == 0) {
            throw new BusinessException("Failed to update appointment");
        }
        return appointmentMapper.selectById(appointmentId);
    }

    private String safeSalaryText(RequirementEntity requirement) {
        if (requirement.getSalaryText() != null && !requirement.getSalaryText().isBlank()) {
            return requirement.getSalaryText();
        }
        return requirement.getRequirementSalary() == null ? "" : requirement.getRequirementSalary().stripTrailingZeros().toPlainString() + "元/小时";
    }

    private String tutoringMethodText(Integer method) {
        if (method == null) return "-";
        if (method == 0) return "线上辅导";
        if (method == 1) return "上门辅导";
        return "线上线下";
    }

    private String maskAddress(String address) {
        if (address == null || address.isBlank()) {
            return "-";
        }
        String text = address.trim();
        if (text.length() <= 6) {
            return text.substring(0, Math.max(1, text.length() / 2)) + "**";
        }
        return text.substring(0, 6) + "***";
    }

    private String maskText(String text) {
        if (text == null || text.isBlank()) {
            return "-";
        }
        return "登录后查看";
    }
}
