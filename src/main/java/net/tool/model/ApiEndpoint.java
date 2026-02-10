package net.tool.model;

import io.swagger.v3.oas.models.Operation;
import lombok.Getter;
import lombok.Setter;

public class ApiEndpoint {

    private final String path;
    private final String method;
    private final Operation operation;

    @Getter
    @Setter
    private boolean hasAuth = false;

    @Getter
    private String risk;

    @Getter
    private int riskScore;
    public ApiEndpoint(String path, String method, Operation operation) {
        this.path = path;
        this.method = method;
        this.operation = operation;

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
