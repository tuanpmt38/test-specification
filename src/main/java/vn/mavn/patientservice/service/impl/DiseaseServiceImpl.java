package vn.mavn.patientservice.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import vn.mavn.patientservice.dto.DiseaseAddDto;
import vn.mavn.patientservice.dto.DiseaseEditDto;
import vn.mavn.patientservice.dto.qobject.QueryDiseaseDto;
import vn.mavn.patientservice.entity.ClinicDisease;
import vn.mavn.patientservice.entity.Disease;
import vn.mavn.patientservice.entity.MedicalRecord;
import vn.mavn.patientservice.entity.MedicineDisease;
import vn.mavn.patientservice.exception.ConflictException;
import vn.mavn.patientservice.exception.NotFoundException;
import vn.mavn.patientservice.repository.ClinicDiseaseRepository;
import vn.mavn.patientservice.repository.DiseaseRepository;
import vn.mavn.patientservice.repository.MedicalRecordRepository;
import vn.mavn.patientservice.repository.MedicineDiseaseRepository;
import vn.mavn.patientservice.repository.spec.DiseaseSpec;
import vn.mavn.patientservice.service.DiseaseService;
import vn.mavn.patientservice.util.TokenUtils;

@Service
public class DiseaseServiceImpl implements DiseaseService {

  @Autowired
  private DiseaseRepository diseaseRepository;

  @Autowired
  private ClinicDiseaseRepository clinicDiseaseRepository;

  @Autowired
  private MedicalRecordRepository medicalRecordRepository;

  @Autowired
  private MedicineDiseaseRepository medicineDiseaseRepository;

  @Autowired
  private HttpServletRequest httpServletRequest;

  @Override
  public Page<Disease> getAllDisease(QueryDiseaseDto data, Pageable pageable) {
    Page<Disease> diseases = null;
    List<Long> diseaseIds = new ArrayList<>();
    if (data == null) {
      Page.empty(pageable);
    } else {
      if (data.getClinicId() != null) {
        List<Long> diseaseIdForClinicId = clinicDiseaseRepository
            .findAllByClinicId(data.getClinicId());
        if (CollectionUtils.isEmpty(diseaseIdForClinicId)) {
          Page.empty(pageable);
        } else {
          diseaseIds.addAll(diseaseIdForClinicId);
        }
      }

      diseases = diseaseRepository.findAll(DiseaseSpec.findAllDiseases(data, diseaseIds), pageable);
      if (CollectionUtils.isEmpty(diseases.getContent())) {
        return Page.empty(pageable);
      }
    }
    return diseases;
  }

  @Override
  public Disease add(DiseaseAddDto data) {
    diseaseRepository.findByName(data.getName()).ifPresent(disease -> {
      throw new ConflictException(
          Collections.singletonList("err.diseases.disease-already-exists"));
    });
    Disease disease = new Disease();
    BeanUtils.copyProperties(data, disease);
    //Get user logged in ID
    Long loggedInUserId = Long.valueOf(TokenUtils.getUserIdFromToken(httpServletRequest));
    disease.setCreatedBy(loggedInUserId);
    disease.setUpdatedBy(loggedInUserId);
    disease.setIsActive(true);
    diseaseRepository.save(disease);
    return disease;
  }

  @Override
  public Disease update(DiseaseEditDto data) {
    Disease disease = diseaseRepository.findById(data.getId())
        .orElseThrow(() -> new NotFoundException(
            Collections.singletonList("err.diseases.disease-not-found")));
    diseaseRepository.findByName(data.getName()).ifPresent(d -> {
      if (!d.getId().equals(disease.getId())) {
        throw new ConflictException(
            Collections.singletonList("err.diseases.disease-already-exists"));
      }
    });
    BeanUtils.copyProperties(data, disease);
    //Get user logged in ID
    Long loggedInUserId = Long.valueOf(TokenUtils.getUserIdFromToken(httpServletRequest));
    disease.setUpdatedBy(loggedInUserId);
    disease.setIsActive(true);
    diseaseRepository.save(disease);
    return disease;
  }

  @Override
  public Disease detail(Long id) {
    return diseaseRepository.findById(id).orElseThrow(() -> new NotFoundException(
        Collections.singletonList("err.diseases.disease-not-found")));
  }

  @Override
  public void removeDisease(Long id) {
    diseaseRepository.findById(id).orElseThrow(() -> new NotFoundException(
        Collections.singletonList("err.diseases.disease-not-found")));
    List<ClinicDisease> clinicDiseases = clinicDiseaseRepository.findByDiseaseId(id);
    List<MedicalRecord> medicalRecords = medicalRecordRepository.findByDiseaseId(id);
    List<MedicineDisease> medicineDiseases = medicineDiseaseRepository.findAllByDiseaseId(id);
    if (!CollectionUtils.isEmpty(clinicDiseases) || !CollectionUtils.isEmpty(medicalRecords)
        || !CollectionUtils.isEmpty(medicineDiseases)) {
      throw new ConflictException(Collections.singletonList("err.diseases.cannot-remove-disease"));
    }
    diseaseRepository.deleteById(id);
  }
}
