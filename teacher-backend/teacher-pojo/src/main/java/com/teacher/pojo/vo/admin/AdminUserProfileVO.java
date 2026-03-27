package com.teacher.pojo.vo.admin;

import com.teacher.pojo.entity.AppointmentEntity;
import com.teacher.pojo.entity.OrderEntity;
import com.teacher.pojo.entity.RequirementEntity;
import com.teacher.pojo.entity.TeacherInfoEntity;
import com.teacher.pojo.entity.UserEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminUserProfileVO {
    private UserEntity user;
    private TeacherInfoEntity teacherInfo;
    private List<RequirementEntity> requirements = new ArrayList<>();
    private List<OrderEntity> parentOrders = new ArrayList<>();
    private List<OrderEntity> teacherOrders = new ArrayList<>();
    private List<AppointmentEntity> appointments = new ArrayList<>();
}
