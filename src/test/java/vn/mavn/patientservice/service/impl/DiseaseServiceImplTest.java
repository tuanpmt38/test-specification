package vn.mavn.patientservice.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
import vn.mavn.patientservice.service.DiseaseService;
import vn.mavn.patientservice.util.Oauth2TokenUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
class DiseaseServiceImplTest {

    @Mock
    private DiseaseRepository diseaseRepository;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private ClinicDiseaseRepository clinicDiseaseRepository;

    @Mock
    private MedicineDiseaseRepository medicineDiseaseRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private DiseaseService diseaseService = new DiseaseServiceImpl();

    private static final String ADDITIONAL_TOKEN_TEST = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxIiwiaWF0IjoxNTg1NjMwMDk5LCJ1c2VyX2lkIjoxLCJyb2xlX3Blcm1pc3Npb25zIjpbeyJyb2xlIjoiVEVTVCIsImRlc2NyaXB0aW9uIjoiMTIzIiwicGVybWlzc2lvbnMiOltdfSx7InJvbGUiOiJST09UIiwiZGVzY3JpcHRpb24iOiJBZG1pbmlzdHJhdG9yIiwicGVybWlzc2lvbnMiOltdfSx7InJvbGUiOiJVU0VSIFNFTEYiLCJkZXNjcmlwdGlvbiI6IiIsInBlcm1pc3Npb25zIjpbeyJuYW1lIjoiQVBQX1VTRVJfVVBEQVRFX1VTRVJfUFJPRklMRSIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3VzZXItc2VydmljZS1ocGRlc2lnbi1uZXcvYXBpL3YxL2FwcC91c2VycyIsIm1ldGhvZCI6IlBVVCIsImRlc2NyaXB0aW9uIjoiIn0seyJuYW1lIjoiQVBQX1BPU1RfQ0hBTkdFX1BBU1NXT1JEIiwiZW5kcG9pbnQiOiIvc2VydmljZXMvbmV3LXVhYS1zZXJ2aWNlL2FwaS92MS9jaGFuZ2UtcGFzc3dvcmQiLCJtZXRob2QiOiJQT1NUIiwiZGVzY3JpcHRpb24iOiLEkOG7lWkgbeG6rXQga2jhuql1In0seyJuYW1lIjoiQ01TX0dFVF9QUk9GSUxFIiwiZW5kcG9pbnQiOiIvc2VydmljZXMvdXNlci1zZXJ2aWNlLWhwZGVzaWduLW5ldy9hcGkvdjEvZ2VuZXJhbC91c2Vycy9wcm9maWxlIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJM4bqleSB0aMO0bmcgdGluIGPDoSBuaMOibiJ9LHsibmFtZSI6IlPhu6xBIE3huqxUIEtI4bqoVSIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3VzZXItc2VydmljZS1ocGRlc2lnbi1uZXcvYXBpL3YxL2dlbmVyYWwvdXNlcnMvcHJvZmlsZSIsIm1ldGhvZCI6IlBPU1QiLCJkZXNjcmlwdGlvbiI6IlPhu6xBIE3huqxUIEtI4bqoVSJ9LHsibmFtZSI6IkFQUF9VU0VSX0dFVF9QUkVTSUdORURfVVJMX0ZPUl9VUExPQURJTkdfQVZBVEFSIiwiZW5kcG9pbnQiOiIvc2VydmljZXMvdXNlci1zZXJ2aWNlLWhwZGVzaWduLW5ldy9hcGkvdjEvYXBwL3VzZXJzL2ltYWdlLXByZS1zaWduZWQtdXJsIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiIifV19LHsicm9sZSI6IkFQUF9ERUZBVUxUX1VTRVIiLCJkZXNjcmlwdGlvbiI6InF1eeG7gW4gY-G7p2EgbmfGsOG7nWkgZMO5bmcgYXBwIiwicGVybWlzc2lvbnMiOlt7Im5hbWUiOiJBUFBfVVNFUl9VUERBVEVfVVNFUl9QUk9GSUxFIiwiZW5kcG9pbnQiOiIvc2VydmljZXMvdXNlci1zZXJ2aWNlLWhwZGVzaWduLW5ldy9hcGkvdjEvYXBwL3VzZXJzIiwibWV0aG9kIjoiUFVUIiwiZGVzY3JpcHRpb24iOiIifSx7Im5hbWUiOiJBUFBfR0VUX0JMVUVQUklOVCIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL2JsdWVwcmludHMiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6IlhlbSB04bqldCBj4bqjIGJsdWVwcmludCB0csOqbiBBcHAifSx7Im5hbWUiOiJyZXNvdXJjZS1oaXN0b3J5LW1hdGVyaWFsLWNvbnRyb2xsZXIiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9tYXRlcmlhbHMvdXBkYXRlZCIsIm1ldGhvZCI6IkdFVCIsImRlc2NyaXB0aW9uIjoicmVzb3VyY2UtaGlzdG9yeS1tYXRlcmlhbC1jb250cm9sbGVyIn0seyJuYW1lIjoiQVBQX0dFVF9ORVhUX1ZFUlNJT04iLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9lYXJseS1hY2Nlc3MvdmVyc2lvbiIsIm1ldGhvZCI6IkdFVCIsImRlc2NyaXB0aW9uIjoiTOG6pXkgcmEgbmV4dCB2ZXJzaW9uIn0seyJuYW1lIjoiT1BFUkFUT1JfQVBQX0dFVF9NQVRFUklBTCIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL2Vhcmx5LWFjY2Vzcy9tYXRlcmlhbHMiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6IlhlbSB04bqldCBj4bqjIG1hdGVyaWFscyBlYXJseS1hY2Nlc3MifSx7Im5hbWUiOiJ1cGRhdGUtbW9kZWwzZCBibHVlcHJpbnQiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9ibHVlcHJpbnRzL3VwZGF0ZS1tb2RlbDNkIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJ1cGRhdGUtbW9kZWwzZCBibHVlcHJpbnQifSx7Im5hbWUiOiJBUFBfVVNFUl9HZXRDdXJyZW50VmVyc2lvbiIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL3ZlcnNpb24iLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6ImzhuqV5IHRow7RuZyB0aW4gdmVyc2lvbiByZXNvdXJjZSBoaeG7h24gdOG6oWkifSx7Im5hbWUiOiJyZXNvdXJjZS1oaXN0b3J5LWJsdWVwcmludC1jb250cm9sbGVyIiwiZW5kcG9pbnQiOiIvc2VydmljZXMvcHJvZHVjdC1zZXJ2aWNlLWhwZGVzaWduL2FwaS92MS9hcHAvYmx1ZXByaW50cy91cGRhdGVkIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJyZXNvdXJjZS1oaXN0b3J5LWJsdWVwcmludC1jb250cm9sbGVyIn0seyJuYW1lIjoiQVBQX0dFVF9QSE9UT19HQUxMRVJJRVMiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9waG90by1nYWxsZXJpZXMiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6IlhlbSB04bqldCBj4bqjIGPDoWMg4bqjbmgifSx7Im5hbWUiOiJBUFBfR0VUX0NPTE9SIiwiZW5kcG9pbnQiOiIvc2VydmljZXMvcHJvZHVjdC1zZXJ2aWNlLWhwZGVzaWduL2FwaS92MS9hcHAvY29sb3JzIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJYZW0gdOG6pXQgY-G6oyBjb2xvciB0csOqbiBBcHAifSx7Im5hbWUiOiJBUFBfR0VUX1NFUlZJQ0VfSU5GTyIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL3Byb2R1Y3RzL3NlcnZpY2UtaW5mbyIsIm1ldGhvZCI6IkdFVCIsImRlc2NyaXB0aW9uIjoiWGVtIHThuqV0IGPhuqMgY8OhYyBzZXJ2aWNlLWluZm8ifSx7Im5hbWUiOiJPUEVSQVRPUl9BUFBfR0VUX1BST0RVQ1RfVVBEQVRFRCIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL2Vhcmx5LWFjY2Vzcy9wcm9kdWN0cy91cGRhdGVkIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJYZW0gdOG6pXQgcHJvZHVjdHMgxJHDoyDEkcaw4bujYyB1cGRhdGUgdGhlbyB04burbmcgdmVyc2lvbiB0w61uaCB04burIHZlcnNpb24gYXBwIGfhu61pIGzDqm4gY2hvIHThu5tpIG5leHQgdmVyc2lvbiJ9LHsibmFtZSI6Ik9QRVJBVE9SX0FQUF9HRVRfUFJPRFVDVCIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL2Vhcmx5LWFjY2Vzcy9wcm9kdWN0cyIsIm1ldGhvZCI6IkdFVCIsImRlc2NyaXB0aW9uIjoiWGVtIHThuqV0IGPhuqMgcHJvZHVjdHMgZWFybHktYWNjZXNzIn0seyJuYW1lIjoidXBkYXRlLW1vZGVsM2QgcHJvZHVjdCIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL3Byb2R1Y3RzL3VwZGF0ZS1tb2RlbDNkIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJ1cGRhdGUtbW9kZWwzZCBwcm9kdWN0In0seyJuYW1lIjoiQVBQX0dFVF9ERVRBSUxfUFJPRFVDVCIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL3Byb2R1Y3RzLyQiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6ImNtc19M4bqleSBjaGkgdGnhur90IHPhuqNuIHBo4bqpbSJ9LHsibmFtZSI6IkFQUF9HRVRfUEFST05BTUEiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9wYW5vcmFtYXMiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6IlhlbSB04bqldCBj4bqjIHBhbm9yYW1hcyB0csOqbiBBcHAifSx7Im5hbWUiOiJBUFBfR0VUX0NBVEFHT1JJRVMiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9waG90by1nYWxsZXJpZXMvY2F0ZWdvcmllcyIsIm1ldGhvZCI6IkdFVCIsImRlc2NyaXB0aW9uIjoiWGVtIHThuqV0IGPhuqMgY8OhYyBjYXRlZ29yaWVzIn0seyJuYW1lIjoiQVBQX1VTRVJfR0VUX1BSRVNJR05FRF9VUkxfRk9SX1VQTE9BRElOR19BVkFUQVIiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy91c2VyLXNlcnZpY2UtaHBkZXNpZ24tbmV3L2FwaS92MS9hcHAvdXNlcnMvaW1hZ2UtcHJlLXNpZ25lZC11cmwiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6IiJ9LHsibmFtZSI6Im5leHQtdmVyc2lvbiIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL3ZlcnNpb24vbmV4dC12ZXJzaW9uIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJuZXh0LXZlcnNpb24ifSx7Im5hbWUiOiJub0hhc01vZGVsM2QgcHJvZHVjdCIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL3Byb2R1Y3RzL25vSGFzTW9kZWwzZCIsIm1ldGhvZCI6IkdFVCIsImRlc2NyaXB0aW9uIjoibm9IYXNNb2RlbDNkIHByb2R1Y3QifSx7Im5hbWUiOiJBUFBfR0VUX0RFVEVBSUxfR0FMTEVSSUVTIiwiZW5kcG9pbnQiOiIvc2VydmljZXMvcHJvZHVjdC1zZXJ2aWNlLWhwZGVzaWduL2FwaS92MS9hcHAvcGhvdG8tZ2FsbGVyaWVzLyQiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6IlhlbSBjaGkgdGnhur90IOG6o25oIn0seyJuYW1lIjoiQVBQX0dFVF9UQUciLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC90YWdzIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJYZW0gdOG6pXQgY-G6oyBjw6FjIHRhZyJ9LHsibmFtZSI6IkFQUF9HRVRfREVURUFJTF9QQVJPTkFNQSIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL3Bhbm9yYW1hcy8kIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJYZW0gY2hpIHRp4bq_dCBwYW5vcmFtYXMifSx7Im5hbWUiOiJBUFBfR0VUX0FMTF9DVVJSRU5UX1VTRVJfUFJPRklMRVMiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL3Byb2ZpbGVzL3NlbGYtcHJvZmlsZXMiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6IlhlbSBkYW5oIHPDoWNoIHThuqV0IGPhuqMgY8OhYyBwcm9maWxlIMSRYW5nIGPDsyBj4bunYSB1c2VyIGhp4buHbiB04bqhaSAifSx7Im5hbWUiOiJBUFBfUE9TVF9DSEFOR0VfUEFTU1dPUkQiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9uZXctdWFhLXNlcnZpY2UvYXBpL3YxL2NoYW5nZS1wYXNzd29yZCIsIm1ldGhvZCI6IlBPU1QiLCJkZXNjcmlwdGlvbiI6IsSQ4buVaSBt4bqtdCBraOG6qXUifSx7Im5hbWUiOiJPUEVSQVRPUl9BUFBfR0VUX01BVEVSSUFMX1VQREFURUQiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9lYXJseS1hY2Nlc3MvbWF0ZXJpYWxzL3VwZGF0ZWQiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6IlhlbSB04bqldCBtYXRlcmlhbHMgxJHDoyDEkcaw4bujYyB1cGRhdGUgdGhlbyB04burbmcgdmVyc2lvbiB0w61uaCB04burIHZlcnNpb24gYXBwIGfhu61pIGzDqm4gY2hvIHThu5tpIG5leHQgdmVyc2lvbiJ9LHsibmFtZSI6IkFQUF9HRVRfREVTSUdOX0NPTkZJR1MiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9kZXNpZ24tY29uZmlncyIsIm1ldGhvZCI6IkdFVCIsImRlc2NyaXB0aW9uIjoiTOG6pXkgZGFuaCBzw6FjaCBjb25maWcifSx7Im5hbWUiOiJyZXNvdXJjZS1oaXN0b3J5LXByb2R1Y3RzLWNvbnRyb2xsZXIiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9wcm9kdWN0cy91cGRhdGVkIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJyZXNvdXJjZS1oaXN0b3J5LXByb2R1Y3RzLWNvbnRyb2xsZXIifSx7Im5hbWUiOiJT4busQSBN4bqsVCBLSOG6qFUiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy91c2VyLXNlcnZpY2UtaHBkZXNpZ24tbmV3L2FwaS92MS9nZW5lcmFsL3VzZXJzL3Byb2ZpbGUiLCJtZXRob2QiOiJQT1NUIiwiZGVzY3JpcHRpb24iOiJT4busQSBN4bqsVCBLSOG6qFUifSx7Im5hbWUiOiJPUEVSQVRPUl9BUFBfR0VUX0JMVUVQUklOVF9VUERBVEVEIiwiZW5kcG9pbnQiOiIvc2VydmljZXMvcHJvZHVjdC1zZXJ2aWNlLWhwZGVzaWduL2FwaS92MS9hcHAvZWFybHktYWNjZXNzL2JsdWVwcmludHMvdXBkYXRlZCIsIm1ldGhvZCI6IkdFVCIsImRlc2NyaXB0aW9uIjoiWGVtIHThuqV0IGJsdWVwcmludHMgxJHDoyDEkcaw4bujYyB1cGRhdGUgdGhlbyB04burbmcgdmVyc2lvbiB0w61uaCB04burIHZlcnNpb24gYXBwIGfhu61pIGzDqm4gY2hvIHThu5tpIG5leHQgdmVyc2lvbiJ9LHsibmFtZSI6IkFQUF9HRVRfUFJPRFVDVCIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL3Byb2R1Y3RzIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJYZW0gdOG6pXQgY-G6oyBjw6FjIHByb2R1Y3QifSx7Im5hbWUiOiJPUEVSQVRPUl9BUFBfR0VUX0JMVUVQUklOVCIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL2Vhcmx5LWFjY2Vzcy9ibHVlcHJpbnRzIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJYZW0gdOG6pXQgY-G6oyBibHVlcHJpbnRzIGVhcmx5LWFjY2VzcyJ9LHsibmFtZSI6Im5vSGFzTW9kZWwzZCBibHVlcHJpbnQiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9ibHVlcHJpbnRzL25vSGFzTW9kZWwzZCIsIm1ldGhvZCI6IkdFVCIsImRlc2NyaXB0aW9uIjoibm9IYXNNb2RlbDNkIGJsdWVwcmludCJ9LHsibmFtZSI6ImdldC1hcHAtcHJvZmlsZSIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL3Byb2ZpbGVzL3NlbGYtcHJvZmlsZXMiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6ImdldC1hcHAtcHJvZmlsZSJ9LHsibmFtZSI6IkFQUF9HRVRfTUFURVJJQUwiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9tYXRlcmlhbCIsIm1ldGhvZCI6IkdFVCIsImRlc2NyaXB0aW9uIjoiWGVtIHThuqV0IGPhuqMgbWF0ZXJpYWwgdHLDqm4gQXBwIn0seyJuYW1lIjoiQVBQX0dFVF9TVFlMRVMiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9waG90by1nYWxsZXJpZXMvc3R5bGVzIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJYZW0gdOG6pXQgY-G6oyBjw6FjIHN0eWxlcyJ9LHsibmFtZSI6IkFQUF9HRVRfTUVOVSIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL21lbnUiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6IkzhuqV5IHbhu4EgZGFuaCBzw6FjaCBtZW51IGhp4buDbiB0aOG7iyB0csOqbiBhcHAifSx7Im5hbWUiOiJBUFBfR0VUX0FUVFJJQlVURVMiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9wcm9kdWN0cy9hdHRyaWJ1dGVzIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJYZW0gdOG6pXQgY-G6oyBjw6FjIHRodeG7mWMgdMOtbmgifV19XSwiZW1haWwiOiJuZ3V5ZW5kaW5odGFpKzIyMkB1bmltYXJtb3Jlcy5jb20iLCJwcm9kdWN0X2FsaWFzIjoiSFBERVNJR04iLCJ0eXBlIjoxLCJleHAiOjE1ODU2MzA0NTd9.bz_1QAR0IoM5xg0rRS6WESdj9iw-Vqzv0ckB33RfntM";

