package com.projectx.backend.global.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Profile("prod")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CloudFrontOriginSecretFilter extends OncePerRequestFilter {

	private static final String HEADER_NAME = "X-ProjectX-Origin-Verify";

	private final String originSecret;

	public CloudFrontOriginSecretFilter(@Value("${CLOUDFRONT_ORIGIN_SECRET:}") String originSecret) {
		if (originSecret.isBlank()) {
			throw new IllegalStateException("CLOUDFRONT_ORIGIN_SECRET must be configured for prod");
		}
		this.originSecret = originSecret;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !request.getRequestURI().startsWith("/api/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (!originSecret.equals(request.getHeader(HEADER_NAME))) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		filterChain.doFilter(request, response);
	}

}
