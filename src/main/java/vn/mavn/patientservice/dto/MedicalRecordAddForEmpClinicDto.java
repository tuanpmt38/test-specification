package vn.mavn.patientservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MedicalRecordAddForEmpClinicDto {

  @NotNull(message = "err-medical-record-advertising-source-id-is-mandatory")
  private Long advertisingSourceId;
  @NotNull(message = "err-medical-record-disease-id-is-mandatory")
  private Long diseaseId;
  @NotBlank(message = "err-medical-record-disease-status-is-mandatory")
  private String diseaseStatus;
  @NotBlank(message = "err-medical-record-consulting-status-code-is-mandatory")
  private String consultingStatusCode;
  private String note;
  @NotNull(message = "err-medical-record-clinic-id-is-mandatory")
  private Long clinicId;
  private String remedyType;
  private Long remedyAmount;
  private String remedies;
  private BigDecimal totalAmount;
  private BigDecimal transferAmount;
  private BigDecimal codAmount;
  private String extraNote;
  private Boolean isActive;
  @NotEmpty(message = "err-medical-record-medicineDtos-is-mandatory")
  private List<MedicineMappingDto> medicineDtos;
  @NotNull(message = "err-medical-record-patient-is-mandatory")
  @Valid
  private PatientDto patientDto;
  private Long clinicBranchId;
  private Long examinationTimes;
  @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
  @NotNull(message = "err.medical-records.consulting-date-is-mandatory")
  @Setter(AccessLevel.NONE)
  @Getter(AccessLevel.NONE)
  private Date examinationDate;

  public void setBillDate(Date examinationDate) {
    this.examinationDate = new Date(examinationDate.getTime());
  }

  public Date getExaminationDate() {
    return this.examinationDate = new Date(examinationDate.getTime());
  }
}
