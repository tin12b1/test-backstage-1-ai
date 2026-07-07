package com.csdl.access.config;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RequestType;
import com.csdl.access.common.exception.BusinessException;
import com.csdl.access.domain.repo.AccessRequestRepository;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * Bo sung thong tin phien vao moi view va xu ly loi nghiep vu chung.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private final UserSession userSession;
    private final AccessRequestRepository accessRequestRepository;

    public GlobalModelAdvice(UserSession userSession, AccessRequestRepository accessRequestRepository) {
        this.userSession = userSession;
        this.accessRequestRepository = accessRequestRepository;
    }

    @ModelAttribute("currentUser")
    public UserSession currentUser() {
        return userSession;
    }

    /**
     * So phieu cho ky chung cung don vi — hien thi badge tren navbar.
     * Chi tinh khi user da dang nhap va co unitId.
     */
    @ModelAttribute("sharedPendingSignCount")
    public long sharedPendingSignCount() {
        if (!userSession.isAuthenticated() || userSession.getUnitId() == null) {
            return 0;
        }
        List<RequestType> types = List.of(RequestType.YCTC_01, RequestType.YCTK_04A);
        return accessRequestRepository
                .findByRequestTypeInAndStatusAndRequesterUnitIdAndRequesterUserIdNot(
                        types, RequestStatus.PENDING_SIGN, userSession.getUnitId(), userSession.getUserId())
                .size();
    }

    @ExceptionHandler(BusinessException.class)
    public ModelAndView handleBusiness(BusinessException ex) {
        ModelAndView mv = new ModelAndView("error/business");
        mv.addObject("errorMessage", ex.getMessage());
        return mv;
    }
}
