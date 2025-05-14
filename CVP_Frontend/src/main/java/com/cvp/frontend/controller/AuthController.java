package com.cvp.frontend.controller;

import com.cvp.frontend.model.Login;
import com.cvp.frontend.model.OrganizationLogin;
import com.cvp.frontend.model.Organization;
import com.cvp.frontend.model.User;
import com.cvp.frontend.service.LoginService;
import com.cvp.frontend.service.OrganizationLoginService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private LoginService loginService; // Volunteer login service

    @Autowired
    private OrganizationLoginService organizationLoginService; // Organization login service

    // Volunteer Login Page
    @GetMapping("/volunteers/login")
    public String showVolunteerLogin(Model model) {
        model.addAttribute("login", new Login());
        return "volunteer-login"; // Corresponds to volunteer-login.html
    }

    // Process Volunteer Login
    @PostMapping("/volunteers/login")
    public String processVolunteerLogin(@Valid @ModelAttribute("login") Login login,
            RedirectAttributes redirectAttributes,
            HttpSession session) {
        try {

            User user = loginService.login(login);
            // Store the logged-in user in session for later use (e.g., task signup)
            session.setAttribute("loggedInUser", user);
            // 🔍 Debug logging
            System.out.println("[LOGIN] Session ID: " + session.getId());
            System.out.println("[LOGIN] loggedInUser: " + user.getId());

            redirectAttributes.addFlashAttribute("successMessage", "Login Successful!");

            System.out.println("Success");
            return "redirect:/users/profile/" + user.getId();
        } catch (Exception e) {
            return "volunteer-login";
        }
    }

    // Organization Login Page
    @GetMapping("/organizer")
    public String showOrganizerLogin(Model model) {
        model.addAttribute("orgLogin", new OrganizationLogin());
        return "organization-login"; // Corresponds to organization-login.html
    }

    @PostMapping("/organizer")
    public String processOrganizerLogin(@Valid @ModelAttribute("orgLogin") OrganizationLogin orgLogin,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            Organization organization = organizationLoginService.login(orgLogin);
            session.setAttribute("loggedInOrganization", organization); // Store in session

            redirectAttributes.addFlashAttribute("successMessage", "Login Successful!");
            System.out.println("Login Success: " + organization.getName());

            return "redirect:/organization/profile"; // Redirect to profile page
            // return "index";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid credentials. Please try again.");
            return "organization-login";
        }
    }

    @GetMapping("/volunteer-register")
    public String showVolRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "volunteer-register";
    }

    @GetMapping("/profile")
    public String showOrganizerProfile(HttpSession session, Model model) {
        Organization organization = (Organization) session.getAttribute("loggedInOrganization");

        if (organization == null) {
            return "organization-login"; // Redirect to login if not logged in
        }

        model.addAttribute("organization", organization);
        return "organizerProfile"; // Show profile page
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clear session
        return "organization-login"; // Redirect to login page
    }

}