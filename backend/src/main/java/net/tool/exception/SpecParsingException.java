package net.tool.exception;

import lombok.Getter;

@Getter
public class SpecParsingException extends RuntimeException {
    private final String specUrl;
    public SpecParsingException(String message, String specUrl) {
        super(message);
        this.specUrl = specUrl;
    }
}
