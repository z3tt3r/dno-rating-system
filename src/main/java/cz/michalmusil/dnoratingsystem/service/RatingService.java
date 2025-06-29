package cz.michalmusil.dnoratingsystem.service;

import cz.michalmusil.dnoratingsystem.model.Risk;
import cz.michalmusil.dnoratingsystem.model.Client; // Nový import pro Client
import cz.michalmusil.dnoratingsystem.repository.RiskRepository;
import cz.michalmusil.dnoratingsystem.repository.ClientRepository; // Nový import pro ClientRepository
// import cz.michalmusil.dnoratingsystem.model.ActivityType; // Stále zakomentováno, dokud jej neimplementujeme
import cz.michalmusil.dnoratingsystem.dto.RiskRequestDto;
import cz.michalmusil.dnoratingsystem.dto.RiskResponseDto;
import cz.michalmusil.dnoratingsystem.dto.ClientRequestDto;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional; // Pro práci s Optional z repository

@Service
public class RatingService {

    private final RiskRepository riskRepository;
    private final ClientRepository clientRepository; // Nová závislost

    public RatingService(RiskRepository riskRepository, ClientRepository clientRepository) { // Přidáno do konstruktoru
        this.riskRepository = riskRepository;
        this.clientRepository = clientRepository;
    }

    // --- Konfigurační konstanty pro rater ---
    private static final BigDecimal MAX_LIMIT_AMOUNT = new BigDecimal("50000000"); // 50 milionů Kč
    private static final BigDecimal TURNOVER_CAP = new BigDecimal("1000000000"); // 1 miliarda Kč

    // Koeficienty pro obrat
    private static final BigDecimal TURNOVER_THRESHOLD_100M = new BigDecimal("100000000"); // 100 milionů
    private static final BigDecimal TURNOVER_THRESHOLD_500M = new BigDecimal("500000000"); // 500 milionů
    private static final BigDecimal TURNOVER_THRESHOLD_750M = new BigDecimal("750000000"); // 750 milionů

    // Koeficienty pro obrat
    private static final BigDecimal COEFFICIENT_UP_TO_100M = new BigDecimal("0.0001");
    private static final BigDecimal COEFFICIENT_100M_TO_500M = new BigDecimal("0.00005");
    private static final BigDecimal COEFFICIENT_500M_TO_750M = new BigDecimal("0.00002");
    private static final BigDecimal COEFFICIENT_ABOVE_750M = new BigDecimal("0.00001");

    // Slevové koeficienty pro pásma limitů
    private static final BigDecimal DISCOUNT_10_20M = new BigDecimal("0.05"); // 5% sleva (tj. multiplikátor 0.95)
    private static final BigDecimal DISCOUNT_20_30M = new BigDecimal("0.065"); // 6.5% sleva (tj. multiplikátor 0.935)
    private static final BigDecimal DISCOUNT_30_40M = new BigDecimal("0.075"); // 7.5% sleva (tj. multiplikátor 0.925)
    private static final BigDecimal DISCOUNT_40_50M = new BigDecimal("0.085"); // 8.5% sleva (tj. multiplikátor 0.915)


