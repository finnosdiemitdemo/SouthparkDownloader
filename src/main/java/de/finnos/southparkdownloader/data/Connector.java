package de.finnos.southparkdownloader.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import de.finnos.southparkdownloader.Helper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Connector<T> {
    private final String filePath;
    private final Class<T> clazz;
    private final Gson gson;

    public Connector(String filePath, Class<T> clazz, Gson gson) {
        this.filePath = filePath;
        this.clazz = clazz;
        this.gson = gson;
    }

    public Connector(String filePath, Class<T> clazz) {
        this.filePath = filePath;
        this.clazz = clazz;
        this.gson = new GsonBuilder().create();
    }

    public void write (final T data) {
        final File databaseFile = new File(filePath);

        boolean fileExists = databaseFile.exists();
        if (!fileExists) {
            try {
                fileExists = databaseFile.createNewFile();
            } catch (IOException e) {
                Helper.showErrorMessage(null, e.getMessage());
            }
        }

        if (fileExists) {
            try {
                final String fileContent = gson.toJson(data);
                Files.writeString(Paths.get(filePath), fileContent, StandardCharsets.UTF_8);
            } catch (IOException e) {
                Helper.showErrorMessage(null, e.getMessage());
            }
        }
    }

    public T read () {
        T result = null;

        final File databaseFile = new File(filePath);
        if (databaseFile.exists()) {
            try {
                result = gson.fromJson(Files.readString(Paths.get(filePath), StandardCharsets.UTF_8), clazz);
            } catch (IOException | JsonSyntaxException e) {
                Helper.showErrorMessage(null, e.getMessage());
            }
        } else {
            Helper.showErrorMessage(null, "Download database file does not exists");
        }

        return result;
    }

    public String getFilePath() {
        return filePath;
    }
}
