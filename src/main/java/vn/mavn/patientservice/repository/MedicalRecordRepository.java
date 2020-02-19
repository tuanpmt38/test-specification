package vn.mavn.patientservice.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.mavn.patientservice.entity.MedicalRecord;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

  @Query("select mr from MedicalRecord mr where mr.advertisingSourceId =:avertId")
  List<MedicalRecord> findByAvertId(Long avertId);
}
