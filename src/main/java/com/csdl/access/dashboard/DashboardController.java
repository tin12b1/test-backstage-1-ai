package com.csdl.access.dashboard;

import com.csdl.access.auth.UserSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Dashboard theo vai tro hien hanh (api-contract.md muc 3). */
@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserSession userSession;

    public DashboardController(DashboardService dashboardService, UserSession userSession) {
        this.dashboardService = dashboardService;
        this.userSession = userSession;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        if (!userSession.isAuthenticated() || userSession.getActiveRole() == null) {
            return "redirect:/login";
        }
        model.addAttribute("view", dashboardService.build(userSession));
        return "dashboard/index";
    }
}
