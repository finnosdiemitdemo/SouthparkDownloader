package de.finnos.southparkdownloader;

import de.finnos.southparkdownloader.data.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class I18N {
    private static final Logger LOG = LoggerFactory.getLogger(I18N.class);

    private static final Map<Locale, ResourceBundle> MAP_I18N_BUNDLES = new HashMap<>();

    public static String i18n(final String key) {
        return i18n(Config.GuiLanguage.valueOfCode(Config.get().getGuiLang()).getLocale(), key);
    }

    public static String i18n(final Locale locale, final String key) {
        try {
            MAP_I18N_BUNDLES.putIfAbsent(locale, ResourceBundle.getBundle("messages/messages", locale));
        } catch (MissingResourceException e) {
            LOG.warn("Unable to load resource bundle for language " + locale, e);
            return locale == Locale.US
                ? i18n(Locale.US, key)
                : "i18n_" + key;
        }
        try {
            return MAP_I18N_BUNDLES.get(locale).getString(key);
        } catch (MissingResourceException e) {
            LOG.info(key + "=");
            return "i18n_" + key;
        }
    }
}
