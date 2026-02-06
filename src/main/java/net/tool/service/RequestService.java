package net.tool.service;

import net.tool.dto.RequestDto;
import org.springframework.stereotype.Service;

@Service
public class RequestService {

    public String check(RequestDto requestDto) {
        return "Yes";
    }
}
