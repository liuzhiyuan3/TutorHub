package com.teacher.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.common.exception.BusinessException;
import com.teacher.common.model.PageResult;
import com.teacher.common.security.LoginUser;
import com.teacher.common.security.LoginUserContext;
import com.teacher.pojo.entity.*;
import com.teacher.pojo.enums.AuditStatusEnum;
import com.teacher.pojo.enums.RequirementStatusEnum;
import com.teacher.pojo.vo.FilterMetaVO;
import com.teacher.pojo.vo.DispatchPublicListItemVO;
import com.teacher.pojo.vo.HomeOverviewVO;
import com.teacher.pojo.vo.PublicOptionVO;
import com.teacher.pojo.vo.PublicRequirementListItemVO;
import com.teacher.pojo.vo.PublicTeacherListItemVO;
import com.teacher.server.mapper.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HomeQueryService {
    private final SubjectMapper subjectMapper;
    private final RegionMapper regionMapper;
    private final SchoolMapper schoolMapper;
    private final RequirementMapper requirementMapper;
    private final DispatchRecordMapper dispatchRecordMapper;
    private final TeacherInfoMapper teacherInfoMapper;
    private final TeacherSubjectMapper teacherSubjectMapper;
    private final TeacherRegionMapper teacherRegionMapper;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final TeacherOrderStatsService teacherOrderStatsService;
    private final MediaUrlService mediaUrlService;

    public HomeQueryService(SubjectMapper subjectMapper, RegionMapper regionMapper, SchoolMapper schoolMapper,
                            RequirementMapper requirementMapper, DispatchRecordMapper dispatchRecordMapper,
                            TeacherInfoMapper teacherInfoMapper, TeacherSubjectMapper teacherSubjectMapper,
                            TeacherRegionMapper teacherRegionMapper, UserMapper userMapper, OrderMapper orderMapper,
                            TeacherOrderStatsService teacherOrderStatsService, MediaUrlService mediaUrlService) {
        this.subjectMapper = subjectMapper;
        this.regionMapper = regionMapper;
        this.schoolMapper = schoolMapper;
        this.requirementMapper = requirementMapper;
        this.dispatchRecordMapper = dispatchRecordMapper;
        this.teacherInfoMapper = teacherInfoMapper;
        this.teacherSubjectMapper = teacherSubjectMapper;
        this.teacherRegionMapper = teacherRegionMapper;
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.teacherOrderStatsService = teacherOrderStatsService;
        this.mediaUrlService = mediaUrlService;
    }

    public HomeOverviewVO overview() {
        List<SubjectEntity> hotSubjects = subjectMapper.selectList(new LambdaQueryWrapper<SubjectEntity>()
                .eq(SubjectEntity::getSubjectDeleteStatus, 0)
                .eq(SubjectEntity::getSubjectStatus, 1)
                .orderByAsc(SubjectEntity::getSubjectSort)
                .last("limit 8"));
        List<RegionEntity> hotRegions = regionMapper.selectList(new LambdaQueryWrapper<RegionEntity>()
                .eq(RegionEntity::getRegionDeleteStatus, 0)
                .eq(RegionEntity::getRegionStatus, 1)
                .orderByAsc(RegionEntity::getRegionSort)
                .last("limit 8"));
        List<SchoolEntity> hotSchools = schoolMapper.selectList(new LambdaQueryWrapper<SchoolEntity>()
                .eq(SchoolEntity::getSchoolDeleteStatus, 0)
                .eq(SchoolEntity::getSchoolStatus, 1)
                .orderByDesc(SchoolEntity::getCreateTime)
                .last("limit 8"));
        List<RequirementEntity> latestRequirements = requirementMapper.selectList(new LambdaQueryWrapper<RequirementEntity>()
                .eq(RequirementEntity::getRequirementDeleteStatus, 0)
                .eq(RequirementEntity::getRequirementAuditStatus, AuditStatusEnum.APPROVED.getCode())
                .orderByDesc(RequirementEntity::getCreateTime)
                .last("limit 10"));
        List<DispatchRecordEntity> latestDispatches = dispatchRecordMapper.selectList(new LambdaQueryWrapper<DispatchRecordEntity>()
                .orderByDesc(DispatchRecordEntity::getDispatchTime)
                .last("limit 10"));
        List<DispatchPublicListItemVO> latestDispatchCards = buildDispatchCards(latestDispatches);
        return new HomeOverviewVO(hotSubjects, hotRegions, hotSchools, latestRequirements, latestDispatches, latestDispatchCards);
    }

    public FilterMetaVO filterMeta() {
        List<PublicOptionVO> subjects = subjectMapper.selectList(new LambdaQueryWrapper<SubjectEntity>()
                .eq(SubjectEntity::getSubjectDeleteStatus, 0)
                .eq(SubjectEntity::getSubjectStatus, 1)
                .orderByAsc(SubjectEntity::getSubjectSort))
                .stream()
                .map(item -> new PublicOptionVO(item.getId(), item.getSubjectCode(), item.getSubjectName()))
                .collect(Collectors.toList());
        List<PublicOptionVO> regions = regionMapper.selectList(new LambdaQueryWrapper<RegionEntity>()
                .eq(RegionEntity::getRegionDeleteStatus, 0)
                .eq(RegionEntity::getRegionStatus, 1)
                .orderByAsc(RegionEntity::getRegionSort))
                .stream()
                .map(item -> new PublicOptionVO(item.getId(), item.getRegionCode(), item.getRegionName()))
                .collect(Collectors.toList());
        List<PublicOptionVO> schools = schoolMapper.selectList(new LambdaQueryWrapper<SchoolEntity>()
                .eq(SchoolEntity::getSchoolDeleteStatus, 0)
                .eq(SchoolEntity::getSchoolStatus, 1)
                .orderByAsc(SchoolEntity::getSchoolName))
                .stream()
                .map(item -> new PublicOptionVO(item.getId(), item.getSchoolCode(), item.getSchoolName()))
                .collect(Collectors.toList());
        return new FilterMetaVO(subjects, regions, schools);
    }

    public PageResult<PublicTeacherListItemVO> teacherSearch(long pageNo, long pageSize, String subjectId, String regionId,
                                                              Integer tutoringMethod, Integer auditStatus, String keyword,
                                                              String schoolKeyword, Integer minTeachingYears, Integer maxTeachingYears,
                                                              BigDecimal userLat, BigDecimal userLng, String sortBy) {
        if (minTeachingYears != null && maxTeachingYears != null && minTeachingYears > maxTeachingYears) {
            throw new BusinessException("Invalid teaching years range");
        }
        Page<TeacherInfoEntity> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<TeacherInfoEntity> wrapper = new LambdaQueryWrapper<TeacherInfoEntity>()
                .eq(TeacherInfoEntity::getTeacherDeleteStatus, 0)
                .eq(TeacherInfoEntity::getTeacherEnableStatus, 1);
        if (tutoringMethod != null) {
            wrapper.eq(TeacherInfoEntity::getTeacherTutoringMethod, tutoringMethod);
        }
        if (auditStatus != null) {
            wrapper.eq(TeacherInfoEntity::getTeacherAuditStatus, auditStatus);
        } else {
            wrapper.eq(TeacherInfoEntity::getTeacherAuditStatus, AuditStatusEnum.APPROVED.getCode());
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(TeacherInfoEntity::getTeacherIdentity, keyword)
                    .or().like(TeacherInfoEntity::getTeacherSchool, keyword)
                    .or().like(TeacherInfoEntity::getTeacherMajor, keyword)
                    .or().like(TeacherInfoEntity::getTeacherSelfDescription, keyword));
        }
        if (schoolKeyword != null && !schoolKeyword.isBlank()) {
            wrapper.like(TeacherInfoEntity::getTeacherSchool, schoolKeyword);
        }
        if (minTeachingYears != null) {
            wrapper.ge(TeacherInfoEntity::getTeacherTeachingYears, minTeachingYears);
        }
        if (maxTeachingYears != null) {
            wrapper.le(TeacherInfoEntity::getTeacherTeachingYears, maxTeachingYears);
        }
        if (subjectId != null && !subjectId.isBlank()) {
            List<String> teacherIds = teacherSubjectMapper.selectList(new LambdaQueryWrapper<TeacherSubjectEntity>()
                            .eq(TeacherSubjectEntity::getSubjectId, subjectId))
                    .stream().map(TeacherSubjectEntity::getTeacherId).distinct().collect(Collectors.toList());
            if (teacherIds.isEmpty()) {
                return new PageResult<>(0, pageNo, pageSize, Collections.emptyList());
            }
            wrapper.in(TeacherInfoEntity::getId, teacherIds);
        }
        if (regionId != null && !regionId.isBlank()) {
            List<String> teacherIds = teacherRegionMapper.selectList(new LambdaQueryWrapper<TeacherRegionEntity>()
                            .eq(TeacherRegionEntity::getRegionId, regionId))
                    .stream().map(TeacherRegionEntity::getTeacherId).distinct().collect(Collectors.toList());
            if (teacherIds.isEmpty()) {
                return new PageResult<>(0, pageNo, pageSize, Collections.emptyList());
            }
            wrapper.in(TeacherInfoEntity::getId, teacherIds);
        }
        if ("latest".equalsIgnoreCase(sortBy)) {
            wrapper.orderByDesc(TeacherInfoEntity::getCreateTime)
                    .orderByDesc(TeacherInfoEntity::getTeacherViewCount);
        } else if ("success".equalsIgnoreCase(sortBy)) {
            wrapper.orderByDesc(TeacherInfoEntity::getTeacherSuccessCount)
                    .orderByDesc(TeacherInfoEntity::getTeacherViewCount);
        } else {
            wrapper.orderByDesc(TeacherInfoEntity::getTeacherViewCount)
                    .orderByDesc(TeacherInfoEntity::getTeacherSuccessCount)
                    .orderByDesc(TeacherInfoEntity::getCreateTime);
        }
        Page<TeacherInfoEntity> result = teacherInfoMapper.selectPage(page, wrapper);
        List<TeacherInfoEntity> records = result.getRecords();
        if (records.isEmpty()) {
            return new PageResult<>(result.getTotal(), pageNo, pageSize, Collections.emptyList());
        }
        List<String> teacherIds = records.stream().map(TeacherInfoEntity::getId).collect(Collectors.toList());
        List<String> userIds = records.stream().map(TeacherInfoEntity::getUserId).collect(Collectors.toList());
        Map<String, String> userNameMap = userMapper.selectBatchIds(userIds)
                .stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getUserName, (a, b) -> a));

        Map<String, List<String>> teacherSubjectNameMap = buildTeacherSubjectNameMap(teacherIds);
        Map<String, List<String>> teacherRegionNameMap = buildTeacherRegionNameMap(teacherIds);
        Map<String, java.time.LocalDateTime> teacherLastOrderTimeMap = buildTeacherLastOrderTimeMap(teacherIds);
        Map<String, TeacherOrderStatsService.TeacherOrderStats> teacherOrderStatsMap = teacherOrderStatsService.buildTeacherOrderStatsMap(teacherIds);

        List<PublicTeacherListItemVO> voList = records.stream().map(item -> {
            PublicTeacherListItemVO vo = new PublicTeacherListItemVO();
            TeacherOrderStatsService.TeacherOrderStats stats = teacherOrderStatsMap.get(item.getId());
            vo.setTeacherId(item.getId());
            vo.setUserName(userNameMap.get(item.getUserId()));
            vo.setTeacherPhoto(mediaUrlService.normalize(item.getTeacherPhoto()));
            vo.setTeacherIdentity(item.getTeacherIdentity());
            vo.setTeacherSchool(item.getTeacherSchool());
            vo.setTeacherMajor(item.getTeacherMajor());
            vo.setTeacherEducation(item.getTeacherEducation());
            vo.setTeacherTutoringMethod(item.getTeacherTutoringMethod());
            vo.setTeacherTeachingYears(item.getTeacherTeachingYears());
            vo.setTeacherSuccessCount(item.getTeacherSuccessCount());
            vo.setTeacherViewCount(item.getTeacherViewCount());
            vo.setHistoryDealCount(stats == null ? 0 : stats.getHistoryDealCount());
            vo.setHireCount(stats == null ? 0 : stats.getHireCount());
            vo.setLastOrderTime(teacherLastOrderTimeMap.get(item.getId()));
            vo.setSubjectNames(teacherSubjectNameMap.getOrDefault(item.getId(), Collections.emptyList()));
            vo.setRegionNames(teacherRegionNameMap.getOrDefault(item.getId(), Collections.emptyList()));
            vo.setDistanceKm(calcDistanceKm(
                    userLat, userLng,
                    item.getTeacherWorkLatitude(),
                    item.getTeacherWorkLongitude()));
            return vo;
        }).collect(Collectors.toList());
        if ("distance".equalsIgnoreCase(sortBy) && userLat != null && userLng != null) {
            voList.sort(Comparator.comparing(
                    PublicTeacherListItemVO::getDistanceKm,
                    Comparator.nullsLast(Double::compareTo)));
        }
        return new PageResult<>(result.getTotal(), pageNo, pageSize, voList);
    }

    private Double calcDistanceKm(BigDecimal userLat, BigDecimal userLng, BigDecimal teacherLat, BigDecimal teacherLng) {
        if (userLat == null || userLng == null || teacherLat == null || teacherLng == null) {
            return null;
        }
        double lat1 = Math.toRadians(userLat.doubleValue());
        double lng1 = Math.toRadians(userLng.doubleValue());
        double lat2 = Math.toRadians(teacherLat.doubleValue());
        double lng2 = Math.toRadians(teacherLng.doubleValue());
        double dLat = lat2 - lat1;
        double dLng = lng2 - lng1;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = 6371.0d * c;
        return BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public PageResult<PublicRequirementListItemVO> requirementSearch(long pageNo, long pageSize, String subjectId, String regionId,
                                                                     Integer tutoringMethod, BigDecimal minSalary, BigDecimal maxSalary,
                                                                     BigDecimal budgetMin, BigDecimal budgetMax, Integer urgency,
                                                                     LocalDateTime startTimeFrom, LocalDateTime startTimeTo,
                                                                     String gradeKeyword, String keyword, String sortBy) {
        BigDecimal finalMinSalary = minSalary != null ? minSalary : budgetMin;
        BigDecimal finalMaxSalary = maxSalary != null ? maxSalary : budgetMax;
        if (finalMinSalary != null && finalMaxSalary != null && finalMinSalary.compareTo(finalMaxSalary) > 0) {
            throw new BusinessException("Invalid salary range");
        }
        if (startTimeFrom != null && startTimeTo != null && startTimeFrom.isAfter(startTimeTo)) {
            throw new BusinessException("Invalid date range");
        }
        Page<RequirementEntity> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<RequirementEntity> wrapper = new LambdaQueryWrapper<RequirementEntity>()
                .eq(RequirementEntity::getRequirementDeleteStatus, 0)
                .eq(RequirementEntity::getRequirementAuditStatus, AuditStatusEnum.APPROVED.getCode())
                .eq(RequirementEntity::getRequirementStatus, RequirementStatusEnum.WAITING.getCode());
        if (subjectId != null && !subjectId.isBlank()) {
            wrapper.eq(RequirementEntity::getSubjectId, subjectId);
        }
        if (regionId != null && !regionId.isBlank()) {
            wrapper.eq(RequirementEntity::getRegionId, regionId);
        }
        if (tutoringMethod != null) {
            wrapper.eq(RequirementEntity::getRequirementTutoringMethod, tutoringMethod);
        }
        if (finalMinSalary != null) {
            wrapper.ge(RequirementEntity::getRequirementSalary, finalMinSalary);
        }
        if (finalMaxSalary != null) {
            wrapper.le(RequirementEntity::getRequirementSalary, finalMaxSalary);
        }
        if (startTimeFrom != null) {
            wrapper.ge(RequirementEntity::getCreateTime, startTimeFrom);
        }
        if (startTimeTo != null) {
            wrapper.le(RequirementEntity::getCreateTime, startTimeTo);
        }
        if (gradeKeyword != null && !gradeKeyword.isBlank()) {
            wrapper.like(RequirementEntity::getRequirementGrade, gradeKeyword);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(RequirementEntity::getRequirementTitle, keyword)
                    .or().like(RequirementEntity::getRequirementDescription, keyword)
                    .or().like(RequirementEntity::getRequirementAddress, keyword)
                    .or().like(RequirementEntity::getRequirementOther, keyword));
        }
        if (urgency != null) {
            if (urgency == 2) {
                wrapper.like(RequirementEntity::getRequirementOther, "urgent");
            } else if (urgency == 1) {
                wrapper.and(w -> w.like(RequirementEntity::getRequirementOther, "fast")
                        .or().like(RequirementEntity::getRequirementOther, "asap"));
            } else {
                wrapper.and(w -> w.notLike(RequirementEntity::getRequirementOther, "urgent")
                        .notLike(RequirementEntity::getRequirementOther, "fast")
                        .notLike(RequirementEntity::getRequirementOther, "asap"));
            }
        }
        if ("salaryAsc".equalsIgnoreCase(sortBy)) {
            wrapper.orderByAsc(RequirementEntity::getRequirementSalary)
                    .orderByDesc(RequirementEntity::getCreateTime);
        } else if ("salaryDesc".equalsIgnoreCase(sortBy)) {
            wrapper.orderByDesc(RequirementEntity::getRequirementSalary)
                    .orderByDesc(RequirementEntity::getCreateTime);
        } else {
            wrapper.orderByDesc(RequirementEntity::getCreateTime);
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
            vo.setRequirementBudgetMin(calcBudgetMin(item.getRequirementSalary()));
            vo.setRequirementBudgetMax(calcBudgetMax(item.getRequirementSalary()));
            Integer urgencyCode = resolveUrgency(item.getRequirementOther());
            vo.setRequirementUrgency(urgencyCode);
            vo.setRequirementUrgencyText(resolveUrgencyText(urgencyCode));
            vo.setRequirementExpectedStartTime(item.getCreateTime() == null ? null : item.getCreateTime().plusDays(1));
            vo.setRequirementExpectedTimeSlots(resolveTimeSlots(item.getRequirementFrequency()));
            vo.setDistanceKm(null);
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

    private BigDecimal calcBudgetMin(BigDecimal salary) {
        if (salary == null) {
            return null;
        }
        return salary.multiply(new BigDecimal("0.9")).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcBudgetMax(BigDecimal salary) {
        if (salary == null) {
            return null;
        }
        return salary.multiply(new BigDecimal("1.1")).setScale(2, RoundingMode.HALF_UP);
    }

    private Integer resolveUrgency(String requirementOther) {
        String text = requirementOther == null ? "" : requirementOther.toLowerCase();
        if (text.contains("urgent")) {
            return 2;
        }
        if (text.contains("fast") || text.contains("asap")) {
            return 1;
        }
        return 0;
    }

    private String resolveUrgencyText(Integer urgency) {
        if (urgency == null || urgency == 0) {
            return "NORMAL";
        }
        if (urgency == 2) {
            return "URGENT";
        }
        return "FAST";
    }

    private List<String> resolveTimeSlots(String requirementFrequency) {
        if (requirementFrequency == null || requirementFrequency.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(requirementFrequency.split("[,閿?]"))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .limit(3)
                .collect(Collectors.toList());
    }

    private Map<String, List<String>> buildTeacherSubjectNameMap(List<String> teacherIds) {
        List<TeacherSubjectEntity> teacherSubjects = teacherSubjectMapper.selectList(new LambdaQueryWrapper<TeacherSubjectEntity>()
                .in(TeacherSubjectEntity::getTeacherId, teacherIds));
        if (teacherSubjects.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> subjectIds = teacherSubjects.stream()
                .map(TeacherSubjectEntity::getSubjectId)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> subjectNameMap = subjectMapper.selectBatchIds(subjectIds)
                .stream()
                .collect(Collectors.toMap(SubjectEntity::getId, SubjectEntity::getSubjectName, (a, b) -> a));
        Map<String, List<String>> result = new HashMap<>();
        for (TeacherSubjectEntity relation : teacherSubjects) {
            result.computeIfAbsent(relation.getTeacherId(), key -> new ArrayList<>())
                    .add(subjectNameMap.getOrDefault(relation.getSubjectId(), ""));
        }
        return result;
    }

    private Map<String, List<String>> buildTeacherRegionNameMap(List<String> teacherIds) {
        List<TeacherRegionEntity> teacherRegions = teacherRegionMapper.selectList(new LambdaQueryWrapper<TeacherRegionEntity>()
                .in(TeacherRegionEntity::getTeacherId, teacherIds));
        if (teacherRegions.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> regionIds = teacherRegions.stream()
                .map(TeacherRegionEntity::getRegionId)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> regionNameMap = regionMapper.selectBatchIds(regionIds)
                .stream()
                .collect(Collectors.toMap(RegionEntity::getId, RegionEntity::getRegionName, (a, b) -> a));
        Map<String, List<String>> result = new HashMap<>();
        for (TeacherRegionEntity relation : teacherRegions) {
            result.computeIfAbsent(relation.getTeacherId(), key -> new ArrayList<>())
                    .add(regionNameMap.getOrDefault(relation.getRegionId(), ""));
        }
        return result;
    }

    private Map<String, java.time.LocalDateTime> buildTeacherLastOrderTimeMap(List<String> teacherIds) {
        List<OrderEntity> orders = orderMapper.selectList(new LambdaQueryWrapper<OrderEntity>()
                .in(OrderEntity::getTeacherId, teacherIds)
                .eq(OrderEntity::getOrderDeleteStatus, 0)
                .orderByDesc(OrderEntity::getCreateTime));
        if (orders.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, java.time.LocalDateTime> result = new HashMap<>();
        for (OrderEntity order : orders) {
            if (order.getTeacherId() == null || order.getCreateTime() == null || result.containsKey(order.getTeacherId())) {
                continue;
            }
            result.put(order.getTeacherId(), order.getCreateTime());
        }
        return result;
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

    private List<DispatchPublicListItemVO> buildDispatchCards(List<DispatchRecordEntity> latestDispatches) {
        if (latestDispatches == null || latestDispatches.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> orderIds = latestDispatches.stream()
                .map(DispatchRecordEntity::getOrderId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .collect(Collectors.toList());
        if (orderIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, OrderEntity> orderMap = orderMapper.selectBatchIds(orderIds)
                .stream()
                .collect(Collectors.toMap(OrderEntity::getId, item -> item, (a, b) -> a));

        List<String> requirementIds = orderMap.values().stream()
                .map(OrderEntity::getRequirementId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .collect(Collectors.toList());
        Map<String, RequirementEntity> requirementMap = requirementIds.isEmpty()
                ? Collections.emptyMap()
                : requirementMapper.selectBatchIds(requirementIds).stream()
                .collect(Collectors.toMap(RequirementEntity::getId, item -> item, (a, b) -> a));

        List<String> subjectIds = requirementMap.values().stream()
                .map(RequirementEntity::getSubjectId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> subjectNameMap = subjectIds.isEmpty()
                ? Collections.emptyMap()
                : subjectMapper.selectBatchIds(subjectIds).stream()
                .collect(Collectors.toMap(SubjectEntity::getId, SubjectEntity::getSubjectName, (a, b) -> a));

        List<String> regionIds = requirementMap.values().stream()
                .map(RequirementEntity::getRegionId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> regionNameMap = regionIds.isEmpty()
                ? Collections.emptyMap()
                : regionMapper.selectBatchIds(regionIds).stream()
                .collect(Collectors.toMap(RegionEntity::getId, RegionEntity::getRegionName, (a, b) -> a));

        List<String> teacherIds = latestDispatches.stream()
                .map(DispatchRecordEntity::getTeacherId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .collect(Collectors.toList());
        Map<String, TeacherInfoEntity> teacherMap = teacherIds.isEmpty()
                ? Collections.emptyMap()
                : teacherInfoMapper.selectBatchIds(teacherIds).stream()
                .collect(Collectors.toMap(TeacherInfoEntity::getId, item -> item, (a, b) -> a));

        List<String> teacherUserIds = teacherMap.values().stream()
                .map(TeacherInfoEntity::getUserId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> teacherUserNameMap = teacherUserIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(teacherUserIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getUserName, (a, b) -> a));

        List<DispatchPublicListItemVO> cards = new ArrayList<>();
        for (DispatchRecordEntity dispatch : latestDispatches) {
            OrderEntity order = orderMap.get(dispatch.getOrderId());
            if (order == null) {
                continue;
            }
            RequirementEntity requirement = requirementMap.get(order.getRequirementId());
            if (requirement == null) {
                continue;
            }
            TeacherInfoEntity teacher = teacherMap.get(dispatch.getTeacherId());
            String teacherName = "";
            if (teacher != null) {
                teacherName = teacherUserNameMap.getOrDefault(teacher.getUserId(), "");
            }

            DispatchPublicListItemVO card = new DispatchPublicListItemVO();
            card.setOrderId(order.getId());
            card.setOrderNumber(order.getOrderNumber());
            card.setRegionName(regionNameMap.getOrDefault(requirement.getRegionId(), "-"));
            card.setRequirementGrade(requirement.getRequirementGrade());
            card.setSubjectName(subjectNameMap.getOrDefault(requirement.getSubjectId(), "-"));
            card.setDispatchTime(dispatch.getDispatchTime());
            card.setSummary("已成功派给" + (teacherName.isBlank() ? "教员" : (teacherName + "教员")) + "...");
            cards.add(card);
        }
        return cards;
    }
}



