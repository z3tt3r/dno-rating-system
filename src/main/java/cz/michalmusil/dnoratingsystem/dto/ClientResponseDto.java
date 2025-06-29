package cz.michalmusil.dnoratingsystem.dto;

import cz.michalmusil.dnoratingsystem.model.Client;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponseDto {

    private Long id;
    private String name;
    private String street;
    private String houseNumber;
    private String orientationNumber; // Volitelný, může být null
    private String city;
    private String postcode;
    private String state;
    private String ico;

    /**
     * Konvertuje Client entitu na ClientResponseDto.
     * Toto DTO je určeno pro odesílání dat klienta na frontend.
     *
     * @param client Entita Client, která má být konvertována.
     * @return ClientResponseDto s daty z entity, nebo null, pokud je vstupní entita null.
     */
    public static ClientResponseDto fromEntity(Client client) {
        if (client == null) {
            return null;
        }
        ClientResponseDto dto = new ClientResponseDto();
        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setStreet(client.getStreet());
        dto.setHouseNumber(client.getHouseNumber());
        dto.setOrientationNumber(client.getOrientationNumber()); // Bude null, pokud entita má null
        dto.setCity(client.getCity());
        dto.setPostcode(client.getPostcode());
        dto.setState(client.getState());
        dto.setIco(client.getIco());
        return dto;
    }
}