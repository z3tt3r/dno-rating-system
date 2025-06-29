package cz.michalmusil.dnoratingsystem.controller;

import cz.michalmusil.dnoratingsystem.dto.ClientResponseDto;
import cz.michalmusil.dnoratingsystem.dto.RiskRequestDto;
import cz.michalmusil.dnoratingsystem.dto.RiskResponseDto;
import cz.michalmusil.dnoratingsystem.model.Client; // Stále potřebujeme pro getAllClients
import cz.michalmusil.dnoratingsystem.model.Risk; // Stále potřebujeme pro getAllRisks
import cz.michalmusil.dnoratingsystem.repository.ClientRepository; // Stále potřebujeme pro getAllClients
import cz.michalmusil.dnoratingsystem.repository.RiskRepository; // Stále potřebujeme pro getAllRisks
import cz.michalmusil.dnoratingsystem.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/risks")
public class RiskController {

    private final RatingService ratingService;
    private final ClientRepository clientRepository; // Ponecháme pro getAllClients
    private final RiskRepository riskRepository; // Ponecháme pro getAllRisks

    public RiskController(RatingService ratingService, ClientRepository clientRepository, RiskRepository riskRepository) {
        this.ratingService = ratingService;
        this.clientRepository = clientRepository;
        this.riskRepository = riskRepository;
    }

    /**
     * Zpracuje požadavek na výpočet pojistného pro riziko.
     * Přijímá RiskRequestDto, deleguje výpočet na RatingService
     * a vrátí RiskResponseDto.
     *
     * @param riskRequestDto DTO obsahující data o riziku a klientovi.
     * @return ResponseEntity s vypočítaným rizikem (RiskResponseDto) nebo chybovou zprávou.
     */
    @PostMapping("/calculate")
    public ResponseEntity<?> calclulateRiskPremium(@Valid @RequestBody RiskRequestDto riskRequestDto) {
        try {
            // Celá logika konverze a výpočtu je nyní v RatingService
            RiskResponseDto riskResponseDto = ratingService.calculateNettoPremium(riskRequestDto);
            return new ResponseEntity<>(riskResponseDto, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Zde by bylo lepší vrátit ErrorResponse DTO s konkrétní chybovou zprávou
            // Namísto "An error occurred", která není moc informativní pro klienta
            return new ResponseEntity<>("Chyba při výpočtu pojistného: " + e.getMessage(), HttpStatus.BAD_REQUEST); // Lepší stavový kód
        } catch (Exception e) {
            // Obecná chyba serveru
            return new ResponseEntity<>("Nastala neočekávaná chyba serveru: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<RiskResponseDto>> getAllRisks() {
        // Delegujeme získání a konverzi rizik na servisní vrstvu (pokud má RatingService takovou metodu)
        // Nebo to uděláme zde, ale je lepší to mít v servisu.
        // Pro jednoduchost a rychlou opravu: ponecháme to zde, ale v ideálním případě by RatingService měl mít metodu getAllRiskResponses()
        List<Risk> risks = riskRepository.findAll();
        List<RiskResponseDto> dtos = risks.stream()
                .map(RiskResponseDto::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping("/clients")
    public ResponseEntity<List<ClientResponseDto>> getAllClients() {
        List<Client> clients = clientRepository.findAll(); // findAll returns List v Spring Data JPA 3.x
        List<ClientResponseDto> dtos = clients.stream()
                .map(ClientResponseDto::fromEntity)
                .collect(Collectors.toList());
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }
}