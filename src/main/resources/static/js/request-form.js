/**
 * request-form.js
 * Client-side logic for request form: auto-save, draft recovery,
 * signing-status polling, submit retry, and network status handling.
 *
 * Requirements: 2.2, 2.3, 2.4, 15.1, 15.2, 15.3, 15.4, 19.2
 */
(function () {
    'use strict';

    // ============================
    // Configuration
    // ============================
    var AUTO_SAVE_INTERVAL_MS = 30000; // 30 seconds
    var POLL_INTERVAL_MS = 5000;       // 5 seconds
    var SUBMIT_MAX_RETRIES = 3;
    var SUBMIT_RETRY_DELAY_MS = 5000;  // 5 seconds between retries
    var MULTI_SIGNER_TYPES = ['01-YCTC', '04A-YCTK', '04B-BGTK'];

    // ============================
    // State
    // ============================
    var lastSavedHash = '';
    var autoSaveTimer = null;
    var pollingTimer = null;
    var isSubmitting = false;

    // ============================
    // Utility: Simple content hash
    // ============================
    function computeHash(str) {
        var hash = 0;
        if (!str || str.length === 0) return '0';
        for (var i = 0; i < str.length; i++) {
            var ch = str.charCodeAt(i);
            hash = ((hash << 5) - hash) + ch;
            hash = hash & hash; // Convert to 32bit integer
        }
        return hash.toString(16);
    }

    // ============================
    // Utility: Get form metadata from DOM
    // ============================
    function getRequestId() {
        var el = document.getElementById('requestId');
        return el ? el.value : null;
    }

    function getRequestType() {
        var el = document.getElementById('requestType');
        return el ? el.value : null;
    }

    function getFormElement() {
        return document.getElementById('requestForm');
    }

    function getCsrfToken() {
        var meta = document.querySelector('meta[name="_csrf"]');
        return meta ? meta.getAttribute('content') : '';
    }

    function getCsrfHeader() {
        var meta = document.querySelector('meta[name="_csrf_header"]');
        return meta ? meta.getAttribute('content') : 'X-CSRF-TOKEN';
    }

    // ============================
    // Utility: Serialize form fields (excluding file and signature)
    // ============================
    function serializeFormFields() {
        var form = getFormElement();
        if (!form) return '';

        var data = {};
        var elements = form.elements;
        for (var i = 0; i < elements.length; i++) {
            var el = elements[i];
            // Skip file inputs, signature fields, buttons
            if (el.type === 'file' || el.type === 'submit' || el.type === 'button') continue;
            if (el.name && el.name.indexOf('signature') !== -1) continue;
            if (!el.name) continue;

            if (el.type === 'checkbox' || el.type === 'radio') {
                if (el.checked) {
                    if (data[el.name]) {
                        data[el.name] += ',' + el.value;
                    } else {
                        data[el.name] = el.value;
                    }
                }
            } else {
                data[el.name] = el.value;
            }
        }
        return JSON.stringify(data);
    }

    // ============================
    // Session Storage Draft Key
    // ============================
    function getDraftKey() {
        var requestType = getRequestType() || 'unknown';
        var requestId = getRequestId() || 'new';
        return 'draft_' + requestType + '_' + requestId;
    }

    // ============================
    // 1. Auto-save timer (30s interval with dirty check)
    // ============================
    function showSavedIndicator() {
        var indicator = document.getElementById('autoSaveIndicator');
        if (!indicator) {
            indicator = document.createElement('span');
            indicator.id = 'autoSaveIndicator';
            indicator.style.cssText =
                'position:fixed;bottom:20px;right:20px;background:#1f7a44;color:#fff;' +
                'padding:8px 16px;border-radius:4px;font-size:13px;z-index:9999;' +
                'opacity:1;transition:opacity 0.5s ease;';
            document.body.appendChild(indicator);
        }
        indicator.textContent = 'Đã lưu tự động';
        indicator.style.opacity = '1';
        indicator.style.display = 'block';
        setTimeout(function () {
            indicator.style.opacity = '0';
            setTimeout(function () {
                indicator.style.display = 'none';
            }, 500);
        }, 2000);
    }

    function performAutoSave() {
        var requestId = getRequestId();
        if (!requestId) return; // No ID means not yet saved to server

        var currentData = serializeFormFields();
        var currentHash = computeHash(currentData);

        if (currentHash === lastSavedHash) return; // No changes

        var headers = { 'Content-Type': 'application/json' };
        var csrfHeader = getCsrfHeader();
        var csrfToken = getCsrfToken();
        if (csrfToken) {
            headers[csrfHeader] = csrfToken;
        }

        fetch('/requests/' + requestId + '/auto-save', {
            method: 'POST',
            headers: headers,
            body: currentData,
            credentials: 'same-origin'
        })
        .then(function (response) {
            if (response.ok) {
                lastSavedHash = currentHash;
                showSavedIndicator();
            }
            // Silent on error — don't disrupt user
        })
        .catch(function () {
            // Network error — silent, will retry next cycle
        });
    }

    function startAutoSave() {
        // Initialize hash with current state
        lastSavedHash = computeHash(serializeFormFields());
        autoSaveTimer = setInterval(performAutoSave, AUTO_SAVE_INTERVAL_MS);
    }

    function stopAutoSave() {
        if (autoSaveTimer) {
            clearInterval(autoSaveTimer);
            autoSaveTimer = null;
        }
    }

    // ============================
    // 2. sessionStorage save on every field change
    // ============================
    function saveToSessionStorage() {
        var key = getDraftKey();
        var data = serializeFormFields();
        if (data) {
            try {
                sessionStorage.setItem(key, data);
            } catch (e) {
                // Storage full or unavailable — ignore
            }
        }
    }

    function attachFieldChangeListeners() {
        var form = getFormElement();
        if (!form) return;

        form.addEventListener('input', function (e) {
            var el = e.target;
            if (el.type === 'file') return;
            if (el.name && el.name.indexOf('signature') !== -1) return;
            saveToSessionStorage();
        });

        form.addEventListener('change', function (e) {
            var el = e.target;
            if (el.type === 'file') return;
            if (el.name && el.name.indexOf('signature') !== -1) return;
            saveToSessionStorage();
        });
    }

    // ============================
    // 3. Draft recovery dialog on page load
    // ============================
    function attemptDraftRecovery() {
        var key = getDraftKey();
        var savedDraft = null;
        try {
            savedDraft = sessionStorage.getItem(key);
        } catch (e) {
            return;
        }

        if (!savedDraft) return;

        var shouldRestore = confirm('Khôi phục bản nháp đã lưu?');
        if (shouldRestore) {
            populateFormFromDraft(savedDraft);
        } else {
            // User declined — remove stored draft
            try {
                sessionStorage.removeItem(key);
            } catch (e) {
                // Ignore
            }
        }
    }

    function populateFormFromDraft(draftJson) {
        var form = getFormElement();
        if (!form) return;

        var data;
        try {
            data = JSON.parse(draftJson);
        } catch (e) {
            return;
        }

        for (var name in data) {
            if (!data.hasOwnProperty(name)) continue;
            var value = data[name];
            var elements = form.elements[name];

            if (!elements) continue;

            // Handle NodeList (multiple elements with same name, e.g. radio/checkbox)
            if (elements.length !== undefined && elements.tagName === undefined) {
                var values = value.split(',');
                for (var i = 0; i < elements.length; i++) {
                    var el = elements[i];
                    if (el.type === 'checkbox' || el.type === 'radio') {
                        el.checked = values.indexOf(el.value) !== -1;
                    }
                }
            } else {
                // Single element
                if (elements.type === 'checkbox' || elements.type === 'radio') {
                    elements.checked = (elements.value === value);
                } else {
                    elements.value = value;
                }
            }
        }
    }

    // ============================
    // 4. Polling for signing-status (5s interval)
    // ============================
    function isMultiSignerForm() {
        var requestType = getRequestType();
        return requestType && MULTI_SIGNER_TYPES.indexOf(requestType) !== -1;
    }

    function startPolling() {
        var requestId = getRequestId();
        if (!requestId || !isMultiSignerForm()) return;

        pollingTimer = setInterval(function () {
            fetch('/requests/' + requestId + '/signing-status', {
                method: 'GET',
                credentials: 'same-origin'
            })
            .then(function (response) {
                if (response.ok) return response.json();
                return null;
            })
            .then(function (data) {
                if (data && data.details) {
                    updateDetailRowsUI(data.details);
                }
            })
            .catch(function () {
                // Network error — silent, will retry next poll
            });
        }, POLL_INTERVAL_MS);
    }

    function stopPolling() {
        if (pollingTimer) {
            clearInterval(pollingTimer);
            pollingTimer = null;
        }
    }

    function updateDetailRowsUI(details) {
        if (!details || !Array.isArray(details)) return;

        for (var i = 0; i < details.length; i++) {
            var detail = details[i];
            var row = document.querySelector('[data-detail-id="' + detail.detailId + '"]');
            if (!row) continue;

            // Update signed badge
            var statusCell = row.querySelector('.detail-status');
            if (statusCell) {
                if (detail.signed) {
                    statusCell.innerHTML = '<span class="badge" style="background:#1f7a44;">Đã ký</span>';
                } else {
                    statusCell.innerHTML = '<span class="badge">Chưa ký</span>';
                }
            }

            // Lock signed rows (make inputs readonly)
            if (detail.signed) {
                var inputs = row.querySelectorAll('input, select, textarea');
                for (var j = 0; j < inputs.length; j++) {
                    inputs[j].setAttribute('readonly', 'readonly');
                    inputs[j].setAttribute('disabled', 'disabled');
                }
                row.classList.add('row-signed');
            }

            // Update signature image if present
            if (detail.signatureImageUrl) {
                var sigCell = row.querySelector('.detail-signature');
                if (sigCell) {
                    sigCell.innerHTML =
                        '<img src="' + detail.signatureImageUrl + '" ' +
                        'alt="Chữ ký" class="signature-img">';
                }
            }
        }
    }

    // ============================
    // 5. Submit with retry (3 attempts × 5s delay)
    // ============================
    function showSubmitError(message) {
        var banner = document.getElementById('submitErrorBanner');
        if (!banner) {
            banner = document.createElement('div');
            banner.id = 'submitErrorBanner';
            banner.className = 'alert alert-error';
            banner.style.cssText = 'position:fixed;top:10px;left:50%;transform:translateX(-50%);' +
                'z-index:10000;min-width:320px;text-align:center;';
            document.body.appendChild(banner);
        }
        banner.textContent = message;
        banner.style.display = 'block';
    }

    function hideSubmitError() {
        var banner = document.getElementById('submitErrorBanner');
        if (banner) {
            banner.style.display = 'none';
        }
    }

    function showSessionExpiredDialog() {
        var confirmed = confirm('Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.');
        if (confirmed || !confirmed) {
            window.location.href = '/login';
        }
    }

    function submitWithRetry(form, attempt) {
        if (attempt === undefined) attempt = 1;

        var formData = new FormData(form);
        var requestId = getRequestId();
        var url = form.action || ('/requests/' + requestId + '/submit');

        var headers = {};
        var csrfHeader = getCsrfHeader();
        var csrfToken = getCsrfToken();
        if (csrfToken) {
            headers[csrfHeader] = csrfToken;
        }

        fetch(url, {
            method: 'POST',
            headers: headers,
            body: formData,
            credentials: 'same-origin'
        })
        .then(function (response) {
            if (response.ok) {
                // Success — clear draft and redirect
                clearDraft();
                isSubmitting = false;
                // Follow redirect or show success
                if (response.redirected) {
                    window.location.href = response.url;
                } else {
                    return response.json().then(function (data) {
                        if (data && data.redirectUrl) {
                            window.location.href = data.redirectUrl;
                        } else {
                            window.location.reload();
                        }
                    });
                }
                return;
            }

            if (response.status === 401) {
                // Session expired — stop retry, show dialog
                isSubmitting = false;
                showSessionExpiredDialog();
                return;
            }

            if (response.status >= 500) {
                // Server error — retry
                throw new Error('Server error: ' + response.status);
            }

            // Client error (4xx other than 401) — show message, don't retry
            isSubmitting = false;
            return response.text().then(function (text) {
                showSubmitError(text || 'Lỗi gửi yêu cầu. Vui lòng kiểm tra lại.');
            });
        })
        .catch(function (error) {
            // Network error or 5xx — retry
            if (attempt < SUBMIT_MAX_RETRIES) {
                setTimeout(function () {
                    submitWithRetry(form, attempt + 1);
                }, SUBMIT_RETRY_DELAY_MS);
            } else {
                isSubmitting = false;
                showSubmitError('Không thể gửi yêu cầu. Vui lòng thử lại sau.');
            }
        });
    }

    function attachSubmitHandler() {
        var form = getFormElement();
        if (!form) return;

        form.addEventListener('submit', function (e) {
            // Only intercept if the submit button is the actual submit action
            var submitBtn = document.activeElement;
            if (submitBtn && submitBtn.getAttribute('data-action') === 'draft') {
                return; // Let draft save go through normally
            }

            e.preventDefault();
            if (isSubmitting) return;

            isSubmitting = true;
            hideSubmitError();
            submitWithRetry(form, 1);
        });
    }

    // ============================
    // 6. Network offline/online banner
    // ============================
    function showOfflineBanner() {
        var banner = document.getElementById('networkBanner');
        if (!banner) {
            banner = document.createElement('div');
            banner.id = 'networkBanner';
            banner.className = 'alert alert-error';
            banner.style.cssText = 'position:fixed;top:0;left:0;right:0;z-index:10001;' +
                'text-align:center;border-radius:0;margin:0;padding:12px;';
            document.body.appendChild(banner);
        }
        banner.textContent = 'Mất kết nối mạng';
        banner.style.display = 'block';
    }

    function hideOfflineBanner() {
        var banner = document.getElementById('networkBanner');
        if (banner) {
            banner.style.display = 'none';
        }
    }

    function attachNetworkListeners() {
        window.addEventListener('offline', showOfflineBanner);
        window.addEventListener('online', hideOfflineBanner);
    }

    // ============================
    // Utility: Clear draft from sessionStorage
    // ============================
    function clearDraft() {
        var key = getDraftKey();
        try {
            sessionStorage.removeItem(key);
        } catch (e) {
            // Ignore
        }
    }

    // ============================
    // Initialization
    // ============================
    function init() {
        var form = getFormElement();
        if (!form) return; // Not on a request form page

        // 3. Draft recovery on page load
        attemptDraftRecovery();

        // 2. Attach field change listeners for sessionStorage
        attachFieldChangeListeners();

        // 1. Start auto-save timer
        startAutoSave();

        // 4. Start polling for multi-signer forms
        startPolling();

        // 5. Attach submit handler with retry
        attachSubmitHandler();

        // 6. Network status listeners
        attachNetworkListeners();
    }

    // Clean up on page unload
    function cleanup() {
        stopAutoSave();
        stopPolling();
    }

    // DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    window.addEventListener('beforeunload', cleanup);

    // Expose for testing/external use
    window.RequestForm = {
        startAutoSave: startAutoSave,
        stopAutoSave: stopAutoSave,
        startPolling: startPolling,
        stopPolling: stopPolling,
        performAutoSave: performAutoSave,
        saveToSessionStorage: saveToSessionStorage,
        clearDraft: clearDraft,
        computeHash: computeHash,
        serializeFormFields: serializeFormFields
    };
})();
