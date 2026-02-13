package net.tool.component;

import net.tool.model.ApiEndpoint;
import net.tool.dto.ApiEndpointDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EndpointMapper {

    public ApiEndpointDto mapEndpoint(ApiEndpoint endpoint) {
        return ApiEndpointDto.builder()
                .path(endpoint.getPath())
                .method(endpoint.getMethod())
                .hasAuth(endpoint.isHasAuth())
                .risk(endpoint.getRisk())
                .riskScore(endpoint.getRiskScore())
                .build();
    }

    public List<ApiEndpointDto> mapEndpoints(List<ApiEndpoint> endpoints) {
        return endpoints.stream()
                .map(this::mapEndpoint)
                .collect(Collectors.toList());
    }
}
