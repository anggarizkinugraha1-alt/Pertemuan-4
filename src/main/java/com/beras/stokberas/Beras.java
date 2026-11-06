package com.beras.stokberas;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "beras")
public class Beras {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String nama;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal stok = BigDecimal.ZERO;

    public Beras() {
    }

    public Beras(String nama) {
        this.nama = nama;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public BigDecimal getStok() { return stok; }
    public void setStok(BigDecimal stok) { this.stok = stok; }

    // Dipakai JComboBox untuk menampilkan nama
    @Override
    public String toString() {
        return nama;
    }
}
