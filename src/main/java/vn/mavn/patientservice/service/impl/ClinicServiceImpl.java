package vn.mavn.patientservice.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import vn.mavn.patientservice.dto.ClinicAddDto;
import vn.mavn.patientservice.dto.ClinicDto;
import vn.mavn.patientservice.dto.ClinicDto.DoctorDto;
import vn.mavn.patientservice.dto.ClinicEditDto;
import vn.mavn.patientservice.dto.DiseaseDto;
import vn.mavn.patientservice.dto.qobject.QueryClinicDto;
import vn.mavn.patientservice.entity.Clinic;
import vn.mavn.patientservice.entity.ClinicDisease;
import vn.mavn.patientservice.entity.ClinicUser;
import vn.mavn.patientservice.entity.Disease;
import vn.mavn.patientservice.entity.Doctor;
import vn.mavn.patientservice.exception.ConflictException;
import vn.mavn.patientservice.exception.NotFoundException;
import vn.mavn.patientservice.repository.ClinicDiseaseRepository;
import vn.mavn.patientservice.repository.ClinicRepository;
import vn.mavn.patientservice.repository.ClinicUserRepository;
import vn.mavn.patientservice.repository.DiseaseRepository;
import vn.mavn.patientservice.repository.DoctorRepository;
import vn.mavn.patientservice.repository.spec.ClinicSpec;
import vn.mavn.patientservice.service.ClinicService;
import vn.mavn.patientservice.util.TokenUtils;

@Service
@Transactional
public class ClinicServiceImpl implements ClinicService {

  @Autowired
  private ClinicRepository clinicRepository;

  @Autowired
  private DoctorRepository doctorRepository;

  @Autowired
  private DiseaseRepository diseaseRepository;

  @Autowired
  private ClinicDiseaseRepository clinicDiseaseRepository;

  @Autowired
  private ClinicUserRepository clinicUserRepository;

  @Autowired
  private HttpServletRequest httpServletRequest;

  @Override
  public Clinic save(ClinicAddDto data) {
    //valid name and phone
    validationNameOrPhoneWhenAddClinic(data);

    //valid doctor
    validDoctor(data.getDoctorId());

    // Validate clinic disease
    validClinicDisease(data.getDiseaseIds());

    // Validate user with clinic
    validateUserWhenAddingClinic(data.getUserIds());

    Clinic clinic = new Clinic();
    BeanUtils.copyProperties(data, clinic);
    clinic.setName(data.getName().trim());
    //Get user logged in ID
    Long loggedInUserId = Long.valueOf(TokenUtils.getUserIdFromToken(httpServletRequest));
    clinic.setCreatedBy(loggedInUserId);
    clinic.setUpdatedBy(loggedInUserId);
    clinic.setIsActive(true);
    clinicRepository.save(clinic);

    //valid disease
    mappingClinicDisease(clinic, data.getDiseaseIds());

    //mapping clinic user
    mappingClinicUser(clinic, data.getUserIds());

    return clinic;
  }

  @Override
  public Clinic update(ClinicEditDto data) {

    Clinic clinic = clinicRepository.findById(data.getId())
            .orElseThrow(
                    () -> new NotFoundException(
                            Collections.singletonList("err.clinic.clinic-does-not-exist")));
    //valid name and phone
    validationNameOrPhoneWhenEditClinic(data);
    //valid doctor
    validDoctor(data.getDoctorId());

    // Validate clinic disease
    validClinicDisease(data.getDiseaseIds());

    // Validate user for clinic
    validateUserWhenEditingClinic(data.getId(), data.getUserIds());

    BeanUtils.copyProperties(data, clinic);
    clinic.setName(data.getName().trim());

    //Get user logged in ID
    clinic.setIsActive(true);
    Long loggedInUserId = Long.valueOf(TokenUtils.getUserIdFromToken(httpServletRequest));
    clinic.setUpdatedBy(loggedInUserId);
    clinicRepository.save(clinic);

    //delete mapping clinic disease
    clinicDiseaseRepository.deleteAllByClinicId(clinic.getId());
    //valid disease
    mappingClinicDisease(clinic, data.getDiseaseIds());

    //delete mapping clinic user
    clinicUserRepository.deleteAllByClinicId(clinic.getId());

    //mapping clinic user
    mappingClinicUser(clinic, data.getUserIds());

    return clinic;
  }

