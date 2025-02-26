package org.example;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.io.FileInputStream;

public class test {
    private static final String CONFIG_FILE = "parser/src/main/resources/config.properties";

    public static void main(String[] args) {
        Properties prop = new Properties();
        try (InputStream inputStream = Files.newInputStream(Paths.get(CONFIG_FILE))) {
            prop.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        prop.forEach((key, value) -> {
            System.out.println(key + " = " + value);
        });
    }
}
