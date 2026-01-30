package com.arn.ycyw.your_car_your_way.controller;

import com.arn.ycyw.your_car_your_way.dto.AgencyDto;
import com.arn.ycyw.your_car_your_way.services.AgencyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agency")
public class AgencyController {
    private final AgencyService agencyService;

    public AgencyController(AgencyService agencyService) {
        this.agencyService = agencyService;
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String,Object>> save(@Valid @RequestBody AgencyDto agencyDto) {
        AgencyDto agencySaved = agencyService.save(agencyDto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(agencySaved.toString())
                .toUri();

        Map<String,Object> body = new HashMap<>();
        body.put("message", "agency created !");
        body.put("agency :", agencySaved);

        return ResponseEntity
                .created(location)
                .body(body);
    }
    @GetMapping("/{id}")
    public ResponseEntity<AgencyDto> findById(@PathVariable("id") int id) {
        AgencyDto agencyDto = agencyService.finById(id);
        return ResponseEntity.ok().body(agencyDto);
    }
    @GetMapping("/all")
    public ResponseEntity<List<AgencyDto>> findAll() {
        List<AgencyDto> agencyDtos = agencyService.findAll();
        return ResponseEntity.ok().body(agencyDtos);
    }
}
