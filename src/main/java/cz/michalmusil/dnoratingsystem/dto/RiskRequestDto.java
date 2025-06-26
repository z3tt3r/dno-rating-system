package cz.michalmusil.dnoratingsystem.dto;

import cz.michalmusil.dnoratingsystem.model.FinancialPerformance;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskRequestDto {

    @NotBlank(message = "Activity is mandatory")
    @Size(max = 255, message = "Activity cannot exceed 255 characters")
    private String activity;

    @NotNull(message = "Turnover is mandatory")
    @DecimalMin(value = "0.0", inclusive = true, message = "Turnover must be a non-negative value")
    private BigDecimal turnoverInThousands;

    @NotNull(message = "Limit is mandatory")
    @DecimalMin(value = "0.0", inclusive = true, message = "Limit must be a non-negative value")
    private BigDecimal limitInMillions;

    @NotNull(message = "Financial Performance is mandatory")
    private FinancialPerformance financialPerformance;

    @NotNull(message = "Broker comission is mandatory")
    @DecimalMin(value = "0.0", inclusive = true, message = "Broker comission muse be a non-negative value")
    private BigDecimal brokerCommissionPercentage;

    @Valid
    @NotNull(message = "Client data is mandatory")
    private ClientRequestDto client;

    // --Methods for conversion DTO to Entity-- //
    public BigDecimal getTurnoverInFullAmount() {
        if (turnoverInThousands == null) {
            return BigDecimal.ZERO;
        }
        return turnoverInThousands.multiply(new BigDecimal("1000"));
    }

    public BigDecimal getLimitInFullAmount() {
        if ((limitInMillions == null)) {
            return BigDecimal.ZERO;
        }
        return  limitInMillions.multiply(new BigDecimal("1000000"));
    }

    public BigDecimal getBrokerCommissionAsDecimal() {
        if (brokerCommissionPercentage == null) {
            return BigDecimal.ZERO;
        }
        return brokerCommissionPercentage.divide(new BigDecimal("100"));
    }
}
