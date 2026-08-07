package ru.senioravanti.commons.files;

import org.apache.tika.Tika;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {
    public static final int BUFFER_SIZE = 8 * 1024;
    public static final Tika TIKA = new Tika();

    public static String getExtension(Path path) {
        var fileName = path.getFileName();
        if (fileName == null) return "";
        String fileString = fileName.toString();

        // Find index of last dot; return empty string if not found or is
        // at first or last position
        int lastDotIndex = fileString.lastIndexOf('.');
        if (lastDotIndex <= 0 || lastDotIndex == fileString.length() - 1) return "";

        // Return characters after, but not including, last dot
        return fileString.substring(lastDotIndex + 1);
    }

    public static String guessContentType(InputStream is) {
        try {
            return TIKA.detect(is.readNBytes(BUFFER_SIZE));
        } catch (IOException ex) {
            throw new IllegalStateException("failed to read %d bytes from input stream".formatted(BUFFER_SIZE), ex);
        }
    }

    public static String guessContentType(Path path) {
        try (var is = Files.newInputStream(path)) {
            return guessContentType(is);
        } catch (IOException ex) {
            throw new IllegalStateException("failed to open file " + path, ex);
        }
    }
}
