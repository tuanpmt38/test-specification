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
public class QueryClinicDto {

  private String name;
  private String phone;
  private Long userId;
  private Boolean isActive;

}
