package com.csdl.access.request;

import com.csdl.access.domain.repo.AccessRequestRepository;
import com.csdl.access.domain.repo.UnitRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Sinh ma yeu cau dang KyhieuDV_NgayThangNam_SoTT (database-schema.md).
 */
@Service
public class RequestCodeGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final UnitRepository unitRepository;
    private final AccessRequestRepository accessRequestRepository;

    public RequestCodeGenerator(UnitRepository unitRepository,
                                AccessRequestRepository accessRequestRepository) {
        this.unitRepository = unitRepository;
        this.accessRequestRepository = accessRequestRepository;
    }

    public String generate(Long unitId) {
        String unitCode = unitId == null ? "DV"
                : unitRepository.findById(unitId).map(u -> u.getCode()).orElse("DV");
        String prefix = unitCode + "_" + LocalDate.now().format(DATE_FMT);
        long count = accessRequestRepository.countByRequestCodeStartingWith(prefix);
        return String.format("%s_%03d", prefix, count + 1);
    }
}
