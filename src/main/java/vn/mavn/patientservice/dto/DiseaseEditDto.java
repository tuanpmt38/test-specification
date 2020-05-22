package vn.mavn.patientservice.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
public class DiseaseEditDto {

  @NotNull(message = "err.diseases.disease-id-is-mandatory")
  private Long id;
  @NotBlank(message = "err.diseases.disease-name-is-mandatory")
  private String name;
  private String description;
  private Boolean isActive;
}
