package vn.mavn.patientservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.mavn.patientservice.dto.MedicalRecordAddDto;
import vn.mavn.patientservice.dto.MedicalRecordDto;
import vn.mavn.patientservice.dto.qobject.QueryMedicalRecordDto;
import vn.mavn.patientservice.entity.MedicalRecord;

public interface MedicalRecordService {

  MedicalRecord addNew(MedicalRecordAddDto medicalRecordAddDto);

  MedicalRecordDto getById(Long id);

  Page<MedicalRecordDto> findAll(QueryMedicalRecordDto queryMedicalRecordDto, Pageable pageable);

}
