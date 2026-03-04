package com.teacher.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    public AdminManageService(RoleMapper roleMapper, MenuMapper menuMapper, RoleMenuMapper roleMenuMapper,
                              DictionaryMapper dictionaryMapper, DictionaryContentMapper dictionaryContentMapper,
                              SlideMapper slideMapper, AdvertisingMapper advertisingMapper, UserMapper userMapper,
                              TeacherInfoMapper teacherInfoMapper, OrderMapper orderMapper) {
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
    }

    public PageResult<RoleEntity> rolePage(long pageNo, long pageSize) {
        return page(roleMapper, pageNo, pageSize, new LambdaQueryWrapper<RoleEntity>().orderByDesc(RoleEntity::getCreateTime));
    }

    public RoleEntity saveRole(RoleEntity entity) {
        return save(entity, roleMapper, RoleEntity::setId, RoleEntity::setCreateTime, RoleEntity::setUpdateTime);
    }

    public PageResult<MenuEntity> menuPage(long pageNo, long pageSize) {
        return page(menuMapper, pageNo, pageSize, new LambdaQueryWrapper<MenuEntity>().orderByAsc(MenuEntity::getMenuPriority));
    }

    public MenuEntity saveMenu(MenuEntity entity) {
        return save(entity, menuMapper, MenuEntity::setId, MenuEntity::setCreateTime, MenuEntity::setUpdateTime);
    }

    public PageResult<RoleMenuEntity> roleMenuPage(long pageNo, long pageSize) {
        return page(roleMenuMapper, pageNo, pageSize, new LambdaQueryWrapper<RoleMenuEntity>().orderByDesc(RoleMenuEntity::getCreateTime));
    }

    public RoleMenuEntity saveRoleMenu(RoleMenuEntity entity) {
        return save(entity, roleMenuMapper, RoleMenuEntity::setId, RoleMenuEntity::setCreateTime, null);
    }

    public PageResult<DictionaryEntity> dictionaryPage(long pageNo, long pageSize) {
        return page(dictionaryMapper, pageNo, pageSize, new LambdaQueryWrapper<DictionaryEntity>().orderByDesc(DictionaryEntity::getCreateTime));
    }

    public DictionaryEntity saveDictionary(DictionaryEntity entity) {
        return save(entity, dictionaryMapper, DictionaryEntity::setId, DictionaryEntity::setCreateTime, DictionaryEntity::setUpdateTime);
    }

    public PageResult<DictionaryContentEntity> dictionaryContentPage(long pageNo, long pageSize) {
        return page(dictionaryContentMapper, pageNo, pageSize, new LambdaQueryWrapper<DictionaryContentEntity>().orderByAsc(DictionaryContentEntity::getDictionaryContentSort));
    }

    public DictionaryContentEntity saveDictionaryContent(DictionaryContentEntity entity) {
        return save(entity, dictionaryContentMapper, DictionaryContentEntity::setId, DictionaryContentEntity::setCreateTime, DictionaryContentEntity::setUpdateTime);
    }

    public PageResult<SlideEntity> slidePage(long pageNo, long pageSize) {
        return page(slideMapper, pageNo, pageSize, new LambdaQueryWrapper<SlideEntity>().orderByDesc(SlideEntity::getCreateTime));
    }

    public SlideEntity saveSlide(SlideEntity entity) {
        return save(entity, slideMapper, SlideEntity::setId, SlideEntity::setCreateTime, SlideEntity::setUpdateTime);
    }

    public PageResult<AdvertisingEntity> advertisingPage(long pageNo, long pageSize) {
        return page(advertisingMapper, pageNo, pageSize, new LambdaQueryWrapper<AdvertisingEntity>().orderByDesc(AdvertisingEntity::getCreateTime));
    }

    public AdvertisingEntity saveAdvertising(AdvertisingEntity entity) {
        return save(entity, advertisingMapper, AdvertisingEntity::setId, AdvertisingEntity::setCreateTime, AdvertisingEntity::setUpdateTime);
    }

    public Map<String, Long> statistics() {
        Map<String, Long> result = new HashMap<>();
        result.put("userTotal", userMapper.selectCount(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUserDeleteStatus, 0)));
        result.put("teacherTotal", teacherInfoMapper.selectCount(new LambdaQueryWrapper<TeacherInfoEntity>().eq(TeacherInfoEntity::getTeacherDeleteStatus, 0)));
        result.put("orderTotal", orderMapper.selectCount(new LambdaQueryWrapper<OrderEntity>().eq(OrderEntity::getOrderDeleteStatus, 0)));
        return result;
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
}
