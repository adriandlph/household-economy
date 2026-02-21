package com.airondlph.economy.household.api.rest;

import com.airondlph.economy.household.api.rest.data.BankDTO;
import com.airondlph.economy.household.api.rest.data.RestApiResult;
import com.airondlph.economy.household.api.rest.exception.SecurityException;
import com.airondlph.economy.household.controller.business.BusinessController;
import com.airondlph.economy.household.controller.data.Result;
import com.airondlph.economy.household.controller.users.SecurityController;
import com.airondlph.economy.household.data.model.BankVO;
import com.airondlph.economy.household.data.model.UserVO;
import com.airondlph.economy.household.exception.ServerErrorException;
import com.auth0.jwt.interfaces.Claim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(value = "financial")
public class FinancialRESTController {

    @Autowired
    private SecurityController securityController;
    @Autowired
    private BusinessController businessController;

    @RequestMapping(
            value = "/bank/{id}/",
            method = GET,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<BankDTO>> getBank(@PathVariable("id") String id) {
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

        Long bankId = Long.valueOf(id);


        Result<BankVO> getBankResult = businessController.getBankByIdVO(UserVO.builder().id(loggedUserId).build(), BankVO.builder().id(bankId).build());

        if (!getBankResult.isValid()) {
            // Server error
            if (getBankResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(getBankResult.getErrCode(), "Server error"));
            // Permission error
            if (getBankResult.getErrCode() == 4) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(getBankResult.getErrCode(), "User does not have access to get this bank data."));
            if (getBankResult.getErrCode() == 5) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(getBankResult.getErrCode(), "Not user logged."));

            String errMessage = switch (getBankResult.getErrCode()) {
                case 2 -> "Bank id not defined.";
                case 3 -> "Bank does not exists.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(getBankResult.getErrCode(), errMessage));
        }

        BankVO bankVO = getBankResult.getResult();

        BankDTO response = BankDTO.builder()
            .id(bankVO.getId())
            .name(bankVO.getName())
            .build();

        return ResponseEntity.ok().body(RestApiResult.Ok(response));
    }

    @RequestMapping(
            value = "/bank/",
            method = POST,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<BankDTO>> createBank(@RequestBody BankDTO bankData) {
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

        BankVO createBankDataVO = BankVO.builder()
            .name(bankData.getName())
            .build();

        Result<BankVO> createBankResult = businessController.createBankVO(UserVO.builder().id(loggedUserId).build(), createBankDataVO);

        if (!createBankResult.isValid()) {

            // Server error
            if (createBankResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(createBankResult.getErrCode(), "Server error"));
            // Permission error
            if (createBankResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(createBankResult.getErrCode(), "Not user logged."));
            if (createBankResult.getErrCode() == 3) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(createBankResult.getErrCode(), "User does not have access to create a bank."));

            String errMessage = switch (createBankResult.getErrCode()) {
                case 4 -> "Bank data not defined.";
                case 5 -> "Bank name not defined.";
                case 6 -> "Invalid bank name.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(createBankResult.getErrCode(), errMessage));
        }

        BankVO bankVO = createBankResult.getResult();

        BankDTO response = BankDTO.builder()
            .id(bankVO.getId())
            .name(bankVO.getName())
            .build();

        return ResponseEntity.ok().body(RestApiResult.Ok(response));
    }

    @RequestMapping(
            value = "/bank/{id}/",
            method = DELETE,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<BankDTO>> deleteBank(@PathVariable("id") String id) {
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

        Long bankId = Long.valueOf(id);

        BankVO deleteBankDataVO = BankVO.builder()
            .id(bankId)
            .build();

        Result<BankVO> deleteBankResult = businessController.deleteBankByIdVO(UserVO.builder().id(loggedUserId).build(), deleteBankDataVO);

        if (!deleteBankResult.isValid()) {
            // Server error
            if (deleteBankResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(deleteBankResult.getErrCode(), "Server error"));
            // Permission error
            if (deleteBankResult.getErrCode() == 5) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(deleteBankResult.getErrCode(), "Not user logged."));
            if (deleteBankResult.getErrCode() == 4) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(deleteBankResult.getErrCode(), "User does not have access to delete this bank."));

            String errMessage = switch (deleteBankResult.getErrCode()) {
                case 2 -> "Bank does not exists.";
                case 3 -> "Bank id not defined.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(deleteBankResult.getErrCode(), errMessage));
        }

        BankVO bankVO = deleteBankResult.getResult();

        BankDTO response = BankDTO.builder()
                .id(bankVO.getId())
                .name(bankVO.getName())
                .build();

        return ResponseEntity.ok().body(RestApiResult.Ok(response));
    }

    @RequestMapping(
            value = "/bank/{id}/",
            method = PUT,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<BankDTO>> setBank(@PathVariable("id") String id, @RequestBody BankDTO bankData) {
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

        Long bankId = Long.valueOf(id);

        BankVO editedBankDataVO = BankVO.builder()
            .id(bankId)
            .name(bankData.getName())
            .build();

        Result<BankVO> editBankResult = businessController.editBankVO(UserVO.builder().id(loggedUserId).build(), editedBankDataVO);

        if (!editBankResult.isValid()) {
            // Server error
            if (editBankResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(editBankResult.getErrCode(), "Server error"));
            // Permission error
            if (editBankResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(editBankResult.getErrCode(), "Not user logged."));
            if (editBankResult.getErrCode() == 3) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(editBankResult.getErrCode(), "User does not have access to edit this bank."));

            String errMessage = switch (editBankResult.getErrCode()) {
                case 4 -> "Bank does not exists.";
                case 5 -> "Bank data not defined.";
                case 6 -> "Bank id not defined.";
                case 7 -> "Invalid bank name.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(editBankResult.getErrCode(), errMessage));
        }

        BankVO bankVO = editBankResult.getResult();

        BankDTO response = BankDTO.builder()
                .id(bankVO.getId())
                .name(bankVO.getName())
                .build();

        return ResponseEntity.ok().body(RestApiResult.Ok(response));
    }
}
