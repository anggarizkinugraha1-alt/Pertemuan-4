package com.beras.stokberas;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Menu utama setelah login: tambah stok & kurangi stok beras.
 */
public class MenuStok extends JFrame {

    private static final DateTimeFormatter FORMAT_WAKTU = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final JComboBox<Beras> cmbBeras = new JComboBox<>();
    private final JTextField txtJumlah = new JTextField(10);
    private final JTextField txtKeterangan = new JTextField(20);

    private final DefaultTableModel modelStok
            = buatModel(new String[]{"No", "Jenis Beras", "Stok (kg)"});
    private final DefaultTableModel modelRiwayat
            = buatModel(new String[]{"Waktu", "Jenis Beras", "Transaksi", "Jumlah (kg)", "Keterangan"});

    public MenuStok(String username) {
        setTitle("Stok Beras - Menu Stok");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // ===== header =====
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));
        header.add(new JLabel("Selamat datang, " + username), BorderLayout.WEST);
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> logout());
        header.add(btnLogout, BorderLayout.EAST);

        // ===== form transaksi =====
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Transaksi Stok"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0;
        form.add(new JLabel("Jenis Beras"), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        form.add(cmbBeras, g);
        g.gridx = 2; g.fill = GridBagConstraints.NONE; g.weightx = 0;
        JButton btnBerasBaru = new JButton("+ Beras Baru");
        btnBerasBaru.addActionListener(e -> berasBaru());
        form.add(btnBerasBaru, g);

        g.gridx = 0; g.gridy = 1;
        form.add(new JLabel("Jumlah (kg)"), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        form.add(txtJumlah, g);

        g.gridx = 0; g.gridy = 2; g.fill = GridBagConstraints.NONE; g.weightx = 0;
        form.add(new JLabel("Keterangan"), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        form.add(txtKeterangan, g);

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton btnTambah = new JButton("Tambah Stok");
        btnTambah.addActionListener(e -> proses(TransaksiStok.Jenis.MASUK));
        JButton btnKurangi = new JButton("Kurangi Stok");
        btnKurangi.addActionListener(e -> proses(TransaksiStok.Jenis.KELUAR));
        tombol.add(btnTambah);
        tombol.add(btnKurangi);
        g.gridx = 1; g.gridy = 3;
        form.add(tombol, g);

        JPanel atas = new JPanel(new BorderLayout());
        atas.add(header, BorderLayout.NORTH);
        JPanel formWrap = new JPanel(new BorderLayout());
        formWrap.setBorder(BorderFactory.createEmptyBorder(8, 12, 0, 12));
        formWrap.add(form, BorderLayout.CENTER);
        atas.add(formWrap, BorderLayout.CENTER);

        // ===== tabel =====
        JTabbedPane tab = new JTabbedPane();
        tab.addTab("Stok Saat Ini", new JScrollPane(new JTable(modelStok)));
        tab.addTab("Riwayat Transaksi", new JScrollPane(new JTable(modelRiwayat)));
        JPanel tabWrap = new JPanel(new BorderLayout());
        tabWrap.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));
        tabWrap.add(tab, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(atas, BorderLayout.NORTH);
        add(tabWrap, BorderLayout.CENTER);

        setSize(720, 560);
        setLocationRelativeTo(null);

        muatData();
    }

    private static DefaultTableModel buatModel(String[] kolom) {
        return new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static String fmt(BigDecimal angka) {
        return angka.stripTrailingZeros().toPlainString();
    }

    private void muatData() {
        try {
            List<Beras> daftar = StokService.semuaBeras();
            List<TransaksiStok> riwayat = StokService.riwayat();

            Beras terpilih = (Beras) cmbBeras.getSelectedItem();
            Integer idTerpilih = terpilih == null ? null : terpilih.getId();

            cmbBeras.removeAllItems();
            modelStok.setRowCount(0);
            int no = 1;
            for (Beras b : daftar) {
                cmbBeras.addItem(b);
                if (b.getId().equals(idTerpilih)) {
                    cmbBeras.setSelectedItem(b);
                }
                modelStok.addRow(new Object[]{no++, b.getNama(), fmt(b.getStok())});
            }

            modelRiwayat.setRowCount(0);
            for (TransaksiStok t : riwayat) {
                modelRiwayat.addRow(new Object[]{
                    t.getWaktu().format(FORMAT_WAKTU),
                    t.getBeras().getNama(),
                    t.getJenis() == TransaksiStok.Jenis.MASUK ? "Stok Masuk" : "Stok Keluar",
                    fmt(t.getJumlah()),
                    t.getKeterangan() == null ? "" : t.getKeterangan()
                });
            }
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage());
        }
    }

    private void berasBaru() {
        String nama = JOptionPane.showInputDialog(this, "Nama jenis beras baru:");
        if (nama == null || nama.trim().isEmpty()) {
            return;
        }
        nama = nama.trim();
        try {
            StokService.tambahBeras(nama);
            muatData();
            for (int i = 0; i < cmbBeras.getItemCount(); i++) {
                if (cmbBeras.getItemAt(i).getNama().equalsIgnoreCase(nama)) {
                    cmbBeras.setSelectedIndex(i);
                    break;
                }
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Gagal menambah beras: " + e.getMessage());
        }
    }

    private void proses(TransaksiStok.Jenis jenis) {
        Beras b = (Beras) cmbBeras.getSelectedItem();
        if (b == null) {
            JOptionPane.showMessageDialog(this, "Pilih jenis beras dulu, atau tambah dengan \"+ Beras Baru\".");
            return;
        }

        BigDecimal jumlah;
        try {
            jumlah = new BigDecimal(txtJumlah.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah harus berupa angka, contoh: 25 atau 12.5");
            return;
        }
        if (jumlah.signum() <= 0) {
            JOptionPane.showMessageDialog(this, "Jumlah harus lebih dari 0.");
            return;
        }

        try {
            StokService.ubahStok(b.getId(), jenis, jumlah, txtKeterangan.getText().trim());
            txtJumlah.setText("");
            txtKeterangan.setText("");
            muatData();
            JOptionPane.showMessageDialog(this,
                    jenis == TransaksiStok.Jenis.MASUK ? "Stok berhasil ditambah." : "Stok berhasil dikurangi.");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + e.getMessage());
        }
    }

    private void logout() {
        dispose();
        Login login = new Login();
        login.setLocationRelativeTo(null);
        login.setVisible(true);
    }
}
