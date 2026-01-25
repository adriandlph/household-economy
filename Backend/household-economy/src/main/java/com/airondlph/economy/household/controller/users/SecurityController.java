package com.airondlph.economy.household.controller.users;

import com.airondlph.economy.household.data.entity.Token;
import com.airondlph.economy.household.data.entity.User;
import com.airondlph.economy.household.data.enumeration.TokenType;
import com.airondlph.economy.household.data.model.TokenVO;
import com.airondlph.economy.household.exception.ServerErrorException;
import com.airondlph.economy.household.util.Crypton;
import com.airondlph.economy.household.api.rest.exception.SecurityException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.airondlph.economy.household.util.LogUtils.Enter;
import static com.airondlph.economy.household.util.LogUtils.Exit;
import static com.airondlph.economy.household.util.LogUtils.Error;

/**
 * @author adriandlph / airondlph
 */
@Slf4j
@Controller
@Transactional
public class SecurityController {

    @Autowired
    private EntityManager em;

    @Value("${login.rsa.key.private.filename}")
    private String loginPrvKeyFilename;
    @Value("${login.rsa.key.public.filename}")
    private String loginPubKeyFilename;

    @Value("${token.login.validTimeSeconds}")
    private Integer LOGIN_TOKEN_VALID_TIME_SECONDS;
    @Value("${token.login.issuer}")
    private String LOGIN_TOKEN_ISSUER;

    public String encodeUserPassword(String plainTextPassword) {
        return getPasswordEncoder().encode(plainTextPassword);

    }

    public TokenVO authenticateUser(String username, String password) throws ServerErrorException {
        User user;
        Token token;

        Enter(log, "authenticateUser", "username, password");

        if (username == null) {
            log.warn("Username username not defined.");
            Exit(log, "authenticateUser");
            throw new IllegalArgumentException("Wrong username or password.");
        }

        if (password == null) {
            log.warn("Password not defined.");
            Exit(log, "authenticateUser");
            throw new IllegalArgumentException("Wrong username or password.");
        }

        Query query = em.createQuery("SELECT u FROM User u WHERE u.username=:username ORDER BY u.id");
        query.setParameter("username", username);
        query.setMaxResults(1);

        try {
            user = (User) query.getResultList().getFirst();
        } catch (NoSuchElementException ex) {
            user = null;
        } catch (Exception ex) {
            Error(log, "Error getting users", null, ex.getMessage());
            Exit(log, "authenticateUser");
            throw new ServerErrorException("Server error.");
        }

        if (user == null) {
            log.warn("Wrong username.");
            Exit(log, "authenticateUser");
            throw new IllegalArgumentException("Wrong username or password.");
        }

        if (!passwordMatch(password, user.getPassword())) {
            log.warn("Wrong password.");
            Exit(log, "authenticateUser");
            throw new IllegalArgumentException("Wrong username or password.");
        }

        Calendar expires = Calendar.getInstance();
        expires.add(Calendar.SECOND, LOGIN_TOKEN_VALID_TIME_SECONDS);

        String tokenStr = createLoginToken(user, expires.getTime());

        token = Token.builder()
            .token(tokenStr)
            .expires(expires)
            .user(user)
            .type(TokenType.LOGIN_TOKEN)
            .build();

        try {
            em.persist(token);
        } catch (Exception ex) {
            Error(log, "Error persisting token", null, ex.getMessage());
            Exit(log, "authenticateUser");
            throw new ServerErrorException(1, "Error creating token.", ex);
        }

        return token.getVO();

    }

    private PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private boolean passwordMatch(String raw, String encoded) {
        return getPasswordEncoder().matches(raw, encoded);
    }

    private String createLoginToken(User user, Date expires) throws ServerErrorException {
        Enter(log, "createLoginToken");

        // Validate token data
        try {
            if (user == null) throw new ServerErrorException("User not defined.");
            if (user.getId() == null) throw new ServerErrorException("User's id not defined.");
            if (user.getUsername() == null) throw new ServerErrorException("User's username not defined.");
            if (expires == null) throw new ServerErrorException("Token expire date not defined.");
        } catch (ServerErrorException ex) {
            Exit(log, "createLoginToken");
            throw ex;
        }

        Algorithm algorithm;
        try {
            algorithm = getLoginEncryptionAlgorithm();
        } catch (ServerErrorException ex) {
            log.error("Error getting encryption algorithm: {}\n{}", ex.getMessage(), ex.getStackTrace());
            Exit(log, "createLoginToken");
            throw ex;
        }

        try {
            return JWT.create()
                .withClaim("userId", user.getId())
                .withClaim("username", user.getUsername())
                .withExpiresAt(expires)
                .withIssuer(LOGIN_TOKEN_ISSUER)
                .sign(algorithm);

        } catch (JWTCreationException ex) {
            log.error("Error creating token: {}\n{}", ex.getMessage(), ex.getStackTrace());
            throw new ServerErrorException(1, "Error creating token.", ex);
        } finally {
            Exit(log, "createLoginToken");
        }
    }

    private Algorithm getLoginEncryptionAlgorithm() throws ServerErrorException {
        Enter(log, "getLoginAlgorithm");
        try {
            RSAPublicKey pubKey = Crypton.ReadX509PublicKey(loginPubKeyFilename);
            RSAPrivateKey prvKey = Crypton.ReadPKCS8PrivateKey(loginPrvKeyFilename);
            return Algorithm.RSA256(pubKey, prvKey);

        } catch (Exception ex) {
            log.error("Error getting encryption algorithm: {}\n{}", ex.getMessage(), ex.getStackTrace());
            throw new ServerErrorException(1, "Error getting encryption algorithm.", ex);

        } finally {
            Exit(log, "getLoginAlgorithm");
        }
    }

    public Map<String, Claim> decodeToken(String token) throws SecurityException, ServerErrorException {
        Enter(log, "decodeToken");

        try {
            if (token == null) throw new SecurityException("Invalid token.");
            if (token.isBlank()) throw new SecurityException("Invalid token.");
        } catch (SecurityException ex) {
            Exit(log, "decodeToken");
            throw ex;
        }

        Algorithm algorithm;
        try {
            algorithm = getLoginEncryptionAlgorithm();
        } catch (ServerErrorException ex) {
            log.error("Error getting encryption algorithm: {}\n{}", ex.getMessage(), ex.getStackTrace());
            Exit(log, "decodeToken");
            throw ex;
        }

        try {
            // Check if it is expired.
            if(JWT.decode(token).getExpiresAt().before(new Date())) {
                log.warn("Token is expired.");
                throw new SecurityException("Token has expired.");
            }

            // Create verifier
            JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(LOGIN_TOKEN_ISSUER)
                .build();

            // Decode and verify token
            DecodedJWT decodedJWT = verifier.verify(token);

            // Get token data
            return decodedJWT.getClaims();
        } catch (JWTVerificationException ex) {
            log.error("Exception: {}\n{}", ex.getMessage(), ex.getStackTrace());
            throw new SecurityException("Not valid token.");
        } finally {
            Exit(log, "decodeToken");
        }

    }

}
