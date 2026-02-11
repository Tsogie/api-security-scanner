package net.tool.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.tool.dto.RequestDto;
import net.tool.dto.ResponseDto;
import net.tool.component.UrlValidator;
import net.tool.model.Report;
import net.tool.service.ScanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/scan")
@Slf4j
public class ScanController {

    private final ScanService scanService;

    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    @PostMapping("/validate")
    public ResponseEntity<ResponseDto> validate(@RequestBody @Valid RequestDto request) throws Exception {

        // get urls from request
        String specUrl = request.getSpecUrl();
        String targetUrl = request.getTargetUrl();

        // do scanning
        Report report = scanService.scan(specUrl, targetUrl);
        return ResponseEntity.ok().body(ResponseDto.success(report));
    }
}
