package com.airondlph.economy.household.api.rest;

import com.airondlph.economy.household.api.rest.data.RestApiResult;
import com.airondlph.economy.household.api.rest.data.TokenStrDTO;
import com.airondlph.economy.household.api.rest.data.UserDTO;
import com.airondlph.economy.household.controller.data.Result;
import com.airondlph.economy.household.controller.users.SecurityController;
import com.airondlph.economy.household.controller.users.UsersController;
import com.airondlph.economy.household.data.model.TokenVO;
import com.airondlph.economy.household.data.model.UserVO;
import com.airondlph.economy.household.api.rest.exception.SecurityException;
import com.airondlph.economy.household.exception.ServerErrorException;
import com.auth0.jwt.interfaces.Claim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * @author adriandlph / airondlph
 */
@RestController
@RequestMapping(value = "user")
public class UsersRESTController {
    @Autowired
    private UsersController usersController;
    @Autowired
    private SecurityController securityController;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @RequestMapping(
            value = "/{id}/",
            method = GET,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<UserDTO>> getUser(@PathVariable("id") String id) {
        Long loggedUserId;
        try {
            String token = SecurityRESTController.getBearerTokenHeader();
            Map<String, Claim> claims = securityController.decodeToken(token);

            Claim userIdClaim = claims.get("userId");
            if (userIdClaim == null || userIdClaim.isMissing() || userIdClaim.isNull() || ((loggedUserId = userIdClaim.asLong()) == null)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(2, "Not Authorized."));
            }

        } catch (ServerErrorException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(-1, "Server error."));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(2, "Invalid token."));
        }

        Long userId = Long.valueOf(id);


        Result<UserVO> createUserResult = usersController.getUserByIdVO(loggedUserId, userId);

        if (!createUserResult.isValid()) {

            if (createUserResult.getErrCode() == 3) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(3, "User does not have access to get this information."));

            String errMessage = switch (createUserResult.getErrCode()) {
                case 2 -> "User id not defined.";
                case 4 -> "User does not exists.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(createUserResult.getErrCode(), errMessage));
        }

        UserVO userVO = createUserResult.getResult();

        UserDTO response = UserDTO.builder()
                .id(userVO.getId())
                .username(userVO.getUsername())
                .firstName(userVO.getFirstName())
                .lastName(userVO.getLastName())
                .email(userVO.getEmail())
                .emailValidated(userVO.getEmailValidated())
                .build();

        return ResponseEntity.ok().body(RestApiResult.Ok(response));
    }

    @RequestMapping(
            value = "/",
            method = POST,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<UserDTO>> createUser(@RequestBody UserDTO userData) {

        UserVO userVO = UserVO.builder()
            .id(0L)
            .username(userData.getUsername())
            .password(userData.getPassword())
            .firstName(userData.getFirstName())
            .lastName(userData.getLastName())
            .email(userData.getEmail())
            .build();

        Result<UserVO> createUserResult = usersController.createUserVO(userVO);

        if (!createUserResult.isValid()) {
            String errMessage = switch (createUserResult.getErrCode()) {
                case 5 -> "User's username not defined";
                case 6 -> "User's username not valid";
                case 7 -> "User's password not defined";
                case 8 -> "User's password not valid";
                case 9 -> "User's first name not defined";
                case 10 -> "User's email not defined";
                case 11 -> "User's email not valid";
                case 12 -> "User's username or email already registered";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(createUserResult.getErrCode(), errMessage));
        }

        userVO = createUserResult.getResult();

        UserDTO response = UserDTO.builder()
            .id(userVO.getId())
            .username(userVO.getUsername())
            .firstName(userVO.getFirstName())
            .lastName(userVO.getLastName())
            .email(userVO.getEmail())
            .emailValidated(userVO.getEmailValidated())
            .build();

        return ResponseEntity.ok().body(RestApiResult.Ok(response));
    }

    @RequestMapping(
            value = "/{id}/",
            method = PUT,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<UserDTO>> setUser(@PathVariable("id") String id, @RequestBody UserDTO userData) {
        Long loggedUserId;
        try {
            String token = SecurityRESTController.getBearerTokenHeader();
            Map<String, Claim> claims = securityController.decodeToken(token);

            Claim userIdClaim = claims.get("userId");
            if (userIdClaim == null || userIdClaim.isMissing() || userIdClaim.isNull() || ((loggedUserId = userIdClaim.asLong()) == null)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(2, "Not Authorized."));
            }

        } catch (ServerErrorException | SecurityException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(-1, "Server error."));
        }

        Long userId = Long.valueOf(id);

        UserVO userVO = UserVO.builder()
            .id(userId)
            .username(userData.getUsername())
            .firstName(userData.getFirstName())
            .lastName(userData.getLastName())
            .email(userData.getEmail())
            .build();

        Result<UserVO> setUserResult = usersController.setUserVO(loggedUserId, userVO);

        if (!setUserResult.isValid()) {
            if (setUserResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(-1, "Server error."));
            if (setUserResult.getErrCode() == 2 || setUserResult.getErrCode() == 4) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(3, "User does not have access to get this information."));

            String errMessage = switch (setUserResult.getErrCode()) {
                case 3 -> "User data not defined.";
                case 5 -> "User's username cannot be blank.";
                case 6 -> "User's username not valid.";
                case 7 -> "User's first namer cannot be blank.";
                case 8 -> "User's email cannot be blank.";
                case 9 -> "User's email not valid.";
                case 10 -> "User's username already in use.";
                case 11 -> "User's email already in use.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(setUserResult.getErrCode(), errMessage));
        }

        userVO = setUserResult.getResult();
        UserDTO response = UserDTO.builder()
                .id(userVO.getId())
                .username(userVO.getUsername())
                .firstName(userVO.getFirstName())
                .lastName(userVO.getLastName())
                .email(userVO.getEmail())
                .emailValidated(userVO.getEmailValidated())
                .build();

        return ResponseEntity.ok().body(RestApiResult.Ok(response));
    }


    @RequestMapping(
            value = "/{id}/",
            method = DELETE,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<Void>> deleteUser(@PathVariable("id") String id) {
        Long loggedUserId;

        try {
            String token = SecurityRESTController.getBearerTokenHeader();
            Map<String, Claim> claims = securityController.decodeToken(token);

            Claim userIdClaim = claims.get("userId");
            if (userIdClaim == null || userIdClaim.isMissing() || userIdClaim.isNull() || ((loggedUserId = userIdClaim.asLong()) == null)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(2, "Not Authorized."));
            }

        } catch (ServerErrorException | SecurityException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(-1, "Server error."));
        }

        Long userToDeleteId;
        try {
            userToDeleteId = Long.parseLong(id);
        } catch (Exception ex) {
            userToDeleteId = null;
        }

        if ((userToDeleteId == null) || (userToDeleteId < 0) || (userToDeleteId >= Long.MAX_VALUE)) {
            return ResponseEntity.badRequest().body(RestApiResult.Error(3, "User not defined."));
        }

        Result<Void> createUserResult = usersController.deleteUserByIdVO(loggedUserId, userToDeleteId);
        if (!createUserResult.isValid()) {
            return switch (createUserResult.getErrCode()) {
                case 2, 4 -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(2, "Not Authorized."));
                case 3 -> ResponseEntity.badRequest().body(RestApiResult.Error(4, "User not found."));
                default -> ResponseEntity.badRequest().body(RestApiResult.Error(1, "Error."));
            };
        }

        return ResponseEntity.ok().body(RestApiResult.Ok(null));
    }

    @RequestMapping(
            value = "/{id}/validate/email/sendCode/",
            method = PUT,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<Void>> sendUserEmailValidationCode(@PathVariable("id") String id) {
        Long loggedUserId;

        try {
            String token = SecurityRESTController.getBearerTokenHeader();
            Map<String, Claim> claims = securityController.decodeToken(token);

            Claim userIdClaim = claims.get("userId");
            if (userIdClaim == null || userIdClaim.isMissing() || userIdClaim.isNull() || ((loggedUserId = userIdClaim.asLong()) == null)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(2, "Not Authorized."));
            }
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(2, "Not Authorized."));
        } catch (ServerErrorException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(-1, "Server error."));
        }

        Long userToValidateEmail;
        try {
            userToValidateEmail = Long.parseLong(id);
        } catch (Exception ex) {
            userToValidateEmail = null;
        }

        if ((userToValidateEmail == null) || (userToValidateEmail < 0) || (userToValidateEmail >= Long.MAX_VALUE)) {
            return ResponseEntity.badRequest().body(RestApiResult.Error(2, "User not defined."));
        }

        Result<Void> createUserResult = usersController.sendValidateUserEmailCodeVO(loggedUserId, userToValidateEmail);
        if (!createUserResult.isValid()) {
            if (createUserResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(-1, "Server error."));

            return switch (createUserResult.getErrCode()) {
                case 2 -> ResponseEntity.badRequest().body(RestApiResult.Error(2, "User not defined."));
                case 3 -> ResponseEntity.badRequest().body(RestApiResult.Error(3, "Email not defined."));
                case 4 -> ResponseEntity.badRequest().body(RestApiResult.Error(4, "Email already validated."));
                case 5 -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(5, "Not Authorized."));
                default -> ResponseEntity.badRequest().body(RestApiResult.Error(1, "Error."));
            };
        }

        return ResponseEntity.ok().body(RestApiResult.Ok(null));
    }

    @RequestMapping(
            value = "/{id}/validate/email/code/{code}/",
            method = {PUT, GET}, // Get just for email links
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<Void>> valdiateUserEmail(@PathVariable("id") String id, @PathVariable("code") String code) {

        Long userToValidateEmail;
        try { userToValidateEmail = Long.parseLong(id); } catch (Exception ex) { userToValidateEmail = null; }

        if ((userToValidateEmail == null) || (userToValidateEmail < 0) || (userToValidateEmail >= Long.MAX_VALUE)) {
            return ResponseEntity.badRequest().body(RestApiResult.Error(2, "User not defined."));
        }

        Result<Void> createUserResult = usersController.validateUserEmailVO(userToValidateEmail, code);
        if (!createUserResult.isValid()) {
            if (createUserResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(-1, "Server error."));

            return switch (createUserResult.getErrCode()) {
                case 2 -> ResponseEntity.badRequest().body(RestApiResult.Error(2, "User not defined."));
                case 3 -> ResponseEntity.badRequest().body(RestApiResult.Error(3, "Incorrect code."));
                case 4 -> ResponseEntity.badRequest().body(RestApiResult.Error(4, "Expired code."));
                default -> ResponseEntity.badRequest().body(RestApiResult.Error(1, "Error."));
            };
        }

        return ResponseEntity.ok().body(RestApiResult.Ok(null));
    }

    @RequestMapping(
            value = "/login/",
            method = POST,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<TokenStrDTO>> login(@RequestBody UserDTO userData) {

        try {
            TokenVO tokenVO = securityController.authenticateUser(userData.getUsername(), userData.getPassword());
            return ResponseEntity.ok().body(RestApiResult.Ok(TokenStrDTO.builder().token(tokenVO.getToken()).build()));
        } catch (ServerErrorException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(-1, "Server error."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestApiResult.Error(1, ex.getMessage()));
        }
    }

}
