package com.beras.stokberas;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Semua akses database lewat Hibernate ada di sini.
 */
public class StokService {

    private StokService() {
    }

    private static <T> T dalamTransaksi(Function<Session, T> kerja) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            try {
                T hasil = kerja.apply(s);
                tx.commit();
                return hasil;
            } catch (RuntimeException e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            }
        }
    }

    public static Pengguna login(String username, String password) {
        return dalamTransaksi(s -> s.createQuery(
                "from Pengguna where username = :u and password = :p", Pengguna.class)
                .setParameter("u", username)
                .setParameter("p", password)
                .setMaxResults(1)
                .uniqueResult());
    }

    public static List<Beras> semuaBeras() {
        return dalamTransaksi(s -> s.createQuery("from Beras order by nama", Beras.class).getResultList());
    }

    public static List<TransaksiStok> riwayat() {
        return dalamTransaksi(s -> s.createQuery(
                "from TransaksiStok t order by t.waktu desc, t.id desc", TransaksiStok.class)
                .setMaxResults(500)
                .getResultList());
    }

    public static Beras tambahBeras(String nama) {
        return dalamTransaksi(s -> {
            Long ada = s.createQuery("select count(b) from Beras b where lower(b.nama) = :n", Long.class)
                    .setParameter("n", nama.toLowerCase())
                    .getSingleResult();
            if (ada > 0) {
                throw new IllegalArgumentException("Jenis beras \"" + nama + "\" sudah ada.");
            }
            Beras b = new Beras(nama);
            s.persist(b);
            return b;
        });
    }

    /**
     * Menambah (MASUK) atau mengurangi (KELUAR) stok, sekaligus mencatat riwayatnya.
     */
    public static void ubahStok(int berasId, TransaksiStok.Jenis jenis, BigDecimal jumlah, String keterangan) {
        BigDecimal qty = jumlah.setScale(2, RoundingMode.HALF_UP);
        if (qty.signum() <= 0) {
            throw new IllegalArgumentException("Jumlah harus lebih dari 0.");
        }
        dalamTransaksi(s -> {
            // kunci baris agar stok tidak bentrok
            Beras b = s.find(Beras.class, berasId, LockModeType.PESSIMISTIC_WRITE);
            if (b == null) {
                throw new IllegalArgumentException("Jenis beras tidak ditemukan.");
            }
            if (jenis == TransaksiStok.Jenis.KELUAR) {
                if (b.getStok().compareTo(qty) < 0) {
                    throw new IllegalArgumentException("Stok tidak cukup. Stok " + b.getNama()
                            + " saat ini: " + b.getStok().stripTrailingZeros().toPlainString() + " kg.");
                }
                b.setStok(b.getStok().subtract(qty));
            } else {
                b.setStok(b.getStok().add(qty));
            }
            s.persist(new TransaksiStok(b, jenis, qty, keterangan));
            return null;
        });
    }
}
