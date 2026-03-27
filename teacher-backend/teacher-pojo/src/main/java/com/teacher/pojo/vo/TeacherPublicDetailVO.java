package com.teacher.pojo.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TeacherPublicDetailVO {
    private String teacherId;
    private String userId;
    private String userName;
    private String teacherPhoto;
    private String teacherIdentity;
    private Integer teacherTutoringMethod;
    private Integer teacherTeachingYears;
    private String teacherSchool;
    private String teacherMajor;
    private String teacherEducation;
    private String teacherExperience;
    private String teacherSelfDescription;
    private Integer teacherSuccessCount;
    private Integer teacherViewCount;
    private Integer historyDealCount;
    private Integer hireCount;
    private List<String> subjectNames = new ArrayList<>();
    private List<String> regionNames = new ArrayList<>();
    private List<TeacherSuccessRecordVO> successRecords = new ArrayList<>();
}
