package de.finnos.southparkdownloader;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexHelper {
    public static void match (final String regex, final String findIn, final Consumer<Matcher> func) {
        Matcher matcher = matchPrepare(regex, findIn);
        while(matcher.find()) {
            func.accept(matcher);
        }
    }

    public static void matchFirst (final String regex, final String findIn, final Consumer<Matcher> func) {
        Matcher matcher = matchPrepare(regex, findIn);
        if(matcher.find()) {
            func.accept(matcher);
        }
    }

    public static int matchCount(final String regex, final String findIn) {
        Matcher matcher = matchPrepare(regex, findIn);
        int count = 0;
        while(matcher.find()) {
            count++;
        }

        return count;
    }

    public static Matcher matchPrepare (final String regex, final String findIn) {
        Pattern pattern = Pattern.compile(regex);

        return pattern.matcher(findIn);
    }

    public static Matcher matchPrepare (final String regex, final String findIn, final boolean multiline) {
        Pattern pattern = Pattern.compile(regex, multiline ? Pattern.MULTILINE : 0);

        return pattern.matcher(findIn);
    }
}
