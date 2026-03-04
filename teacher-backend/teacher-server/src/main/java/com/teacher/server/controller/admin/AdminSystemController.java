package com.teacher.server.controller.admin;

import com.teacher.common.model.ApiResponse;
import com.teacher.common.model.PageResult;
import com.teacher.pojo.entity.*;
import com.teacher.server.service.AdminManageService;
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
    public ApiResponse<RoleEntity> saveRole(@RequestBody RoleEntity entity) {
        return ApiResponse.ok(adminManageService.saveRole(entity));
    }

    @GetMapping("/menus/page")
    public ApiResponse<PageResult<MenuEntity>> menuPage(@RequestParam(defaultValue = "1") long pageNo,
                                                        @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(adminManageService.menuPage(pageNo, pageSize));
    }

    @PostMapping("/menus")
    public ApiResponse<MenuEntity> saveMenu(@RequestBody MenuEntity entity) {
        return ApiResponse.ok(adminManageService.saveMenu(entity));
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

    @GetMapping("/dictionary/page")
    public ApiResponse<PageResult<DictionaryEntity>> dictionaryPage(@RequestParam(defaultValue = "1") long pageNo,
                                                                    @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(adminManageService.dictionaryPage(pageNo, pageSize));
    }

    @PostMapping("/dictionary")
    public ApiResponse<DictionaryEntity> saveDictionary(@RequestBody DictionaryEntity entity) {
        return ApiResponse.ok(adminManageService.saveDictionary(entity));
    }

    @GetMapping("/dictionary-content/page")
    public ApiResponse<PageResult<DictionaryContentEntity>> dictionaryContentPage(@RequestParam(defaultValue = "1") long pageNo,
                                                                                   @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(adminManageService.dictionaryContentPage(pageNo, pageSize));
    }

    @PostMapping("/dictionary-content")
    public ApiResponse<DictionaryContentEntity> saveDictionaryContent(@RequestBody DictionaryContentEntity entity) {
        return ApiResponse.ok(adminManageService.saveDictionaryContent(entity));
    }

    @GetMapping("/slides/page")
    public ApiResponse<PageResult<SlideEntity>> slidePage(@RequestParam(defaultValue = "1") long pageNo,
                                                           @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(adminManageService.slidePage(pageNo, pageSize));
    }

    @PostMapping("/slides")
    public ApiResponse<SlideEntity> saveSlide(@RequestBody SlideEntity entity) {
        return ApiResponse.ok(adminManageService.saveSlide(entity));
    }

    @GetMapping("/advertising/page")
    public ApiResponse<PageResult<AdvertisingEntity>> advertisingPage(@RequestParam(defaultValue = "1") long pageNo,
                                                                      @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(adminManageService.advertisingPage(pageNo, pageSize));
    }

    @PostMapping("/advertising")
    public ApiResponse<AdvertisingEntity> saveAdvertising(@RequestBody AdvertisingEntity entity) {
        return ApiResponse.ok(adminManageService.saveAdvertising(entity));
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Long>> stats() {
        return ApiResponse.ok(adminManageService.statistics());
    }
}
