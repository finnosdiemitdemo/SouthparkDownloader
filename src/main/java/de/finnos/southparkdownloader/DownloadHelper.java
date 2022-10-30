package de.finnos.southparkdownloader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DownloadHelper {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadHelper.class);

    // Proxy stuff for debugging
    /*static {
        System.setProperty("https.proxyHost", "");
        System.setProperty("https.proxyPort", "");

        if (System.getProperties().containsKey("https.proxyHost")) {
            // Trust all sll certificates
            try {
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }}, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier((arg0, arg1) -> true);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }
    }*/

    public static String downloadFile(final String url, final int retryCount) {
        try (final InputStream inputStream = new URL(url).openStream();
             final BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
        ) {
            final StringBuilder stringBuilder = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                stringBuilder.append((char) cp);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            LOG.error("Unable download url: " + url, e);
            if (retryCount > 0) {
                LOG.info("Retry download url: " + url);
                return downloadFile(url, retryCount - 1);
            }
            return "";
        }
    }

    public static String getDomainByGeolocation(String url) throws IOException, URISyntaxException {
        final HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setInstanceFollowRedirects(false);
        con.connect();
        con.getInputStream();
        if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            String redirectUrl = con.getHeaderField("Location");
            return getDomainByGeolocation(redirectUrl);
        }

        final var uri = new URI(url);
        return uri.getHost();
    }

    public static String downloadFile(final String url) {
        return downloadFile(url, 1);
    }

    public static JsonObject downloadJsonFile(final String url, final int retryCount) {
        return JsonParser.parseString(downloadFile(url, retryCount)).getAsJsonObject();
    }

    public static JsonObject downloadJsonFile(final String url) {
        return downloadJsonFile(url, 1);
    }
}
