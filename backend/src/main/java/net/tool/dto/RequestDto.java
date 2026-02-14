package net.tool.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RequestDto {


    @NotBlank(message = "Spec URL is required")
    @URL(message = "Spec URL should be valid URL")
//    @Pattern(
//            regexp = "^http?://.+\\..+",
//            message = "Spec URL must contain http or https scheme"
//    )
    private String specUrl;

}
