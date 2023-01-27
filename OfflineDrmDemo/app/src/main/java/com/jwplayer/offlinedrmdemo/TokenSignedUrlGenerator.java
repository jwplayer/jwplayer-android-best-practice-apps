package com.jwplayer.offlinedrmdemo;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

import java.util.Date;
import java.util.Locale;

/**
 * This is only for demo purposes, in a real production environment you would get a signed URL from your backend API
 */
public class TokenSignedUrlGenerator {
    private final static String API_SECRET = YOUR_V2_API_SECRET;

    private static final String DELIVERY_API_DOMAIN = "https://cdn.jwplayer.com";

    private static final String RESOURCE_TEMPLATE = "/v2/media/%s/drm/%s";
    private static final String TOKEN_TEMPLATE = "?token=%s";

    private static final String RESOURCE_KEY = "resource";
    private static final String EXPIRATION_KEY = "exp";

    public static String get(String mediaId, String policyId) {
        String resource = String.format(Locale.US, RESOURCE_TEMPLATE, mediaId, policyId);

        Date now = new Date();
        Date exp = new Date(now.getTime() + 5 * 60000);
        long expUnix = exp.getTime() / 1000;

        String token = "";
        try {
            Algorithm algorithm = Algorithm.HMAC256(API_SECRET);
            token = JWT.create()
                       .withClaim(RESOURCE_KEY, resource)
                       .withClaim(EXPIRATION_KEY, expUnix)
                       .sign(algorithm);
        } catch (JWTCreationException e) {
            e.printStackTrace();
        }
        return DELIVERY_API_DOMAIN + resource + String.format(Locale.US, TOKEN_TEMPLATE, token);
    }
}
