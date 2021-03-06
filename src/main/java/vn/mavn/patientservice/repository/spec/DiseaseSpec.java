package vn.mavn.patientservice.repository.spec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.criteria.Predicate;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import vn.mavn.patientservice.dto.qobject.QueryDiseaseDto;
import vn.mavn.patientservice.entity.Disease;
import vn.mavn.patientservice.util.SpecUtils;

public class DiseaseSpec {

  public static Specification<Disease> findAllDiseases(QueryDiseaseDto data, List<Long> ids) {
    return (root, criteriaQuery, criteriaBuilder) -> {
      Collection<Predicate> predicates = new ArrayList<>();

      if (!CollectionUtils.isEmpty(ids)) {
        predicates.add(root.get("id").in(ids));
      }
      if (StringUtils.isNotBlank(data.getName())) {
        javax.persistence.criteria.Predicate unaccent = SpecUtils
            .handleAccentExp(criteriaBuilder, root, "name", data.getName());
        predicates.add(unaccent);
      }
      if (data.getIsActive() != null) {
        predicates.add(criteriaBuilder.equal(root.get("isActive"), data.getIsActive()));
      }
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }

}
