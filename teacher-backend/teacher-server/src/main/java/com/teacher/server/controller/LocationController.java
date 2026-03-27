package com.teacher.server.controller;

import com.teacher.common.model.ApiResponse;
import com.teacher.pojo.dto.LocationReverseRequest;
import com.teacher.pojo.vo.LocationReverseVO;
import com.teacher.server.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/location")
public class LocationController {
    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping("/reverse")
    public ApiResponse<LocationReverseVO> reverse(@Valid @RequestBody LocationReverseRequest request) {
        return ApiResponse.ok(locationService.reverse(request.getLatitude(), request.getLongitude()));
    }
}

