package net.tool.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.tool.dto.RequestDto;
import net.tool.dto.ResponseDto;
import net.tool.model.Report;
import net.tool.service.ScanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
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

        // do scanning
        Report report = scanService.scan(specUrl);
        return ResponseEntity.ok().body(ResponseDto.success(report));
    }
}
