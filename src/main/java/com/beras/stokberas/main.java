package com.beras.stokberas;

import javax.swing.SwingUtilities;

public class  main{

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            Login login = new Login();

            login.setTitle("Stok Beras - Login");
            login.setLocationRelativeTo(null);
            login.setVisible(true);

        });

    }
}