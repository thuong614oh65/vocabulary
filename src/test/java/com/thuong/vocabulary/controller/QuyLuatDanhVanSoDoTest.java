package com.thuong.vocabulary.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @Test
    void testHuongDanDocComedy() throws Exception {
        var res = mockMvc.perform(get("/api/huong-dan-doc").param("tu", "comedy").param("phienAm", "/'kɑmʌdi/").param("nghia", "Hài kịch"))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println("=== API HUONG DAN DOC COMEDY ===");
        System.out.println(res.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
        System.out.println("================================");
    }

    @Test
    void testAlphabetAudioResource() throws Exception {
        mockMvc.perform(get("/audio/alphabet/c.mp3"))
                .andExpect(status().isOk());
    }

    @Test
    void testHuongDanDocMultipleWords() throws Exception {
        String[] words = {"action", "beautiful", "important", "computer", "table"};
        for (String w : words) {
            mockMvc.perform(get("/api/huong-dan-doc").param("tu", w))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tu").value(w))
                    .andExpect(jsonPath("$.phienAmTiengViet").isNotEmpty())
                    .andExpect(jsonPath("$.amTiet").isArray())
                    .andExpect(jsonPath("$.amTietBoi").isArray())
                    .andExpect(jsonPath("$.amTietDoc").isArray());
        }
    }

    @Test
    void testHuongDanDocRegistration_Chuan4AmTiet() throws Exception {
        mockMvc.perform(get("/api/huong-dan-doc").param("tu", "registration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tu").value("registration"))
                .andExpect(jsonPath("$.amTiet.length()").value(4))
                .andExpect(jsonPath("$.amTiet[0]").value("re"))
                .andExpect(jsonPath("$.amTiet[1]").value("gis"))
                .andExpect(jsonPath("$.amTiet[2]").value("tra"))
                .andExpect(jsonPath("$.amTiet[3]").value("tion"))
                .andExpect(jsonPath("$.amTietIpa[0]").value("rɛ"))
                .andExpect(jsonPath("$.amTietIpa[1]").value("dʒɪs"))
                .andExpect(jsonPath("$.amTietIpa[2]").value("trˈeɪ"))
                .andExpect(jsonPath("$.amTietIpa[3]").value("ʃʌn"))
                .andExpect(jsonPath("$.amTietBoi[0]").value("re"))
                .andExpect(jsonPath("$.amTietBoi[1]").value("dít"))
                .andExpect(jsonPath("$.amTietBoi[2]").value("TRÂY"))
                .andExpect(jsonPath("$.amTietBoi[3]").value("sần"))
                .andExpect(jsonPath("$.amTietDoc[0]").value("reh"))
                .andExpect(jsonPath("$.amTietDoc[1]").value("jis"))
                .andExpect(jsonPath("$.amTietDoc[2]").value("tray"))
                .andExpect(jsonPath("$.amTietDoc[3]").value("shun"))
                .andExpect(jsonPath("$.amNhanIndex").value(2))
                .andExpect(jsonPath("$.maQuyTacLienKet").value("soft_g"));
    }

    @Test
    void testHuongDanDocLunch_Chuan2AmTiet() throws Exception {
        mockMvc.perform(get("/api/huong-dan-doc").param("tu", "lunch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tu").value("lunch"))
                .andExpect(jsonPath("$.amTiet.length()").value(2))
                .andExpect(jsonPath("$.amTiet[0]").value("lun"))
                .andExpect(jsonPath("$.amTiet[1]").value("ch"))
                .andExpect(jsonPath("$.amTietIpa[0]").value("lˈʌn"))
                .andExpect(jsonPath("$.amTietIpa[1]").value("tʃ"))
                .andExpect(jsonPath("$.amTietBoi[0]").value("LĂN"))
                .andExpect(jsonPath("$.amTietBoi[1]").value("ch"))
                .andExpect(jsonPath("$.maQuyTacLienKet").value("ch_sound"));
    }

    @Test
    void testHuongDanDocWorkshop_Chuan2AmTiet() throws Exception {
        mockMvc.perform(get("/api/huong-dan-doc").param("tu", "workshop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tu").value("workshop"))
                .andExpect(jsonPath("$.amTiet.length()").value(2))
                .andExpect(jsonPath("$.amTiet[0]").value("work"))
                .andExpect(jsonPath("$.amTiet[1]").value("shop"))
                .andExpect(jsonPath("$.amTietBoi[0]").value("QUỚC"))
                .andExpect(jsonPath("$.amTietBoi[1]").value("shop"))
                .andExpect(jsonPath("$.maQuyTacLienKet").value("w_or"));
    }

    @Test
    void testHuongDanDocIncluded_Chuan3AmTiet() throws Exception {
        mockMvc.perform(get("/api/huong-dan-doc").param("tu", "included"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tu").value("included"))
                .andExpect(jsonPath("$.amTiet.length()").value(3))
                .andExpect(jsonPath("$.amTiet[0]").value("in"))
                .andExpect(jsonPath("$.amTiet[1]").value("clu"))
                .andExpect(jsonPath("$.amTiet[2]").value("ded"));
    }

    @Test
    void testHuongDanDocFee_Chuan1AmTiet() throws Exception {
        mockMvc.perform(get("/api/huong-dan-doc").param("tu", "fee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tu").value("fee"))
                .andExpect(jsonPath("$.amTiet.length()").value(1))
                .andExpect(jsonPath("$.amTiet[0]").value("fee"))
                .andExpect(jsonPath("$.maQuyTacLienKet").value("ea_ee"));
    }

    @Test
    void testSoDoSoftG_Va_TionSion() throws Exception {
        mockMvc.perform(get("/so-do-danh-van").param("id", "soft_g"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("soft_g")));

        mockMvc.perform(get("/so-do-danh-van").param("id", "tion_sion"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("tion_sion")));
    }
}