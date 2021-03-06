package vn.mavn.patientservice.service.impl;

import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import vn.mavn.patientservice.dto.AdvertisingSourceAddDto;
import vn.mavn.patientservice.dto.AdvertisingSourceEditDto;
import vn.mavn.patientservice.dto.qobject.QueryAdvertisingSourceDto;
import vn.mavn.patientservice.entity.AdvertisingSource;
import vn.mavn.patientservice.entity.MedicalRecord;
import vn.mavn.patientservice.exception.ConflictException;
import vn.mavn.patientservice.exception.NotFoundException;
import vn.mavn.patientservice.repository.AdvertisingSourceRepository;
import vn.mavn.patientservice.repository.MedicalRecordRepository;
import vn.mavn.patientservice.repository.spec.AdvertisingSourceSpec;
import vn.mavn.patientservice.service.AdvertisingSourceService;
import vn.mavn.patientservice.util.TokenUtils;

@Service
@Transactional
public class AdvertisingSourceServiceImpl implements AdvertisingSourceService {

  @Autowired
  private AdvertisingSourceRepository advertisingSourceRepository;

  @Autowired
  private MedicalRecordRepository medicalRecordRepository;

  @Autowired
  private HttpServletRequest httpServletRequest;


  @Override
  public AdvertisingSource addNew(AdvertisingSourceAddDto advertisingSourceAddDto) {
    //TODO: valid name duplicate
    advertisingSourceRepository.findByName(advertisingSourceAddDto.getName().trim())
        .ifPresent(advert -> {
          throw new ConflictException(Collections.singletonList("err-advertising-duplicate-name"));
        });
    AdvertisingSource advertisingSource = AdvertisingSource.builder()
        .description(advertisingSourceAddDto.getDescription())
        .name(advertisingSourceAddDto.getName().trim())
        .isActive(advertisingSourceAddDto.getIsActive()).build();
    Long userId = Long.parseLong(TokenUtils.getUserIdFromToken(httpServletRequest));
    advertisingSource.setCreatedBy(userId);
    advertisingSource.setUpdatedBy(userId);
    advertisingSource.setIsActive(true);
    return advertisingSourceRepository.save(advertisingSource);
  }

  @Override
  public AdvertisingSource editAdvertSource(AdvertisingSourceEditDto advertisingSourceEditDto) {
    //TODO: check exist advertising_source
    AdvertisingSource advertisingSource = advertisingSourceRepository
        .findById(advertisingSourceEditDto.getId()).orElseThrow(() -> new NotFoundException(
            Collections.singletonList("err-advertising-not-found")));
    //TODO: valid name duplicate
    advertisingSourceRepository
        .findByNameNotEqualId(advertisingSourceEditDto.getName().trim(),
            advertisingSourceEditDto.getId())
        .ifPresent(advert -> {
          throw new ConflictException(Collections.singletonList("err-advertising-duplicate-name"));
        });
    Long userId = Long.parseLong(TokenUtils.getUserIdFromToken(httpServletRequest));
    BeanUtils.copyProperties(advertisingSourceEditDto, advertisingSource);
    advertisingSource.setUpdatedBy(userId);
    advertisingSource.setIsActive(true);
    return advertisingSourceRepository.save(advertisingSource);
  }

  @Override
  public AdvertisingSource getById(Long id) {
    //TODO: check exist advertising_source
    return advertisingSourceRepository
        .findById(id).orElseThrow(() -> new NotFoundException(
            Collections.singletonList("err-advertising-not-found")));
  }

  @Override
  public void delete(Long id) {
    //TODO: check exist advertising_source
    AdvertisingSource advertisingSource = advertisingSourceRepository
        .findById(id).orElseThrow(() -> new NotFoundException(
            Collections.singletonList("err-advertising-not-found")));
    //TODO: valid advertising used or not
    List<MedicalRecord> medicalRecords = medicalRecordRepository
        .findByAvertId(advertisingSource.getId());
    if (!CollectionUtils.isEmpty(medicalRecords)) {
      throw new ConflictException(
          Collections.singletonList("err-advertising-delete-not-successfully"));
    }
    advertisingSourceRepository.deleteAdvert(advertisingSource.getId());
  }

  @Override
  public Page<AdvertisingSource> findAll(QueryAdvertisingSourceDto queryAdvertisingSourceDto,
      Pageable pageable) {
    return (Page<AdvertisingSource>) advertisingSourceRepository.findAll(
        AdvertisingSourceSpec.findAllAdvert(queryAdvertisingSourceDto), pageable);
  }
}
