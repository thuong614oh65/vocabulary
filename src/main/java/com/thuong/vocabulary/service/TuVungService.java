package com.thuong.vocabulary.service;

import com.thuong.vocabulary.dto.TuVungDTO;
import java.util.List;

public interface TuVungService {

    TuVungDTO traTu(String tu);

    List<TuVungDTO> traTuHangLoat(List<String> danhSach, List<String> loi);

}