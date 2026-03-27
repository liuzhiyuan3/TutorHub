package com.teacher.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentCreateRequest {
    @NotBlank(message = "教员ID不能为空")
    private String teacherId;
    @NotBlank(message = "预约科目不能为空")
    private String appointmentSubject;
    @NotBlank(message = "预约年级不能为空")
    private String appointmentGrade;
    @NotBlank(message = "预约地址不能为空")
    private String appointmentAddress;
    @NotNull(message = "预约时间不能为空")
    private LocalDateTime appointmentTime;
    private String appointmentRemark;
}
