package com.csdl.access.common.enums;

/**
 * Loai phieu yeu cau.
 *
 * Ghi chu (vision.md): tai lieu nghiep vu co ca 01-YCCT va 01-YCTC.
 * Theo api-contract.md, ma chinh thuc duoc thong nhat trong code la YCTC_01.
 */
public enum RequestType {

    /** 01 - Truy cap, truy xuat CSDL thong thuong (01-YCCT/01-YCTC). */
    YCTC_01("01-YCTC", "Truy cap, truy xuat CSDL", true),

    /** 02 - Chinh sua du lieu. */
    YCCS_02("02-YCCS", "Chinh sua du lieu", false),

    /** 03 - Thay doi cau truc CSDL. */
    YCCT_03("03-YCCT", "Thay doi cau truc CSDL", false),

    /** 04A - Cap moi / thay doi thuoc tinh tai khoan. */
    YCTK_04A("04A-YCTK", "Cap moi/thay doi thuoc tinh tai khoan", true),

    /** 04B - Bien ban ban giao tai khoan. */
    BGTK_04B("04B-BGTK", "Bien ban ban giao tai khoan", true),

    /** 05A - Truy cap khan cap. */
    YCKC_05A("05A-YCKC", "Truy cap khan cap", false),

    /** 05B - Hoan thanh truy cap khan cap. */
    HTKC_05B("05B-HTKC", "Hoan thanh truy cap khan cap", false);

    private final String formCode;
    private final String displayName;
    private final boolean requiresDetailLines;

    RequestType(String formCode, String displayName, boolean requiresDetailLines) {
        this.formCode = formCode;
        this.displayName = displayName;
        this.requiresDetailLines = requiresDetailLines;
    }

    public String getFormCode() {
        return formCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Mau 01-YCTC va 04A-YCTK bat buoc toi thieu mot dong chi tiet. */
    public boolean requiresDetailLines() {
        return requiresDetailLines;
    }
}
