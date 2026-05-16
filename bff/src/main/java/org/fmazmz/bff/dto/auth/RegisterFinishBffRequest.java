package org.fmazmz.bff.dto.auth;

public record RegisterFinishBffRequest(
        String ceremonyId, String credentialJson, String userName, String email) {}
