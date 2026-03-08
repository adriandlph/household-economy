package com.airondlph.economy.household.api.rest;

import com.airondlph.economy.household.api.rest.data.*;
import com.airondlph.economy.household.api.rest.exception.SecurityException;
import com.airondlph.economy.household.data.model.*;
import com.airondlph.economy.household.logic.financial.FinancialController;
import com.airondlph.economy.household.logic.data.Result;
import com.airondlph.economy.household.logic.users.SecurityController;
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
            if (getBankResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(getBankResult.getErrCode(), "Server error."));
            // Permission error
            if (getBankResult.getErrCode() == 4) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(getBankResult.getErrCode(), "User does not have access to get this bank data."));
            if (getBankResult.getErrCode() == 5) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(getBankResult.getErrCode(), "Not user logged."));

            String errMessage = switch (getBankResult.getErrCode()) {
                case 2 -> "Bank id not defined.";
                case 3 -> "Bank does not exist.";
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
            if (createBankResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(createBankResult.getErrCode(), "Server error."));
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
            if (deleteBankResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(deleteBankResult.getErrCode(), "Server error."));
            // Permission error
            if (deleteBankResult.getErrCode() == 5) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(deleteBankResult.getErrCode(), "Not user logged."));
            if (deleteBankResult.getErrCode() == 4) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(deleteBankResult.getErrCode(), "User does not have access to delete this bank."));

            String errMessage = switch (deleteBankResult.getErrCode()) {
                case 2 -> "Bank does not exist.";
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
            if (editBankResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(editBankResult.getErrCode(), "Server error."));
            // Permission error
            if (editBankResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(editBankResult.getErrCode(), "Not user logged."));
            if (editBankResult.getErrCode() == 3) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(editBankResult.getErrCode(), "User does not have access to edit this bank."));

            String errMessage = switch (editBankResult.getErrCode()) {
                case 4 -> "Bank does not exist.";
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
        Long bankAccountId = Long.valueOf(id);
        getBankAccountCompleteResult = businessController.getBankAccountCompleteVO(UserVO.builder().id(loggedUserId).build(), BankAccountVO.builder().id(bankAccountId).build());

        if (!getBankAccountCompleteResult.isValid()) {
            // Server error
            if (getBankAccountCompleteResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(getBankAccountCompleteResult.getErrCode(), "Server error."));
            // Permission error
            if (getBankAccountCompleteResult.getErrCode() == 4) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(getBankAccountCompleteResult.getErrCode(), "User does not have access to get this bank account data."));
            if (getBankAccountCompleteResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(getBankAccountCompleteResult.getErrCode(), "Not user logged."));

            String errMessage = switch (getBankAccountCompleteResult.getErrCode()) {
                case 3 -> "Bank account not defined or does not exist.";
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
            if (createBankAccountResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(createBankAccountResult.getErrCode(), "Server error."));
            // Permission error
            if (createBankAccountResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(createBankAccountResult.getErrCode(), "Not user logged."));
            if (createBankAccountResult.getErrCode() == 10) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(createBankAccountResult.getErrCode(), "User does not have access to create a bank account."));

            String errMessage = switch (createBankAccountResult.getErrCode()) {
                case 3 -> "Bank account data not defined.";
                case 4 -> "Bank account number not defined.";
                case 5 -> "Bank account number not valid.";
                case 6 -> "Bank account's currency not defined.";
                case 7 -> "Bank account's currency not valid.";
                case 8 -> "Bank account's bank not defined or does not exist.";
                case 9 -> "Bank account's owner/s not defined or does not exist.";
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
        Long ownerId = Long.valueOf(ownerIdStr);
        getOwnerBankAccounts = businessController.getOwnerBankAccountsVO(UserVO.builder().id(loggedUserId).build(), UserVO.builder().id(ownerId).build());

        if (!getOwnerBankAccounts.isValid()) {
            // Server error
            if (getOwnerBankAccounts.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(getOwnerBankAccounts.getErrCode(), "Server error."));
            // Permission error
            if (getOwnerBankAccounts.getErrCode() == 4) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(getOwnerBankAccounts.getErrCode(), "User does not have access to get this bank account data."));
            if (getOwnerBankAccounts.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(getOwnerBankAccounts.getErrCode(), "Not user logged."));

            String errMessage = switch (getOwnerBankAccounts.getErrCode()) {
                case 3 -> "Bank account owner not defined or does not exist.";
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
            if (deleteBankAccountResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(deleteBankAccountResult.getErrCode(), "Server error."));
            // Permission error
            if (deleteBankAccountResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(deleteBankAccountResult.getErrCode(), "Not user logged."));
            if (deleteBankAccountResult.getErrCode() == 5) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(deleteBankAccountResult.getErrCode(), "User does does have permission to delete this bank account."));

            String errMessage = switch (deleteBankAccountResult.getErrCode()) {
                case 3 -> "Bank account not defined.";
                case 4 -> "Bank account does not exist.";
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

    @RequestMapping(
            value = "/bankAccount/{id}/owners",
            method = GET,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<List<UserDTO>>> getBankAccountOwners(@PathVariable("id") String id) {
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

        Result<List<UserVO>> getOwnersResult;
        Long bankAccountId = Long.valueOf(id);
        getOwnersResult = businessController.getBankAccountOwnersVO(UserVO.builder().id(loggedUserId).build(), BankAccountVO.builder().id(bankAccountId).build());

        if (!getOwnersResult.isValid()) {
            // Server error
            if (getOwnersResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(getOwnersResult.getErrCode(), "Server error."));
            // Permission error
            if (getOwnersResult.getErrCode() == 3) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(getOwnersResult.getErrCode(), "User does not have access to this bank account owners."));
            if (getOwnersResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(getOwnersResult.getErrCode(), "Not user logged."));

            String errMessage = switch (getOwnersResult.getErrCode()) {
                case 10 -> "Bank account not defined or does not exist.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(getOwnersResult.getErrCode(), errMessage));
        }

        List<UserVO> ownersVO = getOwnersResult.getResult();

        ArrayList<UserDTO> ownersDTO = new ArrayList<>();
        ownersVO.forEach((ownerVO) -> ownersDTO.add(UserDTO.builder()
            .id(ownerVO.getId())
            .username(ownerVO.getUsername())
            .firstName(ownerVO.getFirstName())
            .lastName(ownerVO.getLastName())
            .build()));

        return ResponseEntity.ok().body(RestApiResult.Ok(ownersDTO));
    }

    @RequestMapping(
            value = "/bankAccount/{id}/owners",
            method = POST,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<Void>> addBankAccountOwner(@PathVariable("id") String id, @RequestBody UserDTO newOwnerDTO) {
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

        Result<Void> addOwnerResult;

        Long bankAccountId = bankAccountId = Long.valueOf(id);
        addOwnerResult = businessController.addBankAccountOwnerVO(
            UserVO.builder().id(loggedUserId).build(),
            BankAccountVO.builder().id(bankAccountId).build(),
            UserVO.builder().id(newOwnerDTO == null ? null : newOwnerDTO.getId()).build());

        if (!addOwnerResult.isValid()) {
            // Server error
            if (addOwnerResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(addOwnerResult.getErrCode(), "Server error."));
            // Permission error
            if (addOwnerResult.getErrCode() == 3) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(addOwnerResult.getErrCode(), "User does not have access to this bank account owners."));
            if (addOwnerResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(addOwnerResult.getErrCode(), "Not user logged."));

            String errMessage = switch (addOwnerResult.getErrCode()) {
                case 10 -> "Bank account not defined or does not exist.";
                case 11 -> "New owner not defined or does not exist.";
                case 12 -> "New owner is already a owner of this bank account.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(addOwnerResult.getErrCode(), errMessage));
        }

        return ResponseEntity.ok().body(RestApiResult.Ok(null));
    }

    @RequestMapping(
            value = "/bankAccount/{bankAccountId}/owners/{ownerId}",
            method = DELETE,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<Void>> deleteBankAccountOwner(@PathVariable("bankAccountId") String bankAccountIdStr, @PathVariable("ownerId") String ownerIdStr) {
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

        Result<Void> addOwnerResult;

        Long bankAccountId = bankAccountId = Long.valueOf(bankAccountIdStr);
        Long ownerId = Long.valueOf(ownerIdStr);
        addOwnerResult = businessController.removeBankAccountOwnerVO(
                UserVO.builder().id(loggedUserId).build(),
                BankAccountVO.builder().id(bankAccountId).build(),
                UserVO.builder().id(ownerId).build());

        if (!addOwnerResult.isValid()) {
            // Server error
            if (addOwnerResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(addOwnerResult.getErrCode(), "Server error."));
            // Permission error
            if (addOwnerResult.getErrCode() == 3) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(addOwnerResult.getErrCode(), "User does not have access to this bank account owners."));
            if (addOwnerResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(addOwnerResult.getErrCode(), "Not user logged."));

            String errMessage = switch (addOwnerResult.getErrCode()) {
                case 10 -> "Bank account not defined or does not exist.";
                case 11 -> "Owner not defined or does not exist.";
                case 12 -> "Owner is not a owner of this bank account.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(addOwnerResult.getErrCode(), errMessage));
        }

        return ResponseEntity.ok().body(RestApiResult.Ok(null));
    }

    @RequestMapping(
            value = "/bankTransfer/{id}/",
            method = GET,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<BankTransferDTO>> getBankTransferById(@PathVariable("id") String id) {
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

        Result<BankTransferVO> getBankTransferResult;
        Long bankTransferId = Long.valueOf(id);
        getBankTransferResult = businessController.getBankTransferByIdVO(UserVO.builder().id(loggedUserId).build(), BankTransferVO.builder().id(bankTransferId).build());

        if (!getBankTransferResult.isValid()) {
            // Server error
            if (getBankTransferResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(getBankTransferResult.getErrCode(), "Server error."));
            // Permission error
            if (getBankTransferResult.getErrCode() == 3) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(getBankTransferResult.getErrCode(), "User does not have access to get this bank transfer data."));
            if (getBankTransferResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(getBankTransferResult.getErrCode(), "Not user logged."));

            String errMessage = switch (getBankTransferResult.getErrCode()) {
                case 10 -> "Bank transfer id not defined or bank transfer does not exist.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(getBankTransferResult.getErrCode(), errMessage));
        }

        BankTransferVO bankTransferVO = getBankTransferResult.getResult();

        BankTransferDTO bankTransferDTO = BankTransferDTO.builder()
            .id(bankTransferVO.getId())
            .concept(bankTransferVO.getConcept())
            .description(bankTransferVO.getDescription())
            .operationType(bankTransferVO.getOperationType())
            .value(bankTransferVO.getValue())
            .fromCurrency(bankTransferVO.getFromCurrency())
            .toCurrency(bankTransferVO.getToCurrency())
            .conversion(bankTransferVO.getConversion())
            .madeWhen(bankTransferVO.getMadeWhen())
            .applyWhen(bankTransferVO.getApplyWhen())
            .me(bankTransferVO.getMe() == null
                    ? null :
                    BankAccountDTO.builder()
                        .id(bankTransferVO.getMe().getId())
                        .bank(bankTransferVO.getMe().getBankVO() == null
                            ? null
                            : BankDTO.builder()
                                .id(bankTransferVO.getMe().getBankVO().getId())
                                .name(bankTransferVO.getMe().getBankVO().getName())
                                .build())
                        .build()
            )
            .other(bankTransferVO.getOther() == null
                    ? null :
                    BankAccountDTO.builder()
                        .id(bankTransferVO.getOther().getId())
                        .bank(bankTransferVO.getOther().getBankVO() == null
                            ? null
                            : BankDTO.builder()
                                .id(bankTransferVO.getOther().getBankVO().getId())
                                .name(bankTransferVO.getOther().getBankVO().getName())
                                .build())
                        .build()
            )
            .build();

        return ResponseEntity.ok().body(RestApiResult.Ok(bankTransferDTO));
    }

    @RequestMapping(
            value = "/bankTransfer/",
            method = POST,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<BankTransferDTO>> createBankTransfer(@RequestBody BankTransferDTO bankTransferDTO) {
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

        BankTransferVO bankTransferDataVO = BankTransferVO.builder()
                .id(bankTransferDTO.getId())
                .concept(bankTransferDTO.getConcept())
                .description(bankTransferDTO.getDescription())
                .operationType(bankTransferDTO.getOperationType())
                .value(bankTransferDTO.getValue())
                .fromCurrency(bankTransferDTO.getFromCurrency())
                .toCurrency(bankTransferDTO.getToCurrency())
                .conversion(bankTransferDTO.getConversion())
                .madeWhen(bankTransferDTO.getMadeWhen())
                .applyWhen(bankTransferDTO.getApplyWhen())
                .me(bankTransferDTO.getMe() == null
                    ? null
                    : BankAccountVO.builder()
                        .id(bankTransferDTO.getMe().getId())
                        .build())
                .other(bankTransferDTO.getOther() == null
                    ? null
                    : BankAccountVO.builder()
                        .id(bankTransferDTO.getOther().getId())
                        .build())
            .build();

        Result<BankTransferVO> createBankTransferResult = businessController.createBankTransferVO(UserVO.builder().id(loggedUserId).build(), bankTransferDataVO);

        if (!createBankTransferResult.isValid()) {

            // Server error
            if (createBankTransferResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(createBankTransferResult.getErrCode(), "Server error."));
            // Permission error
            if (createBankTransferResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(createBankTransferResult.getErrCode(), "Not user logged."));
            if (createBankTransferResult.getErrCode() == 3) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(createBankTransferResult.getErrCode(), "User does not have access to create a bank transfer."));

            String errMessage = switch (createBankTransferResult.getErrCode()) {
                case 10 -> "Bank account transfer data not defined.";
                case 11 -> "Bank account 'me' not defined.";
                case 12 -> "Bank account 'other' not defined.";
                case 13 -> "Bank transfer concept not valid or not defined.";
                case 14 -> "Bank transfer value not defined.";
                case 15 -> "Bank transfer from currency not defined.";
                case 16 -> "Bank transfer conversion of different currencies not defined.";
                case 17 -> "Bank transfer conversion not valid.";
                case 18 -> "Bank transfer operation date not defined.";
                case 19 -> "Bank transfer operation type not defined.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(createBankTransferResult.getErrCode(), errMessage));
        }

        BankTransferVO bankTransferVO = createBankTransferResult.getResult();

        BankTransferDTO response = BankTransferDTO.builder()
            .id(bankTransferVO.getId())
            .concept(bankTransferVO.getConcept())
            .description(bankTransferVO.getDescription())
            .operationType(bankTransferVO.getOperationType())
            .value(bankTransferVO.getValue())
            .fromCurrency(bankTransferVO.getFromCurrency())
            .toCurrency(bankTransferVO.getToCurrency())
            .conversion(bankTransferVO.getConversion())
            .madeWhen(bankTransferVO.getMadeWhen())
            .applyWhen(bankTransferVO.getApplyWhen())
            .me(bankTransferVO.getMe() == null
                ? null :
                BankAccountDTO.builder()
                    .id(bankTransferVO.getMe().getId())
                    .bank(bankTransferVO.getMe().getBankVO() == null
                        ? null
                        : BankDTO.builder()
                        .id(bankTransferVO.getMe().getBankVO().getId())
                        .name(bankTransferVO.getMe().getBankVO().getName())
                        .build())
                    .build()
            )
            .other(bankTransferVO.getOther() == null
                ? null :
                BankAccountDTO.builder()
                    .id(bankTransferVO.getOther().getId())
                    .bank(bankTransferVO.getOther().getBankVO() == null
                        ? null
                        : BankDTO.builder()
                        .id(bankTransferVO.getOther().getBankVO().getId())
                        .name(bankTransferVO.getOther().getBankVO().getName())
                        .build())
                    .build()
            )
            .build();

        return ResponseEntity.ok().body(RestApiResult.Ok(response));
    }

    @RequestMapping(
            value = "/creditCard/{creditCardId}/",
            method = GET,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<CreditCardDTO>> getCreditCardById(@PathVariable("creditCardId") String id) {
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


        Long creditCardId = Long.valueOf(id);
        Result<CreditCardVO> getCreditCardResult = businessController.getCreditCardByIdVO(UserVO.builder().id(loggedUserId).build(), CreditCardVO.builder().id(creditCardId).build());

        if (!getCreditCardResult.isValid()) {
            // Server error
            if (getCreditCardResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(getCreditCardResult.getErrCode(), "Server error."));
            // Permission error
            if (getCreditCardResult.getErrCode() == 3) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(getCreditCardResult.getErrCode(), "User does not have access to get this credit card data."));
            if (getCreditCardResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(getCreditCardResult.getErrCode(), "Not user logged."));

            if (getCreditCardResult.getErrCode() == 11) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestApiResult.Error(11, "Credit card does not exists."));

            String errMessage = switch (getCreditCardResult.getErrCode()) {
                case 10 -> "Credit card ID not defined.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(getCreditCardResult.getErrCode(), errMessage));
        }

        return ResponseEntity.ok().body(RestApiResult.Ok(DTOMapper.creditCardVO2creditCardDTO(getCreditCardResult.getResult())));
    }

    @RequestMapping(
            value = "/creditCard/",
            method = POST,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<CreditCardDTO>> createCreditCard(@RequestBody CreditCardDTO creditCardDTO) {
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

        CreditCardVO creditCardVO = creditCardDTO == null
            ? null
            : CreditCardVO.builder()
                .cardNumber(creditCardDTO.getCardNumber())
                .ccv(creditCardDTO.getCcv())
                .pin(creditCardDTO.getPin())
                .expires(creditCardDTO.getExpires())
                .ownerVO(
                    creditCardDTO.getOwner() == null
                    ? null
                    : UserVO.builder()
                        .id(creditCardDTO.getOwner().getId())
                        .build())
                .bankAccountVO(creditCardDTO.getBankAccount() == null
                    ? null
                    : BankAccountVO.builder()
                        .id(creditCardDTO.getBankAccount().getId())
                        .build())
                .build();

        Result<CreditCardVO> createCreditCardResult = businessController.createCreditCardVO(UserVO.builder().id(loggedUserId).build(), creditCardVO);

        if (!createCreditCardResult.isValid()) {

            // Server error
            if (createCreditCardResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(createCreditCardResult.getErrCode(), "Server error."));
            // Permission error
            if (createCreditCardResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(createCreditCardResult.getErrCode(), "Not user logged."));
            if (createCreditCardResult.getErrCode() == 3) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(createCreditCardResult.getErrCode(), "User does not have access to create a credit card."));

            String errMessage = switch (createCreditCardResult.getErrCode()) {
                case 10 -> "Card data not defined.";
                case 11 -> "Card number not defined.";
                case 12 -> "Card expiration date not defined.";
                case 13 -> "Card owner not defined or does not exists.";
                case 14 -> "Card bank account not defined or does not exists.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(createCreditCardResult.getErrCode(), errMessage));
        }

        return ResponseEntity.ok().body(RestApiResult.Ok(DTOMapper.creditCardVO2creditCardDTO(createCreditCardResult.getResult())));

    }

    @RequestMapping(
            value = "/creditCard/{creditCardId}/",
            method = DELETE,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<CreditCardDTO>> deleteCreditCard(@PathVariable("creditCardId") String id) {
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


        Long creditCardId = Long.valueOf(id);
        Result<CreditCardVO> deleteCreditCardResult = businessController.deleteCreditCardVO(UserVO.builder().id(loggedUserId).build(), CreditCardVO.builder().id(creditCardId).build());

        if (!deleteCreditCardResult.isValid()) {
            // Server error
            if (deleteCreditCardResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(deleteCreditCardResult.getErrCode(), "Server error."));
            // Permission error
            if (deleteCreditCardResult.getErrCode() == 3) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(deleteCreditCardResult.getErrCode(), "User does not have permission to delete this credit card."));
            if (deleteCreditCardResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(deleteCreditCardResult.getErrCode(), "Not user logged."));

            if (deleteCreditCardResult.getErrCode() == 11) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestApiResult.Error(11, "Credit card does not exists."));

            String errMessage = switch (deleteCreditCardResult.getErrCode()) {
                case 10 -> "Credit card ID not defined.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(deleteCreditCardResult.getErrCode(), errMessage));
        }

        return ResponseEntity.ok().body(RestApiResult.Ok(DTOMapper.creditCardVO2creditCardDTO(deleteCreditCardResult.getResult())));
    }

    @RequestMapping(
            value = "/creditCard/{creditCardId}/",
            method = PUT,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<CreditCardDTO>> editCreditCard(@PathVariable("creditCardId") String id, @RequestBody CreditCardDTO creditCardDTO) {
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

        CreditCardVO creditCardVO = DTOMapper.creditCardDTO2creditCardVO(creditCardDTO);
        creditCardVO.setId(Long.valueOf(id));

        Result<CreditCardVO> editCreditCardResult = businessController.editCreditCardVO(UserVO.builder().id(loggedUserId).build(), creditCardVO);

        if (!editCreditCardResult.isValid()) {

            // Server error
            if (editCreditCardResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(editCreditCardResult.getErrCode(), "Server error."));
            // Permission error
            if (editCreditCardResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(editCreditCardResult.getErrCode(), "Not user logged."));
            if (editCreditCardResult.getErrCode() == 3) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(editCreditCardResult.getErrCode(), "User does not have permission to edit this credit card."));

            String errMessage = switch (editCreditCardResult.getErrCode()) {
                case 10 -> "Credit card data not defined.";
                case 11 -> "Credit card number not valid.";
                case 12 -> "Credit card CCV not valid.";
                case 13 -> "Credit card pin not valid.";
                case 14 -> "Credit card expiration date not valid.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(editCreditCardResult.getErrCode(), errMessage));
        }

        return ResponseEntity.ok().body(RestApiResult.Ok(DTOMapper.creditCardVO2creditCardDTO(editCreditCardResult.getResult())));
    }

    @RequestMapping(
            value = "/debitCard/{debitCardId}/",
            method = GET,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<DebitCardDTO>> getDebitCardById(@PathVariable("debitCardId") String id) {
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


        Long debitCardId = Long.valueOf(id);
        Result<DebitCardVO> getDebitCardResult = businessController.getDebitCardByIdVO(UserVO.builder().id(loggedUserId).build(), DebitCardVO.builder().id(debitCardId).build());

        if (!getDebitCardResult.isValid()) {
            // Server error
            if (getDebitCardResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(getDebitCardResult.getErrCode(), "Server error."));
            // Permission error
            if (getDebitCardResult.getErrCode() == 3) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(getDebitCardResult.getErrCode(), "User does not have access to get this debit card data."));
            if (getDebitCardResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(getDebitCardResult.getErrCode(), "Not user logged."));

            if (getDebitCardResult.getErrCode() == 11) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestApiResult.Error(11, "Debit card does not exists."));

            String errMessage = switch (getDebitCardResult.getErrCode()) {
                case 10 -> "Debit card ID not defined.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(getDebitCardResult.getErrCode(), errMessage));
        }

        return ResponseEntity.ok().body(RestApiResult.Ok(DTOMapper.debitCardVO2debitCardDTO(getDebitCardResult.getResult())));
    }

    @RequestMapping(
            value = "/debitCard/",
            method = POST,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<DebitCardDTO>> createDebitCard(@RequestBody DebitCardDTO debitCardDTO) {
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

        DebitCardVO debitCardVO = debitCardDTO == null
                ? null
                : DebitCardVO.builder()
                .id(debitCardDTO.getId())
                .cardNumber(debitCardDTO.getCardNumber())
                .ccv(debitCardDTO.getCcv())
                .pin(debitCardDTO.getPin())
                .expires(debitCardDTO.getExpires())
                .ownerVO(
                        debitCardDTO.getOwner() == null
                                ? null
                                : UserVO.builder()
                                .id(debitCardDTO.getOwner().getId())
                                .build())
                .bankAccountVO(debitCardDTO.getBankAccount() == null
                        ? null
                        : BankAccountVO.builder()
                        .id(debitCardDTO.getBankAccount().getId())
                        .build())
                .build();

        Result<DebitCardVO> createDebitCardResult = businessController.createDebitCardVO(UserVO.builder().id(loggedUserId).build(), debitCardVO);

        if (!createDebitCardResult.isValid()) {

            // Server error
            if (createDebitCardResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(createDebitCardResult.getErrCode(), "Server error."));
            // Permission error
            if (createDebitCardResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(createDebitCardResult.getErrCode(), "Not user logged."));
            if (createDebitCardResult.getErrCode() == 3) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(createDebitCardResult.getErrCode(), "User does not have access to create a debit card."));

            String errMessage = switch (createDebitCardResult.getErrCode()) {
                case 10 -> "Card data not defined.";
                case 11 -> "Card number not defined.";
                case 12 -> "Card expiration date not defined.";
                case 13 -> "Card owner not defined or does not exists.";
                case 14 -> "Card bank account not defined or does not exists.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(createDebitCardResult.getErrCode(), errMessage));
        }

        return ResponseEntity.ok().body(RestApiResult.Ok(DTOMapper.debitCardVO2debitCardDTO(createDebitCardResult.getResult())));

    }

    @RequestMapping(
            value = "/debitCard/{debitCardId}/",
            method = DELETE,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RestApiResult<DebitCardDTO>> deleteDebitCard(@PathVariable("debitCardId") String id) {
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


        Long creditCardId = Long.valueOf(id);
        Result<DebitCardVO> deleteDebitCardResult = businessController.deleteDebitCardVO(UserVO.builder().id(loggedUserId).build(), DebitCardVO.builder().id(creditCardId).build());

        if (!deleteDebitCardResult.isValid()) {
            // Server error
            if (deleteDebitCardResult.getErrCode() < 0) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestApiResult.Error(deleteDebitCardResult.getErrCode(), "Server error."));
            // Permission error
            if (deleteDebitCardResult.getErrCode() == 3) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(deleteDebitCardResult.getErrCode(), "User does not have permission to delete this debit card."));
            if (deleteDebitCardResult.getErrCode() == 2) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestApiResult.Error(deleteDebitCardResult.getErrCode(), "Not user logged."));

            if (deleteDebitCardResult.getErrCode() == 11) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestApiResult.Error(11, "Debit card does not exists."));

            String errMessage = switch (deleteDebitCardResult.getErrCode()) {
                case 10 -> "Debit card ID not defined.";
                default -> "Error.";
            };
            return ResponseEntity.badRequest().body(RestApiResult.Error(deleteDebitCardResult.getErrCode(), errMessage));
        }

        return ResponseEntity.ok().body(RestApiResult.Ok(DTOMapper.debitCardVO2debitCardDTO(deleteDebitCardResult.getResult())));
    }

}
