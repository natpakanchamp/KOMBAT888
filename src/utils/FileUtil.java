package utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

public class FileUtil {
    public static String readFile(String filePath) {
        try {
            // อ่านไฟล์ทั้งหมดออกมาเป็น String
            return Files.readString(Path.of(filePath));
        } catch (IOException e) {
            System.err.println("เกิดข้อผิดพลาดในการอ่านไฟล์: " + e.getMessage());
            return null;
        }
    }
}