package com.jobportal.controller;

import com.jobportal.entity.*;
import com.jobportal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class JobSeekerController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private JobPostActivityRepository jobPostActivityRepository;

    @Autowired
    private JobSeekerProfileRepository jobSeekerProfileRepository;

    @Autowired
    private JobSeekerApplyRepository jobSeekerApplyRepository;

    @Autowired
    private JobSeekerSaveRepository jobSeekerSaveRepository;

    @Autowired
    private SkillsRepository skillsRepository;

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
}
