package com.airondlph.economy.household.controller.business;

import com.airondlph.economy.household.controller.data.Result;
import com.airondlph.economy.household.controller.users.UsersController;
import com.airondlph.economy.household.data.entity.Bank;
import com.airondlph.economy.household.data.entity.User;
import com.airondlph.economy.household.data.enumeration.Permission;
import com.airondlph.economy.household.data.model.BankVO;
import com.airondlph.economy.household.data.model.UserVO;
import com.airondlph.economy.household.exception.ServerErrorException;
import com.airondlph.economy.household.util.ValidationResult;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

import static com.airondlph.economy.household.util.LogUtils.*;

/**
 * @author adriandlph / airondlph
 */
@Slf4j
@Controller
@Transactional
public class BusinessController {

    @Autowired
    private EntityManager em;

    @Autowired
    private UsersController usersController;

    /**
     *
     * Creates a new bank
     *
     * @param bankVO Bank's data
     * @return Bank created or error code if an error has occurred.
     * Error codes:
     *  -1 -> Server error
     *   0 -> Ok
     *   1 -> General error
     *   2 -> Bank data not defined
     *   3 -> Bank name not defined
     *   4 -> Creator not defined
     *   5 -> Creator does not have permission to do this operation
     */
    public Result<BankVO> createBankVO(UserVO creatorUserVO, BankVO bankVO) {
        Enter(log, "createBankVO");

        User creatorUser = em.find(User.class, creatorUserVO.getId());
        Result<Bank> creationResult = createBank(creatorUser, bankVO);

        try {
            if (!creationResult.isValid()) return Result.create(creationResult.getErrCode());
            return Result.create(creationResult.getResult() == null ? null : creationResult.getResult().getVO());
        } finally {
            Exit(log, "createBankVO");
        }
    }

    /**
     *
     * Creates a new bank
     *
     * @param bankVO Bank's data
     * @return Bank created or error code if an error has occurred.
     * Error codes:
     *  -1 -> Server error
     *   0 -> Ok
     *   1 -> General error
     *   2 -> Bank data not defined
     *   3 -> Bank name not defined
     *   4 -> Creator not defined
     *   5 -> Creator does not have permission to do this operation
     *
     */
    private Result<Bank> createBank(User creatorUser, BankVO bankVO) {
        Enter(log, "createBank");

        if (creatorUser == null) {
            Exit(log, "createBank");
            return Result.create(4);
        }

        ValidationResult validationResult = validateBankCreationData(bankVO);
        if (!validationResult.isValid()) {
            ErrorWarning(log, "Error validating bank creation data.", validationResult.getErrCode(), validationResult.getErrMsg());
            int errCode = validationResult.getErrCode()+1;
            Exit(log, "createBank");
            return Result.create(errCode);
        }

        try {
            if (!userCanCreateBank(usersController.getUserPermissions(creatorUser))) {
                log.warn("User has not permission to create a bank.");
                Exit(log, "createBank");
                return Result.create(5);
            }
        } catch (ServerErrorException ex) {
            log.error("{}\n{}", ex.getMessage(), ex.getStackTrace());
            Error(log, "Error getting user permissions.", ex.getCode(), ex.getMessage());
            Exit(log, "createBank");
            return Result.create(-1);
        }

        Bank bank = Bank.builder()
            .name(bankVO.getName())
            .build();

        try {
            log.info("Creating bank...");
            em.persist(bank);
        } catch (Exception ex) {
            log.error("{}", ex);
            Error(log, "Error saving bank", null, ex.getMessage());
            Exit(log, "createBank");
            return Result.create(-1); // Server error
        }

        Exit(log, "createBank");
        return Result.create(bank);

    }

    private ValidationResult validateBankCreationData(BankVO bankVO) {
        if (bankVO == null) return ValidationResult.error(1, "Bank's data not defined.");
        if (bankVO.getName() == null || bankVO.getName().isBlank()) return ValidationResult.error(2, "Bank's name not defined.");

        return ValidationResult.ok();
    }

    private boolean userCanCreateBank(List<Permission> permissions) {
        if (permissions.contains(Permission.SYSTEM)) return true;
        if (permissions.contains(Permission.ADMIN)) return true;
        if (permissions.contains(Permission.ADD_BANK)) return true;

        return false;
    }

    /**
     *
     * Get bank info
     *
     * @param userVO User that will get the bank's data
     * @param bankVO Bank's model with the id
     * @return Bank to get or error code if an error has occurred.
     * Error codes:
     *  -1 -> Server error
     *   0 -> Ok
     *   1 -> General error
     *   2 -> Bank does not exist
     *   3 -> User does not have permission to do this operation
     *   4 -> User not defined
     *
     */
    public Result<BankVO> getBankByIdVO(UserVO userVO, BankVO bankVO) {
        Enter(log, "getBankByIdVO");

        User creatorUser = em.find(User.class, userVO.getId());
        Result<Bank> getResult = getBankById(creatorUser, bankVO);

        try {
            if (!getResult.isValid()) return Result.create(getResult.getErrCode());
            return Result.create(getResult.getResult() == null ? null : getResult.getResult().getVO());
        } finally {
            Exit(log, "getBankByIdVO");
        }
    }

    /**
     *
     * Get bank info
     *
     * @param user User that will get the bank's data
     * @param bankVO Bank's model with the id
     * @return Bank to get or error code if an error has occurred.
     * Error codes:
     *  -1 -> Server error
     *   0 -> Ok
     *   1 -> General error
     *   2 -> Bank does not exist
     *   3 -> User does not have permission to do this operation
     *   4 -> User not defined
     *
     */
    private Result<Bank> getBankById(User user, BankVO bankVO) {
        Enter(log, "getBankById");

        if (user == null) {
            Exit(log, "getBankById");
            return Result.create(4);
        }

        try {
            if (!userCanGetBank(usersController.getUserPermissions(user))) {
                log.warn("User has not permission to get bank info.");
                Exit(log, "getBankById");
                return Result.create(3);
            }
        } catch (ServerErrorException ex) {
            log.error("{}\n{}", ex.getMessage(), ex.getStackTrace());
            Error(log, "Error getting user permissions.", ex.getCode(), ex.getMessage());
            Exit(log, "getBankById");
            return Result.create(-1);
        }

        Bank bank = em.find(Bank.class, bankVO.getId());
        if (bank == null) {
            Exit(log, "getBankById");
            return Result.create(2);
        }

        Exit(log, "getBankById");
        return Result.create(bank);
    }

    private boolean userCanGetBank(List<Permission> permissions) {
        if (permissions.contains(Permission.SYSTEM)) return true;
        if (permissions.contains(Permission.ADMIN)) return true;
        if (permissions.contains(Permission.GET_BANK)) return true;

        return false;
    }

}
