package vn.mavn.patientservice.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import vn.mavn.patientservice.dto.DiseaseAddDto;
import vn.mavn.patientservice.entity.Disease;
import vn.mavn.patientservice.exception.ConflictException;
import vn.mavn.patientservice.repository.DiseaseRepository;
import vn.mavn.patientservice.service.DiseaseService;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
class DiseaseServiceImplTest {

    @Mock
    private DiseaseRepository diseaseRepository;

    @InjectMocks
    private DiseaseService diseaseService = new DiseaseServiceImpl();

//    @BeforeEach()
//    public void setUp() throws Exception {
//        MockitoAnnotations.initMocks(this);
//    }

    @Test
    void addDisease_TC1() {

        DiseaseAddDto diseaseDto = DiseaseAddDto.builder().name("chong").build();

        Disease disease = diseaseRepository.findById(5L).get();

        Mockito.when(diseaseRepository.findByName(diseaseDto.getName()))
                .thenReturn(Optional.of(new Disease()));

        ConflictException conflictException = Assertions.assertThrows(ConflictException.class,
                () -> diseaseRepository.findByName(diseaseDto.getName()));
        Assertions.assertTrue(
                conflictException.getErrCodes().contains("err.diseases.disease-already-exists"));

    }

}
