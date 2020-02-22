package vn.mavn.patientservice.repository.spec;

import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.criteria.Predicate;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import vn.mavn.patientservice.dto.qobject.QueryPatientDto;
import vn.mavn.patientservice.entity.Patient;
import vn.mavn.patientservice.util.SpecUtils;

public class PatientSpec {

  public static Specification findAllPatient(QueryPatientDto queryPatientDto) {
    return (Specification<Patient>) (root, criteriaQuery, criteriaBuilder) -> {
      Collection<Predicate> predicates = new ArrayList<>();
      if (queryPatientDto.getIsActive() != null) {
        predicates.add(
            criteriaBuilder.equal(root.get("isActive"), queryPatientDto.getIsActive()));
      }
      if (StringUtils.isNotBlank(queryPatientDto.getName())) {
        Predicate unaccent = SpecUtils
            .handleAccentExp(criteriaBuilder, root, "name", queryPatientDto.getName());
        predicates.add(unaccent);
      }
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }

}