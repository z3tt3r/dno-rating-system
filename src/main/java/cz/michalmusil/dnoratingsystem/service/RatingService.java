package cz.michalmusil.dnoratingsystem.service;

import cz.michalmusil.dnoratingsystem.model.Risk;
import cz.michalmusil.dnoratingsystem.repository.RiskRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RatingService {

    private final RiskRepository riskRepository;

    public RatingService(RiskRepository riskRepository) {
        this.riskRepository = riskRepository;
    }

    // --- Konfigurační konstanty pro rater ---
    private static final BigDecimal MAX_LIMIT_AMOUNT = new BigDecimal("50000000"); // 50 milionů Kč
    private static final BigDecimal TURNOVER_CAP = new BigDecimal("1000000000"); // 1 miliarda Kč

    // Koeficienty pro obrat
    private static final BigDecimal TURNOVER_LOW_THRESHOLD = new BigDecimal("500000"); // 100 tis.
    private static final BigDecimal TURNOVER_MEDIUM_THRESHOLD = new BigDecimal("1000000"); // 1 mil.
    private static final BigDecimal TURNOVER_HIGH_THRESHOLD = new BigDecimal("10000000"); // 10 mil.

    private static final BigDecimal COEFFICIENT_LOW_TURNOVER = new BigDecimal("0.0001"); // 0.01%
    private static final BigDecimal COEFFICIENT_MEDIUM_TURNOVER = new BigDecimal("0.00005"); // 0.005%
    private static final BigDecimal COEFFICIENT_HIGH_TURNOVER = new BigDecimal("0.00002"); // 0.002%
    private static final BigDecimal COEFFICIENT_VERY_HIGH_TURNOVER = new BigDecimal("0.00001"); // 0.001%

    // *** UPRAVENÉ PRAHOVÉ HODNOTY PRO OBRAT ***
    private static final BigDecimal TURNOVER_THRESHOLD_100M = new BigDecimal("100000000"); // 100 milionů
    private static final BigDecimal TURNOVER_THRESHOLD_500M = new BigDecimal("500000000"); // 500 milionů
    private static final BigDecimal TURNOVER_THRESHOLD_750M = new BigDecimal("750000000"); // 750 milionů

    // Koeficienty pro obrat (ponechány původní hodnoty, jen přejmenovány pro nové rozsahy)
    private static final BigDecimal COEFFICIENT_UP_TO_100M = new BigDecimal("0.0001");
    private static final BigDecimal COEFFICIENT_100M_TO_500M = new BigDecimal("0.00005");
    private static final BigDecimal COEFFICIENT_500M_TO_750M = new BigDecimal("0.00002");
    private static final BigDecimal COEFFICIENT_ABOVE_750M = new BigDecimal("0.00001");


    // Implementace logiky rateru
    public Risk calculateNettoPremium(Risk risk) {
        // 1. Validace maximálního limitu
        if (risk.getLimitAmount().compareTo(MAX_LIMIT_AMOUNT) > 0) {
            throw new IllegalArgumentException("Limit amount (" + risk.getLimitAmount() + " CZK) cannot exceed " + MAX_LIMIT_AMOUNT + " CZK.");
        }

        // 2. Aplikace obratového capu
        BigDecimal effectiveTurnover = risk.getTurnover().min(TURNOVER_CAP);

        // *** NOVÁ VALIDACE: LIMIT NESMÍ BÝT VĚTŠÍ NEŽ OBRAT ***
        if (risk.getLimitAmount().compareTo(effectiveTurnover) > 0) {
            throw new IllegalArgumentException("Limit amount (" + risk.getLimitAmount() + " CZK) cannot be greater than effective turnover (" + effectiveTurnover + " CZK).");
        }

        // 3. Výpočet pojistného na základě obratu (základní pojistné)
        BigDecimal basePremium;
        if (effectiveTurnover.compareTo(TURNOVER_THRESHOLD_100M) <= 0) { // Obrat <= 100 mil.
            basePremium = effectiveTurnover.multiply(COEFFICIENT_UP_TO_100M);
        } else if (effectiveTurnover.compareTo(TURNOVER_THRESHOLD_500M) <= 0) { // 100 mil. < Obrat <= 500 mil.
            basePremium = effectiveTurnover.multiply(COEFFICIENT_100M_TO_500M);
        } else if (effectiveTurnover.compareTo(TURNOVER_THRESHOLD_750M) <= 0) { // 500 mil. < Obrat <= 750 mil.
            basePremium = effectiveTurnover.multiply(COEFFICIENT_500M_TO_750M);
        } else { // Obrat > 750 mil. (až do capu 1 mld.)
            basePremium = effectiveTurnover.multiply(COEFFICIENT_ABOVE_750M);
        }

        // 4. Aplikace koeficientu rizikovosti (prozatím pevně, později to bude složitější)
        BigDecimal riskCoefficient = BigDecimal.ONE; // Výchozí: bez vlivu
        // Zde by se použila logika pro 3 kategorie rizikovosti, které určíme dodatečně

        BigDecimal finalPremium = basePremium.multiply(riskCoefficient);

        // 5. Nastavení vypočteného netto pojistného do objektu Risk
        risk.setNettoPremium(finalPremium.setScale(2, RoundingMode.HALF_UP));

        // 6. Uložíme aktualizovaný objekt Risk do databáze
        return riskRepository.save(risk);
    }
}
