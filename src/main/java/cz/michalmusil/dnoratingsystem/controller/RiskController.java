package cz.michalmusil.dnoratingsystem.controller;

import cz.michalmusil.dnoratingsystem.model.Risk;
import cz.michalmusil.dnoratingsystem.repository.ClientRepository;
import cz.michalmusil.dnoratingsystem.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/risks")
public class RiskController {

    private final RatingService ratingService;
    private final ClientRepository clientRepository;

    public RiskController(RatingService ratingService, ClientRepository clientRepository) {
        this.ratingService = ratingService;
        this.clientRepository = clientRepository;
    }

    @PostMapping("/calculate")
    public ResponseEntity<?> calclulateRiskPremium(@RequestBody Risk risk) {
        try {
        }
    }


}
