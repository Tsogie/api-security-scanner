package net.tool.dto;

import lombok.*;
import net.tool.model.Report;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class ResponseDto {

    private boolean success;
    private String errorMessage;
    private Report report;

    // static factory methods to create response Dto
    public static ResponseDto success(Report report) {
        return ResponseDto.builder()
                .success(true)
                .report(report).build();
    }

    public static ResponseDto error(String errorMessage) {
        return ResponseDto.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
