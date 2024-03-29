package hcmute.puzzle.controller;

import hcmute.puzzle.dto.CandidateDTO;
import hcmute.puzzle.dto.ExperienceDTO;
import hcmute.puzzle.dto.JobAlertDTO;
import hcmute.puzzle.dto.ResponseObject;
import hcmute.puzzle.entities.*;
import hcmute.puzzle.exception.CustomException;
import hcmute.puzzle.filter.JwtAuthenticationFilter;
import hcmute.puzzle.repository.*;
import hcmute.puzzle.services.*;
import hcmute.puzzle.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping(path = "/api")
@CrossOrigin(origins = {Constant.LOCAL_URL, Constant.ONLINE_URL})
public class CandidateController {

  @Autowired CandidateService candidateService;

  @Autowired UserRepository userRepository;

  @Autowired JobPostRepository jobPostRepository;

  @Autowired CandidateRepository candidateRepository;
  @Autowired JwtAuthenticationFilter jwtAuthenticationFilter;

  @Autowired ApplicationRepository applicationRepository;

  @Autowired JobAlertRepository jobAlertRepository;

  @Autowired JobAlertService jobAlertService;

  @Autowired ExperienceService experienceService;

  @Autowired ExperienceRepository experienceRepository;

  @Autowired EmployerService employerService;

  @Autowired
  CompanyService companyService;

  @Autowired
  JobPostService jobPostService;

  @PostMapping("/candidate/add")
  ResponseObject save(
      @RequestBody @Validated CandidateDTO candidate,
      BindingResult bindingResult,
      @RequestHeader(value = "Authorization") String token) {
    if (bindingResult.hasErrors()) {
      throw new RuntimeException(Objects.requireNonNull(bindingResult.getFieldError()).toString());
    }

    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    if (linkUser.get().getEmployerEntity() != null) {
      throw new CustomException("This account is Employer!");
    }

    if (linkUser.get().getCandidateEntity() != null) {
      throw new CustomException("Info candidate for this account was created!");
    }
    candidate.setUserId(linkUser.get().getId());

    Optional<CandidateDTO> candidateDTO = candidateService.save(candidate);
    if (candidateDTO.isPresent()) {
      return new ResponseObject(
          HttpStatus.OK.value(), "Create candidate successfully", candidateDTO.get());
    } else {
      throw new RuntimeException("Add candidate failed");
    }

    //        return new ResponseObject(
    //                HttpStatus.OK.value(), "Create candidate successfully", new CandidateDTO());
  }

  // Gửi Authentication xác thực tài khoản thì xoá.
  @DeleteMapping("/candidate")
  ResponseObject delete(
      @RequestHeader(value = "Authorization") String token) {

    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    return candidateService.delete(linkUser.get().getCandidateEntity().getId());
  }

  @PutMapping("/candidate/update")
  ResponseObject update(
      @RequestBody @Validated CandidateDTO candidate,
      BindingResult bindingResult,
      @RequestHeader(value = "Authorization") String token) {
    if (bindingResult.hasErrors()) {
      throw new RuntimeException(Objects.requireNonNull(bindingResult.getFieldError()).toString());
    }
    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    candidate.setUserId(linkUser.get().getId());
    candidate.setId(linkUser.get().getId());
    return candidateService.update(candidate);
  }

  // public
  @GetMapping("/candidate/get-one/{id}")
  ResponseObject getById(@PathVariable long id) {
    return candidateService.getOne(id);
  }

  @GetMapping("/candidate/follow-employer/{id}")
  ResponseObject followEmployer(
      @PathVariable(value = "id") Long employerId,
      // @RequestBody Map<String, Object> input,
      // @RequestParam(name = "employerId") long employerId,
      @RequestHeader(value = "Authorization") String token) {
    // Map<String, Object> retMap = new HashMap<String, Object>();

    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    // long candidateId = linkUser.get().getId();

    // https://stackoverflow.com/questions/58056944/java-lang-integer-cannot-be-cast-to-java-lang-long
    // long candidateId = ((Number) input.get("candidateId")).longValue();
    // long employerId = ((Number) input.get("employerId")).longValue();

    return candidateService.followEmployer(linkUser.get().getId(), employerId);
  }

  @GetMapping("/candidate/follow-company/{id}")
  ResponseObject followCompany(
      @PathVariable(value = "id") Long companyId,
      // @RequestParam(name = "employerId") long employerId,
      @RequestHeader(value = "Authorization") String token) {
    // Map<String, Object> retMap = new HashMap<String, Object>();

    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    // long candidateId = linkUser.get().getId();

    // https://stackoverflow.com/questions/58056944/java-lang-integer-cannot-be-cast-to-java-lang-long
    // long candidateId = ((Number) input.get("candidateId")).longValue();

    return candidateService.followCompany(linkUser.get().getId(), companyId);
  }

