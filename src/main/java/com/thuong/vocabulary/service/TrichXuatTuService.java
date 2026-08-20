package com.thuong.vocabulary.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface TrichXuatTuService {

    List<String> trichXuatTu(MultipartFile file) throws Exception;

}
