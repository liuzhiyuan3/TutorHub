package com.teacher.server.controller;

import com.teacher.common.model.PageResult;
import com.teacher.pojo.vo.PublicTeacherListItemVO;
import com.teacher.pojo.vo.TeacherPublicDetailVO;
import com.teacher.server.service.HomeQueryService;
import com.teacher.server.service.TeacherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicTeacherMetricsApiTest {
    private HomeQueryService homeQueryService;
    private TeacherService teacherService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        homeQueryService = Mockito.mock(HomeQueryService.class);
        teacherService = Mockito.mock(TeacherService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new HomeController(homeQueryService),
                new TeacherController(teacherService))
                .build();
    }

    @Test
    void teachersSearch_shouldReturnNewMetricsFields() throws Exception {
        PublicTeacherListItemVO item = new PublicTeacherListItemVO();
        item.setTeacherId("t001");
        item.setHistoryDealCount(4);
        item.setHireCount(7);
        PageResult<PublicTeacherListItemVO> page = new PageResult<>(1, 1, 10, Collections.singletonList(item));
        Mockito.when(homeQueryService.teacherSearch(
                        Mockito.anyLong(), Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/home/teachers/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].teacherId").value("t001"))
                .andExpect(jsonPath("$.data.records[0].historyDealCount").value(4))
                .andExpect(jsonPath("$.data.records[0].hireCount").value(7));
    }

    @Test
    void teacherPublicDetail_shouldReturnNewMetricsFields() throws Exception {
        TeacherPublicDetailVO detail = new TeacherPublicDetailVO();
        detail.setTeacherId("t001");
        detail.setHistoryDealCount(3);
        detail.setHireCount(5);
        Mockito.when(teacherService.publicDetail("t001")).thenReturn(detail);

        mockMvc.perform(get("/api/teacher/public/t001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.teacherId").value("t001"))
                .andExpect(jsonPath("$.data.historyDealCount").value(3))
                .andExpect(jsonPath("$.data.hireCount").value(5));
    }
}
