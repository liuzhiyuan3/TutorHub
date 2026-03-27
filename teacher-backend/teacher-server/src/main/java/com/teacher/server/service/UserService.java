package com.teacher.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.common.exception.BusinessException;
import com.teacher.common.model.PageResult;
import com.teacher.common.security.LoginUser;
import com.teacher.common.util.IdUtil;
import com.teacher.pojo.dto.UserProfileCompleteRequest;
import com.teacher.pojo.dto.UserRegionUpdateRequest;
import com.teacher.pojo.dto.UserLocationUpdateRequest;
import com.teacher.pojo.entity.AppointmentEntity;
import com.teacher.pojo.entity.OrderEntity;
import com.teacher.pojo.entity.RequirementEntity;
import com.teacher.pojo.entity.TeacherInfoEntity;
import com.teacher.pojo.entity.UserEntity;
import com.teacher.pojo.vo.UserProfileCompletenessVO;
import com.teacher.pojo.vo.admin.AdminUserProfileVO;
import com.teacher.server.mapper.AppointmentMapper;
import com.teacher.server.mapper.OrderMapper;
import com.teacher.server.mapper.RequirementMapper;
import com.teacher.server.mapper.TeacherInfoMapper;
import com.teacher.server.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final TeacherInfoMapper teacherInfoMapper;
    private final RequirementMapper requirementMapper;
    private final OrderMapper orderMapper;
    private final AppointmentMapper appointmentMapper;
    private final AuthService authService;

    public UserService(UserMapper userMapper,
                       TeacherInfoMapper teacherInfoMapper,
                       RequirementMapper requirementMapper,
                       OrderMapper orderMapper,
                       AppointmentMapper appointmentMapper,
                       AuthService authService) {
        this.userMapper = userMapper;
        this.teacherInfoMapper = teacherInfoMapper;
        this.requirementMapper = requirementMapper;
        this.orderMapper = orderMapper;
        this.appointmentMapper = appointmentMapper;
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

    public UserEntity updateMyLocation(UserLocationUpdateRequest request) {
        LoginUser loginUser = authService.currentLoginUser();
        UserEntity db = userMapper.selectById(loginUser.getId());
        if (db == null) {
            throw new BusinessException("用户不存在");
        }
        db.setUserLocationAddress(request.getUserLocationAddress());
        db.setUserLocationLongitude(request.getUserLocationLongitude());
        db.setUserLocationLatitude(request.getUserLocationLatitude());
        db.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(db);
        if (db.getUserType() != null && db.getUserType() == 1) {
            TeacherInfoEntity teacherInfo = teacherInfoMapper.selectOne(new LambdaQueryWrapper<TeacherInfoEntity>()
                    .eq(TeacherInfoEntity::getUserId, db.getId())
                    .eq(TeacherInfoEntity::getTeacherDeleteStatus, 0)
                    .last("limit 1"));
            if (teacherInfo != null) {
                teacherInfo.setTeacherWorkLongitude(request.getUserLocationLongitude());
                teacherInfo.setTeacherWorkLatitude(request.getUserLocationLatitude());
                if (request.getUserLocationAddress() != null && !request.getUserLocationAddress().isBlank()) {
                    teacherInfo.setTeacherWorkAddress(request.getUserLocationAddress());
                }
                teacherInfo.setUpdateTime(LocalDateTime.now());
                teacherInfoMapper.updateById(teacherInfo);
            }
        }
        db.setUserPassword(null);
        return db;
    }

    public UserEntity updateMyRegion(UserRegionUpdateRequest request) {
        LoginUser loginUser = authService.currentLoginUser();
        UserEntity db = userMapper.selectById(loginUser.getId());
        if (db == null) {
            throw new BusinessException("用户不存在");
        }
        db.setUserRegionCode(trimToEmpty(request.getRegionCode()));
        db.setUserRegionName(trimToEmpty(request.getRegionName()));
        db.setUserRegionProvince(trimToEmpty(request.getRegionProvince()));
        db.setUserRegionCity(trimToEmpty(request.getRegionCity()));
        db.setUserRegionDistrict(trimToEmpty(request.getRegionDistrict()));
        db.setUserRegionSource(trimToEmpty(request.getRegionSource()));
        db.setUserRegionSyncTime(LocalDateTime.now());

        if (request.getUserLocationAddress() != null && !request.getUserLocationAddress().isBlank()) {
            db.setUserLocationAddress(request.getUserLocationAddress());
        }
        if (request.getUserLocationLongitude() != null) {
            db.setUserLocationLongitude(request.getUserLocationLongitude());
        }
        if (request.getUserLocationLatitude() != null) {
            db.setUserLocationLatitude(request.getUserLocationLatitude());
        }
        if (db.getUserType() != null && db.getUserType() == 1) {
            TeacherInfoEntity teacherInfo = findTeacherInfoByUserId(db.getId());
            if (teacherInfo != null) {
                if (request.getUserLocationLongitude() != null) {
                    teacherInfo.setTeacherWorkLongitude(request.getUserLocationLongitude());
                }
                if (request.getUserLocationLatitude() != null) {
                    teacherInfo.setTeacherWorkLatitude(request.getUserLocationLatitude());
                }
                if (request.getUserLocationAddress() != null && !request.getUserLocationAddress().isBlank()) {
                    teacherInfo.setTeacherWorkAddress(request.getUserLocationAddress());
                }
                teacherInfo.setUpdateTime(LocalDateTime.now());
                teacherInfoMapper.updateById(teacherInfo);
            }
        }
        db.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(db);
        db.setUserPassword(null);
        return db;
    }

    public UserProfileCompletenessVO profileCompleteness() {
        LoginUser loginUser = authService.currentLoginUser();
        UserEntity user = userMapper.selectById(loginUser.getId());
        if (user == null || user.getUserDeleteStatus() == 1) {
            throw new BusinessException("用户不存在");
        }

        List<String> missingFields = new ArrayList<>();
        if (isBlank(user.getUserName())) {
            missingFields.add("nickName");
        }
        if (isBlank(user.getUserPortrait())) {
            missingFields.add("avatarUrl");
        }
        String role = user.getUserType() != null && user.getUserType() == 1 ? "teacher" : "parent";

        if ("teacher".equals(role)) {
            TeacherInfoEntity teacherInfo = findTeacherInfoByUserId(user.getId());
            if (teacherInfo == null) {
                missingFields.add("teacherIdentity");
                missingFields.add("teacherTutoringMethod");
                missingFields.add("teacherSchool");
                missingFields.add("teacherMajor");
                missingFields.add("teacherEducation");
            } else {
                if (isBlank(teacherInfo.getTeacherIdentity())) {
                    missingFields.add("teacherIdentity");
                }
                if (teacherInfo.getTeacherTutoringMethod() == null) {
                    missingFields.add("teacherTutoringMethod");
                }
                if (isBlank(teacherInfo.getTeacherSchool())) {
                    missingFields.add("teacherSchool");
                }
                if (isBlank(teacherInfo.getTeacherMajor())) {
                    missingFields.add("teacherMajor");
                }
                if (isBlank(teacherInfo.getTeacherEducation())) {
                    missingFields.add("teacherEducation");
                }
            }
        }

        boolean ready = missingFields.isEmpty();
        UserProfileCompletenessVO vo = new UserProfileCompletenessVO();
        vo.setReady(ready);
        vo.setRole(role);
        vo.setUserName(user.getUserName());
        vo.setUserPortrait(user.getUserPortrait());
        vo.setMissingFields(missingFields);
        return vo;
    }

    public UserProfileCompletenessVO completeProfile(UserProfileCompleteRequest request) {
        LoginUser loginUser = authService.currentLoginUser();
        UserEntity user = userMapper.selectById(loginUser.getId());
        if (user == null || user.getUserDeleteStatus() == 1) {
            throw new BusinessException("用户不存在");
        }
        String role = user.getUserType() != null && user.getUserType() == 1 ? "teacher" : "parent";
        String expectedRole = trimToEmpty(request.getRole());
        if (!expectedRole.isBlank() && !role.equalsIgnoreCase(expectedRole)) {
            throw new BusinessException("身份已变化，请刷新后重试");
        }

        String nickName = normalize(request.getNickName(), 50);
        String avatarUrl = normalize(request.getAvatarUrl(), 255);
        if (nickName.isBlank()) {
            throw new BusinessException("昵称不能为空");
        }
        if (avatarUrl.isBlank()) {
            throw new BusinessException("头像不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        user.setUserName(nickName);
        user.setUserPortrait(avatarUrl);
        user.setNicknameSource("manual");
        user.setAvatarSource("manual");

        if ("teacher".equals(role)) {
            TeacherInfoEntity teacherInfo = upsertTeacherProfile(user.getId(), request, now);
            teacherInfoMapper.updateById(teacherInfo);
        }

        UserProfileCompletenessVO checkBeforeUpdate = profileCompletenessByUser(user);
        boolean ready = checkBeforeUpdate.getReady() != null && checkBeforeUpdate.getReady();
        user.setProfileCompleted(ready ? 1 : 0);
        user.setLastProfileCompleteTime(ready ? now : user.getLastProfileCompleteTime());
        user.setUpdateTime(now);
        userMapper.updateById(user);
        return profileCompleteness();
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

    public void adminUpdateStatus(String id, Integer status) {
        UserEntity db = userMapper.selectById(id);
        if (db == null || db.getUserDeleteStatus() == 1) {
            throw new BusinessException("用户不存在");
        }
        db.setUserStatus(status);
        db.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(db);
    }

    public void adminDelete(String id) {
        UserEntity db = userMapper.selectById(id);
        if (db == null || db.getUserDeleteStatus() == 1) {
            throw new BusinessException("用户不存在");
        }
        db.setUserDeleteStatus(1);
        db.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(db);
    }

    public AdminUserProfileVO adminProfile(String id) {
        UserEntity user = userMapper.selectById(id);
        if (user == null || user.getUserDeleteStatus() == 1) {
            throw new BusinessException("用户不存在");
        }
        user.setUserPassword(null);

        AdminUserProfileVO profile = new AdminUserProfileVO();
        profile.setUser(user);

        TeacherInfoEntity teacherInfo = teacherInfoMapper.selectOne(new LambdaQueryWrapper<TeacherInfoEntity>()
                .eq(TeacherInfoEntity::getUserId, id)
                .eq(TeacherInfoEntity::getTeacherDeleteStatus, 0)
                .last("limit 1"));
        profile.setTeacherInfo(teacherInfo);

        List<RequirementEntity> requirements = requirementMapper.selectList(new LambdaQueryWrapper<RequirementEntity>()
                .eq(RequirementEntity::getParentId, id)
                .eq(RequirementEntity::getRequirementDeleteStatus, 0)
                .orderByDesc(RequirementEntity::getCreateTime)
                .last("limit 20"));
        profile.setRequirements(requirements);

        List<OrderEntity> parentOrders = orderMapper.selectList(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getParentId, id)
                .eq(OrderEntity::getOrderDeleteStatus, 0)
                .orderByDesc(OrderEntity::getCreateTime)
                .last("limit 20"));
        profile.setParentOrders(parentOrders);

        List<AppointmentEntity> appointments = appointmentMapper.selectList(new LambdaQueryWrapper<AppointmentEntity>()
                .eq(AppointmentEntity::getParentId, id)
                .eq(AppointmentEntity::getAppointmentDeleteStatus, 0)
                .orderByDesc(AppointmentEntity::getCreateTime)
                .last("limit 20"));
        profile.setAppointments(appointments);

        if (teacherInfo != null) {
            List<OrderEntity> teacherOrders = orderMapper.selectList(new LambdaQueryWrapper<OrderEntity>()
                    .eq(OrderEntity::getTeacherId, teacherInfo.getId())
                    .eq(OrderEntity::getOrderDeleteStatus, 0)
                    .orderByDesc(OrderEntity::getCreateTime)
                    .last("limit 20"));
            profile.setTeacherOrders(teacherOrders);
        }
        return profile;
    }

    private UserProfileCompletenessVO profileCompletenessByUser(UserEntity user) {
        List<String> missingFields = new ArrayList<>();
        if (isBlank(user.getUserName())) {
            missingFields.add("nickName");
        }
        if (isBlank(user.getUserPortrait())) {
            missingFields.add("avatarUrl");
        }
        String role = user.getUserType() != null && user.getUserType() == 1 ? "teacher" : "parent";
        if ("teacher".equals(role)) {
            TeacherInfoEntity teacherInfo = findTeacherInfoByUserId(user.getId());
            if (teacherInfo == null) {
                missingFields.add("teacherIdentity");
                missingFields.add("teacherTutoringMethod");
                missingFields.add("teacherSchool");
                missingFields.add("teacherMajor");
                missingFields.add("teacherEducation");
            } else {
                if (isBlank(teacherInfo.getTeacherIdentity())) missingFields.add("teacherIdentity");
                if (teacherInfo.getTeacherTutoringMethod() == null) missingFields.add("teacherTutoringMethod");
                if (isBlank(teacherInfo.getTeacherSchool())) missingFields.add("teacherSchool");
                if (isBlank(teacherInfo.getTeacherMajor())) missingFields.add("teacherMajor");
                if (isBlank(teacherInfo.getTeacherEducation())) missingFields.add("teacherEducation");
            }
        }
        UserProfileCompletenessVO vo = new UserProfileCompletenessVO();
        vo.setReady(missingFields.isEmpty());
        vo.setRole(role);
        vo.setUserName(user.getUserName());
        vo.setUserPortrait(user.getUserPortrait());
        vo.setMissingFields(missingFields);
        return vo;
    }

    private TeacherInfoEntity upsertTeacherProfile(String userId, UserProfileCompleteRequest request, LocalDateTime now) {
        String teacherIdentity = normalize(request.getTeacherIdentity(), 50);
        String teacherSchool = normalize(request.getTeacherSchool(), 100);
        String teacherMajor = normalize(request.getTeacherMajor(), 100);
        String teacherEducation = normalize(request.getTeacherEducation(), 50);
        if (teacherIdentity.isBlank()) {
            throw new BusinessException("教员身份不能为空");
        }
        if (request.getTeacherTutoringMethod() == null) {
            throw new BusinessException("授课方式不能为空");
        }
        if (teacherSchool.isBlank()) {
            throw new BusinessException("学校不能为空");
        }
        if (teacherMajor.isBlank()) {
            throw new BusinessException("专业不能为空");
        }
        if (teacherEducation.isBlank()) {
            throw new BusinessException("学历不能为空");
        }
        TeacherInfoEntity teacherInfo = findTeacherInfoByUserId(userId);
        if (teacherInfo == null) {
            teacherInfo = new TeacherInfoEntity();
            teacherInfo.setId(IdUtil.uuid32());
            teacherInfo.setUserId(userId);
            teacherInfo.setTeacherSuccessCount(0);
            teacherInfo.setTeacherViewCount(0);
            teacherInfo.setTeacherAuditStatus(0);
            teacherInfo.setTeacherEnableStatus(1);
            teacherInfo.setTeacherDeleteStatus(0);
            teacherInfo.setCreateTime(now);
        }
        teacherInfo.setTeacherIdentity(teacherIdentity);
        teacherInfo.setTeacherTutoringMethod(request.getTeacherTutoringMethod());
        teacherInfo.setTeacherTeachingYears(request.getTeacherTeachingYears() == null ? 0 : request.getTeacherTeachingYears());
        teacherInfo.setTeacherSchool(teacherSchool);
        teacherInfo.setTeacherMajor(teacherMajor);
        teacherInfo.setTeacherEducation(teacherEducation);
        teacherInfo.setTeacherCertNo(normalize(request.getTeacherCertNo(), 64));
        teacherInfo.setTeacherCertImages(normalize(request.getTeacherCertImages(), 2000));
        teacherInfo.setTeacherExperience(normalize(request.getTeacherExperience(), 2000));
        teacherInfo.setTeacherSelfDescription(normalize(request.getTeacherSelfDescription(), 2000));
        teacherInfo.setTeacherProfileCompleted(1);
        teacherInfo.setUpdateTime(now);
        if (teacherInfoMapper.selectById(teacherInfo.getId()) == null) {
            teacherInfoMapper.insert(teacherInfo);
            return teacherInfo;
        }
        return teacherInfo;
    }

    private TeacherInfoEntity findTeacherInfoByUserId(String userId) {
        return teacherInfoMapper.selectOne(new LambdaQueryWrapper<TeacherInfoEntity>()
                .eq(TeacherInfoEntity::getUserId, userId)
                .eq(TeacherInfoEntity::getTeacherDeleteStatus, 0)
                .last("limit 1"));
    }

    private String trimToEmpty(String text) {
        return text == null ? "" : text.trim();
    }

    private String normalize(String text, int maxLen) {
        String value = trimToEmpty(text);
        if (value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen);
    }

    private boolean isBlank(String text) {
        return trimToEmpty(text).isBlank();
    }
}
