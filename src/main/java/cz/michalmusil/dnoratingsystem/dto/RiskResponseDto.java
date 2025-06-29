package cz.michalmusil.dnoratingsystem.dto;

import cz.michalmusil.dnoratingsystem.model.Risk;
import cz.michalmusil.dnoratingsystem.model.FinancialPerformance; // Důležité pro enum
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode; // Důležité pro RoundingMode

@Data
@NoArgsConstructor
public class RiskResponseDto {

    private Long id;
    private String activity;
    private BigDecimal turnoverInThousands; // Aby odpovídalo frontend DTO
    private BigDecimal limitInMillions;     // Aby odpovídalo frontend DTO
    private FinancialPerformance financialPerformance;
    private BigDecimal brokerCommissionPercentage;
    private BigDecimal nettoPremium;
    private Long clientId;
    private String clientIco;
    private String clientName; // <-- PŘIDANÁ VLASTNOST

    public static RiskResponseDto fromEntity(Risk risk) {
        if (risk == null) {
            return null;
        }
        RiskResponseDto dto = new RiskResponseDto();
        dto.setId(risk.getId());
        dto.setActivity(risk.getActivity());
        dto.setTurnoverInThousands(risk.getTurnover().divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP));
        dto.setLimitInMillions(risk.getLimitAmount().divide(new BigDecimal("1000000"), 2, RoundingMode.HALF_UP));
        dto.setFinancialPerformance(risk.getFinancialPerformance());
        dto.setBrokerCommissionPercentage(risk.getBrokerCommissionPercentage().multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        dto.setNettoPremium(risk.getNettoPremium());

        if (risk.getClient() != null) {
            dto.setClientId(risk.getClient().getId());
            dto.setClientIco(risk.getClient().getIco());
            dto.setClientName(risk.getClient().getName()); // <-- PŘIDANÉ NASTAVENÍ
        }
        return dto;
    }
}