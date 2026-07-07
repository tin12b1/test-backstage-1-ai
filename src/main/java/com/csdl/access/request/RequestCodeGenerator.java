package com.csdl.access.request;

import com.csdl.access.domain.AccessRequest;
import com.csdl.access.domain.repo.AccessRequestRepository;
import com.csdl.access.domain.repo.DepartmentRepository;
import com.csdl.access.domain.repo.InformationSystemRepository;
import com.csdl.access.domain.repo.UnitRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sinh ma yeu cau theo tung loai phieu.
 * <ul>
 *   <li>01-YCTC, 04A-YCTK: generateByUnit → MãĐơnVị_MãPhòng_yyyyMMddHHmmss</li>
 *   <li>02-YCCS, 03-YCCT, 05A-YCKC, 05B-HTKC: generateBySystem → MãHệThống_yyyyMMddHHmmss</li>
 *   <li>05B đặc biệt: generate05B → MãHệThống_yyyyMMdd_Ca{shift}_Lan{consolidated}</li>
 * </ul>
 */
@Service
public class RequestCodeGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final UnitRepository unitRepository;
    private final DepartmentRepository departmentRepository;
    private final InformationSystemRepository informationSystemRepository;
    private final AccessRequestRepository accessRequestRepository;

    public RequestCodeGenerator(UnitRepository unitRepository,
                                DepartmentRepository departmentRepository,
                                InformationSystemRepository informationSystemRepository,
                                AccessRequestRepository accessRequestRepository) {
        this.unitRepository = unitRepository;
        this.departmentRepository = departmentRepository;
        this.informationSystemRepository = informationSystemRepository;
        this.accessRequestRepository = accessRequestRepository;
    }

    /**
     * Sinh ma theo don vi (01-YCTC, 04A-YCTK).
     * Format: {UnitCode}_{DeptCode}_{yyyyMMddHHmmss}
     * Example: TTCNTT_PTPM_20260705143022
     */
    public String generateByUnit(Long unitId, Long departmentId) {
        String unitCode = resolveUnitCode(unitId);
        String deptCode = resolveDepartmentCode(departmentId);
        String timestamp = LocalDateTime.now().format(DATETIME_FMT);
        return unitCode + "_" + deptCode + "_" + timestamp;
    }

    /**
     * Sinh ma theo he thong (02-YCCS, 03-YCCT, 05A-YCKC, 05B-HTKC).
     * Format: {SystemCode}_{yyyyMMddHHmmss}
     * Example: ARS_20260705143022
     */
    public String generateBySystem(Long systemId) {
        String systemCode = resolveSystemCode(systemId);
        String timestamp = LocalDateTime.now().format(DATETIME_FMT);
        return systemCode + "_" + timestamp;
    }

    /**
     * Sinh ma 05B dac biet (consolidate "Lan" tu cac 05A lien ket).
     * Format: {SystemCode}_{yyyyMMdd}_Ca{shift}_Lan{consolidated}
     * Example: ARS_20260705_Ca2_Lan01-02-03
     *
     * <p>"Lan" duoc extract tu access_no cua tung 05A lien ket, sap xep tang dan,
     * format 2 chu so va noi bang dau gach ngang.</p>
     */
    public String generate05B(Long systemId, List<AccessRequest> linked05As) {
        String systemCode = resolveSystemCode(systemId);
        String datePart = LocalDate.now().format(DATE_FMT);

        // Lay shift tu 05A dau tien (tat ca 05A trong cung nhom co cung shift)
        int shift = linked05As.stream()
                .filter(r -> r.getShiftNo() != null)
                .findFirst()
                .map(AccessRequest::getShiftNo)
                .orElse(1);

        // Extract access_no tu cac 05A, sap xep tang dan, format 2 chu so
        String consolidatedLan = linked05As.stream()
                .map(AccessRequest::getAccessNo)
                .filter(n -> n != null)
                .sorted()
                .map(n -> String.format("%02d", n))
                .collect(Collectors.joining("-"));

        return systemCode + "_" + datePart + "_Ca" + shift + "_Lan" + consolidatedLan;
    }

    /**
     * Phuong thuc cu — giu tuong thich nguoc.
     * Format: MaDonVi_yyyyMMdd_SoTT
     */
    public String generate(Long unitId) {
        String unitCode = resolveUnitCode(unitId);
        String prefix = unitCode + "_" + LocalDate.now().format(DATE_FMT);
        long count = accessRequestRepository.countByRequestCodeStartingWith(prefix);
        return String.format("%s_%03d", prefix, count + 1);
    }

    // ===== Private helpers =====

    private String resolveUnitCode(Long unitId) {
        if (unitId == null) {
            return "DV";
        }
        return unitRepository.findById(unitId)
                .map(u -> u.getCode())
                .orElse("DV");
    }

    private String resolveDepartmentCode(Long departmentId) {
        if (departmentId == null) {
            return "PB";
        }
        return departmentRepository.findById(departmentId)
                .map(d -> d.getCode())
                .orElse("PB");
    }

    private String resolveSystemCode(Long systemId) {
        if (systemId == null) {
            return "SYS";
        }
        return informationSystemRepository.findById(systemId)
                .map(s -> s.getCode())
                .orElse("SYS");
    }
}
