package com.teacher.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.common.model.PageResult;
import com.teacher.common.util.IdUtil;
import com.teacher.pojo.entity.RegionEntity;
import com.teacher.pojo.entity.SchoolEntity;
import com.teacher.pojo.entity.SubjectEntity;
import com.teacher.server.mapper.RegionMapper;
import com.teacher.server.mapper.SchoolMapper;
import com.teacher.server.mapper.SubjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Service
public class ContentService {
    private final SubjectMapper subjectMapper;
    private final SchoolMapper schoolMapper;
    private final RegionMapper regionMapper;

    public ContentService(SubjectMapper subjectMapper, SchoolMapper schoolMapper, RegionMapper regionMapper) {
        this.subjectMapper = subjectMapper;
        this.schoolMapper = schoolMapper;
        this.regionMapper = regionMapper;
    }

    public List<SubjectEntity> allSubjects() {
        return subjectMapper.selectList(new LambdaQueryWrapper<SubjectEntity>()
                .eq(SubjectEntity::getSubjectDeleteStatus, 0)
                .eq(SubjectEntity::getSubjectStatus, 1)
                .orderByAsc(SubjectEntity::getSubjectSort));
    }

    public List<RegionEntity> allRegions() {
        return regionMapper.selectList(new LambdaQueryWrapper<RegionEntity>()
                .eq(RegionEntity::getRegionDeleteStatus, 0)
                .eq(RegionEntity::getRegionStatus, 1)
                .orderByAsc(RegionEntity::getRegionSort));
    }

    public List<SchoolEntity> allSchools() {
        return schoolMapper.selectList(new LambdaQueryWrapper<SchoolEntity>()
                .eq(SchoolEntity::getSchoolDeleteStatus, 0)
                .eq(SchoolEntity::getSchoolStatus, 1)
                .orderByAsc(SchoolEntity::getSchoolName));
    }

    public PageResult<SubjectEntity> pageSubjects(long pageNo, long pageSize) {
        Page<SubjectEntity> page = new Page<>(pageNo, pageSize);
        Page<SubjectEntity> result = subjectMapper.selectPage(page, new LambdaQueryWrapper<SubjectEntity>()
                .eq(SubjectEntity::getSubjectDeleteStatus, 0)
                .orderByDesc(SubjectEntity::getCreateTime));
        return new PageResult<>(result.getTotal(), pageNo, pageSize, result.getRecords());
    }

    public PageResult<SchoolEntity> pageSchools(long pageNo, long pageSize) {
        Page<SchoolEntity> page = new Page<>(pageNo, pageSize);
        Page<SchoolEntity> result = schoolMapper.selectPage(page, new LambdaQueryWrapper<SchoolEntity>()
                .eq(SchoolEntity::getSchoolDeleteStatus, 0)
                .orderByDesc(SchoolEntity::getCreateTime));
        return new PageResult<>(result.getTotal(), pageNo, pageSize, result.getRecords());
    }

    public PageResult<RegionEntity> pageRegions(long pageNo, long pageSize) {
        Page<RegionEntity> page = new Page<>(pageNo, pageSize);
        Page<RegionEntity> result = regionMapper.selectPage(page, new LambdaQueryWrapper<RegionEntity>()
                .eq(RegionEntity::getRegionDeleteStatus, 0)
                .orderByDesc(RegionEntity::getCreateTime));
        return new PageResult<>(result.getTotal(), pageNo, pageSize, result.getRecords());
    }

    public SubjectEntity saveSubject(SubjectEntity entity) {
        return saveEntity(entity, subjectMapper, SubjectEntity::getCreateTime, SubjectEntity::setCreateTime, SubjectEntity::setUpdateTime, SubjectEntity::setId);
    }

    public SchoolEntity saveSchool(SchoolEntity entity) {
        return saveEntity(entity, schoolMapper, SchoolEntity::getCreateTime, SchoolEntity::setCreateTime, SchoolEntity::setUpdateTime, SchoolEntity::setId);
    }

    public RegionEntity saveRegion(RegionEntity entity) {
        return saveEntity(entity, regionMapper, RegionEntity::getCreateTime, RegionEntity::setCreateTime, RegionEntity::setUpdateTime, RegionEntity::setId);
    }

    private <T> T saveEntity(
            T entity,
            BaseMapper<T> mapper,
            Function<T, LocalDateTime> createGetter,
            BiConsumer<T, LocalDateTime> createSetter,
            BiConsumer<T, LocalDateTime> updateSetter,
            BiConsumer<T, String> idSetter
    ) {
        LocalDateTime now = LocalDateTime.now();
        if (createGetter.apply(entity) == null) {
            idSetter.accept(entity, IdUtil.uuid32());
            createSetter.accept(entity, now);
            updateSetter.accept(entity, now);
            mapper.insert(entity);
        } else {
            updateSetter.accept(entity, now);
            mapper.updateById(entity);
        }
        return entity;
    }
}
