package cz.michalmusil.dnoratingsystem.dto;

import cz.michalmusil.dnoratingsystem.model.FinancialPerformance;
import cz.michalmusil.dnoratingsystem.model.Risk;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskResponseDto {

    private Long id;
    private String activity;
    private BigDecimal turnoverInThousands;
    private BigDecimal limitInMillions;
    private FinancialPerformance financialPerformance;
    private BigDecimal brokerCommissionPercentage;
    private BigDecimal nettoPremium;
    private Long clientId;
    private String clientIco;

    public static RiskResponseDto fromEntity(Risk risk) {
        if (risk == null) {
            return null;
        }
        RiskResponseDto dto = new RiskResponseDto();
        dto.setId(risk.getId());
        dto.setActivity(risk.getActivity());
        // Zde provedeme reverzní přepočet pro zobrazení uživateli
        dto.setTurnoverInThousands(risk.getTurnover().divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP));
        dto.setLimitInMillions(risk.getLimitAmount().divide(new BigDecimal("1000000"), 2, RoundingMode.HALF_UP));
        dto.setFinancialPerformance(risk.getFinancialPerformance());
        dto.setBrokerCommissionPercentage(risk.getBrokerCommission().multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        dto.setNettoPremium(risk.getNettoPremium());
        if (risk.getClient() != null) {
            dto.setClientId(risk.getClient().getId());
            dto.setClientIco(risk.getClient().getIco());
        }
        return dto;
    }
}
