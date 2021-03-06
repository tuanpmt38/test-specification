package vn.mavn.patientservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.mavn.patientservice.dto.ClinicAddDto;
import vn.mavn.patientservice.dto.ClinicDto;
import vn.mavn.patientservice.dto.ClinicEditDto;
import vn.mavn.patientservice.dto.qobject.QueryClinicDto;
import vn.mavn.patientservice.entity.Clinic;

public interface ClinicService {

  Clinic save(ClinicAddDto data);

  Clinic update(ClinicEditDto data);

  ClinicDto findById(Long id);

  Page<ClinicDto> findAllClinics(QueryClinicDto data, Pageable pageable);

  void delete(Long id);
}
