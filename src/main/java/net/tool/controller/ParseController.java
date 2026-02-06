package net.tool.controller;

import net.tool.dto.RequestDto;
import net.tool.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParseController {

    private RequestService requestService;

    @Autowired
    public ParseController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping("/check")
    public ResponseEntity<String> checkReachability(@RequestBody RequestDto requestDto) {
        String response = requestService.check(requestDto);
        return ResponseEntity.ok(response);
    }

}
