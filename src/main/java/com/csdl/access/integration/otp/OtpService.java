package com.csdl.access.integration.otp;

import com.csdl.access.domain.OtpTransaction;
import com.csdl.access.domain.repo.OtpTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xac thuc OTP va luu giao dich de audit (api-contract.md muc 8).
 */
@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private final OtpClient otpClient;
    private final OtpTransactionRepository otpTransactionRepository;

    public OtpService(OtpClient otpClient, OtpTransactionRepository otpTransactionRepository) {
        this.otpClient = otpClient;
        this.otpTransactionRepository = otpTransactionRepository;
    }

    /**
     * Xac thuc OTP qua OtpClient da cau hinh va luu lai giao dich (thanh cong/that bai) de audit.
     * Gan transactionId vao ket qua tra ve va ghi log ket qua.
     */
    @Transactional
    public OtpVerifyResult verifyOtp(String username, String otp, String purpose, Long requestId) {
        OtpVerifyResult result = otpClient.verify(username, otp, purpose, requestId);

        // Luu ban ghi giao dich OTP de phuc vu audit/tra soat.
        OtpTransaction tx = new OtpTransaction();
        tx.setUsername(username);
        tx.setPurpose(purpose);
        tx.setRequestId(requestId);
        tx.setResult(result.isSuccess() ? "SUCCESS" : "FAILED");
        tx = otpTransactionRepository.save(tx);

        result.setTransactionId(tx.getId());
        // Ghi ra file log ung dung (man hinh Debug) - ca thanh cong lan that bai.
        if (result.isSuccess()) {
            log.info("[OTP] user={} mucDich={} phieu={} ket qua=THANH CONG txId={}",
                    username, purpose, requestId, tx.getId());
        } else {
            log.warn("[OTP] user={} mucDich={} phieu={} ket qua=THAT BAI ({})",
                    username, purpose, requestId, result.getMessage());
        }
        return result;
    }
}
