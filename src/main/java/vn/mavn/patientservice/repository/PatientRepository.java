package vn.mavn.patientservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vn.mavn.patientservice.entity.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long>,
    JpaSpecificationExecutor<Patient> {

  @Query("select p from Patient p where p.id =:id")
  Optional<Patient> findActiveById(Long id);

  @Query("select p from Patient p where p.id =:id")
  Patient findByIdForGetData(Long id);

  @Modifying
  @Query("delete from Patient p where p.id =:id")
  void deletePatient(Long id);

  @Query("SELECT p FROM Patient p WHERE p.id IN :patientIds")
  Set<Patient> findByInIn(List<Long> patientIds);
}
