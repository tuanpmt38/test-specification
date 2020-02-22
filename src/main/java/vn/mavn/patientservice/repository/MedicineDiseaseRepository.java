package vn.mavn.patientservice.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import vn.mavn.patientservice.entity.MedicineDisease;

public interface MedicineDiseaseRepository extends CrudRepository<MedicineDisease, Long> {

  @Query("select md.medicineId from MedicineDisease md where md.diseaseId in :diseaseIds")
  List<Long> findAllMedicineByDiseaseId(List<Long> diseaseIds);
}
