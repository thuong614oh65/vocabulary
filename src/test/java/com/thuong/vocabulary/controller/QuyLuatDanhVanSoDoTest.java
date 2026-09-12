package com.thuong.vocabulary.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class QuyLuatDanhVanSoDoTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testSoDoDefault_ShouldOpenCategoryHub() throws Exception {
        mockMvc.perform(get("/quy-luat-danh-van/so-do"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("viewCategoryHub")))
                .andExpect(content().string(containsString("viewSoundListArena")))
                .andExpect(content().string(containsString("viewMindmapArena")));
    }

    @Test
    void testSoDoWithCategory_ShouldPassCatHienTai() throws Exception {
        mockMvc.perform(get("/quy-luat-danh-van/so-do").param("cat", "NGUYEN_AM_DAC_BIET"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("NGUYEN_AM_DAC_BIET")));
    }

    @Test
    void testSoDoWithRuleId_ShouldPassIdHienTai() throws Exception {
        mockMvc.perform(get("/quy-luat-danh-van/so-do").param("id", "w_or"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("w_or")));
    }
}