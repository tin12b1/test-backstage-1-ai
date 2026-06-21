package com.csdl.access.report;

import com.csdl.access.common.lookup.RequestRow;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Xuat danh sach yeu cau ra Excel theo ket qua dang loc (features/search-report.md muc 5).
 */
@Service
public class ReportExportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] HEADERS = {
            "Ma yeu cau", "Loai yeu cau", "Nguoi lap", "Don vi", "He thong", "CSDL",
            "Thoi gian bat dau", "Thoi gian ket thuc", "Trang thai", "Dang xu ly", "Ngay gui"
    };

    public byte[] toExcel(List<RequestRow> rows) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("YeuCau");
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                header.createCell(i).setCellValue(HEADERS[i]);
            }
            int rowIdx = 1;
            for (RequestRow r : rows) {
                Row row = sheet.createRow(rowIdx++);
                int c = 0;
                row.createCell(c++).setCellValue(nz(r.getRequestCode()));
                row.createCell(c++).setCellValue(nz(r.getRequestType()));
                row.createCell(c++).setCellValue(nz(r.getRequesterName()));
                row.createCell(c++).setCellValue(nz(r.getRequesterUnit()));
                row.createCell(c++).setCellValue(nz(r.getSystemName()));
                row.createCell(c++).setCellValue(nz(r.getDatabaseName()));
                row.createCell(c++).setCellValue(r.getStartTime() == null ? "" : r.getStartTime().format(FMT));
                row.createCell(c++).setCellValue(r.getEndTime() == null ? "" : r.getEndTime().format(FMT));
                row.createCell(c++).setCellValue(nz(r.getStatusLabel()));
                row.createCell(c++).setCellValue(nz(r.getCurrentActorRole()));
                Cell submitted = row.createCell(c);
                submitted.setCellValue(r.getSubmittedAt() == null ? "" : r.getSubmittedAt().format(FMT));
            }
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }
}
