package com.banking.app;

import com.banking.menu.Menu;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {


        try {
            Menu menu = new Menu();
            menu.startMenu();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}
