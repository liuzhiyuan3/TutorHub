package com.teacher.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.common.exception.BusinessException;
import com.teacher.common.model.PageResult;
import com.teacher.common.util.IdUtil;
import com.teacher.pojo.entity.RegionEntity;
import com.teacher.pojo.entity.SchoolEntity;
import com.teacher.pojo.entity.SlideEntity;
import com.teacher.pojo.entity.SubjectEntity;
import com.teacher.pojo.entity.SubjectCategoryEntity;
import com.teacher.pojo.entity.AdvertisingEntity;
import com.teacher.pojo.vo.PublicOptionVO;
import com.teacher.pojo.vo.PublicAdvertisingVO;
import com.teacher.pojo.vo.PublicSlideVO;
import com.teacher.pojo.vo.SubjectCategoryTreeVO;
import com.teacher.server.mapper.AdvertisingMapper;
import com.teacher.server.mapper.RegionMapper;
import com.teacher.server.mapper.SchoolMapper;
import com.teacher.server.mapper.SlideMapper;
import com.teacher.server.mapper.SubjectMapper;
import com.teacher.server.mapper.SubjectCategoryMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Service
public class ContentService {
    private final SubjectMapper subjectMapper;
    private final SchoolMapper schoolMapper;
    private final RegionMapper regionMapper;
    private final SlideMapper slideMapper;
    private final AdvertisingMapper advertisingMapper;
    private final SubjectCategoryMapper subjectCategoryMapper;

    public ContentService(SubjectMapper subjectMapper, SchoolMapper schoolMapper, RegionMapper regionMapper,
                         SlideMapper slideMapper, AdvertisingMapper advertisingMapper, SubjectCategoryMapper subjectCategoryMapper) {
        this.subjectMapper = subjectMapper;
        this.schoolMapper = schoolMapper;
        this.regionMapper = regionMapper;
        this.slideMapper = slideMapper;
        this.advertisingMapper = advertisingMapper;
        this.subjectCategoryMapper = subjectCategoryMapper;
    }

    public List<SubjectEntity> allSubjects() {
        return subjectMapper.selectList(new LambdaQueryWrapper<SubjectEntity>()
                .eq(SubjectEntity::getSubjectDeleteStatus, 0)
                .eq(SubjectEntity::getSubjectStatus, 1)
                .orderByAsc(SubjectEntity::getSubjectSort));
    }

    public List<PublicOptionVO> publicSubjects() {
        List<SubjectEntity> subjects = allSubjects();
        List<PublicOptionVO> result = new ArrayList<>(subjects.size());
        for (SubjectEntity subject : subjects) {
            result.add(new PublicOptionVO(subject.getId(), subject.getSubjectCode(), subject.getSubjectName()));
        }
        return result;
    }

    public List<SubjectCategoryTreeVO> publicSubjectCategoryTree() {
        List<SubjectCategoryEntity> categories = allSubjectCategories();
        List<SubjectEntity> subjects = allSubjects();

        java.util.Map<String, List<SubjectEntity>> grouped = subjects.stream().collect(Collectors.groupingBy(item -> {
            if (item.getSubjectCategoryId() != null && !item.getSubjectCategoryId().isBlank()) {
                return item.getSubjectCategoryId();
            }
            return "legacy_" + (item.getSubjectCategory() == null ? "其他" : item.getSubjectCategory());
        }));

        List<SubjectCategoryTreeVO> tree = new ArrayList<>();
        for (SubjectCategoryEntity category : categories) {
            SubjectCategoryTreeVO vo = new SubjectCategoryTreeVO();
            vo.setId(category.getId());
            vo.setCategoryName(category.getCategoryName());
            vo.setCategoryCode(category.getCategoryCode());
            vo.setCategorySort(category.getCategorySort());
            List<PublicOptionVO> options = grouped.getOrDefault(category.getId(), new ArrayList<>()).stream()
                    .map(item -> new PublicOptionVO(item.getId(), item.getSubjectCode(), item.getSubjectName()))
                    .collect(Collectors.toList());
            vo.setSubjects(options);
            tree.add(vo);
        }

        // fallback legacy categories not mapped by id
        for (java.util.Map.Entry<String, List<SubjectEntity>> entry : grouped.entrySet()) {
            if (!entry.getKey().startsWith("legacy_")) {
                continue;
            }
            String legacyName = entry.getKey().substring("legacy_".length());
            SubjectCategoryTreeVO vo = new SubjectCategoryTreeVO();
            vo.setId(entry.getKey());
            vo.setCategoryName(legacyName);
            vo.setCategoryCode("LEGACY");
            vo.setCategorySort(999);
            vo.setSubjects(entry.getValue().stream()
                    .map(item -> new PublicOptionVO(item.getId(), item.getSubjectCode(), item.getSubjectName()))
                    .collect(Collectors.toList()));
            tree.add(vo);
        }

        // if there are categories but no subjects attached, keep them for config visibility
        tree.sort(java.util.Comparator.comparing(SubjectCategoryTreeVO::getCategorySort, java.util.Comparator.nullsLast(Integer::compareTo)));
        return tree;
    }

