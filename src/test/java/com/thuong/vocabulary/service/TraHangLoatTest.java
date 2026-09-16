package com.thuong.vocabulary.service;

import com.thuong.vocabulary.dto.TuVungDTO;
import com.thuong.vocabulary.service.impl.DanhSachTuServiceImpl;
import com.thuong.vocabulary.service.impl.DictionaryServiceImpl;
import com.thuong.vocabulary.service.impl.PhienAmServiceImpl;
import com.thuong.vocabulary.service.impl.TranslateServiceImpl;
import com.thuong.vocabulary.service.impl.TuVungServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class TraHangLoatTest {

    @Test
    void testTraHangLoat() {
        String input = """
                animal
                journeys
                Saiga
                antelopes
                live
                Central
                Asia
                spring
                they
                walk
                higher
                places
                for
                food
                male
                saiga
                can
                thirty-five
                kilometres
                a
                day
                and
                it's
                faster
                than
                female
                the
                journey
                is
                more
                dangerous
                because
                she
                has
                her
                calf
                Tree
                frogs
                have
                shorter
                journeys
                other
                animals
                But
                small
                frog
                isn't
                easier
                In
                it
                climbs
                metres
                down
                tree
                lays
                its
                eggs
                water
                then
                up
                very
                difficult
                Many
                turtles
                longer
                For
                example
                loggerhead
                turtle
                leaves
                beach
                as
                baby
                swims
                around
                fourteen
                thousand
                Fifteen
                years
                later
                returns
                same
                """;

        DanhSachTuService tachService = new DanhSachTuServiceImpl();
        List<String> words = tachService.tachDanhSach(input);
        System.out.println("Tach duoc " + words.size() + " tu.");

        RestTemplate restTemplate = new RestTemplate();
        DictionaryService dictService = new DictionaryServiceImpl(restTemplate);
        TranslateService transService = new TranslateServiceImpl();
        PhienAmService phienAmService = new PhienAmServiceImpl(dictService, restTemplate);

        TuVungService tuVungService = new TuVungServiceImpl(dictService, transService, phienAmService, null);

        List<String> loi = new ArrayList<>();
        long start = System.currentTimeMillis();
        List<TuVungDTO> ketQua = tuVungService.traTuHangLoat(words, loi);
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("Thoi gian tra: " + elapsed + " ms");
        System.out.println("Ket qua count: " + ketQua.size());
        System.out.println("Loi count: " + loi.size());

        int countCoNghia = 0;
        int countKhongNghia = 0;
        for (TuVungDTO dto : ketQua) {
            if (dto.getTiengViet() != null && !dto.getTiengViet().isBlank()) {
                countCoNghia++;
            } else {
                countKhongNghia++;
                System.out.println("KHONG CO NGHIA: " + dto.getTiengAnh());
            }
        }
        System.out.println("Tong co nghia: " + countCoNghia + " / " + ketQua.size());
        System.out.println("Tong khong co nghia: " + countKhongNghia + " / " + ketQua.size());
    }

    @Test
    void testAccountabilityPhanTich() {
        com.thuong.vocabulary.service.impl.HuongDanDocServiceImpl service = 
                new com.thuong.vocabulary.service.impl.HuongDanDocServiceImpl(null);

        // Case 1: IPA với /ʌ/
        com.thuong.vocabulary.dto.HuongDanDocDTO dto1 = 
                service.layHuongDanDoc("accountability", "/ʌkˈaʊntʌbɪlɪti/", "trách nhiệm giải trình");
        System.out.println("=== TEST ACCOUNTABILITY 1 (ʌ) ===");
        System.out.println("amTiet: " + dto1.getAmTiet());
        System.out.println("amTietIpa: " + dto1.getAmTietIpa());
        System.out.println("amTietBoi: " + dto1.getAmTietBoi());
        org.junit.jupiter.api.Assertions.assertEquals(List.of("ac", "coun", "ta", "bil", "i", "ty"), dto1.getAmTiet());
        org.junit.jupiter.api.Assertions.assertEquals(List.of("ʌ", "kˈaʊn", "tʌ", "ˈbɪl", "ɪ", "ti"), dto1.getAmTietIpa());
        org.junit.jupiter.api.Assertions.assertEquals(List.of("ờ", "cao-n", "tờ", "BÍL", "i", "ti"), dto1.getAmTietBoi());

        // Case 2: IPA Cambridge với /ə/
        com.thuong.vocabulary.dto.HuongDanDocDTO dto2 = 
                service.layHuongDanDoc("accountability", "/əˌkaʊn.təˈbɪl.ə.ti/", "trách nhiệm giải trình");
        System.out.println("=== TEST ACCOUNTABILITY 2 (ə) ===");
        System.out.println("amTiet: " + dto2.getAmTiet());
        System.out.println("amTietIpa: " + dto2.getAmTietIpa());
        System.out.println("amTietBoi: " + dto2.getAmTietBoi());
        org.junit.jupiter.api.Assertions.assertEquals(List.of("ac", "coun", "ta", "bil", "i", "ty"), dto2.getAmTiet());
        org.junit.jupiter.api.Assertions.assertEquals(List.of("ə", "ˌkaʊn", "tə", "ˈbɪl", "ɪ", "ti"), dto2.getAmTietIpa());
        org.junit.jupiter.api.Assertions.assertEquals(List.of("ờ", "cao-n", "tờ", "BÍL", "i", "ti"), dto2.getAmTietBoi());
    }
}
