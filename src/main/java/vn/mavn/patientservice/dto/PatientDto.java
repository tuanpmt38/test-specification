package vn.mavn.patientservice.dto;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDto {

  private Long id;
  @NotBlank(message = "err-patient-name-is-mandatory")
  private String name;
  @NotNull(message = "err-patient-age-is-mandatory")
  private Integer age;
  @NotBlank(message = "err-patient-address-is-mandatory")
  private String address;
  @Pattern(regexp = "^(\\+84|0)((2[0-9]{9})|((3|5|7|8|9){1}([0-9]{8})))$",
      message = "err-patient-phone-invalid")
  @NotBlank(message = "err-patient-phone-is-mandatory")
  private String phone;
  @Pattern(regexp = "^(\\+84|0)((2[0-9]{9})|((3|5|7|8|9){1}([0-9]{8})))$",
      message = "err-patient-za-lo-phone-invalid")
  private String zaloPhone;
  @Pattern(regexp = "^(\\+84|0)((2[0-9]{9})|((3|5|7|8|9){1}([0-9]{8})))$",
      message = "err-patient-other-phone-invalid")
  private String otherPhone;
  @NotNull(message = "err-patient-is-active-is-mandatory")
  private Boolean isActive;
  private Long provinceCode;
  private List<Long> pathologyIds;
  private String code;
}
