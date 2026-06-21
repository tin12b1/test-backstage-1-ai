package com.csdl.access.config;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.exception.BusinessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

/**
 * Bo sung thong tin phien vao moi view va xu ly loi nghiep vu chung.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private final UserSession userSession;

    public GlobalModelAdvice(UserSession userSession) {
        this.userSession = userSession;
    }

    @ModelAttribute("currentUser")
    public UserSession currentUser() {
        return userSession;
    }

    @ExceptionHandler(BusinessException.class)
    public ModelAndView handleBusiness(BusinessException ex) {
        ModelAndView mv = new ModelAndView("error/business");
        mv.addObject("errorMessage", ex.getMessage());
        return mv;
    }
}
