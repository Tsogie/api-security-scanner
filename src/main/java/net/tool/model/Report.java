package net.tool.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.tool.dto.ApiEndpointDto;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Report {

    private String apiTitle;
    private String apiVersion;
    private int totalEndpointCount;
    private int protectedCount;
    private int unprotectedCount;
    private int totalRiskScore;
    private String overallRiskLevel;
    private List<ApiEndpointDto> endpoints;


}
