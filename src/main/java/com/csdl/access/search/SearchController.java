package com.csdl.access.search;

import com.csdl.access.auth.UserSession;
import com.csdl.access.common.enums.RequestStatus;
import com.csdl.access.common.enums.RequestType;
import com.csdl.access.common.lookup.LookupService;
import com.csdl.access.common.lookup.RequestRow;
import com.csdl.access.report.ReportExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * Man hinh tra cuu va xuat danh sach (api-contract.md muc 6).
 */
@Controller
public class SearchController {

    private final SearchService searchService;
    private final ReportExportService reportExportService;
    private final LookupService lookupService;
    private final UserSession userSession;

    public SearchController(SearchService searchService,
                            ReportExportService reportExportService,
                            LookupService lookupService,
                            UserSession userSession) {
        this.searchService = searchService;
        this.reportExportService = reportExportService;
        this.lookupService = lookupService;
        this.userSession = userSession;
    }

    @GetMapping("/search")
    public String search(@ModelAttribute SearchCriteria criteria, Model model) {
        List<RequestRow> rows = searchService.search(criteria, userSession);
        model.addAttribute("rows", rows);
        model.addAttribute("criteria", criteria);
        model.addAttribute("statuses", RequestStatus.values());
        model.addAttribute("types", RequestType.values());
        model.addAttribute("units", lookupService.activeUnits());
        model.addAttribute("systems", lookupService.activeSystems());
        model.addAttribute("databases", lookupService.activeDatabases());
        return "search/index";
    }

    @GetMapping("/reports/export")
    public ResponseEntity<byte[]> export(@ModelAttribute SearchCriteria criteria) {
        List<RequestRow> rows = searchService.search(criteria, userSession);
        byte[] data = reportExportService.toExcel(rows);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "danh-sach-yeu-cau.xlsx");
        return ResponseEntity.ok().headers(headers).body(data);
    }
}
