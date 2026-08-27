package com.thuong.vocabulary.repository;

import com.thuong.vocabulary.entity.TuVung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TuVungRepository extends JpaRepository<TuVung, Long> {

    // =========================================================
    // KIỂM TRA TỪ ĐÃ TỒN TẠI TRONG TÀI KHOẢN HAY CHƯA
    // =========================================================

    boolean existsByTiengAnhIgnoreCaseAndBoTuVungTaiKhoanId(
            String tiengAnh,
            Long taiKhoanId
    );


    // =========================================================
    // LẤY TẤT CẢ TỪ CỦA MỘT TÀI KHOẢN
    // =========================================================

    List<TuVung> findAllByBoTuVungTaiKhoanIdOrderByBoTuVungIdDescIdAsc(
            Long taiKhoanId
    );


    // =========================================================
    // LẤY TỪ THEO BỘ + TÀI KHOẢN
    // =========================================================

    List<TuVung> findAllByBoTuVungIdAndBoTuVungTaiKhoanIdOrderByIdAsc(
            Long boId,
            Long taiKhoanId
    );

    default List<TuVung> findAllByBoTuVungIdAndBoTuVungTaiKhoanId(
            Long boId,
            Long taiKhoanId
    ) {
        return findAllByBoTuVungIdAndBoTuVungTaiKhoanIdOrderByIdAsc(boId, taiKhoanId);
    }

    // =========================================================
    // LẤY TỪ THEO DANH SÁCH NHIỀU BỘ + TÀI KHOẢN
    // =========================================================

    List<TuVung> findAllByBoTuVungIdInAndBoTuVungTaiKhoanIdOrderByIdAsc(
            List<Long> boIds,
            Long taiKhoanId
    );


    // =========================================================
    // LẤY TẤT CẢ TỪ CỦA TÀI KHOẢN
    // =========================================================

    List<TuVung> findAllByBoTuVungTaiKhoanIdOrderByIdAsc(
            Long taiKhoanId
    );

    default List<TuVung> findAllByBoTuVungTaiKhoanId(
            Long taiKhoanId
    ) {
        return findAllByBoTuVungTaiKhoanIdOrderByIdAsc(taiKhoanId);
    }


    // =========================================================
    // TĂNG SỐ LẦN SAI
    // Chỉ được tăng nếu từ thuộc tài khoản đang đăng nhập
    // =========================================================

    @Modifying
    @Transactional
    @Query("""
        UPDATE TuVung t
        SET t.soLanSai = COALESCE(t.soLanSai, 0) + 1
        WHERE t.id = :id
        AND t.boTuVung.taiKhoan.id = :taiKhoanId
    """)
    int tangSoLanSai(
            @Param("id") Long id,
            @Param("taiKhoanId") Long taiKhoanId
    );


    // =========================================================
    // LẤY TỪ SAI CỦA TÀI KHOẢN
    // =========================================================

    List<TuVung>
    findBySoLanSaiGreaterThanAndBoTuVungTaiKhoanIdOrderBySoLanSaiDesc(
            Integer soLanSai,
            Long taiKhoanId
    );


    // =========================================================
    // GIẢM SỐ LẦN SAI
    // Chỉ được giảm nếu từ thuộc tài khoản đang đăng nhập
    // =========================================================

    @Modifying
    @Transactional
    @Query("""
        UPDATE TuVung t
        SET t.soLanSai =
            CASE
                WHEN t.soLanSai > 0 THEN t.soLanSai - 1
                ELSE 0
            END
        WHERE t.id = :id
        AND t.boTuVung.taiKhoan.id = :taiKhoanId
    """)
    int giamSoLanSai(
            @Param("id") Long id,
            @Param("taiKhoanId") Long taiKhoanId
    );
}