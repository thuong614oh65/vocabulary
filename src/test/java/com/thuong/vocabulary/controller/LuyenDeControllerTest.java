package com.thuong.vocabulary.controller;

import com.thuong.vocabulary.dto.luyende.DeThiQ79DTO;
import com.thuong.vocabulary.entity.TaiKhoan;
import com.thuong.vocabulary.service.GeminiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.*;

class LuyenDeControllerTest {

    private LuyenDeController controller;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        GeminiService geminiService = Mockito.mock(GeminiService.class);
        controller = new LuyenDeController(geminiService);

        session = new MockHttpSession();
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setTenDangNhap("testuser");
        session.setAttribute("taiKhoan", taiKhoan);
    }

    @Test
    void testDanvilleCityToursPreset_Id19() {
        ResponseEntity<?> res = controller.layDeMau(19, session);
        assertEquals(200, res.getStatusCode().value());
        assertTrue(res.getBody() instanceof DeThiQ79DTO);
        DeThiQ79DTO dto = (DeThiQ79DTO) res.getBody();

        assertEquals("[Sample Exam] Danville City Tours", dto.getTieuDe());
        assertEquals("Can you tell me how much it costs to take the tour?", dto.getCauHoi1());
        assertEquals("I heard that the tour includes dinner as well as lunch. Is that correct?", dto.getCauHoi2());
        assertEquals("Does the tour take place mostly in the morning, or will we also visit some places after lunch?", dto.getCauHoi3());
        assertNotNull(dto.getGoiYCau1());
        assertNotNull(dto.getGoiYCau2());
        assertNotNull(dto.getGoiYCau3());
    }

    @Test
    void testDrillingSitePreset_Id16() {
        ResponseEntity<?> res = controller.layDeMau(16, session);
        assertEquals(200, res.getStatusCode().value());
        DeThiQ79DTO dto = (DeThiQ79DTO) res.getBody();

        assertEquals("[Text 1] Drilling Site Tour Schedule", dto.getTieuDe());
        assertEquals("What time do we need to meet for the tour?", dto.getCauHoi1());
        assertEquals("I heard that tours are also available on weekends. Is that correct?", dto.getCauHoi2());
        assertEquals("Could you please tell me how long the tour lasts and what we will see during the tour?", dto.getCauHoi3());
    }

    @Test
    void testInternationalWritersConference_Id17() {
        ResponseEntity<?> res = controller.layDeMau(17, session);
        assertEquals(200, res.getStatusCode().value());
        DeThiQ79DTO dto = (DeThiQ79DTO) res.getBody();

        assertEquals("[Text 2] International Writers Conference", dto.getTieuDe());
        assertEquals("Can you please tell me who Angela Moeller is and where she will be speaking?", dto.getCauHoi1());
    }

    @Test
    void testPalmIslandPreset_Id15() {
        ResponseEntity<?> res = controller.layDeMau(15, session);
        assertEquals(200, res.getStatusCode().value());
        DeThiQ79DTO dto = (DeThiQ79DTO) res.getBody();

        assertEquals("Palm Island's New Employee Orientation", dto.getTieuDe());
        assertEquals("What time does the new employee orientation begin, and what is scheduled at that time?", dto.getCauHoi1());
        assertEquals("Who will give the sessions on employee benefits and the resort tour?", dto.getCauHoi2());
        assertEquals("I'm interested in the demonstration of resort security procedures. Could you tell me when it is scheduled?", dto.getCauHoi3());
    }

    @Test
    void testRockFestivalPreset_Id13() {
        ResponseEntity<?> res = controller.layDeMau(13, session);
        assertEquals(200, res.getStatusCode().value());
        DeThiQ79DTO dto = (DeThiQ79DTO) res.getBody();

        assertEquals("High Elevation Rock Festival Tours", dto.getTieuDe());
        assertEquals("When and where will the High Elevation Rock Festival Tour take place in Boston?", dto.getCauHoi1());
        assertEquals("I'd like to attend the tour in New York City. Could you tell me which dates are available and how much the tickets cost?", dto.getCauHoi2());
        assertEquals("I'm planning to buy a ticket for the Boston tour. How much does it cost, and is there any way I can get a discount?", dto.getCauHoi3());
    }

    @Test
    void testUnauthorized() {
        MockHttpSession unauthSession = new MockHttpSession();
        ResponseEntity<?> res = controller.layDeMau(19, unauthSession);
        assertEquals(401, res.getStatusCode().value());
    }
}
