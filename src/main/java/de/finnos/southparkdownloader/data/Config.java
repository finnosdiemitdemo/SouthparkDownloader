package de.finnos.southparkdownloader.data;

import de.finnos.southparkdownloader.DownloadHelper;
import de.finnos.southparkdownloader.I18N;
import de.finnos.southparkdownloader.SouthparkDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

public class Config {
    private static final Logger LOG = LoggerFactory.getLogger(Config.class);

    public static final String CONFIG_PATH = Setup.DATABASE_FOLDER_PATH + "config.json";

    private static final Connector<Config> CONNECTOR = new Connector<>(CONFIG_PATH, Config.class);
    private static Config CONFIG = null;
    public static Config.Domain DOMAIN;

    private static final Map<Domain, Map<DownloadLanguage, DownloadLanguageConfig>> MAP_DOMAIN_TO_AVAILABLE_DOWNLOAD_LANGUAGES;

    static {
        MAP_DOMAIN_TO_AVAILABLE_DOWNLOAD_LANGUAGES = Arrays.stream(Domain.values())
            .collect(Collectors.toMap(domain -> domain, domain -> switch (domain) {
                case SOUTHPARK_CC_COM -> new LinkedHashMap<>(Map.of(
                    DownloadLanguage.ENG, new DownloadLanguageConfig(domain, DownloadLanguage.ENG, ""),
                    DownloadLanguage.ES, new DownloadLanguageConfig(domain, DownloadLanguage.ES, "es")));
                case SOUTHPARK_DE -> new LinkedHashMap<>(Map.of(
                    DownloadLanguage.DE, new DownloadLanguageConfig(domain, DownloadLanguage.DE, ""),
                    DownloadLanguage.ENG, new DownloadLanguageConfig(domain, DownloadLanguage.ENG, "en")));
                case SOUTHPARKSTUDIOS_COM, SOUTHPARKSTUDIOS_CO_UK -> new LinkedHashMap<>(Map.of(
                    DownloadLanguage.ENG, new DownloadLanguageConfig(domain, DownloadLanguage.ENG, "")));
            }));
    }

    public enum Domain {
        SOUTHPARK_CC_COM("https://southpark.cc.com/"),
        SOUTHPARKSTUDIOS_COM("https://southparkstudios.com/"),
        SOUTHPARK_DE("https://www.southpark.de/"),
        SOUTHPARKSTUDIOS_CO_UK("https://southparkstudios.co.uk/");

        private final String host;
        private final String url;

        Domain(final String url) {
            this.url = url;
            try {
                host = new URI(url).getHost();
            } catch (URISyntaxException e) {
                throw new SouthparkDownloader.StartupException(e);
            }
        }

        public String getUrl() {
            return url;
        }

        public String getHost() {
            return host;
        }
    }

    public enum DownloadLanguage {
        // Die Ordnernamen sind extra nicht mit I18n gemacht, damit man diese nicht aus Versehen ändert
        DE("de", "Deutsch", Locale.GERMAN, "Staffel", "Episode"),
        ENG("eng", "English", Locale.US, "Season", "Episode"),
        ES("es", "Espanol", new Locale("es"), "Serie", "Episodio");

        private final String code;
        private final String name;
        private final Locale locale;
        private final String folderNameSeason;
        private final String folderNameEpisode;

        DownloadLanguage(final String code, final String name, final Locale locale, final String folderNameSeason, final String folderNameEpisode) {
            this.code = code;
            this.name = name;
            this.locale = locale;
            this.folderNameSeason = folderNameSeason;
            this.folderNameEpisode = folderNameEpisode;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public Locale getLocale() {
            return locale;
        }

        public String getFolderNameSeason() {
            return folderNameSeason;
        }

        public String getFolderNameEpisode() {
            return folderNameEpisode;
        }

        public static DownloadLanguage valueOfCode(final String code) {
            return Arrays.stream(DownloadLanguage.values())
                .filter(language -> language.getCode().equals(code))
                .findFirst()
                .orElse(DownloadLanguage.DE);
        }
    }

    public enum GuiLanguage {
        DE("de", "Deutsch", Locale.GERMAN),
        ENG("eng", "English", Locale.US);

        private final String code;
        private final String name;
        private final Locale locale;

        GuiLanguage(final String code, final String name, final Locale locale) {
            this.code = code;
            this.name = name;
            this.locale = locale;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public Locale getLocale() {
            return locale;
        }

        public static GuiLanguage valueOfCode(final String code) {
            return Arrays.stream(GuiLanguage.values())
                .filter(language -> language.getCode().equals(code))
                .findFirst()
                .orElse(GuiLanguage.DE);
        }
    }

