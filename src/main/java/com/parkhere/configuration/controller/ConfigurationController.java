package com.parkhere.configuration.controller;

import com.parkhere.configuration.model.ParkingSpot;
import com.parkhere.configuration.service.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class ConfigurationController {

    @Autowired
    private ConfigurationService configurationService;

    @GetMapping("/api/parking-lots/{parkingLotId}")
    public List<ParkingSpot> getParkingSpots(@PathVariable int parkingLotId) {
        log.info("Fetching parking spots for parkingLotId={}", parkingLotId);
        return configurationService.getParkingSpots(parkingLotId);
    }
}