    public List<SubjectCategoryEntity> allSubjectCategories() {
        return subjectCategoryMapper.selectList(new LambdaQueryWrapper<SubjectCategoryEntity>()
                .eq(SubjectCategoryEntity::getCategoryDeleteStatus, 0)
                .eq(SubjectCategoryEntity::getCategoryStatus, 1)
                .orderByAsc(SubjectCategoryEntity::getCategorySort)
                .orderByAsc(SubjectCategoryEntity::getCreateTime));
    }

    public List<RegionEntity> allRegions() {
        return regionMapper.selectList(new LambdaQueryWrapper<RegionEntity>()
                .eq(RegionEntity::getRegionDeleteStatus, 0)
                .eq(RegionEntity::getRegionStatus, 1)
                .orderByAsc(RegionEntity::getRegionSort));
    }

    public List<PublicOptionVO> publicRegions() {
        List<RegionEntity> regions = allRegions();
        List<PublicOptionVO> result = new ArrayList<>(regions.size());
        for (RegionEntity region : regions) {
            result.add(new PublicOptionVO(region.getId(), region.getRegionCode(), region.getRegionName()));
        }
        return result;
    }

    public List<SchoolEntity> allSchools() {
        return schoolMapper.selectList(new LambdaQueryWrapper<SchoolEntity>()
                .eq(SchoolEntity::getSchoolDeleteStatus, 0)
                .eq(SchoolEntity::getSchoolStatus, 1)
                .orderByAsc(SchoolEntity::getSchoolName));
    }

    public List<PublicOptionVO> publicSchools() {
        List<SchoolEntity> schools = allSchools();
        List<PublicOptionVO> result = new ArrayList<>(schools.size());
        for (SchoolEntity school : schools) {
            result.add(new PublicOptionVO(school.getId(), school.getSchoolCode(), school.getSchoolName()));
        }
        return result;
    }

    public List<PublicSlideVO> publicSlides(Integer module) {
        LambdaQueryWrapper<SlideEntity> wrapper = new LambdaQueryWrapper<SlideEntity>()
                .eq(SlideEntity::getSlideDeleteStatus, 0)
                .eq(SlideEntity::getSlideStatus, 1)
                .orderByDesc(SlideEntity::getSlidePriority)
                .orderByDesc(SlideEntity::getCreateTime);
        if (module != null) {
            wrapper.eq(SlideEntity::getSlideModule, module);
        }
        return slideMapper.selectList(wrapper).stream().map(item -> {
            PublicSlideVO vo = new PublicSlideVO();
            vo.setId(item.getId());
            vo.setTitle(item.getSlideNote());
            vo.setImage(item.getSlidePicture());
            vo.setLink(item.getSlideLink());
            vo.setPriority(item.getSlidePriority());
            return vo;
        }).collect(Collectors.toList());
    }