  @GetMapping("/candidate/apply-job-post/{postId}")
  ResponseObject applyJobPost(
      @PathVariable Long postId, @RequestHeader(value = "Authorization") String token) {

    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    if (linkUser.get().getCandidateEntity() == null) {
      throw new CustomException("This account isn't Candidate");
    }

//    Optional<CandidateEntity> candidate =
//        candidateRepository.findById(linkUser.get().getCandidateEntity().getId());
    Optional<JobPostEntity> jobPost = jobPostRepository.findById(postId);
    //    if (candidate.isEmpty()) {
    //      throw new NoSuchElementException("Candidate no value present");
    //    }

    if (jobPost.isEmpty()) {
      throw new NoSuchElementException("JobPost no value present");
    }

    if (!jobPost.get().isActive()) {
      throw new CustomException("You can't apply this jobPost. It isn't active");
    }

    Set<ApplicationEntity> applications =
        applicationRepository.findApplicationByCanIdAndJobPostId(linkUser.get().getId(), postId);
    if (!applications.isEmpty()) {
      throw new CustomException("You applied for this job");
    }

    ApplicationEntity applicationEntity = new ApplicationEntity();
    applicationEntity.setCandidateEntity(linkUser.get().getCandidateEntity());
    applicationEntity.setJobPostEntity(jobPost.get());
    applicationRepository.save(applicationEntity);

    return new ResponseObject(200, "Apply success", null);
  }

  @GetMapping("/candidate/cancel-apply-job-post/{postId}")
  ResponseObject cancelApplyJobPost(
      @PathVariable Long postId, @RequestHeader(value = "Authorization") String token) {

    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    if (linkUser.get().getCandidateEntity() == null) {
      throw new CustomException("This account isn't Candidate");
    }

    Set<ApplicationEntity> applications =
        applicationRepository.findApplicationByCanIdAndJobPostId(linkUser.get().getId(), postId);
    if (applications.isEmpty()) {
      throw new CustomException("You have not applied this JobPost or JobPost doesn't exist");
    }
    applicationRepository.deleteAll(applications);

    return new ResponseObject(200, "Cancel apply success", null);
  }

  @GetMapping("/candidate/save-job-post/{jobPostId}")
  ResponseObject saveJobPost(
      @PathVariable(value = "jobPostId") Long jobPostId,
      // @RequestParam(name = "employerId") long employerId,
      @RequestHeader(value = "Authorization") String token) {
    // Map<String, Object> retMap = new HashMap<String, Object>();

    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    // long candidateId = linkUser.get().getId();

    // https://stackoverflow.com/questions/58056944/java-lang-integer-cannot-be-cast-to-java-lang-long
    // long candidateId = ((Number) input.get("candidateId")).longValue();

    return candidateService.saveJobPost(linkUser.get().getId(), jobPostId);
  }

  @PostMapping("/candidate/add-job-alert")
  ResponseObject addJobAlert(
      @RequestBody @Validated JobAlertDTO jobAlertDTO,
      BindingResult bindingResult,
      @RequestHeader(value = "Authorization") String token) {
    if (bindingResult.hasErrors()) {
      throw new RuntimeException(Objects.requireNonNull(bindingResult.getFieldError()).toString());
    }

    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    if (linkUser.get().getEmployerEntity() != null || linkUser.get().getCandidateEntity() == null) {
      throw new CustomException("This account isn't Candidate!");
    }

    return jobAlertService.save(linkUser.get().getId(), jobAlertDTO);
  }

  @PutMapping("/candidate/update-job-alert")
  ResponseObject updateJobAlert(
      @RequestBody @Validated JobAlertDTO jobAlertDTO,
      BindingResult bindingResult,
      @RequestHeader(value = "Authorization") String token) {
    if (bindingResult.hasErrors()) {
      throw new RuntimeException(Objects.requireNonNull(bindingResult.getFieldError()).toString());
    }

    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    Optional<JobAlertEntity> jobAlert = jobAlertRepository.findById(jobAlertDTO.getId());

    if (jobAlert.isEmpty()) {
      throw new CustomException("Job Alert isn't exists");
    }

    if (jobAlert.get().getCandidateEntity().getId() != linkUser.get().getId()) {
      throw new CustomException("You don't have rights for this JobAlert");
    }

    jobAlertDTO.setCandidateId(linkUser.get().getId());

    return jobAlertService.update(jobAlertDTO);
  }

  @GetMapping("/candidate/delete-job-alert/{jobAlertId}")
  ResponseObject deleteJobAlert(
      @PathVariable(value = "jobAlertId") long id,
      @RequestHeader(value = "Authorization") String token) {

    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    Optional<JobAlertEntity> jobAlert = jobAlertRepository.findById(id);

    if (jobAlert.isEmpty()) {
      throw new CustomException("Job Alert isn't exists");
    }

    if (jobAlert.get().getCandidateEntity().getId() != linkUser.get().getId()) {
      throw new CustomException("You don't have rights for this JobAlert");
    }

    return jobAlertService.delete(id);
  }

  @GetMapping("/candidate/get-job-alert")
  ResponseObject getAllJobAlertByCandidateId(@RequestHeader(value = "Authorization") String token) {
    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    return jobAlertService.getAllJobAlertByCandidateId(linkUser.get().getId());
  }

