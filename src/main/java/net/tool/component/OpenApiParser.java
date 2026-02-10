package net.tool.component;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

@Component
public class OpenApiParser {

    public OpenAPI parseOpenAPI(String urlString) throws Exception {
        String specContent = fetchSpecContent(urlString);
        return parseSpec(specContent);
    }
    /**
     * Helper, Fetch the OpenAPI spec content as a String
     */
    private String fetchSpecContent(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            //System.out.println("Reader lines: " + reader.lines().collect(Collectors.joining("\n")));
            return reader.lines().collect(Collectors.joining("\n"));
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Helper, Parse OpenAPI spec from JSON/YAML string
     */
    private OpenAPI parseSpec(String specContent) {
        try {
            // Validate size
            if (specContent.length() > 10_000_000) { // 10MB limit
                throw new RuntimeException("Spec too large");
            }

            OpenAPIV3Parser parser = new OpenAPIV3Parser();
            ParseOptions options = new ParseOptions();
            options.setResolve(true); // Resolve refs
            options.setResolveFully(false); // Resolve only internal refs

            SwaggerParseResult result = parser.readContents(specContent, null, options);

            // Check for parsing errors
            if (result.getMessages() != null && !result.getMessages().isEmpty()) {
                System.out.println("Warning: Parse messages:");
                result.getMessages().forEach(msg -> System.out.println("  - " + msg));
            }
            return result.getOpenAPI();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAPI spec: " + e.getMessage(), e);
        }
    }
}
