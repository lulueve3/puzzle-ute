package hcmute.puzzle.controller;

import hcmute.puzzle.converter.Converter;
import hcmute.puzzle.dto.CompanyDTO;
import hcmute.puzzle.dto.ResponseObject;
import hcmute.puzzle.dto.UserDTO;
import hcmute.puzzle.filter.JwtAuthenticationFilter;
import hcmute.puzzle.repository.JobPostRepository;
import hcmute.puzzle.repository.UserRepository;
import hcmute.puzzle.services.CompanyService;
import hcmute.puzzle.services.EmployerService;
import hcmute.puzzle.services.JobPostService;
import hcmute.puzzle.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping(path = "/api")
@CrossOrigin(value = "http://localhost:3000")
public class AdminController {

  @Autowired EmployerService employerService;

  @Autowired UserRepository userRepository;

  @Autowired JwtAuthenticationFilter jwtAuthenticationFilter;

  @Autowired JobPostRepository jobPostRepository;

  @Autowired JobPostService jobPostService;

  @Autowired Converter converter;

  @Autowired CompanyService companyService;

  @Autowired
  UserService userService;


  // Company
  @PostMapping("/admin/add-company")
  public ResponseObject createCompany(@RequestBody CompanyDTO companyDTO) {
    return companyService.save(companyDTO);
  }

  @PutMapping("/admin/update-info-company")
  public ResponseObject updateCompany(@RequestBody CompanyDTO companyDTO) {
    return companyService.update(companyDTO);
  }

  @GetMapping("/admin/delete-info-company/{id}")
  public ResponseObject deleteCompany(@PathVariable Long id) {
    return companyService.delete(id);
  }

  @GetMapping("/admin/get-all-company")
  public ResponseObject getAllCompany() {
    return companyService.getAll();
  }

  @GetMapping("/admin/get-one-company/{id}")
  public ResponseObject getOneCompany(@PathVariable Long id) {
    return companyService.getOneById(id);
  }

  // Account
  @PostMapping("/admin/add-account")
  public ResponseObject saveAccount(@RequestBody UserDTO user) {
    return userService.save(user);
  }

  @DeleteMapping("/admin/delete-account/{id}")
  public ResponseObject deleteAccount(@PathVariable Long id) {
    return userService.delete(id);
  }

  @PutMapping("/admin/update-account/{id}")
  public ResponseObject updateAccount(@PathVariable Long id, @RequestBody UserDTO user) {
    return userService.update(id, user);
  }

  @GetMapping("/admin/get-all-account")
  public ResponseObject getAllAccount() {
    return userService.getAll();
  }

  //



}
