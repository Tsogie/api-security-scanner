package net.tool.service;

import net.tool.component.EndpointMapper;
import net.tool.model.ApiEndpoint;
import net.tool.dto.ApiEndpointDto;
import net.tool.model.Report;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReportBuilder {

    private final EndpointMapper endpointMapper;
    public ReportBuilder(EndpointMapper endpointMapper) {
        this.endpointMapper = endpointMapper;
    }

    public Report buildReport(List<ApiEndpoint> endpoints, String title, String version) {

        List <ApiEndpoint> orderedEndpoints = new ArrayList<>(endpoints);
        orderedEndpoints.sort((a, b) -> b.getRiskScore() - a.getRiskScore());

        List<ApiEndpointDto> endpointDtos = endpointMapper.mapEndpoints(orderedEndpoints);

        int totalScore = orderedEndpoints.stream().mapToInt(ApiEndpoint::getRiskScore).sum();
        int totalProtected = (int)endpoints.stream()
                .filter(ApiEndpoint::isHasAuth)
                .count();
        int endpointCount = endpoints.size();
        int unprotectedEndpointCount = endpointCount - totalProtected;
        System.out.println("\n=== Summary ===");
        System.out.println("Total endpoints: " + endpointCount);
        System.out.println("Total protected endpoints: " + totalProtected);
        System.out.println("Total unprotected endpoints: " + (endpoints.size() - totalProtected));
        System.out.println("Total risk score: " + totalScore);
        String overallRiskLevel = getOverallRiskLevel(totalScore, endpointCount);
        System.out.println("Final assessment: " + overallRiskLevel);

        return new Report(title,
                version,
                endpointCount,
                totalProtected,
                unprotectedEndpointCount,
                totalScore,
                overallRiskLevel,
                endpointDtos);
    }
    private String getOverallRiskLevel(int totalScore, int endpointCount) {
        double average = (double) totalScore / endpointCount;

        if (average >= 4.0) return "CRITICAL - Immediate action required";
        if (average >= 3.0) return "HIGH - Significant security gaps";
        if (average >= 2.0) return "MEDIUM - Some improvements needed";
        if (average >= 1.5) return "LOW - Minor issues";
        return "GOOD - Well secured";
    }

}
