package com.tickets.ticket_service.shared;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Genera tokens únicos y criptográficamente seguros para usar como QR codes.
 * Usa SecureRandom (no Random) para evitar predecibilidad.
 */
public final class QrCodeGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int BYTE_LENGTH = 32; // 256 bits de entropía

    private QrCodeGenerator() {}

    public static String generate() {
        byte[] bytes = new byte[BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
