package com.airondlph.economy.household.api.rest;

import com.airondlph.economy.household.api.rest.data.*;
import com.airondlph.economy.household.api.rest.exception.SecurityException;
import com.airondlph.economy.household.logic.financial.FinancialController;
import com.airondlph.economy.household.logic.data.Result;
import com.airondlph.economy.household.logic.users.SecurityController;
import com.airondlph.economy.household.data.model.BankAccountCompleteVO;
import com.airondlph.economy.household.data.model.BankAccountVO;
import com.airondlph.economy.household.data.model.BankVO;
import com.airondlph.economy.household.data.model.UserVO;
import com.airondlph.economy.household.exception.ServerErrorException;
import com.auth0.jwt.interfaces.Claim;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(value = "financial")
@Slf4j
public class FinancialRESTController {

    @Autowired
    private SecurityController securityController;
    @Autowired
    private FinancialController businessController;

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

    @RequestMapping(
            value = "/bankAccount/{id}/",
            method = GET,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<BankAccountCompleteDTO>> getBankAccountComplete(@PathVariable("id") String id) {
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

        Result<BankAccountCompleteVO> getBankAccountCompleteResult;
        try {
            Long bankAccountId = Long.valueOf(id);
            getBankAccountCompleteResult = businessController.getBankAccountCompleteVO(UserVO.builder().id(loggedUserId).build(), BankAccountVO.builder().id(bankAccountId).build());
        } catch (Exception ex) {
            log.error("{}\n{}", ex.getMessage(), ex.getStackTrace());
            return null;
        }

        if (!getBankAccountCompleteResult.isValid()) {
            // Server error
            if (getBankAccountCompleteResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(getBankAccountCompleteResult.getErrCode(), "Server error"));
            // Permission error
            if (getBankAccountCompleteResult.getErrCode() == 4) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(getBankAccountCompleteResult.getErrCode(), "User does not have access to get this bank account data."));
            if (getBankAccountCompleteResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(getBankAccountCompleteResult.getErrCode(), "Not user logged."));

            String errMessage = switch (getBankAccountCompleteResult.getErrCode()) {
                case 3 -> "Bank account not defined or does not exists.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(getBankAccountCompleteResult.getErrCode(), errMessage));
        }

        BankAccountCompleteVO bankAccountCompleteVO = getBankAccountCompleteResult.getResult();

        ArrayList<UserDTO> ownersDTO = new ArrayList<>();
        bankAccountCompleteVO.getOwnersVO().forEach((ownerVO) -> ownersDTO.add(UserDTO.builder()
                                                                                                .id(ownerVO.getId())
                                                                                                .username(ownerVO.getUsername())
                                                                                                .firstName(ownerVO.getFirstName())
                                                                                                .lastName(ownerVO.getLastName())
                                                                                                .build()));

        BankAccountCompleteDTO response = BankAccountCompleteDTO.builder()
            .id(bankAccountCompleteVO.getId())
            .bankAccountNumber(bankAccountCompleteVO.getBankAccountNumber())
            .balance(bankAccountCompleteVO.getBalance())
            .currency(bankAccountCompleteVO.getCurrency())
            .bank(BankDTO.builder()
                .id(bankAccountCompleteVO.getBankVO().getId())
                .name(bankAccountCompleteVO.getBankVO().getName())
                .build())
            .owners(ownersDTO)
            .build();

        return ResponseEntity.ok().body(RestApiResult.Ok(response));
    }

    @RequestMapping(
            value = "/bankAccount/",
            method = POST,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<BankAccountCreationResultDTO>> createBankAccount(@RequestBody BankAccountCreationDTO bankAccountData) {
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

        BankAccountVO createBankAccountDataVO = BankAccountVO.builder()
                .bankAccountNumber(bankAccountData.getBankAccountNumber())
                .currency(bankAccountData.getCurrency())
                .bankVO(BankVO.builder().id(bankAccountData.getBank() == null ? null : bankAccountData.getBank().getId()).build())
                .build();

        List<UserVO> ownersVO = new ArrayList<>();
        if (bankAccountData.getOwners() != null) bankAccountData.getOwners().forEach((ownerDTO) -> ownersVO.add(UserVO.builder().id(ownerDTO.getId()).build()));

        Result<BankAccountVO> createBankAccountResult = businessController.createBankAccountVO(UserVO.builder().id(loggedUserId).build(), createBankAccountDataVO, ownersVO);

        if (!createBankAccountResult.isValid()) {

            // Server error
            if (createBankAccountResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(createBankAccountResult.getErrCode(), "Server error"));
            // Permission error
            if (createBankAccountResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(createBankAccountResult.getErrCode(), "Not user logged."));
            if (createBankAccountResult.getErrCode() == 10) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(createBankAccountResult.getErrCode(), "User does not have access to create a bank account."));

            String errMessage = switch (createBankAccountResult.getErrCode()) {
                case 3 -> "Bank account data not defined.";
                case 4 -> "Bank account number not defined.";
                case 5 -> "Bank account number not valid.";
                case 6 -> "Bank account's currency not defined.";
                case 7 -> "Bank account's currency not valid.";
                case 8 -> "Bank account's bank not defined or does not exists.";
                case 9 -> "Bank account's owner/s not defined or does not exists.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(createBankAccountResult.getErrCode(), errMessage));
        }

        BankAccountVO bankAccountVO = createBankAccountResult.getResult();

        BankAccountCreationResultDTO response = BankAccountCreationResultDTO.builder()
            .id(bankAccountVO.getId())
            .bankAccountNumber(bankAccountVO.getBankAccountNumber())
            .balance(bankAccountVO.getBalance())
            .currency(bankAccountVO.getCurrency())
            .build();

        return ResponseEntity.ok().body(RestApiResult.Ok(response));
    }

    @RequestMapping(
            value = "/bankAccount/owner/{ownerId}/",
            method = GET,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<List<BankAccountDTO>>> getOwnerBankAccounts(@PathVariable("ownerId") String ownerIdStr) {
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

        Result<List<BankAccountVO>> getOwnerBankAccounts;
        try {
            Long ownerId = Long.valueOf(ownerIdStr);
            getOwnerBankAccounts = businessController.getOwnerBankAccountsVO(UserVO.builder().id(loggedUserId).build(), UserVO.builder().id(ownerId).build());
        } catch (Exception ex) {
            log.error("{}\n{}", ex.getMessage(), ex.getStackTrace());
            return null;
        }

        if (!getOwnerBankAccounts.isValid()) {
            // Server error
            if (getOwnerBankAccounts.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(getOwnerBankAccounts.getErrCode(), "Server error"));
            // Permission error
            if (getOwnerBankAccounts.getErrCode() == 4) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(getOwnerBankAccounts.getErrCode(), "User does not have access to get this bank account data."));
            if (getOwnerBankAccounts.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(getOwnerBankAccounts.getErrCode(), "Not user logged."));

            String errMessage = switch (getOwnerBankAccounts.getErrCode()) {
                case 3 -> "Bank account owner not defined or does not exists.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(getOwnerBankAccounts.getErrCode(), errMessage));
        }

        List<BankAccountVO> bankAccountVOs = getOwnerBankAccounts.getResult();

        List<BankAccountDTO> response = new ArrayList<>();

        bankAccountVOs.forEach((bankAccountVO) -> {
            response.add(BankAccountDTO.builder()
                .id(bankAccountVO.getId())
                .bankAccountNumber(bankAccountVO.getBankAccountNumber())
                .balance(bankAccountVO.getBalance())
                .currency(bankAccountVO.getCurrency())
                .bank(bankAccountVO.getBankVO() == null
                    ? null
                    : BankDTO.builder()
                        .id(bankAccountVO.getBankVO().getId())
                        .name(bankAccountVO.getBankVO().getName())
                        .build())
                .build());
        });


        return ResponseEntity.ok().body(RestApiResult.Ok(response));
    }

    @RequestMapping(
            value = "/bankAccount/{id}/",
            method = DELETE,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<BankAccountDTO>> deleteBankAccount(@PathVariable("id") String id) {
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

        Long bankAccountId = Long.valueOf(id);

        BankAccountVO deleteBankAccountDataVO = BankAccountVO.builder()
            .id(bankAccountId)
            .build();

        Result<BankAccountVO> deleteBankAccountResult = businessController.deleteBankAccountByIdVO(UserVO.builder().id(loggedUserId).build(), deleteBankAccountDataVO);

        if (!deleteBankAccountResult.isValid()) {
            // Server error
            if (deleteBankAccountResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(deleteBankAccountResult.getErrCode(), "Server error"));
            // Permission error
            if (deleteBankAccountResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(deleteBankAccountResult.getErrCode(), "Not user logged."));
            if (deleteBankAccountResult.getErrCode() == 5) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(deleteBankAccountResult.getErrCode(), "User does does have permission to delete this bank account."));

            String errMessage = switch (deleteBankAccountResult.getErrCode()) {
                case 3 -> "Bank account not defined.";
                case 4 -> "Bank account does not exists.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(deleteBankAccountResult.getErrCode(), errMessage));
        }

        BankAccountVO bankAccountVO = deleteBankAccountResult.getResult();

        BankAccountDTO response = BankAccountDTO.builder()
                .id(bankAccountVO.getId())
                .bankAccountNumber(bankAccountVO.getBankAccountNumber())
                .balance(bankAccountVO.getBalance())
                .currency(bankAccountVO.getCurrency())
                .bank(bankAccountVO.getBankVO() == null
                    ? null
                    : BankDTO.builder()
                        .id(bankAccountVO.getBankVO().getId())
                        .name(bankAccountVO.getBankVO().getName())
                        .build())
                .build();

        return ResponseEntity.ok().body(RestApiResult.Ok(response));
    }
}
