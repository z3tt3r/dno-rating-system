package cz.michalmusil.dnoratingsystem.repository;

import cz.michalmusil.dnoratingsystem.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByIco(String ico);
}
