package com.beras.stokberas;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static SessionFactory sessionFactory;

    private HibernateUtil() {
    }

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            // membaca src/main/resources/hibernate.cfg.xml
            sessionFactory = new Configuration().configure().buildSessionFactory();
            buatAdminAwal();
        }
        return sessionFactory;
    }

    // Jika tabel pengguna kosong (database baru), buat akun admin/admin123
    private static void buatAdminAwal() {
        try (Session s = sessionFactory.openSession()) {
            Transaction tx = s.beginTransaction();
            Long jumlah = s.createQuery("select count(p) from Pengguna p", Long.class).getSingleResult();
            if (jumlah == 0) {
                s.persist(new Pengguna("admin", "admin123"));
            }
            tx.commit();
        }
    }
}
