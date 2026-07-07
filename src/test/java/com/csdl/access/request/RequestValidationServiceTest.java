package com.csdl.access.request;

import com.csdl.access.common.enums.RequestType;
import com.csdl.access.domain.AccessRequest;
import com.csdl.access.domain.RequestDetail;
import com.csdl.access.request.dto.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequestValidationServiceTest {

    private RequestValidationService service;

    @BeforeEach
    void setUp() {
        service = new RequestValidationService();
    }

    // ============================
    // validateForSubmission tests
    // ============================

    @Test
    void validateForSubmission_allCommonFieldsMissing_returnsErrors() {
        AccessRequest request = new AccessRequest();
        request.setRequestType(RequestType.YCCS_02);
        // requesterUserId, shiftNo, reason are all null

        List<ValidationError> errors = service.validateForSubmission(request, Collections.emptyList());

        assertTrue(errors.stream().anyMatch(e -> "requester_user_id".equals(e.field())));
        assertTrue(errors.stream().anyMatch(e -> "shift_no".equals(e.field())));
        assertTrue(errors.stream().anyMatch(e -> "reason".equals(e.field())));
    }

    @Test
    void validateForSubmission_validCommonFields_noCommonErrors() {
        AccessRequest request = buildValidRequest(RequestType.YCCS_02);
        request.setSystemId(1L);
        request.setDatabaseId(1L);

        List<ValidationError> errors = service.validateForSubmission(request, Collections.emptyList());

        assertTrue(errors.isEmpty());
    }

    @Test
    void validateForSubmission_01YCTC_noDetails_returnsError() {
        AccessRequest request = buildValidRequest(RequestType.YCTC_01);

        List<ValidationError> errors = service.validateForSubmission(request, Collections.emptyList());

        assertTrue(errors.stream().anyMatch(e -> "details".equals(e.field())));
    }

    @Test
    void validateForSubmission_01YCTC_withDetails_noDetailError() {
        AccessRequest request = buildValidRequest(RequestType.YCTC_01);
        RequestDetail detail = new RequestDetail();
        detail.setRequestId(1L);

        List<ValidationError> errors = service.validateForSubmission(request, List.of(detail));

        assertFalse(errors.stream().anyMatch(e -> "details".equals(e.field())));
    }

    @Test
    void validateForSubmission_02YCCS_missingSystemAndDb_returnsErrors() {
        AccessRequest request = buildValidRequest(RequestType.YCCS_02);
        // systemId and databaseId are null

        List<ValidationError> errors = service.validateForSubmission(request, Collections.emptyList());

        assertTrue(errors.stream().anyMatch(e -> "system_id".equals(e.field())));
        assertTrue(errors.stream().anyMatch(e -> "database_id".equals(e.field())));
    }

    @Test
    void validateForSubmission_03YCCT_missingExpectedDate_returnsError() {
        AccessRequest request = buildValidRequest(RequestType.YCCT_03);
        request.setSystemId(1L);
        request.setDatabaseId(1L);
        // expectedExecutionDate is null

        List<ValidationError> errors = service.validateForSubmission(request, Collections.emptyList());

        assertTrue(errors.stream().anyMatch(e -> "expected_execution_date".equals(e.field())));
    }

    @Test
    void validateForSubmission_03YCCT_allFieldsFilled_noErrors() {
        AccessRequest request = buildValidRequest(RequestType.YCCT_03);
        request.setSystemId(1L);
        request.setDatabaseId(1L);
        request.setExpectedExecutionDate(LocalDateTime.now().plusDays(1));

        List<ValidationError> errors = service.validateForSubmission(request, Collections.emptyList());

        assertTrue(errors.isEmpty());
    }

    @Test
    void validateForSubmission_04AYCTK_noDetails_returnsError() {
        AccessRequest request = buildValidRequest(RequestType.YCTK_04A);
        request.setSystemId(1L);
        request.setDatabaseId(1L);

        List<ValidationError> errors = service.validateForSubmission(request, Collections.emptyList());

        assertTrue(errors.stream().anyMatch(e -> "details".equals(e.field())));
    }

    @Test
    void validateForSubmission_05AYCKC_missingTimes_returnsErrors() {
        AccessRequest request = buildValidRequest(RequestType.YCKC_05A);
        request.setSystemId(1L);
        request.setDatabaseId(1L);
        // startTime, endTime are null

        List<ValidationError> errors = service.validateForSubmission(request, Collections.emptyList());

        assertTrue(errors.stream().anyMatch(e -> "start_time".equals(e.field())));
        assertTrue(errors.stream().anyMatch(e -> "end_time".equals(e.field())));
    }

    @Test
    void validateForSubmission_05AYCKC_allFieldsFilled_noErrors() {
        AccessRequest request = buildValidRequest(RequestType.YCKC_05A);
        request.setSystemId(1L);
        request.setDatabaseId(1L);
        request.setStartTime(LocalDateTime.now());
        request.setEndTime(LocalDateTime.now().plusHours(2));

        List<ValidationError> errors = service.validateForSubmission(request, Collections.emptyList());

        assertTrue(errors.isEmpty());
    }

    @Test
    void validateForSubmission_05BHTKC_missingSystemDb_returnsErrors() {
        AccessRequest request = buildValidRequest(RequestType.HTKC_05B);
        // systemId, databaseId null

        List<ValidationError> errors = service.validateForSubmission(request, Collections.emptyList());

        assertTrue(errors.stream().anyMatch(e -> "system_id".equals(e.field())));
        assertTrue(errors.stream().anyMatch(e -> "database_id".equals(e.field())));
    }

    @Test
    void validateForSubmission_nullType_returnsTypeError() {
        AccessRequest request = new AccessRequest();
        request.setRequesterUserId(1L);
        request.setShiftNo(1);
        request.setReason("test");
        // requestType is null

        List<ValidationError> errors = service.validateForSubmission(request, Collections.emptyList());

        assertTrue(errors.stream().anyMatch(e -> "request_type".equals(e.field())));
    }

    // ============================
    // validateForDraft tests
    // ============================

    @Test
    void validateForDraft_emptyForm_noErrors() {
        RequestForm form = new RequestForm();

        List<ValidationError> errors = service.validateForDraft(form);

        assertTrue(errors.isEmpty());
    }

    @Test
    void validateForDraft_reasonTooLong_returnsError() {
        RequestForm form = new RequestForm();
        form.setReason("x".repeat(2001));

        List<ValidationError> errors = service.validateForDraft(form);

        assertTrue(errors.stream().anyMatch(e -> "reason".equals(e.field()) && "MAX_LENGTH".equals(e.code())));
    }

    @Test
    void validateForDraft_invalidShiftNo_returnsError() {
        RequestForm form = new RequestForm();
        form.setShiftNo(5);

        List<ValidationError> errors = service.validateForDraft(form);

        assertTrue(errors.stream().anyMatch(e -> "shift_no".equals(e.field())));
    }

    @Test
    void validateForDraft_validShiftNo_noError() {
        RequestForm form = new RequestForm();
        form.setShiftNo(2);

        List<ValidationError> errors = service.validateForDraft(form);

        assertTrue(errors.isEmpty());
    }

    @Test
    void validateForDraft_invalidStartTimeFormat_returnsError() {
        RequestForm form = new RequestForm();
        form.setStartTime("not-a-date");

        List<ValidationError> errors = service.validateForDraft(form);

        assertTrue(errors.stream().anyMatch(e -> "start_time".equals(e.field())));
    }

    @Test
    void validateForDraft_validStartTimeFormat_noError() {
        RequestForm form = new RequestForm();
        form.setStartTime("2024-06-01T08:00:00");

        List<ValidationError> errors = service.validateForDraft(form);

        assertTrue(errors.isEmpty());
    }

    // ============================
    // validateTimeNotPast tests
    // ============================

    @Test
    void validateTimeNotPast_05BHTKC_alwaysAllows() {
        // Past date but type is 05B — should return null
        ValidationError error = service.validateTimeNotPast(
                1, LocalDate.of(2020, 1, 1), RequestType.HTKC_05B);

        assertNull(error);
    }

    @Test
    void validateTimeNotPast_pastDateShift_returnsError() {
        // A date far in the past
        ValidationError error = service.validateTimeNotPast(
                1, LocalDate.of(2020, 1, 1), RequestType.YCTC_01);

        assertNotNull(error);
        assertEquals("TIME_PAST", error.code());
    }

    @Test
    void validateTimeNotPast_futureDateShift_returnsNull() {
        // A date far in the future
        ValidationError error = service.validateTimeNotPast(
                1, LocalDate.of(2099, 12, 31), RequestType.YCTC_01);

        assertNull(error);
    }

    @Test
    void validateTimeNotPast_nullDateOrShift_returnsNull() {
        assertNull(service.validateTimeNotPast(null, LocalDate.now(), RequestType.YCTC_01));
        assertNull(service.validateTimeNotPast(1, null, RequestType.YCTC_01));
    }

    // ============================
    // checkDuplicateDetails tests
    // ============================

    @Test
    void checkDuplicateDetails_01YCTC_noDuplicates_returnsEmpty() {
        RequestDetail d1 = buildDetail(1L, 1L, "TABLE_A", 10L);
        RequestDetail d2 = buildDetail(1L, 1L, "TABLE_B", 10L);
        RequestDetail d3 = buildDetail(2L, 1L, "TABLE_A", 10L);

        List<ValidationError> errors = service.checkDuplicateDetails(
                RequestType.YCTC_01, List.of(d1, d2, d3));

        assertTrue(errors.isEmpty());
    }

    @Test
    void checkDuplicateDetails_01YCTC_withDuplicate_returnsError() {
        RequestDetail d1 = buildDetail(1L, 2L, "TABLE_A", 10L);
        RequestDetail d2 = buildDetail(1L, 2L, "TABLE_A", 10L); // duplicate

        List<ValidationError> errors = service.checkDuplicateDetails(
                RequestType.YCTC_01, List.of(d1, d2));

        assertEquals(1, errors.size());
        assertEquals("DUPLICATE", errors.get(0).code());
        assertTrue(errors.get(0).field().contains("details[1]"));
    }

    @Test
    void checkDuplicateDetails_01YCTC_sameObjectDifferentUser_noDuplicate() {
        RequestDetail d1 = buildDetail(1L, 2L, "TABLE_A", 10L);
        RequestDetail d2 = buildDetail(1L, 2L, "TABLE_A", 20L); // different user

        List<ValidationError> errors = service.checkDuplicateDetails(
                RequestType.YCTC_01, List.of(d1, d2));

        assertTrue(errors.isEmpty());
    }

    @Test
    void checkDuplicateDetails_04AYCTK_noDuplicateUsers_returnsEmpty() {
        RequestDetail d1 = buildDetail(1L, 1L, "OBJ", 10L);
        RequestDetail d2 = buildDetail(1L, 1L, "OBJ", 20L);

        List<ValidationError> errors = service.checkDuplicateDetails(
                RequestType.YCTK_04A, List.of(d1, d2));

        assertTrue(errors.isEmpty());
    }

    @Test
    void checkDuplicateDetails_04AYCTK_duplicateUser_returnsError() {
        RequestDetail d1 = buildDetail(1L, 1L, "OBJ1", 10L);
        RequestDetail d2 = buildDetail(2L, 2L, "OBJ2", 10L); // same targetUserId

        List<ValidationError> errors = service.checkDuplicateDetails(
                RequestType.YCTK_04A, List.of(d1, d2));

        assertEquals(1, errors.size());
        assertEquals("DUPLICATE", errors.get(0).code());
    }

    @Test
    void checkDuplicateDetails_04AYCTK_nullTargetUserId_notTreatedAsDuplicate() {
        RequestDetail d1 = buildDetail(1L, 1L, "OBJ1", null);
        RequestDetail d2 = buildDetail(2L, 2L, "OBJ2", null);

        List<ValidationError> errors = service.checkDuplicateDetails(
                RequestType.YCTK_04A, List.of(d1, d2));

        assertTrue(errors.isEmpty());
    }

    @Test
    void checkDuplicateDetails_otherType_returnsEmpty() {
        RequestDetail d1 = buildDetail(1L, 1L, "TABLE_A", 10L);
        RequestDetail d2 = buildDetail(1L, 1L, "TABLE_A", 10L);

        List<ValidationError> errors = service.checkDuplicateDetails(
                RequestType.YCCS_02, List.of(d1, d2));

        assertTrue(errors.isEmpty());
    }

    @Test
    void checkDuplicateDetails_nullDetails_returnsEmpty() {
        List<ValidationError> errors = service.checkDuplicateDetails(RequestType.YCTC_01, null);
        assertTrue(errors.isEmpty());
    }

    @Test
    void checkDuplicateDetails_emptyDetails_returnsEmpty() {
        List<ValidationError> errors = service.checkDuplicateDetails(
                RequestType.YCTC_01, Collections.emptyList());
        assertTrue(errors.isEmpty());
    }

    @Test
    void checkDuplicateDetails_01YCTC_multipleDuplicates_returnsMultipleErrors() {
        RequestDetail d1 = buildDetail(1L, 1L, "T", 10L);
        RequestDetail d2 = buildDetail(1L, 1L, "T", 10L); // dup of d1
        RequestDetail d3 = buildDetail(1L, 1L, "T", 10L); // dup of d1

        List<ValidationError> errors = service.checkDuplicateDetails(
                RequestType.YCTC_01, List.of(d1, d2, d3));

        assertEquals(2, errors.size());
    }

    // ============================
    // validateSingleSystemDatabase tests
    // ============================

    @Test
    void validateSingleSystemDatabase_02YCCS_bothPresent_returnsEmpty() {
        AccessRequest request = buildValidRequest(RequestType.YCCS_02);
        request.setSystemId(1L);
        request.setDatabaseId(2L);

        List<ValidationError> errors = service.validateSingleSystemDatabase(
                RequestType.YCCS_02, request);

        assertTrue(errors.isEmpty());
    }

    @Test
    void validateSingleSystemDatabase_02YCCS_missingSystem_returnsError() {
        AccessRequest request = buildValidRequest(RequestType.YCCS_02);
        request.setDatabaseId(2L);
        // systemId is null

        List<ValidationError> errors = service.validateSingleSystemDatabase(
                RequestType.YCCS_02, request);

        assertEquals(1, errors.size());
        assertEquals("system_id", errors.get(0).field());
    }

    @Test
    void validateSingleSystemDatabase_02YCCS_missingDatabase_returnsError() {
        AccessRequest request = buildValidRequest(RequestType.YCCS_02);
        request.setSystemId(1L);
        // databaseId is null

        List<ValidationError> errors = service.validateSingleSystemDatabase(
                RequestType.YCCS_02, request);

        assertEquals(1, errors.size());
        assertEquals("database_id", errors.get(0).field());
    }

    @Test
    void validateSingleSystemDatabase_02YCCS_bothMissing_returnsTwoErrors() {
        AccessRequest request = buildValidRequest(RequestType.YCCS_02);
        // both null

        List<ValidationError> errors = service.validateSingleSystemDatabase(
                RequestType.YCCS_02, request);

        assertEquals(2, errors.size());
    }

    @Test
    void validateSingleSystemDatabase_03YCCT_bothPresent_returnsEmpty() {
        AccessRequest request = buildValidRequest(RequestType.YCCT_03);
        request.setSystemId(1L);
        request.setDatabaseId(2L);

        List<ValidationError> errors = service.validateSingleSystemDatabase(
                RequestType.YCCT_03, request);

        assertTrue(errors.isEmpty());
    }

    @Test
    void validateSingleSystemDatabase_04AYCTK_bothPresent_returnsEmpty() {
        AccessRequest request = buildValidRequest(RequestType.YCTK_04A);
        request.setSystemId(1L);
        request.setDatabaseId(2L);

        List<ValidationError> errors = service.validateSingleSystemDatabase(
                RequestType.YCTK_04A, request);

        assertTrue(errors.isEmpty());
    }

    @Test
    void validateSingleSystemDatabase_05AYCKC_missingBoth_returnsTwoErrors() {
        AccessRequest request = buildValidRequest(RequestType.YCKC_05A);

        List<ValidationError> errors = service.validateSingleSystemDatabase(
                RequestType.YCKC_05A, request);

        assertEquals(2, errors.size());
    }

    @Test
    void validateSingleSystemDatabase_01YCTC_missingBoth_returnsEmpty() {
        AccessRequest request = buildValidRequest(RequestType.YCTC_01);
        // systemId and databaseId are null — allowed for 01-YCTC

        List<ValidationError> errors = service.validateSingleSystemDatabase(
                RequestType.YCTC_01, request);

        assertTrue(errors.isEmpty());
    }

    @Test
    void validateSingleSystemDatabase_05BHTKC_missingBoth_returnsEmpty() {
        AccessRequest request = buildValidRequest(RequestType.HTKC_05B);
        // not enforced for 05B

        List<ValidationError> errors = service.validateSingleSystemDatabase(
                RequestType.HTKC_05B, request);

        assertTrue(errors.isEmpty());
    }

    @Test
    void validateSingleSystemDatabase_nullType_returnsEmpty() {
        AccessRequest request = buildValidRequest(RequestType.YCCS_02);

        List<ValidationError> errors = service.validateSingleSystemDatabase(null, request);

        assertTrue(errors.isEmpty());
    }

    @Test
    void validateSingleSystemDatabase_nullRequest_returnsEmpty() {
        List<ValidationError> errors = service.validateSingleSystemDatabase(
                RequestType.YCCS_02, null);

        assertTrue(errors.isEmpty());
    }

    // ============================
    // validateScriptFile tests
    // ============================

    @Nested
    class ValidateScriptFileTests {

        // --- File name format tests ---

        @Test
        void validFileName_accepted() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615_ABC_001.sql", "text/plain", "SELECT 1".getBytes());

            ValidationError error = service.validateScriptFile(file, null, null);

            assertNull(error);
        }

        @Test
        void validFileName_numericBusinessCode_accepted() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20231201_XY12_999.sql", "text/plain", "SELECT 1".getBytes());

            ValidationError error = service.validateScriptFile(file, null, null);

            assertNull(error);
        }

        @Test
        void invalidFileName_wrongExtension_rejected() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615_ABC_001.txt", "text/plain", "data".getBytes());

            ValidationError error = service.validateScriptFile(file, null, null);

            assertNotNull(error);
            assertEquals("FILE_NAME_FORMAT", error.code());
        }

        @Test
        void invalidFileName_wrongDateFormat_rejected() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "2024615_ABC_001.sql", "text/plain", "data".getBytes());

            ValidationError error = service.validateScriptFile(file, null, null);

            assertNotNull(error);
            assertEquals("FILE_NAME_FORMAT", error.code());
        }

        @Test
        void invalidFileName_invalidMonth13_rejected() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20241301_ABC_001.sql", "text/plain", "data".getBytes());

            ValidationError error = service.validateScriptFile(file, null, null);

            assertNotNull(error);
            assertEquals("FILE_NAME_FORMAT", error.code());
        }

        @Test
        void invalidFileName_invalidMonth00_rejected() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240001_ABC_001.sql", "text/plain", "data".getBytes());

            ValidationError error = service.validateScriptFile(file, null, null);

            assertNotNull(error);
            assertEquals("FILE_NAME_FORMAT", error.code());
        }

        @Test
        void invalidFileName_invalidDay00_rejected() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240100_ABC_001.sql", "text/plain", "data".getBytes());

            ValidationError error = service.validateScriptFile(file, null, null);

            assertNotNull(error);
            assertEquals("FILE_NAME_FORMAT", error.code());
        }

        @Test
        void invalidFileName_invalidDay32_rejected() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240132_ABC_001.sql", "text/plain", "data".getBytes());

            ValidationError error = service.validateScriptFile(file, null, null);

            assertNotNull(error);
            assertEquals("FILE_NAME_FORMAT", error.code());
        }

        @Test
        void invalidFileName_seqNotThreeDigits_rejected() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615_ABC_01.sql", "text/plain", "data".getBytes());

            ValidationError error = service.validateScriptFile(file, null, null);

            assertNotNull(error);
            assertEquals("FILE_NAME_FORMAT", error.code());
        }

        @Test
        void invalidFileName_seqFourDigits_rejected() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615_ABC_0001.sql", "text/plain", "data".getBytes());

            ValidationError error = service.validateScriptFile(file, null, null);

            assertNotNull(error);
            assertEquals("FILE_NAME_FORMAT", error.code());
        }

        @Test
        void invalidFileName_specialCharsInBusinessCode_rejected() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615_AB-C_001.sql", "text/plain", "data".getBytes());

            ValidationError error = service.validateScriptFile(file, null, null);

            assertNotNull(error);
            assertEquals("FILE_NAME_FORMAT", error.code());
        }

        @Test
        void invalidFileName_noBusinessCode_rejected() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615__001.sql", "text/plain", "data".getBytes());

            ValidationError error = service.validateScriptFile(file, null, null);

            assertNotNull(error);
            assertEquals("FILE_NAME_FORMAT", error.code());
        }

        @Test
        void nullFile_returnsNull() {
            ValidationError error = service.validateScriptFile(null, null, null);
            assertNull(error);
        }

        @Test
        void emptyFile_returnsNull() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615_ABC_001.sql", "text/plain", new byte[0]);

            ValidationError error = service.validateScriptFile(file, null, null);

            assertNull(error); // empty file treated as no file
        }

        // --- File size tests ---

        @Test
        void fileSize_exactly10MB_accepted() {
            byte[] content = new byte[10 * 1024 * 1024]; // exactly 10MB
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615_ABC_001.sql", "text/plain", content);

            ValidationError error = service.validateScriptFile(file, null, null);

            assertNull(error);
        }

        @Test
        void fileSize_exceeds10MB_rejected() {
            byte[] content = new byte[10 * 1024 * 1024 + 1]; // 10MB + 1 byte
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615_ABC_001.sql", "text/plain", content);

            ValidationError error = service.validateScriptFile(file, null, null);

            assertNotNull(error);
            assertEquals("FILE_SIZE", error.code());
        }

        // --- Checksum format tests ---

        @Test
        void checksumFormat_validMD5_accepted() {
            String md5 = "d41d8cd98f00b204e9800998ecf8427e"; // MD5 of empty string
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615_ABC_001.sql", "text/plain", "data".getBytes());

            // Format is valid, but may mismatch — we test format acceptance
            ValidationError error = service.validateScriptFile(file, "MD5", md5);

            // Either null (if checksum matches) or CHECKSUM_MISMATCH (if doesn't match)
            // But NOT CHECKSUM_FORMAT
            if (error != null) {
                assertNotEquals("CHECKSUM_FORMAT", error.code());
            }
        }

        @Test
        void checksumFormat_invalidMD5_tooShort_rejected() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615_ABC_001.sql", "text/plain", "data".getBytes());

            ValidationError error = service.validateScriptFile(file, "MD5", "abc123");

            assertNotNull(error);
            assertEquals("CHECKSUM_FORMAT", error.code());
        }

        @Test
        void checksumFormat_invalidMD5_nonHex_rejected() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615_ABC_001.sql", "text/plain", "data".getBytes());

            ValidationError error = service.validateScriptFile(file, "MD5", "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz");

            assertNotNull(error);
            assertEquals("CHECKSUM_FORMAT", error.code());
        }

        @Test
        void checksumFormat_validSHA256_accepted() {
            String sha256 = "a" .repeat(64); // valid hex format (64 chars)
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615_ABC_001.sql", "text/plain", "data".getBytes());

            ValidationError error = service.validateScriptFile(file, "SHA-256", sha256);

            // Format is valid — error should be null or CHECKSUM_MISMATCH (not CHECKSUM_FORMAT)
            if (error != null) {
                assertNotEquals("CHECKSUM_FORMAT", error.code());
            }
        }

        @Test
        void checksumFormat_invalidSHA256_tooShort_rejected() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615_ABC_001.sql", "text/plain", "data".getBytes());

            ValidationError error = service.validateScriptFile(file, "SHA-256", "abcdef1234");

            assertNotNull(error);
            assertEquals("CHECKSUM_FORMAT", error.code());
        }

        @Test
        void checksumFormat_emptyChecksumValue_rejected() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615_ABC_001.sql", "text/plain", "data".getBytes());

            ValidationError error = service.validateScriptFile(file, "MD5", "");

            assertNotNull(error);
            assertEquals("CHECKSUM_FORMAT", error.code());
        }

        @Test
        void checksumFormat_unsupportedType_rejected() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615_ABC_001.sql", "text/plain", "data".getBytes());

            ValidationError error = service.validateScriptFile(file, "SHA-512", "a".repeat(128));

            assertNotNull(error);
            assertEquals("CHECKSUM_FORMAT", error.code());
        }

        // --- Checksum match/mismatch tests ---

        @Test
        void checksum_md5Match_accepted() throws Exception {
            byte[] content = "SELECT * FROM dual".getBytes();
            String md5 = computeHash(content, "MD5");
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615_ABC_001.sql", "text/plain", content);

            ValidationError error = service.validateScriptFile(file, "MD5", md5);

            assertNull(error);
        }

        @Test
        void checksum_md5Mismatch_rejected() {
            byte[] content = "SELECT * FROM dual".getBytes();
            String wrongMd5 = "00000000000000000000000000000000";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615_ABC_001.sql", "text/plain", content);

            ValidationError error = service.validateScriptFile(file, "MD5", wrongMd5);

            assertNotNull(error);
            assertEquals("CHECKSUM_MISMATCH", error.code());
        }

        @Test
        void checksum_sha256Match_accepted() throws Exception {
            byte[] content = "INSERT INTO t VALUES(1)".getBytes();
            String sha256 = computeHash(content, "SHA-256");
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615_ABC_001.sql", "text/plain", content);

            ValidationError error = service.validateScriptFile(file, "SHA-256", sha256);

            assertNull(error);
        }

        @Test
        void checksum_sha256Mismatch_rejected() {
            byte[] content = "INSERT INTO t VALUES(1)".getBytes();
            String wrongSha = "0".repeat(64);
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615_ABC_001.sql", "text/plain", content);

            ValidationError error = service.validateScriptFile(file, "SHA-256", wrongSha);

            assertNotNull(error);
            assertEquals("CHECKSUM_MISMATCH", error.code());
        }

        @Test
        void checksum_caseInsensitiveMatch_accepted() throws Exception {
            byte[] content = "SELECT 1".getBytes();
            String md5Lower = computeHash(content, "MD5");
            String md5Upper = md5Lower.toUpperCase();
            MockMultipartFile file = new MockMultipartFile(
                    "file", "20240615_ABC_001.sql", "text/plain", content);

            // Both lowercase and uppercase should be accepted
            assertNull(service.validateScriptFile(file, "MD5", md5Lower));
            assertNull(service.validateScriptFile(file, "MD5", md5Upper));
        }

        private String computeHash(byte[] content, String algorithm) throws Exception {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hashBytes = digest.digest(content);
            StringBuilder sb = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
    }

    // ============================
    // validate03TabContent tests
    // ============================

    @Nested
    class Validate03TabContentTests {

        @Test
        void hasScript_checksumMatch_returnsEmpty() {
            AccessRequest request = new AccessRequest();
            request.setRequestType(RequestType.YCCT_03);

            List<ValidationError> errors = service.validate03TabContent(request, true, true);

            assertTrue(errors.isEmpty());
        }

        @Test
        void hasScript_checksumNoMatch_noValidationOnTabs() {
            // When has script but checksum doesn't match, we don't enforce tab content
            AccessRequest request = new AccessRequest();
            request.setRequestType(RequestType.YCCT_03);
            request.setReason("some content");

            List<ValidationError> errors = service.validate03TabContent(request, true, false);

            assertTrue(errors.isEmpty());
        }

        @Test
        void noScript_withContent_returnsEmpty() {
            AccessRequest request = new AccessRequest();
            request.setRequestType(RequestType.YCCT_03);
            request.setReason("Detailed content here");

            List<ValidationError> errors = service.validate03TabContent(request, false, false);

            assertTrue(errors.isEmpty());
        }

        @Test
        void noScript_noContent_returnsError() {
            AccessRequest request = new AccessRequest();
            request.setRequestType(RequestType.YCCT_03);
            // reason is null

            List<ValidationError> errors = service.validate03TabContent(request, false, false);

            assertFalse(errors.isEmpty());
            assertTrue(errors.stream().anyMatch(e -> "REQUIRED".equals(e.code())));
        }

        @Test
        void noScript_blankContent_returnsError() {
            AccessRequest request = new AccessRequest();
            request.setRequestType(RequestType.YCCT_03);
            request.setReason("   "); // blank

            List<ValidationError> errors = service.validate03TabContent(request, false, false);

            assertFalse(errors.isEmpty());
            assertTrue(errors.stream().anyMatch(e -> "REQUIRED".equals(e.code())));
        }

        @Test
        void nullRequest_noScript_returnsEmpty() {
            List<ValidationError> errors = service.validate03TabContent(null, false, false);

            // With null request and no script, the condition (request != null) is false
            // so no error is added — returns empty
            assertTrue(errors.isEmpty());
        }
    }

    // ============================
    // Helper methods
    // ============================

    private AccessRequest buildValidRequest(RequestType type) {
        AccessRequest request = new AccessRequest();
        request.setRequestType(type);
        request.setRequesterUserId(1L);
        request.setShiftNo(1);
        request.setReason("Test reason");
        return request;
    }

    private RequestDetail buildDetail(Long systemId, Long databaseId, String objectName, Long targetUserId) {
        RequestDetail detail = new RequestDetail();
        detail.setSystemId(systemId);
        detail.setDatabaseId(databaseId);
        detail.setObjectName(objectName);
        detail.setTargetUserId(targetUserId);
        return detail;
    }
}
