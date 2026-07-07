/**
 * form-01-yctc.js
 *
 * JavaScript logic specific to form 01-YCTC (Yêu cầu truy cập CSDL).
 * Handles:
 * 1. Access rights toggle based on "Loại yêu cầu" selection
 * 2. Type change confirmation dialog when detail rows exist
 * 3. Pre-registration loading on shift selection (AJAX)
 * 4. Re-indexing detail rows for proper Spring MVC form binding
 *
 * Requirements: 11.1, 11.2, 11.3, 11.4, 17.1, 17.4
 */
(function () {
    'use strict';

    // --- Element references ---
    var requestTypeSelect = document.getElementById('requestTypeSelect');
    var shiftSelect = document.getElementById('shiftSelect');
    var detailTable = document.getElementById('detailTable');

    // Track previous request type for revert on cancel
    var previousRequestType = requestTypeSelect ? requestTypeSelect.value : '';

    // --- 1. Access Rights Toggle Logic ---

    /**
     * Get all access right checkboxes on the form (excluding pre-registered rows).
     * Returns NodeList of checkboxes with class .access-right-checkbox
     */
    function getAccessRightCheckboxes() {
        return document.querySelectorAll('#detailTable tbody tr:not([data-pre-registered="true"]) .access-right-checkbox');
    }

    /**
     * Apply access rights rules based on the selected request type.
     * - "Truy vấn": auto-check SELECT only, disable INSERT/UPDATE/DELETE
     * - "Chỉnh sửa": enable all checkboxes, at least 1 must be selected
     *
     * Requirement 11.1, 11.2, 11.4
     */
    function applyAccessRightsForType(requestType) {
        var checkboxes = getAccessRightCheckboxes();

        checkboxes.forEach(function (cb) {
            var value = cb.getAttribute('value') || cb.value;

            if (requestType === 'Truy vấn') {
                // Auto-check SELECT, disable and uncheck others
                if (value === 'SELECT') {
                    cb.checked = true;
                    cb.disabled = false;
                } else {
                    cb.checked = false;
                    cb.disabled = true;
                }
            } else if (requestType === 'Chỉnh sửa') {
                // Enable all checkboxes
                cb.disabled = false;
            } else {
                // No type selected — disable all
                cb.checked = false;
                cb.disabled = true;
            }
        });
    }

    /**
     * Validate that at least 1 access right is selected for "Chỉnh sửa" type.
     * Skips pre-registered rows (they already have access rights via hidden inputs).
     * Returns true if valid, false otherwise.
     */
    function validateAccessRightsSelection() {
        if (!requestTypeSelect || requestTypeSelect.value !== 'Chỉnh sửa') {
            return true;
        }

        // Check only non-pre-registered rows that have data entered
        var editableRows = detailTable
            ? detailTable.querySelectorAll('tbody tr:not([data-pre-registered="true"])')
            : [];

        // If there are pre-registered rows, validation passes (they have rights already)
        var preRegRows = detailTable
            ? detailTable.querySelectorAll('tbody tr[data-pre-registered="true"]')
            : [];
        if (preRegRows.length > 0 && editableRows.length === 0) {
            return true;
        }

        // For editable rows: check if any row has content but no access rights checked
        var hasIssue = false;
        editableRows.forEach(function (row) {
            // Only validate rows that have at least one field filled
            var systemSelect = row.querySelector('select[name*="systemId"]');
            var dbSelect = row.querySelector('select[name*="databaseId"]');
            var userSelect = row.querySelector('select[name*="targetUserId"]');

            var hasContent = (systemSelect && systemSelect.value) ||
                             (dbSelect && dbSelect.value) ||
                             (userSelect && userSelect.value);

            if (hasContent) {
                var checkboxes = row.querySelectorAll('.access-right-checkbox');
                var hasChecked = false;
                checkboxes.forEach(function (cb) {
                    if (cb.checked) hasChecked = true;
                });
                if (!hasChecked) {
                    hasIssue = true;
                }
            }
        });

        return !hasIssue;
    }

    // --- 2. Type Change Confirmation Dialog ---

    /**
     * Check if detail rows have been filled in the table.
     * Returns true if at least one non-empty row exists.
     */
    function hasFilledDetailRows() {
        if (!detailTable) return false;
        var rows = detailTable.querySelectorAll('tbody tr');
        return rows.length > 0;
    }

    /**
     * Reset access rights in all existing detail rows according to the new type.
     * For "Truy vấn": set all rows to SELECT only.
     * For "Chỉnh sửa": leave as-is (user can modify).
     * Skips pre-registered rows.
     */
    function resetAccessRightsInDetailRows(newType) {
        if (!detailTable) return;

        var rows = detailTable.querySelectorAll('tbody tr:not([data-pre-registered="true"])');
        rows.forEach(function (row) {
            var rowCheckboxes = row.querySelectorAll('.access-right-checkbox');
            rowCheckboxes.forEach(function (cb) {
                var value = cb.getAttribute('value') || cb.value;

                if (newType === 'Truy vấn') {
                    if (value === 'SELECT') {
                        cb.checked = true;
                        cb.disabled = false;
                    } else {
                        cb.checked = false;
                        cb.disabled = true;
                    }
                } else if (newType === 'Chỉnh sửa') {
                    cb.disabled = false;
                }
            });

            // Also update any hidden input/text fields that store access rights CSV
            var accessRightsInput = row.querySelector('input[name*="accessRights"]');
            if (accessRightsInput && newType === 'Truy vấn') {
                accessRightsInput.value = 'SELECT';
            }
        });
    }

    /**
     * Handle request type change event.
     * Shows confirmation dialog if detail rows exist, then resets or reverts.
     * Clears pre-registered rows and reloads them for the new type.
     *
     * Requirement 11.3
     */
    function handleRequestTypeChange(event) {
        var newType = event.target.value;

        if (hasFilledDetailRows() && previousRequestType !== '') {
            var confirmed = window.confirm(
                'Thay đổi loại yêu cầu sẽ reset quyền truy cập đã chọn và xóa dữ liệu đăng ký trước đã nạp. Bạn có chắc chắn?'
            );

            if (confirmed) {
                // Xoa cac dong dang ky truoc da nap
                clearPreRegisteredRows();
                // Reset access rights in existing detail rows
                resetAccessRightsInDetailRows(newType);
                applyAccessRightsForType(newType);
                previousRequestType = newType;
                // Re-index after clearing pre-registered rows
                reindexDetailRows();
            } else {
                // Revert selection back to previous value
                event.target.value = previousRequestType;
                return;
            }
        } else {
            // No rows or first selection — just apply
            clearPreRegisteredRows();
            applyAccessRightsForType(newType);
            previousRequestType = newType;
            reindexDetailRows();
        }

        // If shift is already selected, reload pre-registrations for new type
        if (shiftSelect && shiftSelect.value) {
            loadPreRegistrations();
        }
    }

    /**
     * Xoa tat ca dong dang ky truoc da nap vao bang chi tiet.
     */
    function clearPreRegisteredRows() {
        if (!detailTable) return;
        var rows = detailTable.querySelectorAll('tr[data-pre-registered="true"]');
        rows.forEach(function(row) {
            row.parentNode.removeChild(row);
        });
    }

    // --- 3. Pre-registration Loading on Shift Selection ---

    /**
     * Get the unit code from the form (hidden field or data attribute).
     * Falls back to reading from a meta element or pre-populated input.
     */
    function getUnitCode() {
        var unitCodeEl = document.getElementById('unitCode')
            || document.querySelector('input[name="unitCode"]')
            || document.querySelector('[data-unit-code]');

        if (unitCodeEl) {
            return unitCodeEl.value || unitCodeEl.getAttribute('data-unit-code') || '';
        }
        return '';
    }

    /**
     * Get the current date for the form (from date input or today).
     */
    function getFormDate() {
        var dateEl = document.getElementById('requestDate')
            || document.querySelector('input[name="requestDate"]')
            || document.querySelector('input[type="date"]');

        if (dateEl && dateEl.value) {
            return dateEl.value;
        }
        // Default to today in YYYY-MM-DD format
        var now = new Date();
        var year = now.getFullYear();
        var month = String(now.getMonth() + 1).padStart(2, '0');
        var day = String(now.getDate()).padStart(2, '0');
        return year + '-' + month + '-' + day;
    }

    /**
     * Load pre-registrations via AJAX when shift is selected.
     * Calls GET /pre-registrations/load?unitCode={}&date={}&shift={}&requestType={}
     *
     * Requirement 17.1, 17.4
     */
    function loadPreRegistrations() {
        var unitCode = getUnitCode();
        var date = getFormDate();
        var shift = shiftSelect ? shiftSelect.value : '';
        var requestType = requestTypeSelect ? requestTypeSelect.value : '';

        // Only proceed if all required params are available
        if (!unitCode || !date || !shift || !requestType) {
            return;
        }

        var url = '/pre-registrations/load'
            + '?unitCode=' + encodeURIComponent(unitCode)
            + '&date=' + encodeURIComponent(date)
            + '&shift=' + encodeURIComponent(shift)
            + '&requestType=' + encodeURIComponent(requestType);

        var xhr = new XMLHttpRequest();
        xhr.open('GET', url, true);
        xhr.setRequestHeader('Accept', 'application/json');
        xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');

        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    try {
                        var data = JSON.parse(xhr.responseText);
                        populatePreRegisteredRows(data);
                    } catch (e) {
                        console.error('Error parsing pre-registration response:', e);
                    }
                } else {
                    console.error('Failed to load pre-registrations. Status:', xhr.status);
                }
            }
        };

        xhr.send();
    }

    /**
     * Populate the detail table with pre-registered rows.
     * Pre-registered rows are displayed as read-only but include hidden form fields
     * so they get submitted with the form (Spring MVC binding: details[N].xxx).
     *
     * Requirement 17.4
     */
    function populatePreRegisteredRows(preRegistrations) {
        if (!detailTable || !Array.isArray(preRegistrations)) return;

        var tbody = detailTable.querySelector('tbody') || detailTable;

        // Remove existing pre-registered rows (refresh on re-load)
        var existingPreRegRows = tbody.querySelectorAll('tr[data-pre-registered="true"]');
        existingPreRegRows.forEach(function (row) {
            row.parentNode.removeChild(row);
        });

        // Insert pre-registered rows at the top of tbody.
        // They will take indices 0..N-1, then existing Thymeleaf rows will be re-indexed.
        var firstExistingRow = tbody.querySelector('tr:not([data-pre-registered="true"])');

        preRegistrations.forEach(function (preReg, idx) {
            var row = document.createElement('tr');
            row.setAttribute('data-pre-registered', 'true');
            row.setAttribute('data-pre-registration-id', preReg.id || '');
            row.classList.add('pre-registered-row');

            // Build display HTML + hidden form fields (idx will be corrected by reindexDetailRows)
            row.innerHTML = buildPreRegisteredRowHtml(preReg, idx);

            // Insert before the first existing (editable) row
            if (firstExistingRow) {
                tbody.insertBefore(row, firstExistingRow);
            } else {
                tbody.appendChild(row);
            }
        });

        // Re-index ALL rows so form binding indices are sequential
        reindexDetailRows();
    }

    /**
     * Build HTML content for a pre-registered row.
     * Includes hidden form fields for Spring MVC submission AND visible read-only display.
     * The index (idx) is temporary — reindexDetailRows() will fix it after insertion.
     *
     * Columns: STT | He thong | CSDL | Ten doi tuong | Quyen truy cap | Ho va ten | Trang thai ky
     */
    function buildPreRegisteredRowHtml(preReg, idx) {
        var accessRights = preReg.accessRights || 'SELECT';
        var signedBadge = '<span class="badge badge-green">Đã ký</span>';
        var prefix = 'details[' + idx + '].';

        // Build access rights display (read-only checkboxes for visual only)
        var rightsHtml = '';
        var allRights = ['SELECT', 'INSERT', 'UPDATE', 'DELETE'];
        var selectedRights = accessRights.split(',').map(function(s) { return s.trim(); });
        allRights.forEach(function(r) {
            var checked = selectedRights.indexOf(r) >= 0 ? ' checked disabled' : ' disabled';
            rightsHtml += '<label style="font-weight:normal;font-size:12px"><input type="checkbox" class="access-right-checkbox"' + checked + ' value="' + r + '"/> ' + r.charAt(0) + '</label> ';
        });

        // Hidden form fields that will be submitted with the form
        var hiddenFields = ''
            + '<input type="hidden" name="' + prefix + 'systemId" value="' + (preReg.systemId || '') + '"/>'
            + '<input type="hidden" name="' + prefix + 'databaseId" value="' + (preReg.databaseId || '') + '"/>'
            + '<input type="hidden" name="' + prefix + 'objectName" value="' + escapeAttr(preReg.objectName || 'All Schema') + '"/>'
            + '<input type="hidden" name="' + prefix + 'accessRights" value="' + escapeAttr(accessRights) + '" class="access-rights-hidden"/>'
            + '<input type="hidden" name="' + prefix + 'targetUserId" value="' + (preReg.userId || '') + '"/>'
            + '<input type="hidden" name="' + prefix + 'preRegistrationId" value="' + (preReg.id || '') + '"/>';

        return ''
            + '<td>' + (idx + 1) + '</td>'
            + '<td>' + escapeHtml(preReg.systemName || '') + '</td>'
            + '<td>' + escapeHtml(preReg.databaseName || '') + '</td>'
            + '<td>' + escapeHtml(preReg.objectName || 'All Schema') + '</td>'
            + '<td>' + rightsHtml + '</td>'
            + '<td>' + escapeHtml(preReg.userName || '') + '</td>'
            + '<td>' + signedBadge + hiddenFields + '</td>';
    }

    /**
     * Escape HTML special characters to prevent XSS.
     */
    function escapeHtml(str) {
        if (!str) return '';
        var div = document.createElement('div');
        div.appendChild(document.createTextNode(str));
        return div.innerHTML;
    }

    /**
     * Escape a value for use in an HTML attribute.
     */
    function escapeAttr(str) {
        if (!str) return '';
        return str.replace(/&/g, '&amp;')
                  .replace(/"/g, '&quot;')
                  .replace(/'/g, '&#39;')
                  .replace(/</g, '&lt;')
                  .replace(/>/g, '&gt;');
    }

    // --- 4. Re-index Detail Rows ---

    /**
     * Re-index all detail rows in the table so their form field names
     * use sequential indices: details[0].xxx, details[1].xxx, etc.
     *
     * This must be called after inserting/removing pre-registered rows
     * so that Spring MVC binds all rows correctly.
     */
    function reindexDetailRows() {
        if (!detailTable) return;

        var tbody = detailTable.querySelector('tbody') || detailTable;
        var allRows = tbody.querySelectorAll('tr');

        allRows.forEach(function (row, rowIdx) {
            // Update STT column (first td)
            var sttCell = row.querySelector('td:first-child');
            if (sttCell) {
                sttCell.textContent = rowIdx + 1;
            }

            // Update all inputs/selects with names matching details[N].xxx
            var fields = row.querySelectorAll('input[name*="details["], select[name*="details["], textarea[name*="details["]');
            fields.forEach(function (field) {
                var name = field.getAttribute('name');
                if (name) {
                    // Replace details[ANY_NUMBER]. with details[rowIdx].
                    var newName = name.replace(/details\[\d+\]\./, 'details[' + rowIdx + '].');
                    field.setAttribute('name', newName);
                }

                // Also update id attributes if present (Thymeleaf generates id attributes)
                var fieldId = field.getAttribute('id');
                if (fieldId && fieldId.match(/details\d+/)) {
                    var newId = fieldId.replace(/details\d+/, 'details' + rowIdx);
                    field.setAttribute('id', newId);
                }
            });
        });
    }

    // --- 5. Shift Selection Handler ---

    /**
     * Handle shift selection change.
     * Triggers pre-registration loading when shift is selected
     * (only if request type is already chosen).
     */
    function handleShiftChange() {
        if (!requestTypeSelect || !requestTypeSelect.value) {
            return;
        }
        loadPreRegistrations();
    }

    // --- 6. Form Validation Hook ---

    /**
     * Attach validation before form submit to ensure at least 1 access right
     * is checked when type is "Chỉnh sửa" (only for editable rows with content).
     */
    function attachFormValidation() {
        var form = document.querySelector('form[action*="/requests"]');
        if (!form) return;

        form.addEventListener('submit', function (event) {
            if (!validateAccessRightsSelection()) {
                event.preventDefault();
                alert('Vui lòng chọn ít nhất 1 quyền truy cập (SELECT, INSERT, UPDATE hoặc DELETE) cho các dòng đã nhập dữ liệu.');
            }
        });
    }

    // --- Initialization ---

    function init() {
        if (!requestTypeSelect) return; // Not on form 01-YCTC page

        // Set initial state based on current value
        if (requestTypeSelect.value) {
            applyAccessRightsForType(requestTypeSelect.value);
            previousRequestType = requestTypeSelect.value;
        }

        // Bind event listeners
        requestTypeSelect.addEventListener('change', handleRequestTypeChange);

        if (shiftSelect) {
            shiftSelect.addEventListener('change', handleShiftChange);
        }

        attachFormValidation();
    }

    // Run on DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // Expose for testing/external access
    window.Form01YCTC = {
        applyAccessRightsForType: applyAccessRightsForType,
        validateAccessRightsSelection: validateAccessRightsSelection,
        loadPreRegistrations: loadPreRegistrations,
        handleRequestTypeChange: handleRequestTypeChange,
        handleShiftChange: handleShiftChange,
        reindexDetailRows: reindexDetailRows
    };
})();
