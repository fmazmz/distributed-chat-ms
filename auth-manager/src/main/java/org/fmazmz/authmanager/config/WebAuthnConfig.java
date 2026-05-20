package org.fmazmz.authmanager.config;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.fmazmz.authmanager.service.JpaCredentialRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class WebAuthnConfig {

    private final AuthProperties authProperties;
    private final JpaCredentialRepository credentialRepository;

    @Bean
    public RelyingParty relyingParty() {
        var wa = authProperties.getWebauthn();
        RelyingPartyIdentity rpIdentity =
                RelyingPartyIdentity.builder().id(wa.getRpId()).name(wa.getRpName()).build();
        return RelyingParty.builder()
                .identity(rpIdentity)
                .credentialRepository(credentialRepository)
                .origins(new HashSet<>(wa.getOrigins()))
                .build();
    }
}