    @BeforeEach()
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(httpServletRequest.getHeader("authorization"))
                .thenReturn(ADDITIONAL_TOKEN_TEST);
        Long userId = Oauth2TokenUtils.getValueByKeyInTheToken(ADDITIONAL_TOKEN_TEST, "user_id", Long.class);
    }

    @Test
    void addDisease_TC1() {

        DiseaseAddDto data = DiseaseAddDto.builder().name("chong mat").build();

        Mockito.when(diseaseRepository.findByName(Mockito.any()))
                .thenReturn(Optional.of(disease()));

        ConflictException conflictException = Assertions.assertThrows(ConflictException.class,
                () -> diseaseService.add(data));
        Assertions.assertTrue(
                conflictException.getErrCodes().contains("err.diseases.disease-already-exists"));
    }

    @Test
    void addDisease_TC2() {

        DiseaseAddDto data = DiseaseAddDto.builder().name("bbb").isActive(true).description("test").build();

        Mockito.when(diseaseRepository.findByName(data.getName()))
                .thenReturn(Optional.empty());
        //Valid insert successfully
        Disease disease = diseaseService.add(data);
        Assertions.assertNotNull(disease);

    }


    @Test
    void updateDisease_TC1() {

        Mockito.when(diseaseRepository.findById(Mockito.any()))
                .thenReturn(Optional.empty());

        NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class,
                () -> diseaseService.update(new DiseaseEditDto()));
        Assertions.assertTrue(
                notFoundException.getErrCodes().contains("err.diseases.disease-not-found"));
    }

    @Test
    void updateDisease_TC2() {

        Mockito.when(diseaseRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(disease()));

        Mockito.when(diseaseRepository.findByName(Mockito.any()))
                .thenReturn(Optional.of(disease2()));

        ConflictException conflictException = Assertions.assertThrows(ConflictException.class,
                () -> diseaseService.update(new DiseaseEditDto()));
        Assertions.assertTrue(
                conflictException.getErrCodes().contains("err.diseases.disease-already-exists"));
    }

    @Test
    void updateDisease_TC3() {

        Mockito.when(diseaseRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(disease()));

        DiseaseEditDto data = DiseaseEditDto.builder().id(1L).name("bbb").isActive(true).description("test").build();

        Mockito.when(diseaseRepository.findByName(data.getName()))
                .thenReturn(Optional.empty());
        //Valid insert successfully
        Disease disease = diseaseService.update(data);
        Assertions.assertNotNull(disease);
    }

    @Test
    void detail_TC1() {

        Mockito.when(diseaseRepository.findById(Mockito.any()))
                .thenReturn(Optional.empty());
        NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class,
                () -> diseaseService.detail(Mockito.any()));
        Assertions.assertTrue(
                notFoundException.getErrCodes().contains("err.diseases.disease-not-found"));
    }

    @Test
    void detail_TC2() {

        Mockito.when(diseaseRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(disease()));
        Disease disease = diseaseService.detail(Mockito.any());
        Assertions.assertNotNull(disease);

    }

    @Test
    void removeDisease_TC1() {
        Mockito.when(diseaseRepository.findById(Mockito.any()))
                .thenReturn(Optional.empty());
        NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class,
                () -> diseaseService.removeDisease(Mockito.any()));
        Assertions.assertTrue(
                notFoundException.getErrCodes().contains("err.diseases.disease-not-found"));

    }

    @Test
    void removeDisease_TC2() {

        Mockito.when(diseaseRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(disease()));

        Mockito.when(medicalRecordRepository.findByDiseaseId(Mockito.any()))
                .thenReturn(medicalRecords);
        Mockito.when(medicineDiseaseRepository.findAllByDiseaseId(Mockito.any()))
                .thenReturn(medicineDiseases);
        Mockito.when(clinicDiseaseRepository.findByDiseaseId(Mockito.any()))
                .thenReturn(clinicDiseases);

        ConflictException conflictException = Assertions.assertThrows(ConflictException.class,
                () -> diseaseService.removeDisease(Mockito.any()));
        Assertions.assertTrue(
                conflictException.getErrCodes().contains("err.diseases.cannot-remove-disease"));

    }

    @Test
    void removeDisease_TC3() {

        Mockito.when(diseaseRepository.findById(Mockito.any()))
                .thenReturn(Optional.of(disease()));

        Mockito.when(medicalRecordRepository.findByDiseaseId(Mockito.any()))
                .thenReturn(Collections.emptyList());
        Mockito.when(medicineDiseaseRepository.findAllByDiseaseId(Mockito.any()))
                .thenReturn(Collections.emptyList());
        Mockito.when(clinicDiseaseRepository.findByDiseaseId(Mockito.any()))
                .thenReturn(Collections.emptyList());

        diseaseService.removeDisease(Mockito.any());

    }

    @Test
    void getAllDisease_TC1() {
        Page<Disease> diseases = diseaseService.getAllDisease(null, Pageable.unpaged());
        Assertions.assertNull(diseases);

    }

    @Test
    void getAllDisease_TC2() {

        QueryDiseaseDto data = QueryDiseaseDto.builder()
                .clinicId(1L)
                .isActive(true)
                .build();
        Mockito.when(clinicDiseaseRepository
                .findAllByClinicId(data.getClinicId())).thenReturn(Collections.EMPTY_LIST);

        Mockito.when(diseaseRepository
                .findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
                .thenReturn(Page.empty());
        Page<Disease> diseases = diseaseService.getAllDisease(data,Pageable.unpaged());

        Assertions.assertEquals(0L, diseases.getContent().size());
    }

    @Test
    void getAllDisease_TC3() {

        QueryDiseaseDto data = QueryDiseaseDto.builder()
                .clinicId(1L)
                .isActive(true)
                .build();
        Mockito.when(clinicDiseaseRepository
                .findAllByClinicId(data.getClinicId()))
                .thenReturn(new ArrayList<>(Collections.singleton(1L)));

        Mockito.when(diseaseRepository
                .findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
                .thenReturn(Page.empty());
        Page<Disease> diseases = diseaseService.getAllDisease(data,Pageable.unpaged());

        Assertions.assertEquals(0L, diseases.getContent().size());
    }

    //Mock data
    private Disease disease() {
        Disease disease = new Disease();
        disease.setId(1L);
        disease.setName("chong mat");
        disease.setIsActive(true);
        disease.setDescription("test");
        return disease;
    }

    private Disease disease2() {
        Disease disease = new Disease();
        disease.setId(2L);
        disease.setName("chong mat");
        disease.setIsActive(true);
        disease.setDescription("test");
        return disease;
    }

    //mock medical record
    private MedicalRecord medicalRecord() {
        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setId(1L);
        medicalRecord.setDiseaseId(1L);
        return medicalRecord;
    }

    private MedicalRecord medicalRecord2() {
        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setId(2L);
        medicalRecord.setDiseaseId(2L);
        return medicalRecord;
    }

    //mock medicine disease
    private MedicineDisease medicineDisease() {
        MedicineDisease medicineDisease = new MedicineDisease();
        medicineDisease.setId(1L);
        medicineDisease.setDiseaseId(1L);
        return medicineDisease;
    }

    private MedicineDisease medicineDisease2() {
        MedicineDisease medicineDisease = new MedicineDisease();
        medicineDisease.setId(2L);
        medicineDisease.setDiseaseId(2L);
        return medicineDisease;
    }

    //mock clinic disease
    private ClinicDisease clinicDisease() {
        ClinicDisease clinicDisease = new ClinicDisease();
        clinicDisease.setId(1L);
        clinicDisease.setDiseaseId(1L);
        return clinicDisease;
    }

    private ClinicDisease clinicDisease2() {
        ClinicDisease clinicDisease = new ClinicDisease();
        clinicDisease.setId(1L);
        clinicDisease.setDiseaseId(1L);
        return clinicDisease;
    }

    private List<ClinicDisease> clinicDiseases = Arrays.asList(clinicDisease(), clinicDisease2());
    private List<MedicalRecord> medicalRecords = Arrays.asList(medicalRecord(), medicalRecord2());
    private List<MedicineDisease> medicineDiseases = Arrays.asList(medicineDisease(), medicineDisease2());

}
