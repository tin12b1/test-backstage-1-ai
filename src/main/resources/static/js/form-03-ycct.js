/**
 * form-03-ycct.js
 * JavaScript logic for form 03-YCCT (Yêu cầu thay đổi cấu trúc CSDL).
 *
 * 1. Tab display based on "Loại yêu cầu" checkboxes (Tạo mới / Thay đổi / Xóa)
 * 2. SQL script upload with checksum validation feedback
 *
 * Validates: Requirements 12.1, 12.2, 12.3, 12.4
 */
(function () {
    'use strict';

    // ===== CONSTANTS =====
    var TAB_MAP = {
        'CREATE': 'tab-create',
        'MODIFY': 'tab-modify',
        'DELETE': 'tab-delete'
    };

    var CHECKSUM_LENGTHS = {
        'MD5': 32,
        'SHA-256': 64
    };

    // ===== TAB DISPLAY LOGIC =====

    /**
     * Show/hide tab sections based on checked "Loại yêu cầu" checkboxes.
     * Requirement 12.1: Display corresponding tabs when checkbox is checked.
     */
    function updateTabVisibility() {
        var checkboxes = document.querySelectorAll('.request-type-03-checkbox');

        checkboxes.forEach(function (checkbox) {
            var tabId = TAB_MAP[checkbox.value];
            if (!tabId) return;

            var tabElement = document.getElementById(tabId);
            if (!tabElement) return;

            if (checkbox.checked) {
                tabElement.style.display = '';
            } else {
                tabElement.style.display = 'none';
            }
        });
    }

    /**
     * Validate that at least 1 checkbox is selected.
     * Requirement 12.2: At least 1 checkbox must be selected from Tạo mới / Thay đổi / Xóa.
     * @returns {boolean} true if at least one checkbox is checked
     */
    function validateAtLeastOneChecked() {
        var checkboxes = document.querySelectorAll('.request-type-03-checkbox');
        var atLeastOneChecked = false;

        checkboxes.forEach(function (checkbox) {
            if (checkbox.checked) {
                atLeastOneChecked = true;
            }
        });

        return atLeastOneChecked;
    }

    /**
     * Initialize checkbox change listeners for tab toggling.
     */
    function initTabToggle() {
        var checkboxes = document.querySelectorAll('.request-type-03-checkbox');

        checkboxes.forEach(function (checkbox) {
            checkbox.addEventListener('change', function () {
                updateTabVisibility();
            });
        });

        // Set initial visibility based on current checkbox state
        updateTabVisibility();
    }

    // ===== SQL SCRIPT UPLOAD WITH CHECKSUM VALIDATION =====

    /**
     * Display file info when a file is selected.
     */
    function initFileInputDisplay() {
        var fileInput = document.getElementById('scriptFileInput');
        if (!fileInput) return;

        fileInput.addEventListener('change', function () {
            var resultArea = document.getElementById('checksumResult');
            if (!resultArea) return;

            if (fileInput.files && fileInput.files.length > 0) {
                var file = fileInput.files[0];
                var fileSizeKB = (file.size / 1024).toFixed(1);
                resultArea.innerHTML = '<span class="text-muted">File: <b>' +
                    escapeHtml(file.name) + '</b> (' + fileSizeKB + ' KB)</span>';
                resultArea.style.display = 'block';
            } else {
                resultArea.style.display = 'none';
                resultArea.innerHTML = '';
            }
        });
    }

    /**
     * Validate checksum format before sending to server.
     * @param {string} type - MD5 or SHA-256
     * @param {string} value - hex string
     * @returns {boolean}
     */
    function isValidChecksumFormat(type, value) {
        var expectedLength = CHECKSUM_LENGTHS[type];
        if (!expectedLength) return false;

        if (value.length !== expectedLength) return false;

        // Must be hex characters only
        return /^[0-9a-fA-F]+$/.test(value);
    }

    /**
     * POST the file + checksum to /requests/{id}/upload-script and display result.
     * Requirement 12.3: When SQL Script file is uploaded with valid checksum,
     *   detail tab content becomes optional.
     * Requirement 12.4: When no SQL Script file is provided, content in all
     *   selected tabs is required.
     */
    function checkChecksum() {
        var fileInput = document.getElementById('scriptFileInput');
        var checksumTypeSelect = document.getElementById('checksumType');
        var checksumValueInput = document.getElementById('checksumValue');
        var resultArea = document.getElementById('checksumResult');

        if (!fileInput || !checksumTypeSelect || !checksumValueInput || !resultArea) {
            return;
        }

        // Validate file selected
        if (!fileInput.files || fileInput.files.length === 0) {
            showChecksumResult(resultArea, 'error', 'Vui lòng chọn file SQL trước.');
            return;
        }

        var checksumType = checksumTypeSelect.value;
        var checksumValue = checksumValueInput.value.trim();

        // Validate checksum value provided
        if (!checksumValue) {
            showChecksumResult(resultArea, 'error', 'Vui lòng nhập giá trị checksum.');
            return;
        }

        // Validate checksum format
        if (!isValidChecksumFormat(checksumType, checksumValue)) {
            var expectedLen = CHECKSUM_LENGTHS[checksumType] || '?';
            showChecksumResult(resultArea, 'error',
                'Định dạng checksum không hợp lệ. ' + checksumType + ' cần ' + expectedLen + ' ký tự hex.');
            return;
        }

        // Extract request ID from the page (from URL or hidden field)
        var requestId = getRequestId();
        if (!requestId) {
            showChecksumResult(resultArea, 'error', 'Không tìm thấy mã yêu cầu. Vui lòng lưu nháp trước.');
            return;
        }

        // Build FormData
        var formData = new FormData();
        formData.append('file', fileInput.files[0]);
        formData.append('checksumType', checksumType);
        formData.append('checksumValue', checksumValue);

        // Show loading state
        showChecksumResult(resultArea, 'loading', 'Đang kiểm tra...');

        // POST to server
        fetch('/requests/' + requestId + '/upload-script', {
            method: 'POST',
            body: formData
        })
        .then(function (response) {
            return response.json();
        })
        .then(function (data) {
            if (data.success) {
                // Checksum matches — green checkmark
                showChecksumResult(resultArea, 'success', '✓ Checksum hợp lệ');
                // Show message that detail tab content is optional
                showOptionalTabMessage(true);
            } else {
                // Checksum mismatch — red X
                showChecksumResult(resultArea, 'error', '✗ Checksum không khớp');
                showOptionalTabMessage(false);
            }
        })
        .catch(function () {
            showChecksumResult(resultArea, 'error', 'Lỗi kết nối. Vui lòng thử lại.');
            showOptionalTabMessage(false);
        });
    }

    /**
     * Display checksum verification result in the result area.
     * @param {HTMLElement} resultArea
     * @param {'success'|'error'|'loading'} type
     * @param {string} message
     */
    function showChecksumResult(resultArea, type, message) {
        resultArea.style.display = 'block';

        var cssClass = '';
        switch (type) {
            case 'success':
                cssClass = 'text-success';
                break;
            case 'error':
                cssClass = 'text-danger';
                break;
            case 'loading':
                cssClass = 'text-info';
                break;
        }

        resultArea.innerHTML = '<span class="' + cssClass + '">' + escapeHtml(message) + '</span>';
    }

    /**
     * Show/hide message that detail tab content is not required when script is uploaded.
     * Requirement 12.3: "Nội dung tab chi tiết không bắt buộc" when file uploaded with valid checksum.
     * @param {boolean} show
     */
    function showOptionalTabMessage(show) {
        var msgId = 'scriptUploadOptionalMsg';
        var existingMsg = document.getElementById(msgId);

        if (show) {
            if (!existingMsg) {
                var resultArea = document.getElementById('checksumResult');
                if (resultArea) {
                    var msgDiv = document.createElement('div');
                    msgDiv.id = msgId;
                    msgDiv.className = 'alert alert-info';
                    msgDiv.style.marginTop = '8px';
                    msgDiv.textContent = 'Nội dung tab chi tiết không bắt buộc';
                    resultArea.parentNode.insertBefore(msgDiv, resultArea.nextSibling);
                }
            }
        } else {
            if (existingMsg) {
                existingMsg.parentNode.removeChild(existingMsg);
            }
        }
    }

    /**
     * Extract the request ID from the current page URL or a hidden input.
     * @returns {string|null}
     */
    function getRequestId() {
        // Try from URL pattern: /requests/{id}/...
        var match = window.location.pathname.match(/\/requests\/(\d+)/);
        if (match) {
            return match[1];
        }

        // Try from hidden input
        var hiddenInput = document.querySelector('input[name="requestId"]');
        if (hiddenInput && hiddenInput.value) {
            return hiddenInput.value;
        }

        return null;
    }

    /**
     * Escape HTML to prevent XSS in dynamic content.
     * @param {string} str
     * @returns {string}
     */
    function escapeHtml(str) {
        var div = document.createElement('div');
        div.appendChild(document.createTextNode(str));
        return div.innerHTML;
    }

    // ===== FORM SUBMIT VALIDATION =====

    /**
     * Attach submit validation to the form to ensure at least 1 checkbox is checked.
     */
    function initSubmitValidation() {
        var form = document.querySelector('form[method="post"]');
        if (!form) return;

        form.addEventListener('submit', function (e) {
            // Only validate on submit actions (not draft save)
            var submitter = e.submitter;
            if (submitter && submitter.textContent &&
                submitter.textContent.indexOf('Luu') > -1) {
                // Draft save — skip strict validation
                return;
            }

            if (!validateAtLeastOneChecked()) {
                e.preventDefault();
                alert('Vui lòng chọn ít nhất 1 loại yêu cầu (Tạo mới / Thay đổi / Xóa).');
            }
        });
    }

    // ===== INITIALIZATION =====

    /**
     * Initialize all form 03-YCCT logic on DOMContentLoaded.
     */
    function init() {
        initTabToggle();
        initFileInputDisplay();
        initSubmitValidation();

        // Wire the "Kiểm tra" button
        var checkBtn = document.getElementById('checkChecksumBtn');
        if (checkBtn) {
            checkBtn.addEventListener('click', checkChecksum);
        }
    }

    // Run when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // Expose for external use if needed
    window.Form03YCCT = {
        updateTabVisibility: updateTabVisibility,
        validateAtLeastOneChecked: validateAtLeastOneChecked,
        checkChecksum: checkChecksum
    };

})();
