package com.teacher.server.controller.admin;

import com.teacher.common.model.ApiResponse;
import com.teacher.common.model.PageResult;
import com.teacher.pojo.dto.admin.*;
import com.teacher.pojo.entity.*;
import com.teacher.server.service.AdminManageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/system")
public class AdminSystemController {
    private final AdminManageService adminManageService;

    public AdminSystemController(AdminManageService adminManageService) {
        this.adminManageService = adminManageService;
    }

    @GetMapping("/roles/page")
    public ApiResponse<PageResult<RoleEntity>> rolePage(@RequestParam(defaultValue = "1") long pageNo,
                                                        @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(adminManageService.rolePage(pageNo, pageSize));
    }

    @PostMapping("/roles")
    public ApiResponse<RoleEntity> saveRole(@Valid @RequestBody RoleSaveRequest request) {
        RoleEntity entity = new RoleEntity();
        entity.setId(request.getId());
        entity.setRoleName(request.getRoleName());
        entity.setRoleCode(request.getRoleCode());
        entity.setRoleDescription(request.getRoleDescription());
        entity.setRoleDeleteStatus(0);
        return ApiResponse.ok(adminManageService.saveRole(entity));
    }

    @DeleteMapping("/roles/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable String id) {
        adminManageService.deleteRole(id);
        return ApiResponse.ok();
    }

    @GetMapping("/menus/page")
    public ApiResponse<PageResult<MenuEntity>> menuPage(@RequestParam(defaultValue = "1") long pageNo,
                                                        @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(adminManageService.menuPage(pageNo, pageSize));
    }

    @PostMapping("/menus")
    public ApiResponse<MenuEntity> saveMenu(@Valid @RequestBody MenuSaveRequest request) {
        MenuEntity entity = new MenuEntity();
        entity.setId(request.getId());
        entity.setMenuName(request.getMenuName());
        entity.setMenuParent(request.getMenuParent());
        entity.setMenuPriority(request.getMenuPriority());
        entity.setMenuLink(request.getMenuLink());
        entity.setMenuIcon(request.getMenuIcon());
        entity.setMenuType(request.getMenuType());
        entity.setMenuDeleteStatus(0);
        return ApiResponse.ok(adminManageService.saveMenu(entity));
    }

    @DeleteMapping("/menus/{id}")
    public ApiResponse<Void> deleteMenu(@PathVariable String id) {
        adminManageService.deleteMenu(id);
        return ApiResponse.ok();
    }

    @GetMapping("/role-menus/page")
    public ApiResponse<PageResult<RoleMenuEntity>> roleMenuPage(@RequestParam(defaultValue = "1") long pageNo,
                                                                 @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(adminManageService.roleMenuPage(pageNo, pageSize));
    }

    @PostMapping("/role-menus")
    public ApiResponse<RoleMenuEntity> saveRoleMenu(@RequestBody RoleMenuEntity entity) {
        return ApiResponse.ok(adminManageService.saveRoleMenu(entity));
    }

    @DeleteMapping("/role-menus/{id}")
    public ApiResponse<Void> deleteRoleMenu(@PathVariable String id) {
        adminManageService.deleteRoleMenu(id);
        return ApiResponse.ok();
    }

    @GetMapping("/dictionary/page")
    public ApiResponse<PageResult<DictionaryEntity>> dictionaryPage(@RequestParam(defaultValue = "1") long pageNo,
                                                                    @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(adminManageService.dictionaryPage(pageNo, pageSize));
    }

    @PostMapping("/dictionary")
    public ApiResponse<DictionaryEntity> saveDictionary(@Valid @RequestBody DictionarySaveRequest request) {
        DictionaryEntity entity = new DictionaryEntity();
        entity.setId(request.getId());
        entity.setDictionaryName(request.getDictionaryName());
        entity.setDictionaryCode(request.getDictionaryCode());
        entity.setDictionaryDescription(request.getDictionaryDescription());
        entity.setDictionaryDeleteStatus(0);
        return ApiResponse.ok(adminManageService.saveDictionary(entity));
    }

