package net.tool.component;

import net.tool.exception.InvalidUrlException;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

@Component
public class UrlValidator {

    public void validateServerUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            InetAddress resolvedAddress = resolve(url.getHost());
            HttpURLConnection connection = openSafeConnection(url, resolvedAddress, "HEAD");
            connection.getResponseCode();
            connection.disconnect();
        } catch (InvalidUrlException e){
            throw e;
        } catch (Exception e) {
            throw new InvalidUrlException(
                    "Failed to reach target URL: " + e.getMessage()
            );
        }
    }

    // Package-private: exposed for OpenApiParser
    HttpURLConnection openSafeConnection(URL url, InetAddress resolvedAddress, String method) throws Exception {

        URL safeUrl;

        if ("https".equals(url.getProtocol())) {
            // bug fix: server name indication expects host name not ip
            // HTTPS requires hostname for SNI — use original URL for connection
            // re-verify DNS hasn't changed (narrow DNS rebinding window)
            InetAddress currentResolution = InetAddress.getByName(url.getHost());
            if (!currentResolution.equals(resolvedAddress)) {
                throw new SecurityException("DNS resolution changed between checks");
            }
            safeUrl = url;
        } else {
            // HTTP — connect directly to IP (full DNS rebinding protection)
            safeUrl = new URL(
                    url.getProtocol(),
                    resolvedAddress.getHostAddress(),
                    url.getPort() == -1 ? url.getDefaultPort() : url.getPort(),
                    url.getFile()
            );
        }

        HttpURLConnection connection = (HttpURLConnection)safeUrl.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        // no redirects
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("Host", url.getHost());
        return connection;
    }

    // Package-private: exposed for OpenApiParser
    InetAddress resolve(String host) throws UnknownHostException {
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