  @GetMapping("/candidate/get-job-alert-by-id/{jobAlertId}")
  ResponseObject getAllJobAlertById(
      @PathVariable(value = "jobAlertId") long jobAlertId,
      @RequestHeader(value = "Authorization") String token) {
    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    Optional<JobAlertEntity> jobAlert = jobAlertRepository.findById(jobAlertId);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    if (jobAlert.isEmpty()) {
      throw new CustomException("Job Alert have this id isn't exist");
    }

    if (jobAlert.get().getCandidateEntity().getId() != linkUser.get().getId()) {
      throw new CustomException("You don't have right for this Job Alert");
    }

    return jobAlertService.getOneById(jobAlertId);
  }

  @PostMapping("/candidate/add-experience")
  ResponseObject addExperience(
      @RequestBody @Validated ExperienceDTO experienceDTO,
      BindingResult bindingResult,
      @RequestHeader(value = "Authorization") String token) {
    if (bindingResult.hasErrors()) {
      throw new RuntimeException(Objects.requireNonNull(bindingResult.getFieldError()).toString());
    }

    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    if (linkUser.get().getEmployerEntity() != null || linkUser.get().getCandidateEntity() == null) {
      throw new CustomException("This account isn't Candidate!");
    }

    return experienceService.save(linkUser.get().getId(), experienceDTO);
  }

  @PutMapping("/candidate/update-experience")
  ResponseObject updateExperience(
      @RequestBody @Validated ExperienceDTO experienceDTO,
      BindingResult bindingResult,
      @RequestHeader(value = "Authorization") String token) {
    if (bindingResult.hasErrors()) {
      throw new RuntimeException(Objects.requireNonNull(bindingResult.getFieldError()).toString());
    }

    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    Optional<ExperienceEntity> experience = experienceRepository.findById(experienceDTO.getId());

    if (experience.isEmpty()) {
      throw new CustomException("Experience isn't exists");
    }

    if (experience.get().getCandidateEntity().getId() != linkUser.get().getId()) {
      throw new CustomException("You don't have rights for this Experience");
    }

    experienceDTO.setCandidateId(linkUser.get().getId());

    return experienceService.update(experienceDTO);
  }

  @GetMapping("/candidate/delete-experience/{experienceId}")
  ResponseObject deleteExperience(
      @PathVariable(value = "experienceId") long id,
      @RequestHeader(value = "Authorization") String token) {

    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    Optional<ExperienceEntity> experience = experienceRepository.findById(id);

    if (experience.isEmpty()) {
      throw new CustomException("Experience isn't exists");
    }

    if (experience.get().getCandidateEntity().getId() != linkUser.get().getId()) {
      throw new CustomException("You don't have rights for this Experience");
    }

    return experienceService.delete(id);
  }

  @GetMapping("/candidate/get-experience")
  ResponseObject getAllExperienceByCandidateId(
      @RequestHeader(value = "Authorization") String token) {
    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    return experienceService.getAllExperienceByCandidateId(linkUser.get().getId());
  }

  @GetMapping("/candidate/get-experience-by-id/{experienceId}")
  ResponseObject getExperienceById(
      @PathVariable(value = "experienceId") long experienceId,
      @RequestHeader(value = "Authorization") String token) {
    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    Optional<ExperienceEntity> experience = experienceRepository.findById(experienceId);

    if (experience.isEmpty()) {
      throw new CustomException("Experience have this id isn't exist");
    }

    if (experience.get().getCandidateEntity().getId() != linkUser.get().getId()) {
      throw new CustomException("You don't have right for this Experience");
    }

    return experienceService.getOneById(experienceId);
  }

  @GetMapping("/candidate/get-job-post-applied")
  ResponseObject getJobPostAppliedByCandidate(
          @RequestHeader(value = "Authorization") String token) {
    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    return jobPostService.getJobPostAppliedByCandidateId(linkUser.get().getId());
  }

  @GetMapping("/candidate/get-job-post-saved")
  ResponseObject getJobPostSavedByCandidate(
          @RequestHeader(value = "Authorization") String token) {
    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    return jobPostService.getJobPostSavedByCandidateId(linkUser.get().getId());
  }

  @GetMapping("/candidate/get-company-followed")
  ResponseObject getCompanyFollowedByCandidate(
          @RequestHeader(value = "Authorization") String token) {
    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    return companyService.getCompanyFollowedByCandidateId(linkUser.get().getId());
  }

  @GetMapping("/candidate/get-employer-followed")
  ResponseObject getEmployerFollowedByCandidate(
          @RequestHeader(value = "Authorization") String token) {
    Optional<UserEntity> linkUser = jwtAuthenticationFilter.getUserEntityFromToken(token);

    if (linkUser.isEmpty()) {
      throw new CustomException("Not found account");
    }

    return employerService.getEmployerFollowedByCandidateId(linkUser.get().getId());
  }
}
