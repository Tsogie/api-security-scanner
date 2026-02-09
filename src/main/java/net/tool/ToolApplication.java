package net.tool;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import lombok.Getter;
import lombok.Setter;
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
        // https://petstore3.swagger.io/api/v3

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

        //baseUrl = targetUrl + "/v3/api-docs";
        baseUrl = targetUrl + "/openapi.json ";
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
            boolean hasGlobalSecurity;
            if (openAPI.getSecurity() != null){
                hasGlobalSecurity = true;
            } else {
                hasGlobalSecurity = false;
            }
            System.out.println("Global security: " + hasGlobalSecurity);
            openAPI.getPaths().forEach((path, pathItem) -> {
                System.out.println("Path: " + path);

                if (pathItem.getGet() != null) {
                    System.out.println("  - GET" + " - OperationId: " +  pathItem.getGet().getOperationId());
                    endpoints.add(new ApiEndpoint(path, "GET", pathItem.getGet(), hasGlobalSecurity));
                }
                if (pathItem.getPost() != null) {
                    System.out.println("  - POST" + " - OperationId: " + pathItem.getPost().getOperationId());
                    endpoints.add(new ApiEndpoint(path, "POST", pathItem.getPost(), hasGlobalSecurity));
                }
                if (pathItem.getPut() != null) {
                    System.out.println("  - PUT" + " - OperationId: " + pathItem.getPut().getOperationId());
                    endpoints.add(new ApiEndpoint(path, "PUT", pathItem.getPut(), hasGlobalSecurity));
                }
                if (pathItem.getDelete() != null) {
                    System.out.println("  - DELETE" + " - OperationId: " + pathItem.getDelete().getOperationId());
                    endpoints.add(new ApiEndpoint(path, "DELETE", pathItem.getDelete(), hasGlobalSecurity));
                }
                if (pathItem.getPatch() != null) {
                    System.out.println("  - PATCH" + " - OperationId: " + pathItem.getPatch().getOperationId());
                    endpoints.add(new ApiEndpoint(path, "PATCH", pathItem.getPatch(), hasGlobalSecurity));
                }
                if (pathItem.getTrace() != null) {
                    System.out.println("  - TRACE" + " - OperationId: " + pathItem.getTrace().getOperationId());
                    endpoints.add(new ApiEndpoint(path, "TRACE", pathItem.getTrace(), hasGlobalSecurity));
                }

                //System.out.println("Param " + pathItem.getParameters().getFirst());

            });

            System.out.println("\n✓ Ready for security scanning!");

        } catch (Exception e) {
            System.out.println("FAILED");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }

        if (openAPI != null)
            try
            {
                int size = endpoints.size();
                System.out.println("Test endpoints : " + size);

                for (ApiEndpoint endpoint: endpoints){
                    //System.out.println("Endpoint: " + endpoint.getPath() + " => " + endpoint.getMethod());
                    //System.out.println("Parameters: " + endpoint.getOperation().getParameters());
                    //System.out.println("Operation() " + endpoint.getOperation());

                    boolean result = hasAuthentication(endpoint, endpoint.hasGlobalSecurity);
                    endpoint.setHasAuth(result);
                    if (!result) {
                        String rick = assessRisk(endpoint);
                        //System.out.println("Assess: " + rick );
                        endpoint.setRisk(rick);
                    } else {
                        endpoint.setRisk("OK");
                        //System.out.println("Assess: " + endpoint.getRisk() );
                    }
                    //System.out.println("Auth: " + result);

                }

                doReport(endpoints);

            }catch (Exception e){
                System.out.println("FAILED TO CHECK THE OPERATION");
            }



    }


    public boolean hasAuthentication(ApiEndpoint endpoint, boolean hasGlobalSecurity){

        // start with parameter if it has something
        // can have header, query, path. only header is auth indication
        if(endpoint.getOperation().getParameters() != null){
            List <Parameter> params = endpoint.getOperation().getParameters();
            for (Parameter param: params){
                boolean isAuthHeader = "Authorization".equalsIgnoreCase(param.getName())
                        && "header".equalsIgnoreCase(param.getIn());
                if (isAuthHeader) {
                    return true;
                }
            }
        }

        // execute if not null, it can be empty [] or has something
        if(endpoint.getOperation().getSecurity() != null){
            if (endpoint.getOperation().getSecurity().isEmpty()){
                return false;
            } else {
                return true;
            }
        }
        // fallback to global security flag if parameter auth is not found
        // if security field null
        return hasGlobalSecurity;
    }

    public String assessRisk(ApiEndpoint endpoint){

        boolean isDangerous = isDangerousMethod(endpoint.getMethod());
        boolean isSensitive = isSensitivePath(endpoint.getPath());
        if(isDangerous && isSensitive){
            return "CRITICAL";
        }
        if(isDangerous || isSensitive){
            return "HIGH";
        }
        if(endpoint.getMethod().equals("POST")){
            return "MEDIUM";
        }
        return "LOW";
    }

    public boolean isDangerousMethod(String method){
        return method.equals("TRACE")||method.equals("PUT")||method.equals("PATCH")||method.equals("DELETE");
    }

    public boolean isSensitivePath(String path){
        String lowerPath = path.toLowerCase();
        return lowerPath.contains("admin")
                || lowerPath.contains("user")
                || lowerPath.contains("users")
                || lowerPath.contains("account")
                || lowerPath.contains("accounts")
                || lowerPath.contains("password")
                || lowerPath.contains("wallet")
                || lowerPath.contains("profile")
                || lowerPath.contains("transaction")
                || lowerPath.contains("transactions")
                || lowerPath.contains("billing")
                || lowerPath.contains("credit")
                || lowerPath.contains("token")
                || lowerPath.contains("payment");
    }

    public void doReport(List <ApiEndpoint> endpoints){

        List <ApiEndpoint> orderedEndpoints = new ArrayList<>(endpoints);
        orderedEndpoints.sort((a, b) -> b.getRiskScore() - a.getRiskScore());

        System.out.println("\n=== Security Scan Report ===\n");

        orderedEndpoints.forEach(endpoint -> {
            System.out.println("["+endpoint.getRisk() + "] Endpoint: " + endpoint.getPath() + " => " + endpoint.getMethod());
        });

        int totalScore = orderedEndpoints.stream().mapToInt(ApiEndpoint::getRiskScore).sum();
        long totalProtected = endpoints.stream()
                .filter(ApiEndpoint::isHasAuth)
                .count();
        System.out.println("\n=== Summary ===");
        System.out.println("Total endpoints: " + endpoints.size());
        System.out.println("Total protected endpoints: " + totalProtected);
        System.out.println("Total unprotected endpoints: " + (endpoints.size() - totalProtected));
        System.out.println("Total risk score: " + totalScore);
        System.out.println("Final assessment: " + getOverallRiskLevel(totalScore, endpoints.size()));

    }

    private String getOverallRiskLevel(int totalScore, int endpointCount) {
        double average = (double) totalScore / endpointCount;

        if (average >= 4.0) return "CRITICAL - Immediate action required";
        if (average >= 3.0) return "HIGH - Significant security gaps";
        if (average >= 2.0) return "MEDIUM - Some improvements needed";
        if (average >= 1.5) return "LOW - Minor issues";
        return "GOOD - Well secured";
    }

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
        @Getter
        @Setter
        private boolean hasGlobalSecurity;
        @Getter
        @Setter
        private boolean hasAuth = false;

        @Getter
        private String risk;

        @Getter
        private int riskScore;

        public ApiEndpoint(String path, String method, Operation operation, boolean hasGlobalSecurity) {
            this.path = path;
            this.method = method;
            this.operation = operation;
            this.hasGlobalSecurity = hasGlobalSecurity;
        }

        //custom setter for risk

        public void setRisk (String risk){
            this.risk = risk;
            this.riskScore = calculateScore(risk);
        }

        public int calculateScore(String risk){

            switch (risk) {
                case "CRITICAL": return 5;
                case "HIGH": return 4;
                case "MEDIUM": return 3;
                case "LOW": return 2;
                case "OK": return 1;
                default: return 0;
            }

        }
        public String getPath() { return path; }
        public String getMethod() { return method; }
        public Operation getOperation() { return operation; }

    }


}