    public List<PublicAdvertisingVO> publicAdvertising(String source) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<AdvertisingEntity> wrapper = new LambdaQueryWrapper<AdvertisingEntity>()
                .eq(AdvertisingEntity::getAdvertisingDeleteStatus, 0)
                .eq(AdvertisingEntity::getAdvertisingStatus, 1)
                .and(w -> w.isNull(AdvertisingEntity::getAdvertisingExpireTime)
                        .or().ge(AdvertisingEntity::getAdvertisingExpireTime, now))
                .orderByDesc(AdvertisingEntity::getCreateTime);
        if (source != null && !source.isBlank()) {
            wrapper.eq(AdvertisingEntity::getAdvertisingSource, source);
        }
        return advertisingMapper.selectList(wrapper).stream().map(item -> {
            PublicAdvertisingVO vo = new PublicAdvertisingVO();
            vo.setId(item.getId());
            vo.setTitle(item.getAdvertisingTitle());
            vo.setImage(item.getAdvertisingPicture());
            vo.setLink(item.getAdvertisingLink());
            vo.setExpireTime(item.getAdvertisingExpireTime());
            return vo;
        }).collect(Collectors.toList());
    }

    public PageResult<SubjectEntity> pageSubjects(long pageNo, long pageSize) {
        Page<SubjectEntity> page = new Page<>(pageNo, pageSize);
        Page<SubjectEntity> result = subjectMapper.selectPage(page, new LambdaQueryWrapper<SubjectEntity>()
                .eq(SubjectEntity::getSubjectDeleteStatus, 0)
                .orderByDesc(SubjectEntity::getCreateTime));
        return new PageResult<>(result.getTotal(), pageNo, pageSize, result.getRecords());
    }

    public PageResult<SubjectCategoryEntity> pageSubjectCategories(long pageNo, long pageSize) {
        Page<SubjectCategoryEntity> page = new Page<>(pageNo, pageSize);
        Page<SubjectCategoryEntity> result = subjectCategoryMapper.selectPage(page, new LambdaQueryWrapper<SubjectCategoryEntity>()
                .eq(SubjectCategoryEntity::getCategoryDeleteStatus, 0)
                .orderByAsc(SubjectCategoryEntity::getCategorySort)
                .orderByDesc(SubjectCategoryEntity::getCreateTime));
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
        validateSubject(entity);
        if (entity.getSubjectCategoryId() != null && !entity.getSubjectCategoryId().isBlank()) {
            SubjectCategoryEntity category = subjectCategoryMapper.selectById(entity.getSubjectCategoryId());
            if (category == null || category.getCategoryDeleteStatus() == 1) {
                throw new BusinessException("学科分类不存在");
            }
            entity.setSubjectCategory(category.getCategoryName());
        }
        return saveEntity(entity, subjectMapper, SubjectEntity::getCreateTime, SubjectEntity::setCreateTime, SubjectEntity::setUpdateTime, SubjectEntity::setId);
    }

    public SubjectCategoryEntity saveSubjectCategory(SubjectCategoryEntity entity) {
        validateSubjectCategory(entity);
        return saveEntity(entity, subjectCategoryMapper, SubjectCategoryEntity::getCreateTime, SubjectCategoryEntity::setCreateTime, SubjectCategoryEntity::setUpdateTime, SubjectCategoryEntity::setId);
    }

    public SchoolEntity saveSchool(SchoolEntity entity) {
        validateSchool(entity);
        return saveEntity(entity, schoolMapper, SchoolEntity::getCreateTime, SchoolEntity::setCreateTime, SchoolEntity::setUpdateTime, SchoolEntity::setId);
    }

    public RegionEntity saveRegion(RegionEntity entity) {
        validateRegion(entity);
        return saveEntity(entity, regionMapper, RegionEntity::getCreateTime, RegionEntity::setCreateTime, RegionEntity::setUpdateTime, RegionEntity::setId);
    }

    public void deleteSubject(String id) {
        subjectMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SubjectEntity>()
                .set(SubjectEntity::getSubjectDeleteStatus, 1)
                .set(SubjectEntity::getUpdateTime, LocalDateTime.now())
                .eq(SubjectEntity::getId, id));
    }

    public void deleteSubjectCategory(String id) {
        SubjectCategoryEntity category = subjectCategoryMapper.selectById(id);
        if (category == null || category.getCategoryDeleteStatus() == 1) {
            return;
        }
        long bindCount = subjectMapper.selectCount(new LambdaQueryWrapper<SubjectEntity>()
                .eq(SubjectEntity::getSubjectCategoryId, id)
                .eq(SubjectEntity::getSubjectDeleteStatus, 0));
        if (bindCount > 0) {
            throw new BusinessException("该分类下仍有学科，无法删除");
        }
        subjectCategoryMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SubjectCategoryEntity>()
                .set(SubjectCategoryEntity::getCategoryDeleteStatus, 1)
                .set(SubjectCategoryEntity::getUpdateTime, LocalDateTime.now())
                .eq(SubjectCategoryEntity::getId, id));
    }

    public void deleteSchool(String id) {
        schoolMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SchoolEntity>()
                .set(SchoolEntity::getSchoolDeleteStatus, 1)
                .set(SchoolEntity::getUpdateTime, LocalDateTime.now())
                .eq(SchoolEntity::getId, id));
    }

    public void deleteRegion(String id) {
        regionMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<RegionEntity>()
                .set(RegionEntity::getRegionDeleteStatus, 1)
                .set(RegionEntity::getUpdateTime, LocalDateTime.now())
                .eq(RegionEntity::getId, id));
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

    private void validateSubject(SubjectEntity entity) {
        if (entity.getSubjectName() == null || entity.getSubjectName().isBlank()) {
            throw new BusinessException("学科名称不能为空");
        }
        if (entity.getSubjectCode() == null || entity.getSubjectCode().isBlank()) {
            throw new BusinessException("学科编码不能为空");
        }
        LambdaQueryWrapper<SubjectEntity> wrapper = new LambdaQueryWrapper<SubjectEntity>()
                .eq(SubjectEntity::getSubjectCode, entity.getSubjectCode());
        if (entity.getId() != null && !entity.getId().isBlank()) {
            wrapper.ne(SubjectEntity::getId, entity.getId());
        }
        if (subjectMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("学科编码已存在");
        }
        if (entity.getSubjectCategoryId() != null && !entity.getSubjectCategoryId().isBlank()) {
            SubjectCategoryEntity category = subjectCategoryMapper.selectById(entity.getSubjectCategoryId());
            if (category == null || category.getCategoryDeleteStatus() == 1) {
                throw new BusinessException("学科分类不存在");
            }
        }
    }

    private void validateSubjectCategory(SubjectCategoryEntity entity) {
        if (entity.getCategoryName() == null || entity.getCategoryName().isBlank()) {
            throw new BusinessException("分类名称不能为空");
        }
        if (entity.getCategoryCode() == null || entity.getCategoryCode().isBlank()) {
            throw new BusinessException("分类编码不能为空");
        }
        LambdaQueryWrapper<SubjectCategoryEntity> wrapper = new LambdaQueryWrapper<SubjectCategoryEntity>()
                .eq(SubjectCategoryEntity::getCategoryCode, entity.getCategoryCode())
                .eq(SubjectCategoryEntity::getCategoryDeleteStatus, 0);
        if (entity.getId() != null && !entity.getId().isBlank()) {
            wrapper.ne(SubjectCategoryEntity::getId, entity.getId());
        }
        if (subjectCategoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("分类编码已存在");
        }
    }

    private void validateSchool(SchoolEntity entity) {
        if (entity.getSchoolName() == null || entity.getSchoolName().isBlank()) {
            throw new BusinessException("学校名称不能为空");
        }
        if (entity.getSchoolCode() == null || entity.getSchoolCode().isBlank()) {
            throw new BusinessException("学校编码不能为空");
        }
        if (entity.getSchoolAddress() == null || entity.getSchoolAddress().isBlank()) {
            throw new BusinessException("学校地址不能为空");
        }
        LambdaQueryWrapper<SchoolEntity> wrapper = new LambdaQueryWrapper<SchoolEntity>()
                .eq(SchoolEntity::getSchoolCode, entity.getSchoolCode());
        if (entity.getId() != null && !entity.getId().isBlank()) {
            wrapper.ne(SchoolEntity::getId, entity.getId());
        }
        if (schoolMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("学校编码已存在");
        }
    }

    private void validateRegion(RegionEntity entity) {
        if (entity.getRegionName() == null || entity.getRegionName().isBlank()) {
            throw new BusinessException("区域名称不能为空");
        }
        if (entity.getRegionCode() == null || entity.getRegionCode().isBlank()) {
            throw new BusinessException("区域编码不能为空");
        }
    }
}
