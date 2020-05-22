package vn.mavn.patientservice.dto.qobject;

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
public class QueryDiseaseDto {

  private String name;
  private Boolean isActive;
  private Long clinicId;
}
