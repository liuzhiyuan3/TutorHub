package com.teacher.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.common.exception.BusinessException;
import com.teacher.common.model.PageResult;
import com.teacher.common.util.IdUtil;
import com.teacher.pojo.entity.*;
import com.teacher.server.mapper.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminManageService {
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final DictionaryMapper dictionaryMapper;
    private final DictionaryContentMapper dictionaryContentMapper;
    private final SlideMapper slideMapper;
    private final AdvertisingMapper advertisingMapper;
    private final UserMapper userMapper;
    private final TeacherInfoMapper teacherInfoMapper;
    private final OrderMapper orderMapper;
    private final RequirementMapper requirementMapper;
    private final SubjectMapper subjectMapper;
    private final SchoolMapper schoolMapper;
    private final RegionMapper regionMapper;

    public AdminManageService(RoleMapper roleMapper, MenuMapper menuMapper, RoleMenuMapper roleMenuMapper,
                              DictionaryMapper dictionaryMapper, DictionaryContentMapper dictionaryContentMapper,
                              SlideMapper slideMapper, AdvertisingMapper advertisingMapper, UserMapper userMapper,
                              TeacherInfoMapper teacherInfoMapper, OrderMapper orderMapper,
                              RequirementMapper requirementMapper, SubjectMapper subjectMapper,
                              SchoolMapper schoolMapper, RegionMapper regionMapper) {
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.dictionaryMapper = dictionaryMapper;
        this.dictionaryContentMapper = dictionaryContentMapper;
        this.slideMapper = slideMapper;
        this.advertisingMapper = advertisingMapper;
        this.userMapper = userMapper;
        this.teacherInfoMapper = teacherInfoMapper;
        this.orderMapper = orderMapper;
        this.requirementMapper = requirementMapper;
        this.subjectMapper = subjectMapper;
        this.schoolMapper = schoolMapper;
        this.regionMapper = regionMapper;
    }

    public PageResult<RoleEntity> rolePage(long pageNo, long pageSize) {
        return page(roleMapper, pageNo, pageSize, new LambdaQueryWrapper<RoleEntity>().orderByDesc(RoleEntity::getCreateTime));
    }

    public RoleEntity saveRole(RoleEntity entity) {
        assertUniqueRoleCode(entity);
        return save(entity, roleMapper, RoleEntity::setId, RoleEntity::setCreateTime, RoleEntity::setUpdateTime);
    }

    public PageResult<MenuEntity> menuPage(long pageNo, long pageSize) {
        return page(menuMapper, pageNo, pageSize, new LambdaQueryWrapper<MenuEntity>().orderByAsc(MenuEntity::getMenuPriority));
    }

    public MenuEntity saveMenu(MenuEntity entity) {
        if (entity.getMenuName() == null || entity.getMenuName().isBlank()) {
            throw new BusinessException("菜单名称不能为空");
        }
        return save(entity, menuMapper, MenuEntity::setId, MenuEntity::setCreateTime, MenuEntity::setUpdateTime);
    }

    public PageResult<RoleMenuEntity> roleMenuPage(long pageNo, long pageSize) {
        return page(roleMenuMapper, pageNo, pageSize, new LambdaQueryWrapper<RoleMenuEntity>().orderByDesc(RoleMenuEntity::getCreateTime));
    }

    public RoleMenuEntity saveRoleMenu(RoleMenuEntity entity) {
        long roleCount = roleMapper.selectCount(new LambdaQueryWrapper<RoleEntity>().eq(RoleEntity::getId, entity.getRoleId()));
        if (roleCount == 0) {
            throw new BusinessException("角色不存在");
        }
        long menuCount = menuMapper.selectCount(new LambdaQueryWrapper<MenuEntity>().eq(MenuEntity::getId, entity.getMenuId()));
        if (menuCount == 0) {
            throw new BusinessException("菜单不存在");
        }
        return save(entity, roleMenuMapper, RoleMenuEntity::setId, RoleMenuEntity::setCreateTime, null);
    }

    public PageResult<DictionaryEntity> dictionaryPage(long pageNo, long pageSize) {
        return page(dictionaryMapper, pageNo, pageSize, new LambdaQueryWrapper<DictionaryEntity>().orderByDesc(DictionaryEntity::getCreateTime));
    }

    public DictionaryEntity saveDictionary(DictionaryEntity entity) {
        assertUniqueDictionaryCode(entity);
        return save(entity, dictionaryMapper, DictionaryEntity::setId, DictionaryEntity::setCreateTime, DictionaryEntity::setUpdateTime);
    }

    public PageResult<DictionaryContentEntity> dictionaryContentPage(long pageNo, long pageSize) {
        return page(dictionaryContentMapper, pageNo, pageSize, new LambdaQueryWrapper<DictionaryContentEntity>().orderByAsc(DictionaryContentEntity::getDictionaryContentSort));
    }

    public DictionaryContentEntity saveDictionaryContent(DictionaryContentEntity entity) {
        long dictCount = dictionaryMapper.selectCount(new LambdaQueryWrapper<DictionaryEntity>().eq(DictionaryEntity::getId, entity.getDictionaryId()));
        if (dictCount == 0) {
            throw new BusinessException("字典不存在");
        }
        return save(entity, dictionaryContentMapper, DictionaryContentEntity::setId, DictionaryContentEntity::setCreateTime, DictionaryContentEntity::setUpdateTime);
    }

    public PageResult<SlideEntity> slidePage(long pageNo, long pageSize) {
        return page(slideMapper, pageNo, pageSize, new LambdaQueryWrapper<SlideEntity>().orderByDesc(SlideEntity::getCreateTime));
    }

    public SlideEntity saveSlide(SlideEntity entity) {
        if (entity.getSlidePicture() == null || entity.getSlidePicture().isBlank()) {
            throw new BusinessException("轮播图图片不能为空");
        }
        return save(entity, slideMapper, SlideEntity::setId, SlideEntity::setCreateTime, SlideEntity::setUpdateTime);
    }

    public PageResult<AdvertisingEntity> advertisingPage(long pageNo, long pageSize) {
        return page(advertisingMapper, pageNo, pageSize, new LambdaQueryWrapper<AdvertisingEntity>().orderByDesc(AdvertisingEntity::getCreateTime));
    }

    public AdvertisingEntity saveAdvertising(AdvertisingEntity entity) {
        if (entity.getAdvertisingTitle() == null || entity.getAdvertisingTitle().isBlank()) {
            throw new BusinessException("广告标题不能为空");
        }
        return save(entity, advertisingMapper, AdvertisingEntity::setId, AdvertisingEntity::setCreateTime, AdvertisingEntity::setUpdateTime);
    }

    public Map<String, Long> statistics() {
        Map<String, Long> result = new HashMap<>();
        result.put("userTotal", userMapper.selectCount(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUserDeleteStatus, 0)));
        result.put("teacherTotal", teacherInfoMapper.selectCount(new LambdaQueryWrapper<TeacherInfoEntity>().eq(TeacherInfoEntity::getTeacherDeleteStatus, 0)));
        result.put("orderTotal", orderMapper.selectCount(new LambdaQueryWrapper<OrderEntity>().eq(OrderEntity::getOrderDeleteStatus, 0)));
        result.put("requirementTotal", requirementMapper.selectCount(new LambdaQueryWrapper<RequirementEntity>().eq(RequirementEntity::getRequirementDeleteStatus, 0)));
        return result;
    }

    public Map<String, Long> businessStatistics() {
        Map<String, Long> result = new HashMap<>();
        result.put("roleTotal", roleMapper.selectCount(new LambdaQueryWrapper<>()));
        result.put("menuTotal", menuMapper.selectCount(new LambdaQueryWrapper<>()));
        result.put("roleMenuTotal", roleMenuMapper.selectCount(new LambdaQueryWrapper<>()));
        result.put("dictionaryTotal", dictionaryMapper.selectCount(new LambdaQueryWrapper<>()));
        result.put("dictionaryContentTotal", dictionaryContentMapper.selectCount(new LambdaQueryWrapper<>()));
        result.put("subjectTotal", subjectMapper.selectCount(new LambdaQueryWrapper<SubjectEntity>()
                .eq(SubjectEntity::getSubjectDeleteStatus, 0)));
        result.put("schoolTotal", schoolMapper.selectCount(new LambdaQueryWrapper<SchoolEntity>()
                .eq(SchoolEntity::getSchoolDeleteStatus, 0)));
        result.put("regionTotal", regionMapper.selectCount(new LambdaQueryWrapper<RegionEntity>()
                .eq(RegionEntity::getRegionDeleteStatus, 0)));
        result.put("slideTotal", slideMapper.selectCount(new LambdaQueryWrapper<SlideEntity>()
                .eq(SlideEntity::getSlideDeleteStatus, 0)));
        result.put("slideEnabledTotal", slideMapper.selectCount(new LambdaQueryWrapper<SlideEntity>()
                .eq(SlideEntity::getSlideDeleteStatus, 0)
                .eq(SlideEntity::getSlideStatus, 1)));
        result.put("advertisingTotal", advertisingMapper.selectCount(new LambdaQueryWrapper<AdvertisingEntity>()
                .eq(AdvertisingEntity::getAdvertisingDeleteStatus, 0)));
        result.put("advertisingEnabledTotal", advertisingMapper.selectCount(new LambdaQueryWrapper<AdvertisingEntity>()
                .eq(AdvertisingEntity::getAdvertisingDeleteStatus, 0)
                .eq(AdvertisingEntity::getAdvertisingStatus, 1)));
        result.put("teacherPendingAuditTotal", teacherInfoMapper.selectCount(new LambdaQueryWrapper<TeacherInfoEntity>()
                .eq(TeacherInfoEntity::getTeacherDeleteStatus, 0)
                .eq(TeacherInfoEntity::getTeacherAuditStatus, 0)));
        result.put("requirementPendingAuditTotal", requirementMapper.selectCount(new LambdaQueryWrapper<RequirementEntity>()
                .eq(RequirementEntity::getRequirementDeleteStatus, 0)
                .eq(RequirementEntity::getRequirementAuditStatus, 0)));
        return result;
    }

    public void deleteRole(String id) {
        long bind = roleMenuMapper.selectCount(new LambdaQueryWrapper<RoleMenuEntity>().eq(RoleMenuEntity::getRoleId, id));
        if (bind > 0) {
            throw new BusinessException("角色已绑定菜单，无法删除");
        }
        roleMapper.deleteById(id);
    }

    public void deleteMenu(String id) {
        long children = menuMapper.selectCount(new LambdaQueryWrapper<MenuEntity>().eq(MenuEntity::getMenuParent, id));
        if (children > 0) {
            throw new BusinessException("请先删除子菜单");
        }
        menuMapper.deleteById(id);
    }

    public void deleteRoleMenu(String id) {
        roleMenuMapper.deleteById(id);
    }

    public void deleteDictionary(String id) {
        long bind = dictionaryContentMapper.selectCount(new LambdaQueryWrapper<DictionaryContentEntity>()
                .eq(DictionaryContentEntity::getDictionaryId, id));
        if (bind > 0) {
            throw new BusinessException("请先删除字典项");
        }
        dictionaryMapper.deleteById(id);
    }

    public void deleteDictionaryContent(String id) {
        dictionaryContentMapper.deleteById(id);
    }

    public void deleteSlide(String id) {
        slideMapper.deleteById(id);
    }

    public void deleteAdvertising(String id) {
        advertisingMapper.deleteById(id);
    }

    private <T> PageResult<T> page(BaseMapper<T> mapper, long pageNo, long pageSize, LambdaQueryWrapper<T> wrapper) {
        Page<T> page = new Page<>(pageNo, pageSize);
        Page<T> result = mapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), pageNo, pageSize, result.getRecords());
    }

    private <T> T save(T entity, BaseMapper<T> mapper, java.util.function.BiConsumer<T, String> idSetter,
                       java.util.function.BiConsumer<T, LocalDateTime> createSetter,
                       java.util.function.BiConsumer<T, LocalDateTime> updateSetter) {
        LocalDateTime now = LocalDateTime.now();
        try {
            Object id = entity.getClass().getMethod("getId").invoke(entity);
            if (id == null || id.toString().isBlank()) {
                idSetter.accept(entity, IdUtil.uuid32());
                if (createSetter != null) {
                    createSetter.accept(entity, now);
                }
                if (updateSetter != null) {
                    updateSetter.accept(entity, now);
                }
                mapper.insert(entity);
            } else {
                if (updateSetter != null) {
                    updateSetter.accept(entity, now);
                }
                mapper.updateById(entity);
            }
        } catch (Exception e) {
            throw new IllegalStateException("保存数据失败: " + e.getMessage(), e);
        }
        return entity;
    }

    private void assertUniqueRoleCode(RoleEntity entity) {
        if (entity.getRoleCode() == null || entity.getRoleCode().isBlank()) {
            throw new BusinessException("角色编码不能为空");
        }
        LambdaQueryWrapper<RoleEntity> wrapper = new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getRoleCode, entity.getRoleCode());
        if (entity.getId() != null && !entity.getId().isBlank()) {
            wrapper.ne(RoleEntity::getId, entity.getId());
        }
        if (roleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("角色编码已存在");
        }
    }

    private void assertUniqueDictionaryCode(DictionaryEntity entity) {
        if (entity.getDictionaryCode() == null || entity.getDictionaryCode().isBlank()) {
            throw new BusinessException("字典编码不能为空");
        }
        LambdaQueryWrapper<DictionaryEntity> wrapper = new LambdaQueryWrapper<DictionaryEntity>()
                .eq(DictionaryEntity::getDictionaryCode, entity.getDictionaryCode());
        if (entity.getId() != null && !entity.getId().isBlank()) {
            wrapper.ne(DictionaryEntity::getId, entity.getId());
        }
        if (dictionaryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("字典编码已存在");
        }
    }
}
