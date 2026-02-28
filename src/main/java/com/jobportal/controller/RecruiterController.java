package com.jobportal.controller;

import com.jobportal.dto.JobPostDto;
import com.jobportal.entity.*;
import com.jobportal.repository.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class RecruiterController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private JobPostActivityRepository jobPostActivityRepository;

    @Autowired
    private JobCompanyRepository jobCompanyRepository;

    @Autowired
    private JobLocationRepository jobLocationRepository;

    @Autowired
    private JobSeekerApplyRepository jobSeekerApplyRepository;

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    // Post New Job (Form)
    @GetMapping("/jobs/new")
    public String newJob(Model model) {
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

        // Handle company logo upload
        String logoPath = null;
        MultipartFile logoFile = jobPostDto.getCompanyLogo();
        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                Path uploadPath = Paths.get(uploadDir, "logos");
                Files.createDirectories(uploadPath);
                String fileName = UUID.randomUUID() + "_" + logoFile.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(logoFile.getInputStream(), filePath);
                logoPath = "/uploads/logos/" + fileName;
            } catch (IOException e) {
                model.addAttribute("error", "Failed to upload logo. Please try again.");
                return "post-job";
            }
        }

        // Create or get company
        JobCompany company = new JobCompany();
        company.setName(jobPostDto.getCompanyName());
        company.setLogo(logoPath);
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

    // Edit Job (Form)
    @GetMapping("/jobs/{id}/edit")
    @Transactional(readOnly = true)
    public String editJob(@PathVariable Integer id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<Users> userOpt = usersRepository.findByEmail(auth.getName());
        if (userOpt.isEmpty()) return "redirect:/login";

        Optional<JobPostActivity> jobOpt = jobPostActivityRepository.findById(id);
        if (jobOpt.isEmpty()) return "redirect:/jobs";

        JobPostActivity job = jobOpt.get();
        if (!job.getPostedBy().getUserId().equals(userOpt.get().getUserId())) {
            return "redirect:/jobs";
        }

        JobPostDto dto = new JobPostDto();
        dto.setJobTitle(job.getJobTitle());
        dto.setDescriptionOfJob(job.getDescriptionOfJob());
        dto.setJobType(job.getJobType());
        dto.setSalary(job.getSalary());
        dto.setRemote(job.getRemote());
        dto.setCompanyName(job.getJobCompany() != null ? job.getJobCompany().getName() : "");
        dto.setCity(job.getJobLocation() != null ? job.getJobLocation().getCity() : "");
        dto.setState(job.getJobLocation() != null ? job.getJobLocation().getState() : "");
        dto.setCountry(job.getJobLocation() != null ? job.getJobLocation().getCountry() : "");

        model.addAttribute("jobPostDto", dto);
        model.addAttribute("jobId", id);
        model.addAttribute("currentLogo", job.getJobCompany() != null ? job.getJobCompany().getLogo() : null);
        return "edit-job";
    }

    // Edit Job (Submit)
    @PostMapping("/jobs/{id}/edit")
    public String editJobSubmit(@PathVariable Integer id,
                                @Valid @ModelAttribute("jobPostDto") JobPostDto jobPostDto,
                                BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("jobId", id);
            return "edit-job";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<Users> userOpt = usersRepository.findByEmail(auth.getName());
        if (userOpt.isEmpty()) return "redirect:/login";

        Optional<JobPostActivity> jobOpt = jobPostActivityRepository.findById(id);
        if (jobOpt.isEmpty()) return "redirect:/jobs";

        JobPostActivity job = jobOpt.get();
        if (!job.getPostedBy().getUserId().equals(userOpt.get().getUserId())) {
            return "redirect:/jobs";
        }

        // Handle logo upload
        MultipartFile logoFile = jobPostDto.getCompanyLogo();
        String logoPath = job.getJobCompany() != null ? job.getJobCompany().getLogo() : null;
        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                Path uploadPath = Paths.get(uploadDir, "logos");
                Files.createDirectories(uploadPath);
                String fileName = UUID.randomUUID() + "_" + logoFile.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(logoFile.getInputStream(), filePath);
                logoPath = "/uploads/logos/" + fileName;
            } catch (IOException e) {
                model.addAttribute("error", "Failed to upload logo.");
                model.addAttribute("jobId", id);
                return "edit-job";
            }
        }

        // Update company
        JobCompany company = job.getJobCompany();
        if (company == null) company = new JobCompany();
        company.setName(jobPostDto.getCompanyName());
        company.setLogo(logoPath);
        jobCompanyRepository.save(company);

        // Update location
        JobLocation location = job.getJobLocation();
        if (location == null) location = new JobLocation();
        location.setCity(jobPostDto.getCity());
        location.setState(jobPostDto.getState());
        location.setCountry(jobPostDto.getCountry());
        jobLocationRepository.save(location);

        // Update job
        job.setJobTitle(jobPostDto.getJobTitle());
        job.setDescriptionOfJob(jobPostDto.getDescriptionOfJob());
        job.setJobType(jobPostDto.getJobType());
        job.setSalary(jobPostDto.getSalary());
        job.setRemote(jobPostDto.getRemote());
        job.setJobCompany(company);
        job.setJobLocation(location);
        jobPostActivityRepository.save(job);

        redirectAttributes.addFlashAttribute("success", "Job updated successfully!");
        return "redirect:/jobs";
    }

    // Delete Job
    @PostMapping("/jobs/{id}/delete")
    public String deleteJob(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<Users> userOpt = usersRepository.findByEmail(auth.getName());
        if (userOpt.isEmpty()) return "redirect:/login";

        Optional<JobPostActivity> jobOpt = jobPostActivityRepository.findById(id);
        if (jobOpt.isEmpty()) return "redirect:/jobs";

        JobPostActivity job = jobOpt.get();
        if (!job.getPostedBy().getUserId().equals(userOpt.get().getUserId())) {
            return "redirect:/jobs";
        }

        jobPostActivityRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Job deleted successfully!");
        return "redirect:/jobs";
    }

    // All Applications
    @GetMapping("/applications")
    @Transactional(readOnly = true)
    public String allApplications(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<Users> userOpt = usersRepository.findByEmail(auth.getName());
        if (userOpt.isEmpty()) return "redirect:/login";

        Users user = userOpt.get();
        List<JobPostActivity> postedJobs = jobPostActivityRepository.findByPostedByUserId(user.getUserId());

        // Get all applications for jobs posted by this recruiter
        List<JobSeekerApply> allApplications = postedJobs.stream()
                .flatMap(job -> jobSeekerApplyRepository.findByJobJobPostId(job.getJobPostId()).stream())
                .sorted((a, b) -> {
                    if (a.getApplyDate() == null) return 1;
                    if (b.getApplyDate() == null) return -1;
                    return b.getApplyDate().compareTo(a.getApplyDate());
                })
                .toList();

        model.addAttribute("applications", allApplications);
        return "applications";
    }

    // View Single Application Detail
    @GetMapping("/applications/{id}")
    @Transactional(readOnly = true)
    public String viewApplication(@PathVariable Integer id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<Users> userOpt = usersRepository.findByEmail(auth.getName());
        if (userOpt.isEmpty()) return "redirect:/login";

        Optional<JobSeekerApply> appOpt = jobSeekerApplyRepository.findById(id);
        if (appOpt.isEmpty()) return "redirect:/applications";

        JobSeekerApply application = appOpt.get();

        // Verify the recruiter owns the job this application is for
        if (!application.getJob().getPostedBy().getUserId().equals(userOpt.get().getUserId())) {
            return "redirect:/applications";
        }

        // Force-initialize lazy fields for template access
        JobPostActivity job = application.getJob();
        if (job != null) {
            if (job.getJobCompany() != null) job.getJobCompany().getName();
            if (job.getJobLocation() != null) job.getJobLocation().getCity();
        }
        if (application.getUser() != null) {
            application.getUser().getFirstName();
        }

        model.addAttribute("application", application);
        return "application-detail";
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
