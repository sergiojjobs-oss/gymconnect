package com.gymconnect.api.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LoginRateLimiter implements Filter {

    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_MS = 60_000; // 1 minuto

    private record Bucket(AtomicInteger count, long windowStart) {}
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) req;
        if ("POST".equals(http.getMethod()) && http.getRequestURI().contains("/api/auth/login")) {
            String ip = getClientIp(http);
            long now = System.currentTimeMillis();
            Bucket b = buckets.compute(ip, (k, existing) -> {
                if (existing == null || now - existing.windowStart() > WINDOW_MS)
                    return new Bucket(new AtomicInteger(1), now);
                existing.count().incrementAndGet();
                return existing;
            });
            if (b.count().get() > MAX_ATTEMPTS) {
                HttpServletResponse httpRes = (HttpServletResponse) res;
                httpRes.setStatus(429);
                httpRes.setContentType("application/json");
                httpRes.getWriter().write("{\"error\":\"Demasiados intentos. Espera 1 minuto.\"}");
                return;
            }
        }
        chain.doFilter(req, res);
    }

    private String getClientIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return req.getRemoteAddr();
    }
}
