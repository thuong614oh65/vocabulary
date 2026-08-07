package com.thuong.vocabulary.entity;


import jakarta.persistence.*;


@Entity
@Table(name="tu_vung")
public class TuVung {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String tiengAnh;


    private String tiengViet;


    private String phienAm;


    private String viDu;

    @Column(name = "so_lan_sai")
    private Integer soLanSai = 0;


    @ManyToOne
    @JoinColumn(name="bo_id")
    private BoTuVung boTuVung;



    public Long getId() {
        return id;
    }


    public String getTiengAnh() {
        return tiengAnh;
    }


    public void setTiengAnh(String tiengAnh) {
        this.tiengAnh = tiengAnh;
    }


    public String getTiengViet() {
        return tiengViet;
    }


    public void setTiengViet(String tiengViet) {
        this.tiengViet = tiengViet;
    }


    public String getPhienAm() {
        return phienAm;
    }


    public void setPhienAm(String phienAm) {
        this.phienAm = phienAm;
    }


    public String getViDu() {
        return viDu;
    }


    public void setViDu(String viDu) {
        this.viDu = viDu;
    }


    public BoTuVung getBoTuVung() {
        return boTuVung;
    }


    public void setBoTuVung(BoTuVung boTuVung) {
        this.boTuVung = boTuVung;
    }

    public Integer getSoLanSai() {
        return soLanSai;
    }

    public void setSoLanSai(Integer soLanSai) {
        this.soLanSai = soLanSai;
    }

}