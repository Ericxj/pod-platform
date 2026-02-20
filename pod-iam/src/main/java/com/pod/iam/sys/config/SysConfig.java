package com.pod.iam.sys.config;

import com.pod.iam.sys.service.CredentialEncryptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SysConfig {

    @Value("${pod.sys.credential.secret:default-dev-secret-16bytes}")
    private String credentialSecret;

    @Bean
    public CredentialEncryptionService credentialEncryptionService() {
        return new CredentialEncryptionService(credentialSecret);
    }
}
