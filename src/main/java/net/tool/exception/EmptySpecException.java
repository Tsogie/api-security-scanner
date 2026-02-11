package net.tool.exception;

public class EmptySpecException extends SpecParsingException {
    public EmptySpecException(String specUrl) {
        super("OpenAPI spec contains no paths to analyze", specUrl);
    }
}
