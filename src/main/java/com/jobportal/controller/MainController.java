package com.jobportal.controller;

import com.jobportal.dto.UserRegistrationDto;
import com.jobportal.entity.*;
import com.jobportal.repository.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
public class MainController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private UserTypeRepository userTypeRepository;

    @Autowired
    private JobPostActivityRepository jobPostActivityRepository;

    @Autowired
    private JobSeekerProfileRepository jobSeekerProfileRepository;

    @Autowired
    private RecruiterProfileRepository recruiterProfileRepository;

    @Autowired
    private JobSeekerApplyRepository jobSeekerApplyRepository;

    @Autowired
    private JobSeekerSaveRepository jobSeekerSaveRepository;

    @Autowired
    private SkillsRepository skillsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Home Page
    @GetMapping("/")
    public String home() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            Optional<Users> userOpt = usersRepository.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                Users user = userOpt.get();
                boolean isRecruiter = user.getUserType() != null &&
                        user.getUserType().getUserTypeName().equals("Recruiter");
                if (!isRecruiter) {
                    return "redirect:/jobs";
                }
            }
        }
        return "index";
    }

    // Login Page
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Register Page
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto());
        return "register";
    }

    // Register Success
    @GetMapping("/register/success")
    public String registerSuccess() {
        return "register-success";
    }

    // Register Submit
    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("userDto") UserRegistrationDto userDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("userDto", userDto);
            return "register";
        }

        if (usersRepository.existsByEmail(userDto.getEmail())) {
            model.addAttribute("error", "Email already exists");
            model.addAttribute("userDto", userDto);
            return "register";
        }

        // Create new user
        Users user = new Users();
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setIsActive(true);
        user.setRegistrationDate(LocalDateTime.now());

        // Set user type
        Optional<UserType> userType = userTypeRepository.findByUserTypeName(
                userDto.getUserType().equals("recruiter") ? "Recruiter" : "Job Seeker");
        user.setUserType(userType.orElse(null));

        usersRepository.saveAndFlush(user);

        // Create profile based on user type
        if (userDto.getUserType().equals("recruiter")) {
            RecruiterProfile profile = new RecruiterProfile();
            profile.setUserAccountId(user.getUserId());
            profile.setFirstName(userDto.getFirstName());
            profile.setLastName(userDto.getLastName());
            recruiterProfileRepository.save(profile);
        } else {
            JobSeekerProfile profile = new JobSeekerProfile();
            profile.setUserAccountId(user.getUserId());
            profile.setFirstName(userDto.getFirstName());
            profile.setLastName(userDto.getLastName());
            jobSeekerProfileRepository.save(profile);
        }

        return "redirect:/register/success";
    }

    // Jobs List
    @GetMapping("/jobs")
    public String jobs(Model model) {
        List<JobPostActivity> jobs = jobPostActivityRepository.findAll();
        model.addAttribute("jobs", jobs);
        return "jobs";
    }

    // Jobs Search
    @GetMapping("/jobs/search")
    public String searchJobs(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            Model model) {
        List<JobPostActivity> jobs;
        if (keyword != null && !keyword.isEmpty()) {
            jobs = jobPostActivityRepository.findByJobTitleContaining(keyword);
        } else {
            jobs = jobPostActivityRepository.findAll();
        }

        // Filter by location if provided
        if (location != null && !location.isEmpty()) {
            String loc = location.toLowerCase();
            jobs = jobs.stream()
                    .filter(job -> job.getJobLocation() != null &&
                            ((job.getJobLocation().getCity() != null && job.getJobLocation().getCity().toLowerCase().contains(loc)) ||
                             (job.getJobLocation().getState() != null && job.getJobLocation().getState().toLowerCase().contains(loc)) ||
                             (job.getJobLocation().getCountry() != null && job.getJobLocation().getCountry().toLowerCase().contains(loc))))
                    .toList();
        }

        model.addAttribute("jobs", jobs);
        return "jobs";
    }

    // Job Details
    @GetMapping("/jobs/{id}")
    public String jobDetails(@PathVariable Integer id, Model model) {
        Optional<JobPostActivity> job = jobPostActivityRepository.findById(id);
        if (job.isPresent()) {
            model.addAttribute("job", job.get());
            return "job-details";
        }
        return "redirect:/jobs";
    }

    // Dashboard
    @GetMapping("/dashboard")
    @Transactional(readOnly = true)
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Users> userOpt = usersRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        Users user = userOpt.get();
        boolean isRecruiter = user.getUserType() != null &&
                user.getUserType().getUserTypeName().equals("Recruiter");

        model.addAttribute("isRecruiter", isRecruiter);
        model.addAttribute("isJobSeeker", !isRecruiter);

        if (isRecruiter) {
            List<JobPostActivity> postedJobs = jobPostActivityRepository.findByPostedByUserId(user.getUserId());
            model.addAttribute("postedJobs", postedJobs);
            model.addAttribute("postedJobsCount", postedJobs.size());
            int totalApplicants = postedJobs.stream()
                    .mapToInt(j -> j.getApplicants() != null ? j.getApplicants().size() : 0)
                    .sum();
            model.addAttribute("totalApplicants", totalApplicants);

            // Admin stats
            long totalUsers = usersRepository.count();
            long totalJobs = jobPostActivityRepository.count();
            long totalApplications = jobSeekerApplyRepository.count();
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalJobs", totalJobs);
            model.addAttribute("totalApplications", totalApplications);

            // Recently applied (latest 5)
            List<JobSeekerApply> recentApplications = jobSeekerApplyRepository.findAllByOrderByApplyDateDesc(PageRequest.of(0, 5));
            model.addAttribute("recentApplications", recentApplications);
        } else {
            List<JobSeekerApply> appliedJobs = jobSeekerApplyRepository.findByUserUserAccountId(user.getUserId());
            List<JobSeekerSave> savedJobs = jobSeekerSaveRepository.findByUserUserAccountId(user.getUserId());
            long totalJobsCount = jobPostActivityRepository.count();

            model.addAttribute("appliedJobs", appliedJobs);
            model.addAttribute("appliedJobsCount", appliedJobs.size());
            model.addAttribute("savedJobsCount", savedJobs.size());
            model.addAttribute("totalJobsCount", totalJobsCount);
        }

        return "dashboard";
    }

    // Profile
    @GetMapping("/profile")
    @Transactional(readOnly = true)
    public String profile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Users> userOpt = usersRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        Users user = userOpt.get();
        boolean isRecruiter = user.getUserType() != null &&
                user.getUserType().getUserTypeName().equals("Recruiter");

        model.addAttribute("isRecruiter", isRecruiter);
        model.addAttribute("isJobSeeker", !isRecruiter);
        model.addAttribute("userEmail", user.getEmail());

        if (isRecruiter) {
            Optional<RecruiterProfile> profile = recruiterProfileRepository.findById(user.getUserId());
            profile.ifPresent(p -> model.addAttribute("recruiterProfile", p));
        } else {
            Optional<JobSeekerProfile> profile = jobSeekerProfileRepository.findById(user.getUserId());
            profile.ifPresent(p -> model.addAttribute("jobSeekerProfile", p));

            List<Skills> skills = skillsRepository.findByJobSeekerProfileUserAccountId(user.getUserId());
            model.addAttribute("skills", skills);

            List<JobSeekerApply> appliedJobs = jobSeekerApplyRepository.findByUserUserAccountId(user.getUserId());
            List<JobSeekerSave> savedJobs = jobSeekerSaveRepository.findByUserUserAccountId(user.getUserId());
            model.addAttribute("appliedJobs", appliedJobs);
            model.addAttribute("savedJobs", savedJobs);
        }

        return "profile";
    }

    // Update Profile
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam(required = false) String firstName,
                                @RequestParam(required = false) String lastName,
                                @RequestParam(required = false) String city,
                                @RequestParam(required = false) String state,
                                @RequestParam(required = false) String country,
                                @RequestParam(required = false) String company,
                                @RequestParam(required = false) String employmentType,
                                @RequestParam(required = false) String workAuthorization,
                                @RequestParam(required = false) String resume,
                                @RequestParam(required = false) String newEmail,
                                RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Users> userOpt = usersRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        Users user = userOpt.get();
        boolean isRecruiter = user.getUserType() != null &&
                user.getUserType().getUserTypeName().equals("Recruiter");

        // Update email if changed
        if (newEmail != null && !newEmail.isBlank() && !newEmail.equals(user.getEmail())) {
            Optional<Users> existingUser = usersRepository.findByEmail(newEmail);
            if (existingUser.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Email is already in use by another account.");
                return "redirect:/profile";
            }
            user.setEmail(newEmail);
            usersRepository.save(user);
        }

        if (isRecruiter) {
            Optional<RecruiterProfile> profileOpt = recruiterProfileRepository.findById(user.getUserId());
            if (profileOpt.isPresent()) {
                RecruiterProfile profile = profileOpt.get();
                profile.setFirstName(firstName);
                profile.setLastName(lastName);
                profile.setCity(city);
                profile.setState(state);
                profile.setCountry(country);
                profile.setCompany(company);
                recruiterProfileRepository.save(profile);
            }
        } else {
            Optional<JobSeekerProfile> profileOpt = jobSeekerProfileRepository.findById(user.getUserId());
            if (profileOpt.isPresent()) {
                JobSeekerProfile profile = profileOpt.get();
                profile.setFirstName(firstName);
                profile.setLastName(lastName);
                profile.setCity(city);
                profile.setState(state);
                profile.setCountry(country);
                profile.setEmploymentType(employmentType);
                profile.setWorkAuthorization(workAuthorization);
                profile.setResume(resume);
                jobSeekerProfileRepository.save(profile);
            }
        }

        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/profile";
    }
}