  private void mappingClinicUser(Clinic clinic, List<Long> userIds) {
    if (CollectionUtils.isEmpty(userIds)) {
      return;
    }

    Set<Long> setUserIds = new HashSet<>();
    if (!CollectionUtils.isEmpty(userIds)) {
      setUserIds.addAll(userIds);
    }

    setUserIds.forEach(user -> {
      ClinicUser clinicUser = ClinicUser.builder().clinicId(clinic.getId()).userId(user).build();
      clinicUserRepository.save(clinicUser);
    });
  }

  @Override
  public ClinicDto findById(Long id) {

    Clinic clinic = clinicRepository.findById(id).orElseThrow(
        () -> new NotFoundException(Collections.singletonList("err.clinic.clinic-does-not-exist")));

    //get doctor
    DoctorDto doctorDto = getDoctorDto(clinic);
    //get disease
    List<DiseaseDto> diseases = getDiseaseDtos(clinic);

    //get list userIds
    List<Long> userIds = clinicUserRepository.findAllUserIdByClinicId(clinic.getId());

    return getClinicDto(clinic, doctorDto, diseases, userIds);
  }

  private List<DiseaseDto> getDiseaseDtos(Clinic clinic) {
    List<DiseaseDto> diseases = new ArrayList<>();
    List<Long> diseasesIds = clinicDiseaseRepository.findAllDiseaseById(clinic.getId());
    diseasesIds.forEach(diseasesId -> {
      Disease disease = diseaseRepository.findDiseaseById(diseasesId);
      if (disease != null) {
        diseases.add(DiseaseDto.builder().id(diseasesId).name(disease.getName()).build());
      }

    });
    return diseases;
  }

  @Override
  public Page<ClinicDto> findAllClinics(QueryClinicDto data, Pageable pageable) {
    Page<Clinic> clinics;
    List<Long> clinicIds = new ArrayList<>();
    if (data == null) {
      return Page.empty(pageable);
    } else {
      if (data.getUserId() != null) {
        List<Long> clinicIdForClinicUser = clinicUserRepository
            .findAllClinicByUserId(data.getUserId())
            .stream().map(ClinicUser::getClinicId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(clinicIdForClinicUser)) {
          return Page.empty(pageable);
        } else {
          clinicIds.addAll(clinicIdForClinicUser);
        }
      }
      clinics = clinicRepository.findAll(ClinicSpec.findAllClinic(data, clinicIds), pageable);
      if (CollectionUtils.isEmpty(clinics.getContent())) {
        return Page.empty(pageable);
      }

      return clinics.map(clinic -> {

        //get doctor
        DoctorDto doctorDto = getDoctorDto(clinic);
        //get disease
        List<DiseaseDto> diseases = getDiseaseDtos(clinic);

        List<Long> userIdForClinicUser = clinicUserRepository
            .findAllUserIdByClinicId(clinic.getId());
        List<Long> userId = new ArrayList<>();
        if (data.getUserId() != null) {
          userId.add(data.getUserId());
        } else {
          userId.addAll(userIdForClinicUser);
        }
        return ClinicDto.builder()
            .id(clinic.getId())
            .name(clinic.getName())
            .phone(clinic.getPhone())
            .address(clinic.getAddress())
            .description(clinic.getDescription())
            .doctor(doctorDto)
            .diseases(diseases)
            .isActive(clinic.getIsActive())
            .userIds(userId)
            .updatedAt(clinic.getUpdatedAt())
            .build();
      });

    }

  }

  private ClinicDto getClinicDto(Clinic clinic, DoctorDto doctorDto, List<DiseaseDto> diseases,
      List<Long> userIds) {
    List<Long> userIdForClinicUser = clinicUserRepository.findAllUserIdByClinicId(clinic.getId());
    List<Long> staffUserIds = new ArrayList<>();
    if (!CollectionUtils.isEmpty(userIds)) {
      staffUserIds.addAll(userIds);
    } else {
      staffUserIds.addAll(userIdForClinicUser);
    }
    return ClinicDto.builder()
        .id(clinic.getId())
        .name(clinic.getName())
        .phone(clinic.getPhone())
        .address(clinic.getAddress())
        .description(clinic.getDescription())
        .doctor(doctorDto)
        .diseases(diseases)
        .isActive(clinic.getIsActive())
        .userIds(staffUserIds)
        .build();
  }

