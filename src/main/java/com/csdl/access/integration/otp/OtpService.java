package com.csdl.access.integration.otp;

import com.csdl.access.domain.OtpTransaction;
import com.csdl.access.domain.repo.OtpTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xac thuc OTP va luu giao dich de audit (api-contract.md muc 8).
 */
@Service
public class OtpService {

    private final OtpClient otpClient;
    private final OtpTransactionRepository otpTransactionRepository;

    public OtpService(OtpClient otpClient, OtpTransactionRepository otpTransactionRepository) {
        this.otpClient = otpClient;
        this.otpTransactionRepository = otpTransactionRepository;
    }

    @Transactional
    public OtpVerifyResult verifyOtp(String username, String otp, String purpose, Long requestId) {
        OtpVerifyResult result = otpClient.verify(username, otp, purpose, requestId);

        OtpTransaction tx = new OtpTransaction();
        tx.setUsername(username);
        tx.setPurpose(purpose);
        tx.setRequestId(requestId);
        tx.setResult(result.isSuccess() ? "SUCCESS" : "FAILED");
        tx = otpTransactionRepository.save(tx);

        result.setTransactionId(tx.getId());
        return result;
    }
}
