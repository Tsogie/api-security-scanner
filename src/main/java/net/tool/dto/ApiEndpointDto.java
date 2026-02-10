package net.tool.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ApiEndpointDto {

    private String path;
    private String method;
    private boolean hasAuth;
    private String risk;
    private int riskScore;

}
