package cz.michalmusil.dnoratingsystem.repository;

import cz.michalmusil.dnoratingsystem.model.Risk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiskRepository extends JpaRepository <Risk, Long> {


}