    public RiskResponseDto calculateNettoPremium(RiskRequestDto requestDto) {

        // Konverze vstupních dat na BigDecimal a plné hodnoty
        BigDecimal limitAmount = requestDto.getLimitInMillions().multiply(new BigDecimal("1000000"));
        BigDecimal turnover = requestDto.getTurnoverInThousands().multiply(new BigDecimal("1000"));
        BigDecimal brokerCommissionPercentage = new BigDecimal(String.valueOf(requestDto.getBrokerCommissionPercentage()));

        // --- ZDE JE NOVÁ LOGIKA PRO PRÁCI S KLIENTEM ---
        ClientRequestDto clientDto = requestDto.getClient();
        Client clientEntity;

        // Pokusíme se najít klienta podle IČO
        Optional<Client> existingClient = clientRepository.findByIco(clientDto.getIco());

        if (existingClient.isPresent()) {
            // Klient existuje, použijeme ho
            clientEntity = existingClient.get();
            // Můžete zde volitelně aktualizovat ostatní údaje klienta, pokud se změnily
            clientEntity.setName(clientDto.getName());
            clientEntity.setStreet(clientDto.getStreet());
            clientEntity.setHouseNumber(clientDto.getHouseNumber());
            clientEntity.setOrientationNumber(clientDto.getOrientationNumber());
            clientEntity.setCity(clientDto.getCity());
            clientEntity.setPostcode(clientDto.getPostcode());
            clientEntity.setState(clientDto.getState());
            clientRepository.save(clientEntity); // Uložíme případné změny
        } else {
            // Klient neexistuje, vytvoříme nového
            clientEntity = new Client();
            clientEntity.setName(clientDto.getName());
            clientEntity.setStreet(clientDto.getStreet());
            clientEntity.setHouseNumber(clientDto.getHouseNumber());
            clientEntity.setOrientationNumber(clientDto.getOrientationNumber());
            clientEntity.setCity(clientDto.getCity());
            clientEntity.setPostcode(clientDto.getPostcode());
            clientEntity.setState(clientDto.getState());
            clientEntity.setIco(clientDto.getIco());
            clientEntity = clientRepository.save(clientEntity); // Uložíme nového klienta, aby získal ID
        }
        // ---------------------------------------------

        // 1. Validace maximálního limitu
        if (limitAmount.compareTo(MAX_LIMIT_AMOUNT) > 0) {
            throw new IllegalArgumentException("Limit amount (" + limitAmount + " CZK) cannot exceed " + MAX_LIMIT_AMOUNT + " CZK.");
        }

        // 2. Aplikace obratového capu
        BigDecimal effectiveTurnover = turnover.min(TURNOVER_CAP);

        // 3. NOVÁ VALIDACE: LIMIT NESMÍ BÝT VĚTŠÍ NEŽ OBRAT
        if (limitAmount.compareTo(effectiveTurnover) > 0) {
            throw new IllegalArgumentException("Limit amount (" + limitAmount + " CZK) cannot be greater than effective turnover (" + effectiveTurnover + " CZK).");
        }

        // 4. Určení základní sazby za milion pro první pásmo (0-10M)
        BigDecimal turnoverCoefficient;
        if (effectiveTurnover.compareTo(TURNOVER_THRESHOLD_100M) <= 0) {
            turnoverCoefficient = COEFFICIENT_UP_TO_100M;
        } else if (effectiveTurnover.compareTo(TURNOVER_THRESHOLD_500M) <= 0) {
            turnoverCoefficient = COEFFICIENT_100M_TO_500M;
        } else if (effectiveTurnover.compareTo(TURNOVER_THRESHOLD_750M) <= 0) {
            turnoverCoefficient = COEFFICIENT_500M_TO_750M;
        } else {
            turnoverCoefficient = COEFFICIENT_ABOVE_750M;
        }

        BigDecimal baseRatePerMillion = new BigDecimal("1000000").multiply(turnoverCoefficient);

        // 5. Výpočet celkové prémie na základě limitu a pásmových slev (ILF model)
        BigDecimal totalLimitPremium = BigDecimal.ZERO;
        BigDecimal currentRatePerMillion = baseRatePerMillion;

        for (int i = 1; i <= limitAmount.divide(new BigDecimal("1000000"), RoundingMode.HALF_UP).intValue(); i++) {
            if (i == 11) {
                currentRatePerMillion = baseRatePerMillion.multiply(BigDecimal.ONE.subtract(DISCOUNT_10_20M));
            } else if (i == 21) {
                currentRatePerMillion = baseRatePerMillion
                        .multiply(BigDecimal.ONE.subtract(DISCOUNT_10_20M))
                        .multiply(BigDecimal.ONE.subtract(DISCOUNT_20_30M));
            } else if (i == 31) {
                currentRatePerMillion = baseRatePerMillion
                        .multiply(BigDecimal.ONE.subtract(DISCOUNT_10_20M))
                        .multiply(BigDecimal.ONE.subtract(DISCOUNT_20_30M))
                        .multiply(BigDecimal.ONE.subtract(DISCOUNT_30_40M));
            } else if (i == 41) {
                currentRatePerMillion = baseRatePerMillion
                        .multiply(BigDecimal.ONE.subtract(DISCOUNT_10_20M))
                        .multiply(BigDecimal.ONE.subtract(DISCOUNT_20_30M))
                        .multiply(BigDecimal.ONE.subtract(DISCOUNT_30_40M))
                        .multiply(BigDecimal.ONE.subtract(DISCOUNT_40_50M));
            }
            totalLimitPremium = totalLimitPremium.add(currentRatePerMillion);
        }

        // 6. Aplikace faktoru finanční výkonnosti
        BigDecimal financialPerformanceFactor;
        switch (requestDto.getFinancialPerformance()) {
            case BELOW_AVERAGE: financialPerformanceFactor = new BigDecimal("1.2"); break;
            case ABOVE_AVERAGE: financialPerformanceFactor = new BigDecimal("0.8"); break;
            case AVERAGE: default: financialPerformanceFactor = BigDecimal.ONE; break;
        }
        totalLimitPremium = totalLimitPremium.multiply(financialPerformanceFactor);

        // 7. Aplikace indexu aktivity - PROZATÍM PEVNĚ NA 1.0
        BigDecimal activityIndex = BigDecimal.ONE;
        totalLimitPremium = totalLimitPremium.multiply(activityIndex);

        // 8. Odečtení provize brokera
        BigDecimal brokerCommissionFactor = BigDecimal.ONE.subtract(brokerCommissionPercentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        BigDecimal nettoPremium = totalLimitPremium.multiply(brokerCommissionFactor);

        nettoPremium = nettoPremium.setScale(2, RoundingMode.HALF_UP);

        // 9. Vytvoření a uložení Risk entity
        Risk riskEntity = new Risk();
        riskEntity.setClient(clientEntity); // *** ZDE JE KLÍČOVÁ ZMĚNA ***
        //riskEntity.setClientName(requestDto.getClient().getName()); // TOTO UŽ NENÍ POTŘEBA
        //riskEntity.setClientId(requestDto.getClient().getIco()); // TOTO UŽ NENÍ POTŘEBA

        riskEntity.setActivity(requestDto.getActivity());
        riskEntity.setTurnover(turnover);
        riskEntity.setLimitAmount(limitAmount);
        riskEntity.setFinancialPerformance(requestDto.getFinancialPerformance());
        // Zde by mělo být nastaveno `brokerCommission` (bez "percentage")
        // Ale v RiskRequestDto to máš jako "brokerCommissionPercentage", což je správné pro vstup
        // Takže to jen mapuj na správné pole v Risk entitě.
        // Dle tvého Risk.java: private BigDecimal brokerCommission;
        riskEntity.setBrokerCommissionPercentage(brokerCommissionPercentage); // Zde se ukládá procento provize

        riskEntity.setNettoPremium(nettoPremium);
        riskEntity.setCalculationDate(LocalDateTime.now()); // Vyžaduje import LocalDateTime

        riskRepository.save(riskEntity);

        // 10. Vrácení RiskResponseDto
        RiskResponseDto responseDto = new RiskResponseDto(); // Použijeme prázdný konstruktor
        responseDto.setId(riskEntity.getId()); // Získáváme ID z uložené entity
        responseDto.setActivity(riskEntity.getActivity()); // Získáváme aktivitu z entity
        responseDto.setTurnoverInThousands(
                turnover.divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP)
        );
        responseDto.setLimitInMillions(
                limitAmount.divide(new BigDecimal("1000000"), 2, RoundingMode.HALF_UP)
        );
        responseDto.setFinancialPerformance(requestDto.getFinancialPerformance()); // Ponecháme jako enum, RiskResponseDto jej přijímá
        responseDto.setBrokerCommissionPercentage(brokerCommissionPercentage); // Toto je již BigDecimal
        responseDto.setNettoPremium(nettoPremium); // Toto je již BigDecimal

        // Nastavení informací o klientovi z requestDto (nebo přímo z clientEntity, pokud je potřeba)
        if (requestDto.getClient() != null) {
            responseDto.setClientId(clientEntity.getId()); // Vezmeme ID z uložené Client entity
            responseDto.setClientIco(requestDto.getClient().getIco());
            responseDto.setClientName(requestDto.getClient().getName());
        }

        return responseDto;
    }
}