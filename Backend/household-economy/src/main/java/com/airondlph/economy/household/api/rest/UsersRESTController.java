package com.airondlph.economy.household.api.rest;

import com.airondlph.economy.household.api.rest.data.RestApiResult;
import com.airondlph.economy.household.api.rest.data.UserDTO;
import com.airondlph.economy.household.controller.data.Result;
import com.airondlph.economy.household.controller.users.UsersController;
import com.airondlph.economy.household.data.model.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author adriandlph / airondlph
 */
@Slf4j
@RestController
@RequestMapping(value = "user")
public class UsersRESTController {
    @Autowired
    UsersController usersController;


    @RequestMapping(
            value = "/",
            method = POST,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<UserDTO>> createUser(@RequestBody UserDTO userData) {

        UserVO userVO = UserVO.builder()
            .id(0L)
            .username(userData.getUsername())
            .firstName(userData.getFirstName())
            .lastName(userData.getLastName())
            .email(userData.getEmail())
            .build();

        Result<UserVO> createUserResult = usersController.createUserVO(userVO);

        if (!createUserResult.isValid()) {
            String errMessage = switch (createUserResult.getErrCode()) {
                case 2 -> "User's data not defined";
                case 3 -> "User's username not defined";
                case 4 -> "User's username not valid";
                case 5 -> "User's first name not defined";
                case 6 -> "User's email not defined";
                case 7 -> "User's email not valid";
                case 8 -> "User's username or email already registered";
                default -> "Error.";
            };
            log.error("{}", RestApiResult.Error(createUserResult.getErrCode(), errMessage));
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

}
