package com.teacher.server.controller;

import com.teacher.common.model.ApiResponse;
import com.teacher.pojo.vo.FileUploadVO;
import com.teacher.server.service.AuthService;
import com.teacher.server.service.OssStorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
public class FileController {
    private final OssStorageService ossStorageService;
    private final AuthService authService;

    public FileController(OssStorageService ossStorageService, AuthService authService) {
        this.ossStorageService = ossStorageService;
        this.authService = authService;
    }

    @PostMapping("/upload")
    public ApiResponse<FileUploadVO> upload(@RequestPart("file") MultipartFile file,
                                            @RequestParam(value = "biz", required = false) String biz) {
        authService.currentLoginUser();
        return ApiResponse.ok(ossStorageService.uploadImage(file, biz));
    }
}