    @DeleteMapping("/dictionary/{id}")
    public ApiResponse<Void> deleteDictionary(@PathVariable String id) {
        adminManageService.deleteDictionary(id);
        return ApiResponse.ok();
    }

    @GetMapping("/dictionary-content/page")
    public ApiResponse<PageResult<DictionaryContentEntity>> dictionaryContentPage(@RequestParam(defaultValue = "1") long pageNo,
                                                                                   @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(adminManageService.dictionaryContentPage(pageNo, pageSize));
    }

    @PostMapping("/dictionary-content")
    public ApiResponse<DictionaryContentEntity> saveDictionaryContent(@Valid @RequestBody DictionaryContentSaveRequest request) {
        DictionaryContentEntity entity = new DictionaryContentEntity();
        entity.setId(request.getId());
        entity.setDictionaryId(request.getDictionaryId());
        entity.setDictionaryContentText(request.getDictionaryContentText());
        entity.setDictionaryContentValue(request.getDictionaryContentValue());
        entity.setDictionaryContentSort(request.getDictionaryContentSort());
        entity.setDictionaryContentStatus(request.getDictionaryContentStatus());
        return ApiResponse.ok(adminManageService.saveDictionaryContent(entity));
    }

    @DeleteMapping("/dictionary-content/{id}")
    public ApiResponse<Void> deleteDictionaryContent(@PathVariable String id) {
        adminManageService.deleteDictionaryContent(id);
        return ApiResponse.ok();
    }

    @GetMapping("/slides/page")
    public ApiResponse<PageResult<SlideEntity>> slidePage(@RequestParam(defaultValue = "1") long pageNo,
                                                           @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(adminManageService.slidePage(pageNo, pageSize));
    }

    @PostMapping("/slides")
    public ApiResponse<SlideEntity> saveSlide(@Valid @RequestBody SlideSaveRequest request) {
        SlideEntity entity = new SlideEntity();
        entity.setId(request.getId());
        entity.setSlidePicture(request.getSlidePicture());
        entity.setSlideLink(request.getSlideLink());
        entity.setSlideNote(request.getSlideNote());
        entity.setSlidePriority(request.getSlidePriority());
        entity.setSlideStatus(request.getSlideStatus());
        entity.setSlideModule(request.getSlideModule());
        entity.setSlideDeleteStatus(0);
        return ApiResponse.ok(adminManageService.saveSlide(entity));
    }

    @DeleteMapping("/slides/{id}")
    public ApiResponse<Void> deleteSlide(@PathVariable String id) {
        adminManageService.deleteSlide(id);
        return ApiResponse.ok();
    }

    @GetMapping("/advertising/page")
    public ApiResponse<PageResult<AdvertisingEntity>> advertisingPage(@RequestParam(defaultValue = "1") long pageNo,
                                                                      @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(adminManageService.advertisingPage(pageNo, pageSize));
    }

    @PostMapping("/advertising")
    public ApiResponse<AdvertisingEntity> saveAdvertising(@Valid @RequestBody AdvertisingSaveRequest request) {
        AdvertisingEntity entity = new AdvertisingEntity();
        entity.setId(request.getId());
        entity.setAdvertisingSource(request.getAdvertisingSource());
        entity.setAdvertisingTitle(request.getAdvertisingTitle());
        entity.setAdvertisingLink(request.getAdvertisingLink());
        entity.setAdvertisingPicture(request.getAdvertisingPicture());
        entity.setAdvertisingStatus(request.getAdvertisingStatus());
        entity.setAdvertisingExpireTime(request.getAdvertisingExpireTime());
        entity.setAdvertisingDeleteStatus(0);
        return ApiResponse.ok(adminManageService.saveAdvertising(entity));
    }

    @DeleteMapping("/advertising/{id}")
    public ApiResponse<Void> deleteAdvertising(@PathVariable String id) {
        adminManageService.deleteAdvertising(id);
        return ApiResponse.ok();
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Long>> stats() {
        return ApiResponse.ok(adminManageService.statistics());
    }
}
