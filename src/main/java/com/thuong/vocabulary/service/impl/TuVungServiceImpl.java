package com.thuong.vocabulary.service.impl;

import com.thuong.vocabulary.dto.TuVungDTO;
import com.thuong.vocabulary.dto.dictionary.Definition;
import com.thuong.vocabulary.dto.dictionary.DictionaryResponse;
import com.thuong.vocabulary.dto.dictionary.Meaning;
import com.thuong.vocabulary.dto.dictionary.Phonetic;
import com.thuong.vocabulary.service.DictionaryService;
import com.thuong.vocabulary.service.PhienAmService;
import com.thuong.vocabulary.service.TranslateService;
import com.thuong.vocabulary.service.TuVungService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TuVungServiceImpl implements TuVungService {

    private final DictionaryService dictionaryService;
    private final TranslateService translateService;
    private final PhienAmService phienAmService;

    // Bộ nhớ cache nhanh lưu kết quả tra từ hoàn chỉnh trong RAM (0ms cho các lần sau)
    private final Map<String, TuVungDTO> tuVungCache = new ConcurrentHashMap<>();

    // Executor sử dụng Virtual Threads (Java 21) siêu nhẹ, xử lý đồng thời hàng chục request mà không nghẽn luồng
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public TuVungServiceImpl(
            DictionaryService dictionaryService,
            TranslateService translateService,
            PhienAmService phienAmService
    ) {
        this.dictionaryService = dictionaryService;
        this.translateService = translateService;
        this.phienAmService = phienAmService;
    }

    @Override
    public TuVungDTO traTu(String tu) {
        if (tu == null || tu.isBlank()) {
            return null;
        }

        String tuChuanHoa = tu.trim();
        String tuLower = tuChuanHoa.toLowerCase();

        if (tuVungCache.containsKey(tuLower)) {
            return saoChepTuVung(tuVungCache.get(tuLower));
        }

        // TỐI ƯU TỐC ĐỘ: Chạy song song Dịch nghĩa và Tra từ điển cùng 1 lúc (tiết kiệm 50% thời gian)
        CompletableFuture<String> dichFuture = CompletableFuture.supplyAsync(
                () -> translateService.dich(tuChuanHoa),
                executor
        );

        CompletableFuture<DictionaryResponse> dictFuture = CompletableFuture.supplyAsync(
                () -> dictionaryService.traTu(tuChuanHoa),
                executor
        );

        try {
            CompletableFuture.allOf(dichFuture, dictFuture).join();

            String nghia = dichFuture.get();
            DictionaryResponse response = dictFuture.get();

            TuVungDTO dto = new TuVungDTO();
            dto.setTiengAnh(tuChuanHoa);
            dto.setTiengViet(chuanHoaNghia(nghia, tuChuanHoa));

            String phienAm = null;
            String viDu = null;

            if (response != null) {
                if (response.getWord() != null && !response.getWord().isBlank()) {
                    dto.setTiengAnh(response.getWord());
                }

                // Trích xuất phiên âm từ response (tránh gọi trùng API FreeDict lần 2)
                phienAm = trichXuatPhienAm(response);

                // Trích xuất câu ví dụ
                viDu = trichXuatViDu(response);
            }

            // Nếu từ điển chưa có phiên âm, dùng phienAmService để tra tiếp (Datamuse/ghép âm)
            if (phienAm == null || phienAm.isBlank()) {
                phienAm = phienAmService.layPhienAm(tuChuanHoa);
            }

            dto.setPhienAm(phienAm != null ? phienAm : "");
            dto.setViDu(viDu != null ? viDu : "");

            tuVungCache.put(tuLower, dto);
            return dto;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<TuVungDTO> traTuHangLoat(List<String> danhSach, List<String> loi) {
        if (danhSach == null || danhSach.isEmpty()) {
            return Collections.emptyList();
        }

        // BƯỚC 1: DỊCH HÀNG LOẠT SIÊU TỐC: Dịch tất cả các từ trong 1 hoặc 2 request duy nhất (~200ms)
        Map<String, String> banDichMap = translateService.dichHangLoat(danhSach);

        // BƯỚC 2: TRA CỨU TỪ ĐIỂN VÀ PHIÊN ÂM BẤT ĐỒNG BỘ ĐA LUỒNG (PARALLEL EXECUTION)
        List<CompletableFuture<TuVungKetQua>> futures = new ArrayList<>();

        for (int i = 0; i < danhSach.size(); i++) {
            final int index = i;
            final String tuGoc = danhSach.get(i).trim();
            final String tuLower = tuGoc.toLowerCase();

            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    // Kiểm tra cache trước
                    if (tuVungCache.containsKey(tuLower)) {
                        return new TuVungKetQua(index, tuGoc, saoChepTuVung(tuVungCache.get(tuLower)));
                    }

                    TuVungDTO dto = new TuVungDTO();
                    dto.setTiengAnh(tuGoc);

                    // Lấy nghĩa từ bảng dịch hàng loạt đã chuẩn bị ở Bước 1
                    String nghia = banDichMap.get(tuLower);
                    if (nghia == null || nghia.isBlank()) {
                        nghia = translateService.dich(tuGoc);
                    }
                    dto.setTiengViet(chuanHoaNghia(nghia, tuGoc));

                    // Tra từ điển lấy phiên âm và ví dụ trong 1 lần gọi
                    DictionaryResponse response = dictionaryService.traTu(tuGoc);
                    String phienAm = null;
                    String viDu = null;

                    if (response != null) {
                        if (response.getWord() != null && !response.getWord().isBlank()) {
                            dto.setTiengAnh(response.getWord());
                        }

                        phienAm = trichXuatPhienAm(response);
                        viDu = trichXuatViDu(response);
                    }

                    // Nếu chưa có phiên âm, tra cứu qua phienAmService
                    if (phienAm == null || phienAm.isBlank()) {
                        phienAm = phienAmService.layPhienAm(tuGoc);
                    }

                    dto.setPhienAm(phienAm != null ? phienAm : "");
                    dto.setViDu(viDu != null ? viDu : "");

                    tuVungCache.put(tuLower, dto);
                    return new TuVungKetQua(index, tuGoc, dto);

                } catch (Exception ex) {
                    return new TuVungKetQua(index, tuGoc, null);
                }
            }, executor));
        }

        // Chờ tất cả các từ hoàn tất đồng thời
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // BƯỚC 3: THU THẬP KẾT QUẢ ĐÚNG NGUYÊN VẸN THEO THỨ TỰ BAN ĐẦU
        List<TuVungDTO> ketQua = new ArrayList<>();
        for (CompletableFuture<TuVungKetQua> future : futures) {
            try {
                TuVungKetQua kq = future.join();
                if (kq.dto != null) {
                    ketQua.add(kq.dto);
                } else {
                    if (loi != null) loi.add(kq.tuGoc);
                }
            } catch (Exception ex) {
                // Ignore failure for single future
            }
        }

        return ketQua;
    }

    private String trichXuatPhienAm(DictionaryResponse response) {
        if (response == null) return null;
        if (response.getPhonetics() != null) {
            for (Phonetic p : response.getPhonetics()) {
                if (p.getText() != null && !p.getText().isBlank()) {
                    return p.getText();
                }
            }
        }
        return null;
    }

    private String trichXuatViDu(DictionaryResponse response) {
        if (response == null || response.getMeanings() == null) return null;
        for (Meaning m : response.getMeanings()) {
            if (m.getDefinitions() != null) {
                for (Definition d : m.getDefinitions()) {
                    if (d.getExample() != null && !d.getExample().isBlank()) {
                        return d.getExample();
                    }
                }
            }
        }
        return null;
    }

    private String chuanHoaNghia(String nghia, String tu) {
        if (nghia == null || nghia.isBlank()) {
            return "";
        }
        nghia = nghia.trim();
        return nghia.substring(0, 1).toUpperCase() + nghia.substring(1);
    }

    private TuVungDTO saoChepTuVung(TuVungDTO nguon) {
        if (nguon == null) return null;
        TuVungDTO copy = new TuVungDTO();
        copy.setTiengAnh(nguon.getTiengAnh());
        copy.setTiengViet(nguon.getTiengViet());
        copy.setPhienAm(nguon.getPhienAm());
        copy.setViDu(nguon.getViDu());
        return copy;
    }

    private static class TuVungKetQua {
        int index;
        String tuGoc;
        TuVungDTO dto;

        TuVungKetQua(int index, String tuGoc, TuVungDTO dto) {
            this.index = index;
            this.tuGoc = tuGoc;
            this.dto = dto;
        }
    }
}