    public record DownloadLanguageConfig(Domain domain, DownloadLanguage language, String prefix) {
    }

    public static void init() {
        DOMAIN = loadAndValidateDomain();

        final File configFile = new File(CONNECTOR.getFilePath());
        if (!configFile.exists()) {
            CONFIG = new Config();
            CONNECTOR.write(CONFIG);
        } else {
            CONFIG = CONNECTOR.read();
        }

        final var mapDomainToAvailableLanguages = MAP_DOMAIN_TO_AVAILABLE_DOWNLOAD_LANGUAGES.get(DOMAIN);
        final var downloadLanguage = DownloadLanguage.valueOfCode(CONFIG.downloadLang);
        if (mapDomainToAvailableLanguages.values().stream()
            .noneMatch(languageConfig -> languageConfig.language.equals(downloadLanguage))) {
            CONFIG.downloadLang = mapDomainToAvailableLanguages.keySet().stream().findFirst().map(lang -> lang.code).orElse("");
            save();
        }
    }

    public static DownloadLanguageConfig getLanguageConfig() {
        final var mapDomainToAvailableLanguages = MAP_DOMAIN_TO_AVAILABLE_DOWNLOAD_LANGUAGES.get(DOMAIN);
        final var downloadLanguage = DownloadLanguage.valueOfCode(CONFIG.downloadLang);
        return mapDomainToAvailableLanguages.entrySet().stream()
            .filter(entry -> entry.getKey().equals(downloadLanguage))
            .findFirst()
            .map(Map.Entry::getValue)
            .orElse(null);
    }

    public static void save() {
        CONNECTOR.write(CONFIG);
    }

    public static Config get() {
        if (CONFIG == null) {
            CONFIG = new Config();
        }

        return CONFIG;
    }

    public static Set<DownloadLanguage> getAvailableDownloadLanguages() {
        if (!MAP_DOMAIN_TO_AVAILABLE_DOWNLOAD_LANGUAGES.containsKey(DOMAIN)) {
            return Set.of();
        }
        return MAP_DOMAIN_TO_AVAILABLE_DOWNLOAD_LANGUAGES.get(DOMAIN).keySet();
    }

    private static Domain loadAndValidateDomain() throws SouthparkDownloader.StartupException {
        try {
            /*
             * Beim aufruf der Seite wird man automatisch auf die entsprechende Domain weitergeleitet.
             * So findet man am schnellsten die richtige domain
             */
            final var domainString = DownloadHelper.getDomainByGeolocation(Config.Domain.SOUTHPARK_CC_COM.getUrl());
            final var optCurrentDomain = Arrays.stream(Config.Domain.values())
                .filter(domain -> domain.getHost().equals(domainString))
                .findFirst();
            if (optCurrentDomain.isEmpty()) {
                throw new SouthparkDownloader.StartupException(I18N.i18n("startup.error.invalid_domain").formatted(domainString));
            }
            return optCurrentDomain.get();
        } catch (IOException | URISyntaxException e) {
            throw new SouthparkDownloader.StartupException(I18N.i18n("startup.error.unable_to_load_domain"), e);
        }
    }


    private String ffmpegFilePath = "";
    private String ffprobeFilePath = "";
    private String downloadLang = DownloadLanguage.ENG.code;
    private String guiLang = GuiLanguage.ENG.code;
    private int maxFfmpegProcesses = 20;

    public String getFfmpegFilePath() {
        return ffmpegFilePath;
    }

    public void setFfmpegFilePath(String ffmpegFilePath) {
        this.ffmpegFilePath = ffmpegFilePath;
    }

    public String getFfprobeFilePath() {
        return ffprobeFilePath;
    }

    public void setFfprobeFilePath(String ffprobeFilePath) {
        this.ffprobeFilePath = ffprobeFilePath;
    }

    public String getDownloadLang() {
        return downloadLang;
    }

    public void setDownloadLang(final String downloadLang) {
        this.downloadLang = downloadLang;
    }

    public String getGuiLang() {
        return guiLang;
    }

    public void setGuiLang(final String guiLang) {
        this.guiLang = guiLang;
    }

    public int getMaxFfmpegProcesses() {
        return maxFfmpegProcesses;
    }

    public void setMaxFfmpegProcesses(final int maxFfmpegProcesses) {
        this.maxFfmpegProcesses = maxFfmpegProcesses;
    }
}
