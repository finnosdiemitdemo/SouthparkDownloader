package de.finnos.southparkdownloader.data;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import de.finnos.southparkdownloader.classes.Episode;
import de.finnos.southparkdownloader.classes.Season;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadDatabase {
    public static final String DOWNLOAD_DATABASE_PATH = Setup.DATABASE_FOLDER_PATH + "seasons.json";

    private static final Connector<DownloadDatabase> CONNECTOR = new Connector<>(DOWNLOAD_DATABASE_PATH,
        DownloadDatabase.class, createJavaTimeGsonBuilder()
        .registerTypeAdapter(Season.class, (JsonDeserializer<Season>) (jsonElement, type, c) -> {
            final Gson gson = createJavaTimeGsonBuilder()
                .registerTypeAdapter(Episode.class, (JsonDeserializer<Episode>) (jsonElement1, type1, c1) -> new Episode(
                    createJavaTimeGsonBuilder().create().fromJson(jsonElement1.toString(), Episode.class)))
                .create();

            final Season season = gson.fromJson(jsonElement.toString(), Season.class);

            for (final Episode episode : season.getEpisodes()) {
                episode.setSeason(season);
            }

            season.setPath();

            return season;
        })

        .excludeFieldsWithoutExposeAnnotation()
        .create()
    );

    private static GsonBuilder createJavaTimeGsonBuilder () {
        return new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                (JsonSerializer<LocalDate>) (localDate, type, jsonSerializationContext) -> new JsonPrimitive(localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .registerTypeAdapter(LocalDate.class,
                (JsonDeserializer<LocalDate>) (jsonElement, type, jsonDeserializationContext) -> LocalDate.parse(jsonElement.getAsJsonPrimitive().getAsString(),
                    DateTimeFormatter.ISO_LOCAL_DATE))
            .registerTypeAdapter(Duration.class, (JsonSerializer<Duration>) (duration, type, jsonSerializationContext) -> new JsonPrimitive(duration.toString()))
            .registerTypeAdapter(Duration.class,
                (JsonDeserializer<Duration>) (jsonElement, type, jsonDeserializationContext) -> Duration.parse(jsonElement.getAsJsonPrimitive().getAsString()));
    }

    private static DownloadDatabase DOWNLOAD_DATABASE = null;

    @Expose
    private Map<String, List<Season>> seasons = new HashMap<>();

    public List<Season> getSeasons() {
        final var downloadLang = Config.get().getDownloadLang();
        seasons.putIfAbsent(downloadLang, new ArrayList<>());
        return seasons.get(downloadLang);
    }

    public static void init() {
        final File configFile = new File(CONNECTOR.getFilePath());
        if (!configFile.exists()) {
            DOWNLOAD_DATABASE = new DownloadDatabase();
            CONNECTOR.write(DOWNLOAD_DATABASE);
        } else {
            DOWNLOAD_DATABASE = CONNECTOR.read();
        }
    }

    public static DownloadDatabase get() {
        if (DOWNLOAD_DATABASE == null) {
            DOWNLOAD_DATABASE = new DownloadDatabase();
        }

        return DOWNLOAD_DATABASE;
    }

    public static void update(final List<Season> seasons) {
        DOWNLOAD_DATABASE.seasons.put(Config.get().getDownloadLang(), seasons);
        CONNECTOR.write(DOWNLOAD_DATABASE);
    }

    public static boolean isEmpty() {
        return get().getSeasons().isEmpty();
    }
}