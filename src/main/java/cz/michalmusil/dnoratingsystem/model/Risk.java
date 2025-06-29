package cz.michalmusil.dnoratingsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime; // <-- DŮLEŽITÝ IMPORT

@Entity
@Table(name = "risks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Risk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String activity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal turnover;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal limitAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinancialPerformance financialPerformance;

    @Column(name = "broker_commission_percentage", nullable = false, precision = 7, scale = 5)
    private BigDecimal brokerCommissionPercentage; // Přejmenováno pro jasnost

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal nettoPremium;

    @Column(name = "calculation_date", nullable = false) // Můžete nastavit nullable na false, pokud je vždy požadováno
    private LocalDateTime calculationDate;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

}