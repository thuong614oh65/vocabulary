package com.thuong.vocabulary.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tai_khoan")
public class TaiKhoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_dang_nhap", nullable = false, unique = true)
    private String tenDangNhap;

    @Column(name = "mat_khau", nullable = false)
    private String matKhau;

    @Column(name = "ho_ten")
    private String hoTen;

    @Column(name = "email", unique = true)
    private String email;

    @OneToMany(mappedBy = "taiKhoan", cascade = CascadeType.ALL)
    private List<BoTuVung> danhSachBo = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<BoTuVung> getDanhSachBo() {
        return danhSachBo;
    }

    public void setDanhSachBo(List<BoTuVung> danhSachBo) {
        this.danhSachBo = danhSachBo;
    }

}