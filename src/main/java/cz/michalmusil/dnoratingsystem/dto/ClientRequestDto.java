package cz.michalmusil.dnoratingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequestDto {

    @NotBlank(message = "Name is mandatory")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    @NotBlank(message = "Street is mandatory")
    @Size(max = 255, message = "Street cannot exceed 255 characters")
    private String street;

    @NotBlank(message = "House number is mandatory")
    @Size(max = 255, message = "House number cannot exceed 255 characters")
    private String houseNumber;

    @Size(max = 255, message = "Orientation number cannot exceed 255 characters")
    private String orientationNumber;

    @NotBlank(message = "City is mandatory")
    @Size(max = 255, message = "City cannot exceed 255 characters")
    private String city;

    @NotBlank(message = "Postcode is mandatory")
    @Size(min = 5, max = 10, message = "Postcode length must be between 5 and 10 characters")
    private String postcode;

    @NotBlank(message = "State is mandatory")
    @Size(max = 255, message = "Sate cannot exceed 255 characters")
    private String state;

    @NotBlank(message = "ICO is mandatory")
    @Size(min = 8, max = 8, message = "ICO must be 8 characters long")
    private String ico;

}
