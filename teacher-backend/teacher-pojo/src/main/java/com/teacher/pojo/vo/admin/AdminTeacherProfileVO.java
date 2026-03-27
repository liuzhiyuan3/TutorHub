package com.teacher.pojo.vo.admin;

import com.teacher.pojo.entity.OrderEntity;
import com.teacher.pojo.entity.TeacherAuditEntity;
import com.teacher.pojo.entity.TeacherInfoEntity;
import com.teacher.pojo.entity.TeacherSuccessRecordEntity;
import com.teacher.pojo.entity.UserEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminTeacherProfileVO {
    private UserEntity user;
    private TeacherInfoEntity teacherInfo;
    private List<String> subjectNames = new ArrayList<>();
    private List<String> regionNames = new ArrayList<>();
    private List<TeacherSuccessRecordEntity> successRecords = new ArrayList<>();
    private List<TeacherAuditEntity> auditRecords = new ArrayList<>();
    private List<OrderEntity> recentOrders = new ArrayList<>();
}
