package net.tool;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@SpringBootApplication
public class ToolApplication implements CommandLineRunner {

	public static void main(String[] args) {
        SpringApplication.run(ToolApplication.class, args);
	}

    public void run(String... args) throws Exception {

        // http://localhost:3000/v3/api-docs
        // http://localhost:3000

        //Get input from user (target URL)
        String baseUrl;
        String targetUrl;
        OpenAPI openAPI = null;
        List<ApiEndpoint> endpoints = new ArrayList<>();
        Scanner sc = new Scanner(System.in);

        //System.out.println("Please enter your base URL");
        //baseUrl = sc.nextLine();
        System.out.println("Please enter your target URL");
        targetUrl = sc.nextLine();

        baseUrl = targetUrl + "/v3/api-docs";
        //baseUrl = targetUrl + "/openapi.json ";
        System.out.println("Success! \n" +  baseUrl + " \n" + targetUrl);

        // Check URLs availability

        // Validate OpenAPI spec URL
        System.out.print("\nChecking OpenAPI spec/base URL... ");
        if (!isSpecUrlValid(baseUrl)) {
            System.out.println("FAILED");
            System.out.println("Error: Cannot reach " + baseUrl);
            return;
        }
        System.out.println("Base, OK");

        // Validate target base URL
        System.out.print("Checking target base URL... ");
        if (!isServerReachable(targetUrl)) {
            System.out.println("FAILED");
            System.out.println("Error: Cannot reach " + targetUrl);
            return;
        }
        System.out.println("Target, OK");

        // 3. Parse and validate spec

        System.out.print("\nParsing OpenAPI specification... ");
        try {
            // Fetch the spec content
            String specContent = fetchSpecContent(baseUrl);

            // Parse the spec
            openAPI = parseSpec(specContent);

            if (openAPI == null) {
                System.out.println("FAILED");
                System.out.println("Error: Unable to parse OpenAPI spec");
                return;
            }

            System.out.println("OK");

            // Display spec information
            System.out.println("\n=== OpenAPI Specification Info ===");
            System.out.println("Title: " + openAPI.getInfo().getTitle());
            System.out.println("Version: " + openAPI.getInfo().getVersion());
            System.out.println("Total Endpoints: " + openAPI.getPaths().size());

            // Show endpoints
            System.out.println("\n=== Available Endpoints ===");
            openAPI.getPaths().forEach((path, pathItem) -> {
                System.out.println("Path: " + path);

                if (pathItem.getGet() != null) {
                    System.out.println("  - GET" + " - OperationId: " +  pathItem.getGet().getOperationId());
                    endpoints.add(new ApiEndpoint(path, "GET", pathItem.getGet()));
                }
                if (pathItem.getPost() != null)
                    System.out.println("  - POST" + " - OperationId: " +  pathItem.getPost().getOperationId());
                if (pathItem.getPut() != null)
                    System.out.println("  - PUT" + " - OperationId: " +  pathItem.getPut().getOperationId());
                if (pathItem.getDelete() != null)
                    System.out.println("  - DELETE" + " - OperationId: " +  pathItem.getDelete().getOperationId());
                if (pathItem.getPatch() != null)
                    System.out.println("  - PATCH" + " - OperationId: " +  pathItem.getPatch().getOperationId());



                //System.out.println("Param " + pathItem.getParameters().getFirst());

            });

            System.out.println("\n✓ Ready for security scanning!");

        } catch (Exception e) {
            System.out.println("FAILED");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }

//        if (openAPI != null)
//            try
//            {
//                for (Map :openAPI.getPaths())
//
//
//            }catch (Exception e){
//                System.out.println("FAILED TO CHECK THE OPERATION");
//            }

    }

//    private boolean hasAuthorizationHeader(){
//
//    }

    private boolean isSpecUrlValid(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode >= 200 && responseCode < 400;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isServerReachable(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            connection.getResponseCode();
            connection.disconnect();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Fetch the OpenAPI spec content as a String
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
     * Parse OpenAPI spec from JSON/YAML string
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

    public class ApiEndpoint {

        private final String path;
        private final String method;
        private final Operation operation;

        public ApiEndpoint(String path, String method, Operation operation) {
            this.path = path;
            this.method = method;
            this.operation = operation;
        }

        public String getPath() { return path; }
        public String getMethod() { return method; }
        public Operation getOperation() { return operation; }
    }

}
