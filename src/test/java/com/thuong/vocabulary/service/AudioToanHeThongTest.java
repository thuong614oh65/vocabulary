package com.thuong.vocabulary.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AudioToanHeThongTest {

    @Autowired
    private AudioService audioService;

    @Test
    void testAudioDoanVanVaCacheTamThoi() {
        String doanVan = "Good morning, this is a temporary test phrase for audio system.";
        
        long start1 = System.currentTimeMillis();
        AudioService.AudioResult result1 = audioService.phatAudioToanHeThong(doanVan, "+0%");
        long dur1 = System.currentTimeMillis() - start1;

        assertNotNull(result1, "Result 1 không được null");
        assertNotNull(result1.getData(), "Data audio không được null");
        assertTrue(result1.getData().length > 0, "Data audio phải có byte");
        assertFalse(result1.isPermanent(), "Đoạn văn ngoài CSDL phải là temporary (isPermanent = false)");

        System.out.println("Lần 1 tạo audio mất: " + dur1 + " ms, dung lượng: " + result1.getData().length + " bytes");

        // Lần 2: Phải lấy từ RAM cache ngay lập tức (< 10ms)
        long start2 = System.currentTimeMillis();
        AudioService.AudioResult result2 = audioService.phatAudioToanHeThong(doanVan, "+0%");
        long dur2 = System.currentTimeMillis() - start2;

        System.out.println("Lần 2 lấy từ RAM cache mất: " + dur2 + " ms");
        assertNotNull(result2);
        assertEquals(result1.getData().length, result2.getData().length, "Dung lượng dữ liệu cache phải trùng khớp");
        assertTrue(dur2 < 50, "Lần 2 phải lấy từ RAM cache siêu tốc (< 50ms, thực tế: " + dur2 + "ms)");
    }

    @Test
    void testAudioTuVungCSDL() {
        // Kiểm tra từ đơn
        String tu = "hello";
        AudioService.AudioResult result = audioService.phatAudioToanHeThong(tu, "+0%");
        assertNotNull(result);
        assertNotNull(result.getData());
        assertTrue(result.getData().length > 0);
        System.out.println("Kết quả đọc từ '" + tu + "': isPermanent=" + result.isPermanent() + ", dung lượng=" + result.getData().length + " bytes");
    }
}
