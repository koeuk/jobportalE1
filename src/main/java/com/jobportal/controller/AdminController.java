package com.jobportal.controller;

import com.jobportal.entity.*;
import com.jobportal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private UserTypeRepository userTypeRepository;

    @Autowired
    private JobPostActivityRepository jobPostActivityRepository;

    @Autowired
    private JobSeekerApplyRepository jobSeekerApplyRepository;

    @Autowired
    private RecruiterProfileRepository recruiterProfileRepository;

    @Autowired
    private JobSeekerProfileRepository jobSeekerProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long totalUsers = usersRepository.count();
        long totalJobs = jobPostActivityRepository.count();
        long totalApplications = jobSeekerApplyRepository.count();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalJobs", totalJobs);
        model.addAttribute("totalApplications", totalApplications);
        model.addAttribute("activePage", "dashboard");
        return "admin/dashboard";
    }

    // ==================== USERS ====================

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<Users> users = usersRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("recruiterProfiles", recruiterProfileRepository.findAll());
        model.addAttribute("seekerProfiles", jobSeekerProfileRepository.findAll());
        model.addAttribute("activePage", "users");
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new Users());
        model.addAttribute("userTypes", userTypeRepository.findAll());
        model.addAttribute("activePage", "users");
        return "admin/user-form";
    }

    @PostMapping("/users")
    public String createUser(@ModelAttribute Users user,
                             @RequestParam("userTypeId") Integer userTypeId,
                             @RequestParam(value = "firstName", required = false) String firstName,
                             @RequestParam(value = "lastName", required = false) String lastName,
                             @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                             RedirectAttributes redirectAttributes) {
        if (usersRepository.existsByEmail(user.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Email already exists.");
            return "redirect:/admin/users/new";
        }
        if (confirmPassword != null && !user.getPassword().equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/admin/users/new";
        }
        Optional<UserType> userType = userTypeRepository.findById(userTypeId);
        if (userType.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invalid user type.");
            return "redirect:/admin/users/new";
        }
        user.setUserType(userType.get());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRegistrationDate(LocalDateTime.now());
        user.setIsActive(user.getIsActive() != null && user.getIsActive());
        usersRepository.saveAndFlush(user);

        // Create profile based on user type
        String typeName = userType.get().getUserTypeName();
        if ("Recruiter".equals(typeName)) {
            RecruiterProfile profile = new RecruiterProfile();
            profile.setUserAccountId(user.getUserId());
            profile.setFirstName(firstName);
            profile.setLastName(lastName);
            recruiterProfileRepository.save(profile);
        } else {
            JobSeekerProfile profile = new JobSeekerProfile();
            profile.setUserAccountId(user.getUserId());
            profile.setFirstName(firstName);
            profile.setLastName(lastName);
            jobSeekerProfileRepository.save(profile);
        }

        redirectAttributes.addFlashAttribute("success", "User created successfully.");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Users> user = usersRepository.findById(id);
        if (user.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/admin/users";
        }
        model.addAttribute("user", user.get());
        model.addAttribute("userTypes", userTypeRepository.findAll());
        model.addAttribute("activePage", "users");

        // Load profile info
        Optional<RecruiterProfile> rp = recruiterProfileRepository.findById(id);
        Optional<JobSeekerProfile> sp = jobSeekerProfileRepository.findById(id);
        if (rp.isPresent()) {
            model.addAttribute("firstName", rp.get().getFirstName());
            model.addAttribute("lastName", rp.get().getLastName());
        } else if (sp.isPresent()) {
            model.addAttribute("firstName", sp.get().getFirstName());
            model.addAttribute("lastName", sp.get().getLastName());
        }
        return "admin/user-form";
    }

    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable Integer id,
                             @ModelAttribute Users user,
                             @RequestParam("userTypeId") Integer userTypeId,
                             @RequestParam(value = "newPassword", required = false) String newPassword,
                             @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                             @RequestParam(value = "firstName", required = false) String firstName,
                             @RequestParam(value = "lastName", required = false) String lastName,
                             RedirectAttributes redirectAttributes) {
        Optional<Users> existing = usersRepository.findById(id);
        if (existing.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/admin/users";
        }
        if (newPassword != null && !newPassword.isBlank()) {
            if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
                return "redirect:/admin/users/" + id + "/edit";
            }
        }
        Users existingUser = existing.get();
        Optional<UserType> userType = userTypeRepository.findById(userTypeId);
        if (userType.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invalid user type.");
            return "redirect:/admin/users/" + id + "/edit";
        }
        existingUser.setEmail(user.getEmail());
        existingUser.setUserType(userType.get());
        existingUser.setIsActive(user.getIsActive() != null && user.getIsActive());
        if (newPassword != null && !newPassword.isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(newPassword));
        }
        usersRepository.save(existingUser);

        // Update profile name
        Optional<RecruiterProfile> rp = recruiterProfileRepository.findById(id);
        Optional<JobSeekerProfile> sp = jobSeekerProfileRepository.findById(id);
        if (rp.isPresent()) {
            rp.get().setFirstName(firstName);
            rp.get().setLastName(lastName);
            recruiterProfileRepository.save(rp.get());
        } else if (sp.isPresent()) {
            sp.get().setFirstName(firstName);
            sp.get().setLastName(lastName);
            jobSeekerProfileRepository.save(sp.get());
        }

        redirectAttributes.addFlashAttribute("success", "User updated successfully.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        if (usersRepository.existsById(id)) {
            usersRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found.");
        }
        return "redirect:/admin/users";
    }

    // ==================== USER TYPES ====================

    @GetMapping("/user-types")
    public String listUserTypes(Model model) {
        model.addAttribute("userTypes", userTypeRepository.findAll());
        model.addAttribute("activePage", "user-types");
        return "admin/user-types";
    }

    @GetMapping("/user-types/new")
    public String newUserTypeForm(Model model) {
        model.addAttribute("userType", new UserType());
        model.addAttribute("activePage", "user-types");
        return "admin/user-type-form";
    }

    @PostMapping("/user-types")
    public String createUserType(@ModelAttribute UserType userType, RedirectAttributes redirectAttributes) {
        userTypeRepository.save(userType);
        redirectAttributes.addFlashAttribute("success", "User type created successfully.");
        return "redirect:/admin/user-types";
    }

    @GetMapping("/user-types/{id}/edit")
    public String editUserTypeForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<UserType> userType = userTypeRepository.findById(id);
        if (userType.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User type not found.");
            return "redirect:/admin/user-types";
        }
        model.addAttribute("userType", userType.get());
        model.addAttribute("activePage", "user-types");
        return "admin/user-type-form";
    }

    @PostMapping("/user-types/{id}/edit")
    public String updateUserType(@PathVariable Integer id,
                                 @ModelAttribute UserType userType,
                                 RedirectAttributes redirectAttributes) {
        Optional<UserType> existing = userTypeRepository.findById(id);
        if (existing.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User type not found.");
            return "redirect:/admin/user-types";
        }
        UserType existingType = existing.get();
        existingType.setUserTypeName(userType.getUserTypeName());
        userTypeRepository.save(existingType);
        redirectAttributes.addFlashAttribute("success", "User type updated successfully.");
        return "redirect:/admin/user-types";
    }

    @PostMapping("/user-types/{id}/delete")
    public String deleteUserType(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        if (userTypeRepository.existsById(id)) {
            userTypeRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "User type deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "User type not found.");
        }
        return "redirect:/admin/user-types";
    }
}
