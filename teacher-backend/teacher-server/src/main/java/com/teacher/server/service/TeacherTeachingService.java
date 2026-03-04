package com.teacher.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teacher.common.security.LoginUser;
import com.teacher.common.util.IdUtil;
import com.teacher.pojo.entity.TeacherInfoEntity;
import com.teacher.pojo.entity.TeacherRegionEntity;
import com.teacher.pojo.entity.TeacherSubjectEntity;
import com.teacher.pojo.entity.TeacherSuccessRecordEntity;
import com.teacher.server.mapper.TeacherInfoMapper;
import com.teacher.server.mapper.TeacherRegionMapper;
import com.teacher.server.mapper.TeacherSubjectMapper;
import com.teacher.server.mapper.TeacherSuccessRecordMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeacherTeachingService {
    private final TeacherInfoMapper teacherInfoMapper;
    private final TeacherSubjectMapper teacherSubjectMapper;
    private final TeacherRegionMapper teacherRegionMapper;
    private final TeacherSuccessRecordMapper teacherSuccessRecordMapper;
    private final AuthService authService;

    public TeacherTeachingService(TeacherInfoMapper teacherInfoMapper, TeacherSubjectMapper teacherSubjectMapper, TeacherRegionMapper teacherRegionMapper, TeacherSuccessRecordMapper teacherSuccessRecordMapper, AuthService authService) {
        this.teacherInfoMapper = teacherInfoMapper;
        this.teacherSubjectMapper = teacherSubjectMapper;
        this.teacherRegionMapper = teacherRegionMapper;
        this.teacherSuccessRecordMapper = teacherSuccessRecordMapper;
        this.authService = authService;
    }

    public TeacherSubjectEntity addSubject(String subjectId) {
        TeacherInfoEntity teacherInfo = currentTeacher();
        TeacherSubjectEntity item = new TeacherSubjectEntity();
        item.setId(IdUtil.uuid32());
        item.setTeacherId(teacherInfo.getId());
        item.setSubjectId(subjectId);
        item.setCreateTime(LocalDateTime.now());
        teacherSubjectMapper.insert(item);
        return item;
    }

    public TeacherRegionEntity addRegion(String regionId) {
        TeacherInfoEntity teacherInfo = currentTeacher();
        TeacherRegionEntity item = new TeacherRegionEntity();
        item.setId(IdUtil.uuid32());
        item.setTeacherId(teacherInfo.getId());
        item.setRegionId(regionId);
        item.setCreateTime(LocalDateTime.now());
        teacherRegionMapper.insert(item);
        return item;
    }

    public TeacherSuccessRecordEntity addSuccessRecord(TeacherSuccessRecordEntity entity) {
        TeacherInfoEntity teacherInfo = currentTeacher();
        entity.setId(IdUtil.uuid32());
        entity.setTeacherId(teacherInfo.getId());
        entity.setSuccessDeleteStatus(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        teacherSuccessRecordMapper.insert(entity);
        return entity;
    }

    public List<TeacherSubjectEntity> mySubjects() {
        TeacherInfoEntity teacherInfo = currentTeacher();
        return teacherSubjectMapper.selectList(new LambdaQueryWrapper<TeacherSubjectEntity>()
                .eq(TeacherSubjectEntity::getTeacherId, teacherInfo.getId()));
    }

    public List<TeacherRegionEntity> myRegions() {
        TeacherInfoEntity teacherInfo = currentTeacher();
        return teacherRegionMapper.selectList(new LambdaQueryWrapper<TeacherRegionEntity>()
                .eq(TeacherRegionEntity::getTeacherId, teacherInfo.getId()));
    }

    public List<TeacherSuccessRecordEntity> mySuccessRecords() {
        TeacherInfoEntity teacherInfo = currentTeacher();
        return teacherSuccessRecordMapper.selectList(new LambdaQueryWrapper<TeacherSuccessRecordEntity>()
                .eq(TeacherSuccessRecordEntity::getTeacherId, teacherInfo.getId())
                .eq(TeacherSuccessRecordEntity::getSuccessDeleteStatus, 0)
                .orderByDesc(TeacherSuccessRecordEntity::getCreateTime));
    }

    private TeacherInfoEntity currentTeacher() {
        LoginUser loginUser = authService.currentLoginUser();
        return teacherInfoMapper.selectOne(new LambdaQueryWrapper<TeacherInfoEntity>()
                .eq(TeacherInfoEntity::getUserId, loginUser.getId())
                .eq(TeacherInfoEntity::getTeacherDeleteStatus, 0)
                .last("limit 1"));
    }
}
