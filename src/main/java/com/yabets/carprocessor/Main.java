package com.yabets.carprocessor;

import com.yabets.carprocessor.cli.CarProcessorCli;

public class Main {

    public static void main(String[] args) {

        try {

            CarProcessorCli cli = new CarProcessorCli();
            cli.run();

        } catch (Exception e) {

            System.err.println("Application error: " + e.getMessage());
            System.exit(1);
        }
    }
}