  private DoctorDto getDoctorDto(Clinic clinic) {
    Doctor doctor = doctorRepository.findDoctorById(clinic.getDoctorId());
    DoctorDto doctorDto;
    if (doctor == null) {
      doctorDto = null;
    } else {
      doctorDto = DoctorDto.builder().id(doctor.getId()).name(doctor.getName()).build();
    }
    return doctorDto;
  }

  @Override
  public void delete(Long id) {
    Clinic clinic = clinicRepository.findById(id).orElseThrow(
        () -> new NotFoundException(Collections.singletonList("err.clinic.clinic-does-not-exist")));

    List<Long> clinicIds = clinicDiseaseRepository.findAllClinicById(clinic.getId());
    List<ClinicUser> clinicIdForUser = clinicUserRepository.findAllClinicById(clinic.getId());
    if (!CollectionUtils.isEmpty(clinicIds) || !CollectionUtils.isEmpty(clinicIdForUser)) {
      throw new ConflictException(Collections.singletonList("err.clinic.clinic-already-exists"));
    } else {
      clinicRepository.delete(clinic);
    }
  }

  private void validDoctor(Long doctorId) {
    doctorRepository.findById(doctorId).orElseThrow(() ->
        new NotFoundException(
            Collections.singletonList("err.doctor.doctor-does-not-exist")));
  }

  private void validClinicDisease(List<Long> diseases) {
    Set<Long> setDisease = new HashSet<>(diseases);
    setDisease.forEach(disease -> {
      diseaseRepository.findById(disease).orElseThrow(() -> new NotFoundException(
          Collections.singletonList("err.diseases.disease-not-found")));
    });
  }

  private void mappingClinicDisease(Clinic clinic, List<Long> diseases) {
    Set<Long> setDisease = new HashSet<>(diseases);
    //mapping clinic disease
    List<ClinicDisease> clinicDiseases = new ArrayList<>();
    setDisease.forEach(disease -> {
          clinicDiseases
              .add(ClinicDisease.builder().clinicId(clinic.getId()).diseaseId(disease).build());
        }
    );
    clinicDiseaseRepository.saveAll(clinicDiseases);
  }

  private void validationNameOrPhoneWhenAddClinic(ClinicAddDto data) {
    List<String> failReasons = new ArrayList<>();
    clinicRepository.findByName(data.getName().trim()).ifPresent(doctor -> {
      failReasons.add("err.clinic.name-is-duplicate");
    });

    clinicRepository.findByPhone(data.getPhone()).ifPresent(doctor -> {
      failReasons.add("err.clinic.phone-is-duplicate");
    });
    if (!CollectionUtils.isEmpty(failReasons)) {
      throw new ConflictException(failReasons);
    }
  }

  private void validationNameOrPhoneWhenEditClinic(ClinicEditDto data) {
    List<String> failReasons = new ArrayList<>();
    clinicRepository.findByNameAndIdNot(data.getName().trim(), data.getId()).ifPresent(doctor -> {
      failReasons.add("err.clinic.name-is-duplicate");
    });

    clinicRepository.findByPhoneAndIdNot(data.getPhone(), data.getId()).ifPresent(doctor -> {
      failReasons.add("err.clinic.phone-is-duplicate");
    });
    if (!CollectionUtils.isEmpty(failReasons)) {
      throw new ConflictException(failReasons);
    }
  }

  private void validateUserWhenAddingClinic(List<Long> userIds) {
    if (CollectionUtils.isEmpty(userIds)) {
      return;
    }
    userIds.forEach(userId -> {
      clinicUserRepository.findByUserId(userId).ifPresent(clinicUser -> {
        throw new ConflictException(Arrays.asList("err.clinic.user-already-exits"));
      });
    });
  }

  private void validateUserWhenEditingClinic(Long clinicId, List<Long> userIds) {
    if (CollectionUtils.isEmpty(userIds)) {
      return;
    }
    userIds.forEach(userId -> {
      clinicUserRepository.findByUserIdExceptClinicId(clinicId, userId).ifPresent(clinicUser -> {
        throw new ConflictException(Arrays.asList("err.clinic.user-already-exits"));
      });
    });
  }
}
