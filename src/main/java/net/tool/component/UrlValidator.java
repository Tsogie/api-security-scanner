package net.tool.component;

import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

@Component
public class UrlValidator {

    public boolean isSpecUrlValid(String urlString) {
        try {
            URL url = new URL(urlString);
            InetAddress resolvedAddress = resolve(url.getHost());
            HttpURLConnection connection = openSafeConnection(url, resolvedAddress, "GET");
            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode >= 200 && responseCode < 300;

        } catch (Exception e) {
            return false;
        }
    }

    public boolean isServerReachable(String urlString) {
        try {
            URL url = new URL(urlString);
            InetAddress resolvedAddress = resolve(url.getHost());
            HttpURLConnection connection = openSafeConnection(url, resolvedAddress, "HEAD");
            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private HttpURLConnection openSafeConnection(URL url, InetAddress resolvedAddress, String method) throws Exception {

        URL safeUrl = new URL(
                url.getProtocol(),
                resolvedAddress.getHostAddress(),
                url.getPort() == -1 ? url.getDefaultPort() : url.getPort(),
                url.getFile()
        );

        HttpURLConnection connection = (HttpURLConnection)safeUrl.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        // no redirects
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("Host", url.getHost());

        return connection;
    }
    private InetAddress resolve(String host) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(host);
        if (isBlocklisted(address)){
            throw new SecurityException("Blocklisted address" + address);
        }
        return address;
    }

    private boolean isBlocklisted(InetAddress address) {
        return address.isAnyLocalAddress() ||
                address.isLoopbackAddress() ||
                address.isLinkLocalAddress() ||
                address.isMulticastAddress() ||
                address.isSiteLocalAddress() ||
                isCloudMetaDataIp(address);
    }
    private boolean isCloudMetaDataIp(InetAddress address) {
        String ip = address.getHostAddress();
        return ip.startsWith("169.254.") || ip.equals("100.100.100.200");
    }

}
