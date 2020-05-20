package vn.mavn.patientservice.dto;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiseaseAddDto {

  @NotBlank(message = "err.diseases.disease-name-is-mandatory")
  private String name;
  private String description;
  private Boolean isActive;

}
