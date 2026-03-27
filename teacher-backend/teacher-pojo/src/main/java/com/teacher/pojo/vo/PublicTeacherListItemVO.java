package com.teacher.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class PublicTeacherListItemVO {
    private String teacherId;
    private String userName;
    private String teacherPhoto;
    private String teacherIdentity;
    private String teacherSchool;
    private String teacherMajor;
    private String teacherEducation;
    private Integer teacherTutoringMethod;
    private Integer teacherTeachingYears;
    private Integer teacherSuccessCount;
    private Integer teacherViewCount;
    private Integer historyDealCount;
    private Integer hireCount;
    private Double distanceKm;
    private LocalDateTime lastOrderTime;
    private List<String> subjectNames = new ArrayList<>();
    private List<String> regionNames = new ArrayList<>();
}
