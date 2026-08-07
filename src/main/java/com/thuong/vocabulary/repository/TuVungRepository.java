package com.thuong.vocabulary.repository;


import com.thuong.vocabulary.entity.TuVung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


public interface TuVungRepository
        extends JpaRepository<TuVung,Long> {

    boolean existsByTiengAnhIgnoreCase(String tiengAnh);

    List<TuVung> findAllByOrderByBoTuVungIdDescIdAsc();

    List<TuVung> findAllByBoTuVungId(Long boId);

    @Modifying
    @Transactional
    @Query("""
UPDATE TuVung t
SET t.soLanSai = COALESCE(t.soLanSai,0) + 1
WHERE t.id = :id
""")
    void tangSoLanSai(Long id);

    List<TuVung> findBySoLanSaiGreaterThanOrderBySoLanSaiDesc(Integer soLanSai);

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
""")
    void giamSoLanSai(Long id);
}