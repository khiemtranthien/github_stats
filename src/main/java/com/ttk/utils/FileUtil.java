package com.ttk.utils;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class FileUtil {
    public static void unzipGzFiles(String inputPath, String outputPath) throws IOException {
        File file = new File(inputPath);
        if (file.exists() && file.isFile()) {

            byte[] buffer = new byte[1024];

            try (
                    GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(inputPath));

                    FileOutputStream out = new FileOutputStream(outputPath)
            ) {
                int len;
                while ((len = gzis.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }
        } else {
            throw new FileNotFoundException(String.format("%s file not found or not a file", inputPath));
        }
    }

    public static boolean removeFile(String filePath) {
        File file = new File(filePath);

        return file.isFile() && file.delete();
    }
}
