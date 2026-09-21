package com.thuong.vocabulary.service;

import com.thuong.vocabulary.dto.HuongDanDocDTO;
import com.thuong.vocabulary.service.impl.HuongDanDocServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HuongDanDocTest {

    private final HuongDanDocServiceImpl service = new HuongDanDocServiceImpl(null);

    @Test
    void testPostpone() {
        HuongDanDocDTO dto = service.layHuongDanDoc("postpone", "/poʊstˈpoʊn/", "trì hoãn");
        assertNotNull(dto);
        System.out.println("=== TEST POSTPONE ===");
        System.out.println("Word: " + dto.getTu());
        System.out.println("IPA: " + dto.getPhienAm());
        System.out.println("Vietnamese Respelling: " + dto.getPhienAmTiengViet());
        System.out.println("Syllables: " + dto.getAmTiet());
        System.out.println("Syllable IPAs: " + dto.getAmTietIpa());
        System.out.println("Syllable Bồi: " + dto.getAmTietBoi());
        System.out.println("Syllable TTS: " + dto.getAmTietDoc());
        System.out.println("Stress index: " + dto.getAmNhanIndex());

        // Syllables must be post and pone
        assertEquals(2, dto.getAmTiet().size(), "Must have 2 syllables");
        assertEquals("post", dto.getAmTiet().get(0).toLowerCase());
        assertEquals("pone", dto.getAmTiet().get(1).toLowerCase());

        // Stress must be syllable 2 (index 1)
        assertEquals(1, dto.getAmNhanIndex(), "Stress must be syllable 2 (pone)");

        // Syllables must NOT be postpo and ne!
        assertNotEquals("postpo", dto.getAmTiet().get(0).toLowerCase());
        assertNotEquals("ne", dto.getAmTiet().get(1).toLowerCase());
    }

    @Test
    void testReschedule() {
        HuongDanDocDTO dto = service.layHuongDanDoc("reschedule", "/riːˈskɛdʒuːl/", "sắp xếp lại lịch");
        assertNotNull(dto);
        System.out.println("=== TEST RESCHEDULE ===");
        System.out.println("Word: " + dto.getTu());
        System.out.println("IPA: " + dto.getPhienAm());
        System.out.println("Vietnamese Respelling: " + dto.getPhienAmTiengViet());
        System.out.println("Syllables: " + dto.getAmTiet());
        System.out.println("Syllable IPAs: " + dto.getAmTietIpa());
        System.out.println("Syllable Bồi: " + dto.getAmTietBoi());
        System.out.println("Syllable TTS: " + dto.getAmTietDoc());
        System.out.println("Stress index: " + dto.getAmNhanIndex());

        // Syllables must be re, sched, ule
        assertEquals(3, dto.getAmTiet().size(), "Must have 3 syllables");
        assertEquals("re", dto.getAmTiet().get(0).toLowerCase());
        assertEquals("sched", dto.getAmTiet().get(1).toLowerCase());
        assertEquals("ule", dto.getAmTiet().get(2).toLowerCase());

        // Stress must be syllable 2 (index 1)
        assertEquals(1, dto.getAmNhanIndex(), "Stress must be syllable 2 (sched)");

        // Must NOT be resche, du, le!
        assertNotEquals("resche", dto.getAmTiet().get(0).toLowerCase());
    }

    @Test
    void testToeicSpeakingWords() {
        HuongDanDocDTO cancel = service.layHuongDanDoc("cancel", "/ˈkænsəl/", "hủy");
        assertEquals(2, cancel.getAmTiet().size());
        assertEquals("can", cancel.getAmTiet().get(0));
        assertEquals("cel", cancel.getAmTiet().get(1));

        HuongDanDocDTO delay = service.layHuongDanDoc("delay", "/dɪˈleɪ/", "hoãn");
        assertEquals(2, delay.getAmTiet().size());
        assertEquals("de", delay.getAmTiet().get(0));
        assertEquals("lay", delay.getAmTiet().get(1));

        HuongDanDocDTO pres = service.layHuongDanDoc("presentation", "/ˌprɛzənˈteɪʃən/", "thuyết trình");
        assertEquals(4, pres.getAmTiet().size());
        assertEquals("pre", pres.getAmTiet().get(0));
        assertEquals("sen", pres.getAmTiet().get(1));
        assertEquals("ta", pres.getAmTiet().get(2));
        assertEquals("tion", pres.getAmTiet().get(3));

        HuongDanDocDTO avail = service.layHuongDanDoc("available", "/əˈveɪləbəl/", "có sẵn");
        assertEquals(4, avail.getAmTiet().size());
        assertEquals("a", avail.getAmTiet().get(0));
        assertEquals("vail", avail.getAmTiet().get(1));
        assertEquals("a", avail.getAmTiet().get(2));
        assertEquals("ble", avail.getAmTiet().get(3));
    }
}
