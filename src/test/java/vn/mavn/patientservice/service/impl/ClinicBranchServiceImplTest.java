package vn.mavn.patientservice.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import vn.mavn.patientservice.dto.ClinicBranchAddDto;
import vn.mavn.patientservice.dto.ClinicBranchDto;
import vn.mavn.patientservice.dto.ClinicBranchEditDto;
import vn.mavn.patientservice.entity.ClinicBranch;
import vn.mavn.patientservice.exception.ConflictException;
import vn.mavn.patientservice.exception.NotFoundException;
import vn.mavn.patientservice.repository.ClinicBranchRepository;
import vn.mavn.patientservice.service.ClinicBranchService;
import vn.mavn.patientservice.util.Oauth2TokenUtils;

import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class ClinicBranchServiceImplTest {

    @Mock
    private ClinicBranchRepository clinicBranchRepository;

    @InjectMocks
    private ClinicBranchService clinicBranchService = new ClinicBranchServiceImpl();

    @Mock
    private HttpServletRequest httpServletRequest;

    private static final String ADDITIONAL_TOKEN_TEST = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxIiwiaWF0IjoxNTg1NjMwMDk5LCJ1c2VyX2lkIjoxLCJyb2xlX3Blcm1pc3Npb25zIjpbeyJyb2xlIjoiVEVTVCIsImRlc2NyaXB0aW9uIjoiMTIzIiwicGVybWlzc2lvbnMiOltdfSx7InJvbGUiOiJST09UIiwiZGVzY3JpcHRpb24iOiJBZG1pbmlzdHJhdG9yIiwicGVybWlzc2lvbnMiOltdfSx7InJvbGUiOiJVU0VSIFNFTEYiLCJkZXNjcmlwdGlvbiI6IiIsInBlcm1pc3Npb25zIjpbeyJuYW1lIjoiQVBQX1VTRVJfVVBEQVRFX1VTRVJfUFJPRklMRSIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3VzZXItc2VydmljZS1ocGRlc2lnbi1uZXcvYXBpL3YxL2FwcC91c2VycyIsIm1ldGhvZCI6IlBVVCIsImRlc2NyaXB0aW9uIjoiIn0seyJuYW1lIjoiQVBQX1BPU1RfQ0hBTkdFX1BBU1NXT1JEIiwiZW5kcG9pbnQiOiIvc2VydmljZXMvbmV3LXVhYS1zZXJ2aWNlL2FwaS92MS9jaGFuZ2UtcGFzc3dvcmQiLCJtZXRob2QiOiJQT1NUIiwiZGVzY3JpcHRpb24iOiLEkOG7lWkgbeG6rXQga2jhuql1In0seyJuYW1lIjoiQ01TX0dFVF9QUk9GSUxFIiwiZW5kcG9pbnQiOiIvc2VydmljZXMvdXNlci1zZXJ2aWNlLWhwZGVzaWduLW5ldy9hcGkvdjEvZ2VuZXJhbC91c2Vycy9wcm9maWxlIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJM4bqleSB0aMO0bmcgdGluIGPDoSBuaMOibiJ9LHsibmFtZSI6IlPhu6xBIE3huqxUIEtI4bqoVSIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3VzZXItc2VydmljZS1ocGRlc2lnbi1uZXcvYXBpL3YxL2dlbmVyYWwvdXNlcnMvcHJvZmlsZSIsIm1ldGhvZCI6IlBPU1QiLCJkZXNjcmlwdGlvbiI6IlPhu6xBIE3huqxUIEtI4bqoVSJ9LHsibmFtZSI6IkFQUF9VU0VSX0dFVF9QUkVTSUdORURfVVJMX0ZPUl9VUExPQURJTkdfQVZBVEFSIiwiZW5kcG9pbnQiOiIvc2VydmljZXMvdXNlci1zZXJ2aWNlLWhwZGVzaWduLW5ldy9hcGkvdjEvYXBwL3VzZXJzL2ltYWdlLXByZS1zaWduZWQtdXJsIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiIifV19LHsicm9sZSI6IkFQUF9ERUZBVUxUX1VTRVIiLCJkZXNjcmlwdGlvbiI6InF1eeG7gW4gY-G7p2EgbmfGsOG7nWkgZMO5bmcgYXBwIiwicGVybWlzc2lvbnMiOlt7Im5hbWUiOiJBUFBfVVNFUl9VUERBVEVfVVNFUl9QUk9GSUxFIiwiZW5kcG9pbnQiOiIvc2VydmljZXMvdXNlci1zZXJ2aWNlLWhwZGVzaWduLW5ldy9hcGkvdjEvYXBwL3VzZXJzIiwibWV0aG9kIjoiUFVUIiwiZGVzY3JpcHRpb24iOiIifSx7Im5hbWUiOiJBUFBfR0VUX0JMVUVQUklOVCIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL2JsdWVwcmludHMiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6IlhlbSB04bqldCBj4bqjIGJsdWVwcmludCB0csOqbiBBcHAifSx7Im5hbWUiOiJyZXNvdXJjZS1oaXN0b3J5LW1hdGVyaWFsLWNvbnRyb2xsZXIiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9tYXRlcmlhbHMvdXBkYXRlZCIsIm1ldGhvZCI6IkdFVCIsImRlc2NyaXB0aW9uIjoicmVzb3VyY2UtaGlzdG9yeS1tYXRlcmlhbC1jb250cm9sbGVyIn0seyJuYW1lIjoiQVBQX0dFVF9ORVhUX1ZFUlNJT04iLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9lYXJseS1hY2Nlc3MvdmVyc2lvbiIsIm1ldGhvZCI6IkdFVCIsImRlc2NyaXB0aW9uIjoiTOG6pXkgcmEgbmV4dCB2ZXJzaW9uIn0seyJuYW1lIjoiT1BFUkFUT1JfQVBQX0dFVF9NQVRFUklBTCIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL2Vhcmx5LWFjY2Vzcy9tYXRlcmlhbHMiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6IlhlbSB04bqldCBj4bqjIG1hdGVyaWFscyBlYXJseS1hY2Nlc3MifSx7Im5hbWUiOiJ1cGRhdGUtbW9kZWwzZCBibHVlcHJpbnQiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9ibHVlcHJpbnRzL3VwZGF0ZS1tb2RlbDNkIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJ1cGRhdGUtbW9kZWwzZCBibHVlcHJpbnQifSx7Im5hbWUiOiJBUFBfVVNFUl9HZXRDdXJyZW50VmVyc2lvbiIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL3ZlcnNpb24iLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6ImzhuqV5IHRow7RuZyB0aW4gdmVyc2lvbiByZXNvdXJjZSBoaeG7h24gdOG6oWkifSx7Im5hbWUiOiJyZXNvdXJjZS1oaXN0b3J5LWJsdWVwcmludC1jb250cm9sbGVyIiwiZW5kcG9pbnQiOiIvc2VydmljZXMvcHJvZHVjdC1zZXJ2aWNlLWhwZGVzaWduL2FwaS92MS9hcHAvYmx1ZXByaW50cy91cGRhdGVkIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJyZXNvdXJjZS1oaXN0b3J5LWJsdWVwcmludC1jb250cm9sbGVyIn0seyJuYW1lIjoiQVBQX0dFVF9QSE9UT19HQUxMRVJJRVMiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9waG90by1nYWxsZXJpZXMiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6IlhlbSB04bqldCBj4bqjIGPDoWMg4bqjbmgifSx7Im5hbWUiOiJBUFBfR0VUX0NPTE9SIiwiZW5kcG9pbnQiOiIvc2VydmljZXMvcHJvZHVjdC1zZXJ2aWNlLWhwZGVzaWduL2FwaS92MS9hcHAvY29sb3JzIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJYZW0gdOG6pXQgY-G6oyBjb2xvciB0csOqbiBBcHAifSx7Im5hbWUiOiJBUFBfR0VUX1NFUlZJQ0VfSU5GTyIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL3Byb2R1Y3RzL3NlcnZpY2UtaW5mbyIsIm1ldGhvZCI6IkdFVCIsImRlc2NyaXB0aW9uIjoiWGVtIHThuqV0IGPhuqMgY8OhYyBzZXJ2aWNlLWluZm8ifSx7Im5hbWUiOiJPUEVSQVRPUl9BUFBfR0VUX1BST0RVQ1RfVVBEQVRFRCIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL2Vhcmx5LWFjY2Vzcy9wcm9kdWN0cy91cGRhdGVkIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJYZW0gdOG6pXQgcHJvZHVjdHMgxJHDoyDEkcaw4bujYyB1cGRhdGUgdGhlbyB04burbmcgdmVyc2lvbiB0w61uaCB04burIHZlcnNpb24gYXBwIGfhu61pIGzDqm4gY2hvIHThu5tpIG5leHQgdmVyc2lvbiJ9LHsibmFtZSI6Ik9QRVJBVE9SX0FQUF9HRVRfUFJPRFVDVCIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL2Vhcmx5LWFjY2Vzcy9wcm9kdWN0cyIsIm1ldGhvZCI6IkdFVCIsImRlc2NyaXB0aW9uIjoiWGVtIHThuqV0IGPhuqMgcHJvZHVjdHMgZWFybHktYWNjZXNzIn0seyJuYW1lIjoidXBkYXRlLW1vZGVsM2QgcHJvZHVjdCIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL3Byb2R1Y3RzL3VwZGF0ZS1tb2RlbDNkIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJ1cGRhdGUtbW9kZWwzZCBwcm9kdWN0In0seyJuYW1lIjoiQVBQX0dFVF9ERVRBSUxfUFJPRFVDVCIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL3Byb2R1Y3RzLyQiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6ImNtc19M4bqleSBjaGkgdGnhur90IHPhuqNuIHBo4bqpbSJ9LHsibmFtZSI6IkFQUF9HRVRfUEFST05BTUEiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9wYW5vcmFtYXMiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6IlhlbSB04bqldCBj4bqjIHBhbm9yYW1hcyB0csOqbiBBcHAifSx7Im5hbWUiOiJBUFBfR0VUX0NBVEFHT1JJRVMiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9waG90by1nYWxsZXJpZXMvY2F0ZWdvcmllcyIsIm1ldGhvZCI6IkdFVCIsImRlc2NyaXB0aW9uIjoiWGVtIHThuqV0IGPhuqMgY8OhYyBjYXRlZ29yaWVzIn0seyJuYW1lIjoiQVBQX1VTRVJfR0VUX1BSRVNJR05FRF9VUkxfRk9SX1VQTE9BRElOR19BVkFUQVIiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy91c2VyLXNlcnZpY2UtaHBkZXNpZ24tbmV3L2FwaS92MS9hcHAvdXNlcnMvaW1hZ2UtcHJlLXNpZ25lZC11cmwiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6IiJ9LHsibmFtZSI6Im5leHQtdmVyc2lvbiIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL3ZlcnNpb24vbmV4dC12ZXJzaW9uIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJuZXh0LXZlcnNpb24ifSx7Im5hbWUiOiJub0hhc01vZGVsM2QgcHJvZHVjdCIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL3Byb2R1Y3RzL25vSGFzTW9kZWwzZCIsIm1ldGhvZCI6IkdFVCIsImRlc2NyaXB0aW9uIjoibm9IYXNNb2RlbDNkIHByb2R1Y3QifSx7Im5hbWUiOiJBUFBfR0VUX0RFVEVBSUxfR0FMTEVSSUVTIiwiZW5kcG9pbnQiOiIvc2VydmljZXMvcHJvZHVjdC1zZXJ2aWNlLWhwZGVzaWduL2FwaS92MS9hcHAvcGhvdG8tZ2FsbGVyaWVzLyQiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6IlhlbSBjaGkgdGnhur90IOG6o25oIn0seyJuYW1lIjoiQVBQX0dFVF9UQUciLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC90YWdzIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJYZW0gdOG6pXQgY-G6oyBjw6FjIHRhZyJ9LHsibmFtZSI6IkFQUF9HRVRfREVURUFJTF9QQVJPTkFNQSIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL3Bhbm9yYW1hcy8kIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJYZW0gY2hpIHRp4bq_dCBwYW5vcmFtYXMifSx7Im5hbWUiOiJBUFBfR0VUX0FMTF9DVVJSRU5UX1VTRVJfUFJPRklMRVMiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL3Byb2ZpbGVzL3NlbGYtcHJvZmlsZXMiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6IlhlbSBkYW5oIHPDoWNoIHThuqV0IGPhuqMgY8OhYyBwcm9maWxlIMSRYW5nIGPDsyBj4bunYSB1c2VyIGhp4buHbiB04bqhaSAifSx7Im5hbWUiOiJBUFBfUE9TVF9DSEFOR0VfUEFTU1dPUkQiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9uZXctdWFhLXNlcnZpY2UvYXBpL3YxL2NoYW5nZS1wYXNzd29yZCIsIm1ldGhvZCI6IlBPU1QiLCJkZXNjcmlwdGlvbiI6IsSQ4buVaSBt4bqtdCBraOG6qXUifSx7Im5hbWUiOiJPUEVSQVRPUl9BUFBfR0VUX01BVEVSSUFMX1VQREFURUQiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9lYXJseS1hY2Nlc3MvbWF0ZXJpYWxzL3VwZGF0ZWQiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6IlhlbSB04bqldCBtYXRlcmlhbHMgxJHDoyDEkcaw4bujYyB1cGRhdGUgdGhlbyB04burbmcgdmVyc2lvbiB0w61uaCB04burIHZlcnNpb24gYXBwIGfhu61pIGzDqm4gY2hvIHThu5tpIG5leHQgdmVyc2lvbiJ9LHsibmFtZSI6IkFQUF9HRVRfREVTSUdOX0NPTkZJR1MiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9kZXNpZ24tY29uZmlncyIsIm1ldGhvZCI6IkdFVCIsImRlc2NyaXB0aW9uIjoiTOG6pXkgZGFuaCBzw6FjaCBjb25maWcifSx7Im5hbWUiOiJyZXNvdXJjZS1oaXN0b3J5LXByb2R1Y3RzLWNvbnRyb2xsZXIiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9wcm9kdWN0cy91cGRhdGVkIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJyZXNvdXJjZS1oaXN0b3J5LXByb2R1Y3RzLWNvbnRyb2xsZXIifSx7Im5hbWUiOiJT4busQSBN4bqsVCBLSOG6qFUiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy91c2VyLXNlcnZpY2UtaHBkZXNpZ24tbmV3L2FwaS92MS9nZW5lcmFsL3VzZXJzL3Byb2ZpbGUiLCJtZXRob2QiOiJQT1NUIiwiZGVzY3JpcHRpb24iOiJT4busQSBN4bqsVCBLSOG6qFUifSx7Im5hbWUiOiJPUEVSQVRPUl9BUFBfR0VUX0JMVUVQUklOVF9VUERBVEVEIiwiZW5kcG9pbnQiOiIvc2VydmljZXMvcHJvZHVjdC1zZXJ2aWNlLWhwZGVzaWduL2FwaS92MS9hcHAvZWFybHktYWNjZXNzL2JsdWVwcmludHMvdXBkYXRlZCIsIm1ldGhvZCI6IkdFVCIsImRlc2NyaXB0aW9uIjoiWGVtIHThuqV0IGJsdWVwcmludHMgxJHDoyDEkcaw4bujYyB1cGRhdGUgdGhlbyB04burbmcgdmVyc2lvbiB0w61uaCB04burIHZlcnNpb24gYXBwIGfhu61pIGzDqm4gY2hvIHThu5tpIG5leHQgdmVyc2lvbiJ9LHsibmFtZSI6IkFQUF9HRVRfUFJPRFVDVCIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL3Byb2R1Y3RzIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJYZW0gdOG6pXQgY-G6oyBjw6FjIHByb2R1Y3QifSx7Im5hbWUiOiJPUEVSQVRPUl9BUFBfR0VUX0JMVUVQUklOVCIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL2Vhcmx5LWFjY2Vzcy9ibHVlcHJpbnRzIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJYZW0gdOG6pXQgY-G6oyBibHVlcHJpbnRzIGVhcmx5LWFjY2VzcyJ9LHsibmFtZSI6Im5vSGFzTW9kZWwzZCBibHVlcHJpbnQiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9ibHVlcHJpbnRzL25vSGFzTW9kZWwzZCIsIm1ldGhvZCI6IkdFVCIsImRlc2NyaXB0aW9uIjoibm9IYXNNb2RlbDNkIGJsdWVwcmludCJ9LHsibmFtZSI6ImdldC1hcHAtcHJvZmlsZSIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL3Byb2ZpbGVzL3NlbGYtcHJvZmlsZXMiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6ImdldC1hcHAtcHJvZmlsZSJ9LHsibmFtZSI6IkFQUF9HRVRfTUFURVJJQUwiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9tYXRlcmlhbCIsIm1ldGhvZCI6IkdFVCIsImRlc2NyaXB0aW9uIjoiWGVtIHThuqV0IGPhuqMgbWF0ZXJpYWwgdHLDqm4gQXBwIn0seyJuYW1lIjoiQVBQX0dFVF9TVFlMRVMiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9waG90by1nYWxsZXJpZXMvc3R5bGVzIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJYZW0gdOG6pXQgY-G6oyBjw6FjIHN0eWxlcyJ9LHsibmFtZSI6IkFQUF9HRVRfTUVOVSIsImVuZHBvaW50IjoiL3NlcnZpY2VzL3Byb2R1Y3Qtc2VydmljZS1ocGRlc2lnbi9hcGkvdjEvYXBwL21lbnUiLCJtZXRob2QiOiJHRVQiLCJkZXNjcmlwdGlvbiI6IkzhuqV5IHbhu4EgZGFuaCBzw6FjaCBtZW51IGhp4buDbiB0aOG7iyB0csOqbiBhcHAifSx7Im5hbWUiOiJBUFBfR0VUX0FUVFJJQlVURVMiLCJlbmRwb2ludCI6Ii9zZXJ2aWNlcy9wcm9kdWN0LXNlcnZpY2UtaHBkZXNpZ24vYXBpL3YxL2FwcC9wcm9kdWN0cy9hdHRyaWJ1dGVzIiwibWV0aG9kIjoiR0VUIiwiZGVzY3JpcHRpb24iOiJYZW0gdOG6pXQgY-G6oyBjw6FjIHRodeG7mWMgdMOtbmgifV19XSwiZW1haWwiOiJuZ3V5ZW5kaW5odGFpKzIyMkB1bmltYXJtb3Jlcy5jb20iLCJwcm9kdWN0X2FsaWFzIjoiSFBERVNJR04iLCJ0eXBlIjoxLCJleHAiOjE1ODU2MzA0NTd9.bz_1QAR0IoM5xg0rRS6WESdj9iw-Vqzv0ckB33RfntM";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(httpServletRequest.getHeader("authorization"))
                .thenReturn(ADDITIONAL_TOKEN_TEST);
        Long userId = Oauth2TokenUtils.getValueByKeyInTheToken(ADDITIONAL_TOKEN_TEST, "user_id", Long.class);
    }

    @Test
    void save_TC1() {
        ClinicBranchAddDto data = ClinicBranchAddDto.builder().name("clinicBrand").build();
        Mockito.when(clinicBranchRepository.findByName(Mockito.any())).thenReturn(Optional.of(clinicBranch()));
        ConflictException conflictException = Assertions.assertThrows(ConflictException.class,
                ()-> clinicBranchService.save(data));
        Assertions.assertTrue(conflictException.getErrCodes().contains("err.clinic-branch.name-is-duplicate"));

    }

    @Test
    void save_TC2() {
        ClinicBranchAddDto data = ClinicBranchAddDto.builder().name("clinicBrand").build();
        Mockito.when(clinicBranchRepository.findByName(Mockito.any())).thenReturn(Optional.empty());
        ClinicBranch clinicBranch = clinicBranchService.save(data);
        Assertions.assertNotNull(clinicBranch);

    }

    @Test
    void update_TC1() {

        ClinicBranchEditDto data = ClinicBranchEditDto.builder().id(1L).name("clinicBrand").build();
        Mockito.when(clinicBranchRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class,
                ()-> clinicBranchService.update(data));
        Assertions.assertTrue(notFoundException.getErrCodes().contains("err.clinic-branch.clinic-branch-does-not-exist"));
    }

    @Test
    void update_TC2() {

        ClinicBranchEditDto data = ClinicBranchEditDto.builder().id(1L).name("clinicBrand").build();
        Mockito.when(clinicBranchRepository.findById(Mockito.any())).thenReturn(Optional.of(clinicBranch()));
        Mockito.when(clinicBranchRepository.findByNameAndIdNot(Mockito.any(), Mockito.any())).thenReturn(Optional.of(clinicBranch()));
        ConflictException conflictException = Assertions.assertThrows(ConflictException.class,
                ()-> clinicBranchService.update(data));
        Assertions.assertTrue(conflictException.getErrCodes().contains("err.clinic-branch.name-is-duplicate"));
    }

    @Test
    void update_TC3() {

        ClinicBranchEditDto data = ClinicBranchEditDto.builder().id(1L).name("clinicBrand").build();
        Mockito.when(clinicBranchRepository.findById(Mockito.any())).thenReturn(Optional.of(clinicBranch()));
        Mockito.when(clinicBranchRepository.findByNameAndIdNot(Mockito.any(), Mockito.any())).thenReturn(Optional.empty());
        ClinicBranch clinicBranch = clinicBranchService.update(data);
        Assertions.assertNotNull(clinicBranch);
    }

    @Test
    void findById_TC1() {

        Mockito.when(clinicBranchRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class,
                ()-> clinicBranchService.findById(Mockito.any()));
        Assertions.assertTrue(notFoundException.getErrCodes().contains("err.clinic-branch.clinic-branch-does-not-exist"));

    }

    @Test
    void findById_TC2() {

        Mockito.when(clinicBranchRepository.findById(Mockito.any())).thenReturn(Optional.of(clinicBranch()));
        ClinicBranchDto clinicBranchDto = clinicBranchService.findById(Mockito.any());
        Assertions.assertNotNull(clinicBranchDto);
    }

    @Test
    void findAllClinics_TC1() {

        String name = "name";
        Mockito.when(clinicBranchRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
                .thenReturn(Page.empty());
        Page<ClinicBranchDto> clinicBranchDtoPage = clinicBranchService.findAllClinics(name, Pageable.unpaged());
        Assertions.assertEquals(0L, clinicBranchDtoPage.getSize());
    }

    @Test
    void findAllClinics_TC2() {

        String name = "name";
        //mock data page clinicBrand
        Page<ClinicBranch> clinicBranchPage = new PageImpl<>(Arrays.asList(clinicBranch()), Pageable.unpaged(),
                1);

        Mockito.when(clinicBranchRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
                .thenReturn(clinicBranchPage);
        Page<ClinicBranchDto> clinicBranchDtoPage = clinicBranchService.findAllClinics(name, Pageable.unpaged());
        Assertions.assertEquals(1L, clinicBranchDtoPage.getContent().size());
    }

    @Test
    void delete_TC1() {

        Mockito.when(clinicBranchRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class,
                ()-> clinicBranchService.delete(Mockito.any()));
        Assertions.assertTrue(notFoundException.getErrCodes().contains("err.clinic-branch.clinic-branch-does-not-exist"));
    }

    @Test
    void delete_TC2() {

        Mockito.when(clinicBranchRepository.findById(Mockito.any())).thenReturn(Optional.of(clinicBranch()));
        clinicBranchService.delete(Mockito.any());
    }

    private ClinicBranch clinicBranch () {
        ClinicBranch clinicBranch = ClinicBranch.builder()
                .name("name")
                .isActive(true)
                .build();
        return clinicBranch;
    }
}
