package com.csdl.access.domain;

import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** request_script_file - file SQL/script dinh kem cho 02-YCCS, 03-YCCT. */
@Entity
@Table(name = "request_script_file")
@Getter
@Setter
public class RequestScriptFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "detail_id")
    private Long detailId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Lob
    @Column(name = "file_content")
    private byte[] fileContent;

    @Column(length = 200)
    private String checksum;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
}
