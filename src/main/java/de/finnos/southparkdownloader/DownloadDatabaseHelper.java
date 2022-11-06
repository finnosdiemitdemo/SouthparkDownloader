package de.finnos.southparkdownloader;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.finnos.southparkdownloader.classes.Episode;
import de.finnos.southparkdownloader.classes.Season;
import de.finnos.southparkdownloader.data.Config;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.BiConsumer;

public class DownloadDatabaseHelper {

    private static final String API_URL_FORMAT =
        "https://media.mtvnservices.com/pmt/e1/access/index.html?uri=mgid:arc:episode:southpark.intl:%s&configtype=edge&ref=%s";
    private static final String SEASONS_URL_PART = "seasons/south-park";

    public static ArrayList<Season> findSeasons() {
        final var langConfig = Config.getLanguageConfig();
        ArrayList<Season> result = new ArrayList<>();
        final String prefix = langConfig.prefix() + (!langConfig.prefix().isBlank() ? "/" : "");
        final String seasonsUrlPart = prefix + SEASONS_URL_PART;
        final BiConsumer<String, String> findSeasons = (htmlContent, regex) ->
            RegexHelper.match(regex, htmlContent, matcher -> {
                final String url = langConfig.domain().getUrl() + matcher.group(1);
                final String name = matcher.group(2).replaceAll("\\u00A0", " ");
                final Integer number = Integer.parseInt(matcher.group(3));

                result.add(new Season(name, number, url));
            });

        findSeasons.accept(
            DownloadHelper.downloadFile(langConfig.domain().getUrl() + prefix + SEASONS_URL_PART),
            "href=\\\"(/%s/\\w{6}/[\\w-]+)\\\".*?>((?:Staffel|Season)[\\u00A0 ](\\d+))</a>".formatted(seasonsUrlPart)
        );

        // Die letzte Staffel ist in der Combobox nicht drin
        if (!result.isEmpty()) {
            result.sort((o1, o2) -> o1.getNumber() > o2.getNumber() ? 0 : 1);
            final int missingSeasonNumber = result.get(0).getNumber() + 1;
            findSeasons.accept(
                DownloadHelper.downloadFile(result.get(0).getUrl()),
                String.format("(%s/\\S{6}[^\"]*)-%d\".*?>((?:Staffel|Season).(%d))", seasonsUrlPart, missingSeasonNumber, missingSeasonNumber));
        }

        result.sort(Comparator.comparing(Season::getNumber));

        return result;
    }

    public static ArrayList<Episode> findEpisodes(final Season season) {
        final var langConfig = Config.getLanguageConfig();

        final String regex = "url\":\"([^\"]+\\\\u002F(?:ascending|descending))";
        final String htmlContent = DownloadHelper.downloadFile(season.getUrl());

        final ArrayList<Episode> episodes = new ArrayList<>();

        RegexHelper.matchFirst(regex, htmlContent, matcher -> {
            String url = matcher.group(1);
            url = url.replaceAll("\\\\u002F", "/");
            url = url.replaceAll("episode/(\\d+/\\d+)/(?:ascending|descending)", "episode/1/1/ascending");
            url = langConfig.domain().getUrl() + url;

            final JsonObject apiResponse = DownloadHelper.downloadJsonFile(url);
            if (apiResponse.has("items")) {
                JsonArray items = apiResponse.getAsJsonArray("items");
                for (int i = 0; i < items.size(); i++) {
                    final Episode episode = parseApiResponseEpisode(season, items.get(i).getAsJsonObject(), i);

                    episodes.add(episode);
                }
            }
        });

        return episodes;
    }

    public static ArrayList<String> findEpisodePartUrls(final Episode episode) throws DownloadHelperException {
        final String apiUrl = String.format(API_URL_FORMAT, episode.getId(), episode.getUrl());
        final JsonObject objectApiResult = DownloadHelper.downloadJsonFile(apiUrl);

        ArrayList<String> urls = new ArrayList<>();

        if (objectApiResult.has("errorSlateMessage")) {
            final var errorMessage = objectApiResult.get("errorSlateMessage").getAsString();
            throw new DownloadHelperException("Could not download media generator url: " + errorMessage);
        }

        if (objectApiResult.has("feed")) {
            final JsonObject objectFeed = objectApiResult.getAsJsonObject("feed");
            if (objectFeed.has("items")) {
                final JsonArray items = objectFeed.getAsJsonArray("items");
                for (int i = 0; i < items.size(); i++) {
                    final JsonObject item = items.get(i).getAsJsonObject();
                    if (item.has("group")) {
                        final JsonObject objectGroup = item.getAsJsonObject("group");
                        if (objectGroup.has("content")) {
                            String url = objectGroup.get("content").getAsString();
                            if (url != null) {
                                url = url.replaceAll("&device=\\{device\\}", "");
                                url += "&format=json&acceptMethods=hls";

                                urls.add(url);
                            }
                        }
                    }
                }
            }
        }

        return urls;
    }

    private static Episode parseApiResponseEpisode(final Season season, final JsonObject objectEpisode, final int index) {
        final var langConfig = Config.getLanguageConfig();

        String url = "";
        String id = "";
        String name = "";
        String description = "";
        String thumbnailUrl = "";
        Duration duration = Duration.ZERO;
        LocalDate publishDate = null;

        if (objectEpisode.has("url")) {
            url = objectEpisode.get("url").getAsString();
        }

        if (objectEpisode.has("id")) {
            id = objectEpisode.get("id").getAsString();
        }

        if (objectEpisode.has("meta")) {
            final JsonObject objectMeta = objectEpisode.getAsJsonObject("meta");
            if (objectMeta.has("subHeader")) {
                name = objectMeta.get("subHeader").getAsString();
            }

            if (objectMeta.has("description")) {
                description = objectMeta.get("description").getAsString();
            }

            if (objectMeta.has("date")) {
                try {
                    publishDate = new SimpleDateFormat("dd.MM.yyyy")
                        .parse(objectMeta.get("date").getAsString())
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                } catch (ParseException ignored) {

                }
            }
        }

        if (objectEpisode.has("media")) {
            final var media = objectEpisode.get("media").getAsJsonObject();
            if (media.has("image")) {
                final var image = media.get("image").getAsJsonObject();
                if (image.has("url")) {
                    thumbnailUrl = image.get("url").getAsString();
                }
            }

            if (media.has("duration")) {
                final var durationParts = Arrays.stream(media.get("duration").getAsString().split(":")).toList();
                final var hours = durationParts.size() == 2 ? 0 : Integer.parseInt(durationParts.get(0));
                duration = Duration.ofHours(hours)
                    .plusMinutes(Integer.parseInt(durationParts.size() == 2 ? durationParts.get(0) : durationParts.get(1)))
                    .plusSeconds(Integer.parseInt(durationParts.size() == 2 ? durationParts.get(1) : durationParts.get(2)));
            }
        }

        return new Episode(season,
            id,
            name,
            index + 1,
            langConfig.domain().getUrl() + url,
            description,
            thumbnailUrl,
            duration,
            publishDate
        );
    }


    public static class DownloadHelperException extends Exception {
        public DownloadHelperException(final String message) {
            super(message);
        }
    }
}
