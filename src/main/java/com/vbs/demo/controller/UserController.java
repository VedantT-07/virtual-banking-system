package com.vbs.demo.controller;

import com.vbs.demo.dto.DisplayDto;
import com.vbs.demo.dto.LoginDto;
import com.vbs.demo.dto.UpdateDto;
import com.vbs.demo.models.History;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.HistoryRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins="*") //permission allowed
public class UserController {
    @Autowired
    UserRepo userRepo;
    @Autowired
    HistoryRepo historyRepo;

    @PostMapping("/register")
    public String register(@RequestBody User user)
    {
        if (userRepo.existsByUsername(user.getUsername())) //extra
        {
            return "Username already in use";
        }
        userRepo.save(user);
        History h1 = new History();
        h1.setDescription("User Self Created: "+user.getUsername());
        historyRepo.save(h1);
        return "Signup Successful!";
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginDto u)
    {
        User user = userRepo.findByUsername(u.getUsername());
        if(user==null)
        {
            return "User not found";
        }
        if(!u.getPassword().equals(user.getPassword()))
        {
            return "Incorrect Password!";
        }
        if(!u.getRole().equals(user.getRole()))
        {
            return "Incorrect Role!";
        }
        return String.valueOf(user.getId());
    }

    @GetMapping("/get-details/{id}")
    public DisplayDto display(@PathVariable int id)
    {
        User user = userRepo.findById(id).orElseThrow(()->new RuntimeException("User not found!"));
        DisplayDto displayDto = new DisplayDto();
        displayDto.setUsername(user.getUsername());
        displayDto.setBalance(user.getBalance());
        return displayDto;
    }

    @PostMapping("/update")
    public String update(@RequestBody UpdateDto obj)
    {
        User user = userRepo.findById(obj.getId()).orElseThrow(()->new RuntimeException("User not found!"));
        History h1 = new History();
        if ("name".equalsIgnoreCase(obj.getKey()))
        {
            if(obj.getValue().equals(user.getName()))
            {
                return "New and old names must be different!";
            }
            h1.setDescription("User "+user.getUsername()+" changed Name from "+user.getName()+" to "+obj.getValue());
            user.setName(obj.getValue());
        }
        else if ("password".equalsIgnoreCase(obj.getKey()))
        {
            if(obj.getValue().equals(user.getPassword()))
            {
                return "New and old passwords must be different!";
            }
            user.setPassword(obj.getValue());
            h1.setDescription("User "+user.getUsername()+" changed Password");
        }
        else if ("email".equalsIgnoreCase(obj.getKey()))
        {
            if(obj.getValue().equals(user.getEmail()))
            {
                return "New and old e-mail must be different!";
            }
            if (userRepo.existsByEmail(obj.getValue()))
            {
                return "E-mail already in use!";
            }
            h1.setDescription("User "+user.getUsername()+" changed Email from "+user.getEmail()+" to" +obj.getValue());
            user.setEmail(obj.getValue());
        }
        else return "Invalid key!";
        userRepo.save(user);
        historyRepo.save(h1);
        return "successfully done";
    }

    @PostMapping("/add/{adminId}")
    public String add(@RequestBody User user, @PathVariable int adminId)
    {
        History h1 = new History();
        h1.setDescription("User "+user.getUsername()+" Created by admin "+adminId);
        historyRepo.save(h1);
        userRepo.save(user);
        return "Added successfully";
    }

    @GetMapping("/users")
    public List<User> getAllUsers(@RequestParam String sortBy, @RequestParam String order)
    {
        Sort sort;
        if(order.equalsIgnoreCase("desc"))
        {
            sort = Sort.by(sortBy).descending();
        }
        else sort = Sort.by(sortBy).ascending();
        return userRepo.findAllByRole("customer", sort);
    }

    @GetMapping("/users/{keyword}")
    public List<User> getUsers(@PathVariable String keyword)
    {
        return userRepo.findAllByUsernameContainingIgnoreCaseAndRole(keyword,"customer");
    }

    @DeleteMapping("/delete-user/{userId}/admin/{adminId}")
    public String deleteUser(@PathVariable int userId, @PathVariable int adminId)
    {
        User user = userRepo.findById(userId).orElseThrow(()->new RuntimeException("Not Found!"));
        History h1 = new History();
        h1.setDescription("User "+user.getUsername()+" is deleted by admin "+adminId);
        if(user.getBalance() > 0) return "Balance should be zero!";
        historyRepo.save(h1);
        userRepo.delete(user);
        return "User Deleted Successfully!";
    }
}