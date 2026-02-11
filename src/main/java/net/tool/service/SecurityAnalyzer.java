package net.tool.service;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;
import lombok.extern.slf4j.Slf4j;
import net.tool.model.ApiEndpoint;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SecurityAnalyzer {

    // add null safety
    public List<ApiEndpoint> analyze(OpenAPI openAPI) {

        List<ApiEndpoint> endpoints = new ArrayList<>();

        // empty or null
        boolean hasGlobalSecurity = openAPI.getSecurity() != null
                && !openAPI.getSecurity().isEmpty();

        log.info("Global security: {}", hasGlobalSecurity);

        openAPI.getPaths().forEach((path, pathItem) -> {
            log.info("Path: {}", path);

            if (pathItem.getGet() != null) {
                log.info("- GET: {}", pathItem.getGet().getOperationId());
                endpoints.add(new ApiEndpoint(path, "GET", pathItem.getGet()));
            }
            if (pathItem.getPost() != null) {
                log.info("- POST: {}", pathItem.getPost().getOperationId());
                endpoints.add(new ApiEndpoint(path, "POST", pathItem.getPost()));
            }
            if (pathItem.getPut() != null) {
                log.info("- PUT: {}", pathItem.getPut().getOperationId());
                endpoints.add(new ApiEndpoint(path, "PUT", pathItem.getPut()));
            }
            if (pathItem.getDelete() != null) {
                log.info("- DELETE: {}", pathItem.getDelete().getOperationId());
                endpoints.add(new ApiEndpoint(path, "DELETE", pathItem.getDelete()));
            }
            if (pathItem.getPatch() != null) {
                log.info("- PATCH: {}", pathItem.getPatch().getOperationId());
                endpoints.add(new ApiEndpoint(path, "PATCH", pathItem.getPatch()));
            }
            if (pathItem.getTrace() != null) {
                log.info("- TRACE: {}", pathItem.getTrace().getOperationId());
                endpoints.add(new ApiEndpoint(path, "TRACE", pathItem.getTrace()));
            }
        });

        for (ApiEndpoint endpoint: endpoints){
            //log.info("Endpoint: {}  =>  {}", endpoint.getPath(), endpoint.getMethod());
            //log.info("Parameters: {}", endpoint.getOperation().getParameters());
            //log.info("Operation(): {}", endpoint.getOperation());

            boolean result = hasAuthentication(endpoint, hasGlobalSecurity);
            endpoint.setHasAuth(result);
            if (!result) {
                String rick = assessRisk(endpoint);
                endpoint.setRisk(rick);
            } else {
                endpoint.setRisk("OK");
            }
        }
        return endpoints;
    }

    public boolean hasAuthentication(ApiEndpoint endpoint, boolean hasGlobalSecurity){

        // start with parameter if it has something
        // header, query
        if(endpoint.getOperation().getParameters() != null){
            List <Parameter> params = endpoint.getOperation().getParameters();
            for (Parameter param: params){
                String paramName = param.getName().toLowerCase();
                String paramIn = param.getIn();

                if("header".equalsIgnoreCase(paramIn) && AUTH_HEADER_NAMES.contains(paramName)){
                    log.info("Auth detected: {} header", param.getName());
                    return true;
                }
                if("query".equalsIgnoreCase(paramIn) && AUTH_PARAM_NAMES.contains(paramName)){
                    log.info("Auth detected: {} query", param.getName());
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

    // Common auth header names
    private static final List<String> AUTH_HEADER_NAMES = List.of(
            "authorization",      // Standard OAuth/JWT
            "x-api-key",         // Common API key header
            "apikey",            // Alternative API key header
            "api-key",           // Another variant
            "x-auth-token",      // Custom token header
            "x-access-token"    // Access token variant
    );

    // Common auth query parameter names
    private static final List<String> AUTH_PARAM_NAMES = List.of(
            "api_key",           // Google Maps, OpenWeatherMap
            "apikey",            // Alternative spelling
            "access_token",      // OAuth 2.0 query param
            "token",             // Generic token
            "key",               // Simple key param
            "appid",             // OpenWeatherMap specific
            "auth"               // Generic auth param
    );

    private static final List<String> SENSITIVE_KEYWORDS =
            List.of("admin", "user", "account", "password", "wallet",
                    "profile", "transaction", "billing", "credit", "token", "payment");

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
        return SENSITIVE_KEYWORDS.stream().anyMatch(lowerPath::contains);
    }
}
