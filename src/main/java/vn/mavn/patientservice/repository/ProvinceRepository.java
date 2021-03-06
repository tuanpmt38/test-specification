package vn.mavn.patientservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.mavn.patientservice.entity.Province;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long>,
    JpaSpecificationExecutor<Province> {

  @Query("select p from Province p where p.code =:code")
  Province findByCode(Long code);
}
