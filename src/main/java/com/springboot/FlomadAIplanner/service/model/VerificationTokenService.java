package com.springboot.FlomadAIplanner.service.model;

import com.springboot.FlomadAIplanner.model.entities.login.VerificationToken;
import com.springboot.FlomadAIplanner.model.repository.login.VerificationTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VerificationTokenService {
    private final VerificationTokenRepository verificationTokenRepository;

    public VerificationTokenService(VerificationTokenRepository verificationTokenRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
    }

    public VerificationToken getVerificationTokenById(String id) {
        log.debug("Attempting to find verification token with ID: {}", id);
        VerificationToken token = verificationTokenRepository.findById(id).orElse(null);
        if (token == null) {
            log.warn("No verification token found with ID: {}", id);
        } else {
            log.info("Found verification token with ID: {}", id);
        }
        return token;
    }

    public VerificationToken getVerificationTokenByUserId(String userId) {
        log.debug("Attempting to find verification token for user ID: {}", userId);
        VerificationToken token = verificationTokenRepository.findByUserId(userId);
        if (token == null) {
            log.warn("No verification token found for user ID: {}", userId);
        } else {
            log.info("Found verification token for user ID: {}", userId);
        }
        return token;
    }

    @SuppressWarnings("all")
    public VerificationToken saveVerificationToken(VerificationToken verificationToken) {
        log.debug("Saving verification token for user ID: {}", verificationToken.getUser().getId());
        VerificationToken savedToken = verificationTokenRepository.save(verificationToken);
        log.info("Saved verification token for user ID: {}", savedToken.getUser().getId());
        return savedToken;
    }

    public void deleteVerificationTokenByUserId(String userId) {
        log.debug("Deleting verification token for user ID: {}", userId);
        int deletedCount = verificationTokenRepository.deleteByUserId(userId);
        if (deletedCount == 0) {
            log.warn("No verification token found to delete for user ID: {}", userId);
        } else {
            log.info("Deleted verification token for user ID: {}", userId);
        }
    }
}
