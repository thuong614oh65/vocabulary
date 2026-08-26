package com.thuong.vocabulary.service;

import com.thuong.vocabulary.dto.tratu.LuuTuNhanhRequest;
import com.thuong.vocabulary.dto.tratu.TraTuRequest;
import com.thuong.vocabulary.dto.tratu.TraTuResponse;

public interface TraTuService {

    TraTuResponse traTu(TraTuRequest request);

    String luuTuNhanh(LuuTuNhanhRequest request, Long taiKhoanId);

}
