package cz.michalmusil.dnoratingsystem.controller;

import cz.michalmusil.dnoratingsystem.dto.ClientRequestDto;
import cz.michalmusil.dnoratingsystem.dto.RiskRequestDto;
import cz.michalmusil.dnoratingsystem.dto.RiskResponseDto;
import cz.michalmusil.dnoratingsystem.model.Client;
import cz.michalmusil.dnoratingsystem.model.Risk;
import cz.michalmusil.dnoratingsystem.repository.ClientRepository;
import cz.michalmusil.dnoratingsystem.repository.RiskRepository;
import cz.michalmusil.dnoratingsystem.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/risks")
public class RiskController {

    private final RatingService ratingService;
    private final ClientRepository clientRepository;
    private final RiskRepository riskRepository;

    public RiskController(RatingService ratingService, ClientRepository clientRepository, RiskRepository riskRepository) {
        this.ratingService = ratingService;
        this.clientRepository = clientRepository;
        this.riskRepository = riskRepository;
    }

    /**
     * Zpracuje požadavek na výpočet pojistného pro riziko.
     * Přijímá RiskRequestDto, konvertuje ho na entitu Risk, provede výpočet
     * a vrátí RiskResponseDto.
     *
     * @param riskRequestDto DTO obsahující data o riziku a klientovi.
     * @return ResponseEntity s vypočítaným rizikem (RiskResponseDto) nebo chybovou zprávou.
     */
    @PostMapping("/calculate")
    public ResponseEntity<?> calclulateRiskPremium(@Valid @RequestBody RiskRequestDto riskRequestDto) {
        try {
            // First step: conversion DTO to entity Client
            ClientRequestDto clientDto = riskRequestDto.getClient();
            Client client = clientRepository.findByIco(clientDto.getIco())
                    .orElseGet(() -> {
                        Client newClient = new Client();
                        newClient.setName(clientDto.getName());
                        newClient.setStreet(clientDto.getStreet());
                        newClient.setHouseNumber(clientDto.getHouseNumber());
                        newClient.setOrientationNumber(clientDto.getOrientationNumber());
                        newClient.setCity(clientDto.getCity());
                        newClient.setPostcode(clientDto.getPostcode());
                        newClient.setState(clientDto.getState());
                        newClient.setIco(clientDto.getIco());
                        return clientRepository.save(newClient);
                    });
            // Second step: Conversion DTO to entity Risk and setting of conversion values
            Risk risk = new Risk();
            risk.setActivity(riskRequestDto.getActivity());
            risk.setTurnover(riskRequestDto.getTurnoverInFullAmount());
            risk.setLimitAmount(riskRequestDto.getLimitInFullAmount());
            risk.setFinancialPerformance(riskRequestDto.getFinancialPerformance());
            risk.setBrokerCommission(riskRequestDto.getBrokerCommissionAsDecimal());
            risk.setNettoPremium(BigDecimal.ZERO);
            risk.setClient(client);

            // Third step: Calling of service layer for calculation and saving
            Risk calculatedRisk = ratingService.calculateNettoPremium(risk);

            //Fourth step: Covnversion of outcome entity Risk to RiskResponeDto for return
            RiskResponseDto riskResponseDto = RiskResponseDto.fromEntity(calculatedRisk);

            return new ResponseEntity<>(riskResponseDto, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("An error aoccured " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<RiskResponseDto>> getAllRisks() {
        List<Risk> risk = riskRepository.findAll();
        List<RiskResponseDto> dtos = risk.stream()
                .map(RiskResponseDto::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping("/clients")
    public ResponseEntity<Iterable<Client>> getAllClients() {
        return new ResponseEntity<>(clientRepository.findAll(), HttpStatus.OK);
    }
}
