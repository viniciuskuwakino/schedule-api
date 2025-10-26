package com.evoluservices.schedule_api.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

//    @GetMapping
//    public List<User> findAll() {
//        User newUser = userService.createUser(dto);
//        return ResponseEntity.ok(newUser).getBody();
//    }

}
