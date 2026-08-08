package com.thuong.vocabulary.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name="bo_tu_vung")
public class BoTuVung {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tai_khoan_id")
    private TaiKhoan taiKhoan;


    private String tenBo;


    private LocalDateTime ngayTao;

    @OneToMany(mappedBy = "boTuVung")
    private List<TuVung> tuVungs;

    public Long getId() {
        return id;
    }


    public String getTenBo() {
        return tenBo;
    }


    public void setTenBo(String tenBo) {
        this.tenBo = tenBo;
    }


    public LocalDateTime getNgayTao() {
        return ngayTao;
    }


    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public int getSoLuongTu() {
        if (tuVungs == null) {
            return 0;
        }

        return tuVungs.size();
    }

    public TaiKhoan getTaiKhoan() {
        return taiKhoan;
    }

    public void setTaiKhoan(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
    }

}