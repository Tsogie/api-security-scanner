package net.tool.service;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.parameters.Parameter;
import lombok.extern.slf4j.Slf4j;
import net.tool.model.ApiEndpoint;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class SecurityAnalyzer {

    // add null safety
    public List<ApiEndpoint> analyze(OpenAPI openAPI) {

        List<ApiEndpoint> endpoints = new ArrayList<>();

        // Check if security schemes are defined in components
        boolean hasSecuritySchemes = openAPI.getComponents() != null
                && openAPI.getComponents().getSecuritySchemes() != null
                && !openAPI.getComponents().getSecuritySchemes().isEmpty();


        Set<String> definedSchemes = getDefinedSchemes(openAPI);

        log.info("###Security Schemes: {}", hasSecuritySchemes);
        log.info("###Security Schemes: {}", definedSchemes);

        boolean hasGlobalSecurity = openAPI.getSecurity() != null &&
                !openAPI.getSecurity().isEmpty();

        log.info("###Global security: {}", hasGlobalSecurity);
        if (openAPI.getSecurity() != null && !openAPI.getSecurity().isEmpty()) {
            for (SecurityRequirement securityRequirement : openAPI.getSecurity()) {
                Set<String> globalSchemes = securityRequirement.keySet();
                log.info("###Global Security Requirements: {}", globalSchemes);
            }
        }
        boolean hasValidGlobalSecurity = hasValidSecurity(openAPI.getSecurity(), definedSchemes);
        log.info("###Valid Global security: {}", hasValidGlobalSecurity);
        //List<SecurityRequirement> security = openAPI.getSecurity();

        // check global security against security scheme definition
        //boolean hasGlobalSecurity = hasValidGlobalSecurity(openAPI);

        openAPI.getPaths().forEach((path, pathItem) -> {
            log.info("Path: {}", path);

            if (pathItem.getGet() != null) {
                //log.info("- GET: {}", pathItem.getGet().getOperationId());
                endpoints.add(new ApiEndpoint(path, "GET", pathItem.getGet()));
            }
            if (pathItem.getPost() != null) {
                //log.info("- POST: {}", pathItem.getPost().getOperationId());
                endpoints.add(new ApiEndpoint(path, "POST", pathItem.getPost()));
            }
            if (pathItem.getPut() != null) {
                //log.info("- PUT: {}", pathItem.getPut().getOperationId());
                endpoints.add(new ApiEndpoint(path, "PUT", pathItem.getPut()));
            }
            if (pathItem.getDelete() != null) {
                //log.info("- DELETE: {}", pathItem.getDelete().getOperationId());
                endpoints.add(new ApiEndpoint(path, "DELETE", pathItem.getDelete()));
            }
            if (pathItem.getPatch() != null) {
                //log.info("- PATCH: {}", pathItem.getPatch().getOperationId());
                endpoints.add(new ApiEndpoint(path, "PATCH", pathItem.getPatch()));
            }
            if (pathItem.getTrace() != null) {
                //log.info("- TRACE: {}", pathItem.getTrace().getOperationId());
                endpoints.add(new ApiEndpoint(path, "TRACE", pathItem.getTrace()));
            }
        });

        for (ApiEndpoint endpoint: endpoints){
            //log.info("Endpoint: {}  =>  {}", endpoint.getPath(), endpoint.getMethod());
            //log.info("Parameters: {}", endpoint.getOperation().getParameters());
            //log.info("Operation(): {}", endpoint.getOperation());

            // Debug logging
            log.info("Checking endpoint: {} {}", endpoint.getMethod(), endpoint.getPath());
            //log.info("Global security: {}", hasGlobalSecurity);
            log.info("Operation security: {}", endpoint.getOperation().getSecurity());

            boolean result = hasAuthentication(endpoint, hasValidGlobalSecurity, definedSchemes);
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

    private Set<String> getDefinedSchemes(OpenAPI openAPI) {
        if (openAPI.getComponents() != null
                && openAPI.getComponents().getSecuritySchemes() != null) {
            return openAPI.getComponents().getSecuritySchemes().keySet();
        }
        return Collections.emptySet();
    }

    public boolean hasValidSecurity(List<SecurityRequirement> securityRequirements, Set<String> definedSchemes) {

        if(securityRequirements == null || securityRequirements.isEmpty()){
            return false;
        }

        for (SecurityRequirement req : securityRequirements) {

            if(req.isEmpty()){
                continue;
            }

            boolean allDefined = definedSchemes.containsAll(req.keySet());
            if(allDefined){
                log.info("  Valid security requirement found: {}", req.keySet());
                return true;
            } else {
                req.keySet().stream()
                        .filter(name -> !definedSchemes.contains(name))
                        .forEach(name -> log.warn("  Undefined security scheme referenced: '{}'", name));

            }
        }
        return false;
    }

    public boolean hasAuthentication(ApiEndpoint endpoint, boolean hasValidGlobalSecurity,
                                     Set<String> definedSchemes) {

        // start with parameter if it has something
        // header, query
        boolean authInParam = false;
        if(endpoint.getOperation().getParameters() != null){
            List <Parameter> params = endpoint.getOperation().getParameters();
            for (Parameter param: params){
                String paramName = param.getName().toLowerCase();
                String paramIn = param.getIn();

                if("header".equalsIgnoreCase(paramIn) && AUTH_HEADER_NAMES.contains(paramName)){
                    log.info("Auth detected: {} header", param.getName());
                    authInParam = true;

                }
                if("query".equalsIgnoreCase(paramIn) && AUTH_PARAM_NAMES.contains(paramName)){
                    log.info("Auth detected: {} query", param.getName());
                    authInParam = true;
                }
            }
        }
        log.info("Possible Auth In Param: {}", authInParam);

        // execute if not null, it can be empty [] or has something
        if (endpoint.getOperation().getSecurity() != null) {
            if (endpoint.getOperation().getSecurity().isEmpty()) {
                log.info("  Endpoint explicitly opts out of security (empty [])");
                return false;
            }
                boolean valid = hasValidSecurity(endpoint.getOperation().getSecurity(), definedSchemes);
                log.info("  Operation-level security valid: {}", valid);
                return valid;
        }
            // fallback to global security flag if parameter auth is not found
            // if security field null
        return hasValidGlobalSecurity;
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
