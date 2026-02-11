package net.tool.model;

import io.swagger.v3.oas.models.Operation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiEndpoint {

    private final String path;
    private final String method;
    private final Operation operation;

    private boolean hasAuth = false;
    private String risk;
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

}
