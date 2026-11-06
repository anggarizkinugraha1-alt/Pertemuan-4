package com.beras.stokberas;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaksi_stok")
public class TransaksiStok {

    public enum Jenis {
        MASUK, KELUAR
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "beras_id", nullable = false)
    private Beras beras;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Jenis jenis;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal jumlah;

    @Column(length = 200)
    private String keterangan;

    @Column(nullable = false)
    private LocalDateTime waktu = LocalDateTime.now();

    public TransaksiStok() {
    }

    public TransaksiStok(Beras beras, Jenis jenis, BigDecimal jumlah, String keterangan) {
        this.beras = beras;
        this.jenis = jenis;
        this.jumlah = jumlah;
        this.keterangan = keterangan;
    }

    public Integer getId() { return id; }
    public Beras getBeras() { return beras; }
    public Jenis getJenis() { return jenis; }
    public BigDecimal getJumlah() { return jumlah; }
    public String getKeterangan() { return keterangan; }
    public LocalDateTime getWaktu() { return waktu; }
}
