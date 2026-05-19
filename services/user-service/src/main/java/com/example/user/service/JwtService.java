package com.example.user.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
	private static final String SECRET_KEY = "mysupersecretkeymysupersecretkeymysupersecretkey";
	private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

	public String generateToken(String username, String role) {
		return Jwts.builder().setSubject(username).claim("role", role).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 3_600_000)) // 60 minutes Expiry
				.signWith(key, SignatureAlgorithm.HS256).compact();
	}

	public boolean validateToken(String token) {
		try {
			Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);

			return !claims.getBody().getExpiration().before(new Date());
		} catch (JwtException e) {
			return false;
		}
	}

	public String extractUsername(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
	}

	public String extractRole(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("role",
				String.class);
	}
}
