package com.greenlink.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Utility class to read file contents.
 *
 * @author DO.IT-SUDPARIS
 */
public class FileTool {
    static public String getContents(File aFile) {
        StringBuilder contents = new StringBuilder();
        try (BufferedReader input = new BufferedReader(new FileReader(aFile))) {
            String line;
            while ((line = input.readLine()) != null) {
                contents.append(line);
                contents.append(System.getProperty("line.separator"));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return contents.toString();
    }
}

