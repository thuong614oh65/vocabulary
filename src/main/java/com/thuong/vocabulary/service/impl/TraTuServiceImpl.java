package com.thuong.vocabulary.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thuong.vocabulary.dto.dictionary.Definition;
import com.thuong.vocabulary.dto.dictionary.DictionaryResponse;
import com.thuong.vocabulary.dto.dictionary.Meaning;
import com.thuong.vocabulary.dto.dictionary.Phonetic;
import com.thuong.vocabulary.dto.tratu.*;
import com.thuong.vocabulary.entity.BoTuVung;
import com.thuong.vocabulary.entity.TaiKhoan;
import com.thuong.vocabulary.entity.TuVung;
import com.thuong.vocabulary.repository.BoTuVungRepository;
import com.thuong.vocabulary.repository.TaiKhoanRepository;
import com.thuong.vocabulary.repository.TuVungRepository;
import com.thuong.vocabulary.service.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class TraTuServiceImpl implements TraTuService {

    private final TranslateService translateService;
    private final PhienAmService phienAmService;
    private final DictionaryService dictionaryService;
    private final GeminiService geminiService;
    private final BoTuVungRepository boRepo;
    private final TuVungRepository tuRepo;
    private final TaiKhoanRepository taiKhoanRepo;
    private final ObjectMapper objectMapper;

    private static final Pattern VIETNAMESE_PATTERN = Pattern.compile(
            "[àáảãạăắằẳẵặâấầẩẫậđèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵ]",
            Pattern.CASE_INSENSITIVE
    );

    public TraTuServiceImpl(
            TranslateService translateService,
            PhienAmService phienAmService,
            DictionaryService dictionaryService,
            GeminiService geminiService,
            BoTuVungRepository boRepo,
            TuVungRepository tuRepo,
            TaiKhoanRepository taiKhoanRepo
    ) {
        this.translateService = translateService;
        this.phienAmService = phienAmService;
        this.dictionaryService = dictionaryService;
        this.geminiService = geminiService;
        this.boRepo = boRepo;
        this.tuRepo = tuRepo;
        this.taiKhoanRepo = taiKhoanRepo;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public TraTuResponse traTu(TraTuRequest request) {
        TraTuResponse response = new TraTuResponse();

        if (request == null || request.getText() == null || request.getText().isBlank()) {
            response.setThanhCong(false);
            response.setThongBaoLoi("Vui lòng nhập từ hoặc câu cần tra.");
            return response;
        }

        String rawText = request.getText().trim();
        response.setTuGoc(rawText);

        // Xác định chiều dịch
        String mode = request.getMode();
        if (mode == null || mode.isBlank() || "AUTO".equalsIgnoreCase(mode)) {
            if (laTiengViet(rawText)) {
                mode = "VI_EN";
            } else {
                mode = "EN_VI";
            }
        } else {
            mode = mode.toUpperCase();
        }

        response.setLoaiDich(mode);

        if ("VI_EN".equals(mode)) {
            xuLyDichVietAnh(rawText, response, request.isQuickMode());
        } else {
            xuLyDichAnhViet(rawText, response, request.isQuickMode());
        }

        return response;
    }

    private void xuLyDichAnhViet(String text, TraTuResponse response, boolean quickMode) {
        response.setNgonNguNguon("en");
        response.setNgonNguDich("vi");

        // 1. Dịch nhanh nghĩa chính qua Google GTX / MyMemory
        String banDich = translateService.dichAnhSangViet(text);
        response.setBanDich(banDich);

        // 2. Lấy phiên âm IPA
        String phienAm = phienAmService.layPhienAm(text);
        response.setPhienAm(phienAm);

        // 3. Tra cứu từ điển chuẩn Free Dictionary API (nếu là từ đơn hoặc cụm từ ngắn)
        if (!text.contains("\n") && text.length() <= 50) {
            try {
                DictionaryResponse dictRes = dictionaryService.traTu(text.replaceAll("[^a-zA-Z\\s\\-']", ""));
                if (dictRes != null) {
                    // Audio URL
                    if (dictRes.getPhonetics() != null) {
                        for (Phonetic p : dictRes.getPhonetics()) {
                            if (p.getAudio() != null && !p.getAudio().isBlank()) {
                                response.setAudioUrl(p.getAudio());
                                break;
                            }
                        }
                    }

                    // Meanings & Definitions
                    if (dictRes.getMeanings() != null) {
                        for (Meaning m : dictRes.getMeanings()) {
                            if (response.getTuLoai() == null && m.getPartOfSpeech() != null) {
                                response.setTuLoai(chuanHoaTuLoai(m.getPartOfSpeech()));
                            }

                            if (m.getDefinitions() != null) {
                                for (Definition d : m.getDefinitions()) {
                                    DinhNghiaItem item = new DinhNghiaItem();
                                    item.setPartOfSpeech(chuanHoaTuLoai(m.getPartOfSpeech()));
                                    item.setDefinitionEn(d.getDefinition());
                                    if (d.getExample() != null && !d.getExample().isBlank()) {
                                        item.getExamples().add(d.getExample());
                                        // Thêm vào danh sách ví dụ chính
                                        response.getViDu().add(new ViDuItem(d.getExample(), ""));
                                    }
                                    response.getDinhNghia().add(item);
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // 4. Bổ sung làm giàu bằng Gemini AI (Chỉ khi không phải dịch nhanh)
        if (!quickMode) {
            lamGiauDuLieuQuaGemini(text, "EN_VI", response);
        }
    }

    private void xuLyDichVietAnh(String text, TraTuResponse response, boolean quickMode) {
        response.setNgonNguNguon("vi");
        response.setNgonNguDich("en");

        // 1. Dịch nhanh nghĩa chính tiếng Anh qua Google GTX / MyMemory
        String banDich = translateService.dichVietSangAnh(text);
        response.setBanDich(banDich);

        // 2. Lấy phiên âm IPA cho từ/câu tiếng Anh dịch được
        if (banDich != null && !banDich.isBlank()) {
            String phienAm = phienAmService.layPhienAm(banDich);
            response.setPhienAm(phienAm);

            // Tra cứu audio của từ tiếng Anh dịch được
            if (!banDich.contains(" ") && banDich.length() <= 30) {
                try {
                    DictionaryResponse dictRes = dictionaryService.traTu(banDich.replaceAll("[^a-zA-Z\\-']", ""));
                    if (dictRes != null && dictRes.getPhonetics() != null) {
                        for (Phonetic p : dictRes.getPhonetics()) {
                            if (p.getAudio() != null && !p.getAudio().isBlank()) {
                                response.setAudioUrl(p.getAudio());
                                break;
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        // 3. Bổ sung làm giàu bằng Gemini AI
        if (!quickMode) {
            lamGiauDuLieuQuaGemini(text, "VI_EN", response);
        }
    }

    private void lamGiauDuLieuQuaGemini(String text, String mode, TraTuResponse response) {
        try {
            String aiJson = geminiService.traTuChuyenSau(text, mode);
            if (aiJson != null && !aiJson.isBlank()) {
                JsonNode root = objectMapper.readTree(aiJson);

                // Cập nhật bản dịch nếu bản dịch cũ trống hoặc bản dịch AI mượt hơn
                String aiBanDich = root.path("banDich").asText("");
                if (!aiBanDich.isBlank() && (response.getBanDich() == null || response.getBanDich().isBlank())) {
                    response.setBanDich(aiBanDich);
                }

                // Cập nhật phiên âm nếu chưa có
                String aiPhienAm = root.path("phienAm").asText("");
                if (!aiPhienAm.isBlank() && (response.getPhienAm() == null || response.getPhienAm().isBlank())) {
                    response.setPhienAm(aiPhienAm);
                }

                // Từ loại
                String aiTuLoai = root.path("tuLoai").asText("");
                if (!aiTuLoai.isBlank() && (response.getTuLoai() == null || response.getTuLoai().isBlank())) {
                    response.setTuLoai(chuanHoaTuLoai(aiTuLoai));
                }

                // Giải thích
                String aiGiaiThich = root.path("giaiThich").asText("");
                if (!aiGiaiThich.isBlank()) {
                    response.setGiaiThich(aiGiaiThich);
                }

                // Các nghĩa khác
                JsonNode arrNghiaKhac = root.path("cacNghiaKhac");
                if (arrNghiaKhac.isArray()) {
                    for (JsonNode n : arrNghiaKhac) {
                        String s = n.asText("").trim();
                        if (!s.isEmpty() && !response.getCacNghiaKhac().contains(s)) {
                            response.getCacNghiaKhac().add(s);
                        }
                    }
                }

                // Đồng nghĩa
                JsonNode arrDongNghia = root.path("dongNghia");
                if (arrDongNghia.isArray()) {
                    for (JsonNode n : arrDongNghia) {
                        String s = n.asText("").trim();
                        if (!s.isEmpty() && !response.getDongNghia().contains(s)) {
                            response.getDongNghia().add(s);
                        }
                    }
                }

                // Trái nghĩa
                JsonNode arrTraiNghia = root.path("traiNghia");
                if (arrTraiNghia.isArray()) {
                    for (JsonNode n : arrTraiNghia) {
                        String s = n.asText("").trim();
                        if (!s.isEmpty() && !response.getTraiNghia().contains(s)) {
                            response.getTraiNghia().add(s);
                        }
                    }
                }

                // Ví dụ câu
                JsonNode arrViDu = root.path("viDu");
                if (arrViDu.isArray() && arrViDu.size() > 0) {
                    List<ViDuItem> danhSachViDu = new ArrayList<>();
                    for (JsonNode item : arrViDu) {
                        String en = item.path("cauTiengAnh").asText("").trim();
                        String vi = item.path("cauTiengViet").asText("").trim();
                        if (!en.isEmpty()) {
                            danhSachViDu.add(new ViDuItem(en, vi));
                        }
                    }
                    if (!danhSachViDu.isEmpty()) {
                        response.setViDu(danhSachViDu);
                    }
                }

                // Định nghĩa
                JsonNode arrDinhNghia = root.path("dinhNghia");
                if (arrDinhNghia.isArray() && arrDinhNghia.size() > 0 && response.getDinhNghia().isEmpty()) {
                    for (JsonNode d : arrDinhNghia) {
                        DinhNghiaItem item = new DinhNghiaItem();
                        item.setPartOfSpeech(chuanHoaTuLoai(d.path("partOfSpeech").asText("")));
                        item.setDefinitionEn(d.path("definitionEn").asText(""));
                        item.setDefinitionVi(d.path("definitionVi").asText(""));
                        JsonNode exArr = d.path("examples");
                        if (exArr.isArray()) {
                            for (JsonNode ex : exArr) {
                                if (!ex.asText("").isBlank()) {
                                    item.getExamples().add(ex.asText("").trim());
                                }
                            }
                        }
                        response.getDinhNghia().add(item);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[TraTuService] Lỗi làm giàu dữ liệu Gemini: " + e.getMessage());
        }
    }

    private boolean laTiengViet(String text) {
        if (text == null) return false;
        return VIETNAMESE_PATTERN.matcher(text).find();
    }

    private String chuanHoaTuLoai(String raw) {
        if (raw == null || raw.isBlank()) return "Từ vựng";
        String r = raw.trim().toLowerCase();
        if (r.contains("noun") || r.contains("danh từ")) return "Danh từ (Noun)";
        if (r.contains("verb") || r.contains("động từ")) return "Động từ (Verb)";
        if (r.contains("adjective") || r.contains("adj") || r.contains("tính từ")) return "Tính từ (Adjective)";
        if (r.contains("adverb") || r.contains("adv") || r.contains("trạng từ")) return "Trạng từ (Adverb)";
        if (r.contains("preposition") || r.contains("giới từ")) return "Giới từ (Preposition)";
        if (r.contains("conjunction") || r.contains("liên từ")) return "Liên từ (Conjunction)";
        if (r.contains("idiom") || r.contains("thành ngữ")) return "Thành ngữ (Idiom)";
        if (r.contains("phrase") || r.contains("cụm từ")) return "Cụm từ (Phrase)";
        if (r.contains("sentence") || r.contains("câu")) return "Câu (Sentence)";
        return raw;
    }

    @Override
    public String luuTuNhanh(LuuTuNhanhRequest request, Long taiKhoanId) {
        if (taiKhoanId == null) {
            return "Chưa đăng nhập! Vui lòng đăng nhập để lưu từ.";
        }

        TaiKhoan taiKhoan = taiKhoanRepo.findById(taiKhoanId).orElse(null);
        if (taiKhoan == null) {
            return "Tài khoản không hợp lệ.";
        }

        if (request.getTiengAnh() == null || request.getTiengAnh().isBlank()) {
            return "Từ tiếng Anh không được để trống.";
        }

        String tuChuan = request.getTiengAnh().trim();

        // Kiểm tra từ đã tồn tại trong tài khoản chưa
        if (tuRepo.existsByTiengAnhIgnoreCaseAndBoTuVungTaiKhoanId(tuChuan, taiKhoanId)) {
            return "Từ \"" + tuChuan + "\" đã có trong danh sách từ vựng của bạn rồi.";
        }

        // Chọn hoặc tạo bộ từ
        BoTuVung bo = null;
        if (request.getBoId() != null && request.getBoId() > 0) {
            bo = boRepo.findById(request.getBoId()).orElse(null);
            if (bo != null && !bo.getTaiKhoan().getId().equals(taiKhoanId)) {
                bo = null; // Không phải của user này
            }
        }

        if (bo == null && request.getTenBoMoi() != null && !request.getTenBoMoi().isBlank()) {
            String tenMoi = request.getTenBoMoi().trim();
            bo = new BoTuVung();
            bo.setTenBo(tenMoi);
            bo.setNgayTao(LocalDateTime.now());
            bo.setTaiKhoan(taiKhoan);
            bo = boRepo.save(bo);
        }

        if (bo == null) {
            // Lấy bộ mới nhất hoặc tạo Bộ 1
            List<BoTuVung> dsBo = boRepo.findByTaiKhoanIdOrderByIdDesc(taiKhoanId);
            if (dsBo != null && !dsBo.isEmpty()) {
                bo = dsBo.get(0);
            } else {
                bo = new BoTuVung();
                bo.setTenBo("Bộ 1");
                bo.setNgayTao(LocalDateTime.now());
                bo.setTaiKhoan(taiKhoan);
                bo = boRepo.save(bo);
            }
        }

        // Lưu từ vựng
        TuVung tu = new TuVung();
        tu.setTiengAnh(tuChuan);
        tu.setTiengViet(request.getTiengViet() != null ? request.getTiengViet().trim() : "");
        tu.setPhienAm(request.getPhienAm() != null ? request.getPhienAm().trim() : "");
        tu.setViDu(request.getViDu() != null ? request.getViDu().trim() : "");
        tu.setBoTuVung(bo);

        tuRepo.save(tu);

        return "SUCCESS: Đã lưu \"" + tuChuan + "\" vào \"" + bo.getTenBo() + "\" thành công!";
    }
}
