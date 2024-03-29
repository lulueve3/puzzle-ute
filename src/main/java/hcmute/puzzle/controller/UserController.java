package hcmute.puzzle.controller;

import hcmute.puzzle.dto.ResponseObject;
import hcmute.puzzle.dto.UserDTO;
import hcmute.puzzle.services.UserService;
import hcmute.puzzle.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping(path = "/api")
@CrossOrigin(origins = {Constant.LOCAL_URL, Constant.ONLINE_URL})
public class UserController {

  @Autowired public UserService userService;

  @GetMapping("/user")
  public ResponseObject getAll() {
    return userService.getAll();
  }

  @PostMapping("/user")
  public ResponseObject save(@ModelAttribute UserDTO user) {
    Set<String> roleCodes = new HashSet<>();
    roleCodes.add("user");

    user.setRoleCodes(roleCodes);
    return userService.save(user);
  }

  @DeleteMapping("/user/{id}")
  public ResponseObject delete(@PathVariable Long id) {
    return userService.delete(id);
  }

  @PutMapping("/user/{id}")
  public ResponseObject update(@PathVariable Long id, @RequestBody UserDTO user) {
    return userService.update(id, user);
  }

  @GetMapping("/test")
  public String update() {
    return "OK";
  }
}
