package com.jobportal.controller;

import com.jobportal.dto.JobPostDto;
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
    private JobCompanyRepository jobCompanyRepository;

    @Autowired
    private JobLocationRepository jobLocationRepository;

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

        if (isRecruiter) {
            Optional<RecruiterProfile> profile = recruiterProfileRepository.findById(user.getUserId());
            profile.ifPresent(p -> model.addAttribute("recruiterProfile", p));
        } else {
            Optional<JobSeekerProfile> profile = jobSeekerProfileRepository.findById(user.getUserId());
            profile.ifPresent(p -> model.addAttribute("jobSeekerProfile", p));

            List<Skills> skills = skillsRepository.findByJobSeekerProfileUserAccountId(user.getUserId());
            model.addAttribute("skills", skills);
        }

        return "profile";
    }

    // Apply for Job
    @PostMapping("/jobs/{id}/apply")
    public String applyJob(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Users> userOpt = usersRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        Users user = userOpt.get();
        Optional<JobPostActivity> jobOpt = jobPostActivityRepository.findById(id);

        if (jobOpt.isEmpty()) {
            return "redirect:/jobs";
        }

        // Check if already applied
        if (jobSeekerApplyRepository.existsByJobJobPostIdAndUserUserAccountId(id, user.getUserId())) {
            return "redirect:/jobs/" + id + "?error=already-applied";
        }

        JobSeekerApply apply = new JobSeekerApply();
        apply.setUser(jobSeekerProfileRepository.findById(user.getUserId()).orElse(null));
        apply.setJob(jobOpt.get());
        apply.setApplyDate(LocalDateTime.now());

        jobSeekerApplyRepository.save(apply);

        return "redirect:/jobs/" + id + "?applied=true";
    }

    // Save Job
    @PostMapping("/jobs/{id}/save")
    public String saveJob(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Users> userOpt = usersRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        Users user = userOpt.get();
        Optional<JobPostActivity> jobOpt = jobPostActivityRepository.findById(id);

        if (jobOpt.isEmpty()) {
            return "redirect:/jobs";
        }

        // Check if already saved
        if (jobSeekerSaveRepository.existsByJobJobPostIdAndUserUserAccountId(id, user.getUserId())) {
            return "redirect:/jobs/" + id + "?error=already-saved";
        }

        JobSeekerSave save = new JobSeekerSave();
        save.setUser(jobSeekerProfileRepository.findById(user.getUserId()).orElse(null));
        save.setJob(jobOpt.get());

        jobSeekerSaveRepository.save(save);

        return "redirect:/jobs/" + id + "?saved=true";
    }

    // Post New Job (Form)
    @GetMapping("/jobs/new")
    public String newJob(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Users> userOpt = usersRepository.findByEmail(email);

        if (userOpt.isEmpty() || userOpt.get().getUserType() == null ||
                !userOpt.get().getUserType().getUserTypeName().equals("Recruiter")) {
            return "redirect:/login";
        }

        model.addAttribute("jobPostDto", new JobPostDto());
        return "post-job";
    }

    // Post New Job (Submit)
    @PostMapping("/jobs/new")
    public String newJobSubmit(@Valid @ModelAttribute("jobPostDto") JobPostDto jobPostDto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "post-job";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Users> userOpt = usersRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        Users user = userOpt.get();

        // Create or get company
        JobCompany company = new JobCompany();
        company.setName(jobPostDto.getCompanyName());
        company.setLogo(jobPostDto.getCompanyLogo());
        company = jobCompanyRepository.save(company);

        // Create or get location
        JobLocation location = new JobLocation();
        location.setCity(jobPostDto.getCity());
        location.setState(jobPostDto.getState());
        location.setCountry(jobPostDto.getCountry());
        location = jobLocationRepository.save(location);

        // Create job post
        JobPostActivity job = new JobPostActivity();
        job.setJobTitle(jobPostDto.getJobTitle());
        job.setDescriptionOfJob(jobPostDto.getDescriptionOfJob());
        job.setJobType(jobPostDto.getJobType());
        job.setSalary(jobPostDto.getSalary());
        job.setRemote(jobPostDto.getRemote());
        job.setPostedDate(LocalDateTime.now());
        job.setJobCompany(company);
        job.setJobLocation(location);
        job.setPostedBy(user);

        jobPostActivityRepository.save(job);

        return "redirect:/dashboard";
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

    // Add Skill
    @PostMapping("/profile/skills/add")
    public String addSkill(@RequestParam String skillName,
                           @RequestParam(required = false) String experienceLevel,
                           @RequestParam(required = false) String yearsOfExperience,
                           RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Users> userOpt = usersRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        Users user = userOpt.get();
        Optional<JobSeekerProfile> profileOpt = jobSeekerProfileRepository.findById(user.getUserId());

        if (profileOpt.isEmpty()) {
            return "redirect:/profile";
        }

        Skills skill = new Skills();
        skill.setName(skillName);
        skill.setExperienceLevel(experienceLevel);
        skill.setYearsOfExperience(yearsOfExperience);
        skill.setJobSeekerProfile(profileOpt.get());
        skillsRepository.save(skill);

        redirectAttributes.addFlashAttribute("success", "Skill added successfully!");
        return "redirect:/profile";
    }

    // View Applicants for a Job
    @GetMapping("/jobs/{id}/applicants")
    @Transactional(readOnly = true)
    public String viewApplicants(@PathVariable Integer id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Optional<Users> userOpt = usersRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        Optional<JobPostActivity> jobOpt = jobPostActivityRepository.findById(id);
        if (jobOpt.isEmpty()) {
            return "redirect:/dashboard";
        }

        JobPostActivity job = jobOpt.get();

        // Verify the current user is the one who posted this job
        if (!job.getPostedBy().getUserId().equals(userOpt.get().getUserId())) {
            return "redirect:/dashboard";
        }

        List<JobSeekerApply> applicants = jobSeekerApplyRepository.findByJobJobPostId(id);
        model.addAttribute("job", job);
        model.addAttribute("applicants", applicants);

        return "applicants";
    }
}
