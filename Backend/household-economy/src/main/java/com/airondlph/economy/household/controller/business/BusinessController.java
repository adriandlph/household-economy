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
     *   2 -> User not defined
     *   3 -> User does not have permission to do this operation
     *   4 -> Bank data not defined
     *   5 -> Bank name not defined
     *   6 -> Invalid bank name
     *
     */
    public Result<BankVO> createBankVO(UserVO userVO, BankVO bankVO) {
        Enter(log, "createBankVO");

        User user = em.find(User.class, userVO.getId());
        Result<Bank> creationResult = createBank(user, bankVO);

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
     *   2 -> User not defined
     *   3 -> User does not have permission to do this operation
     *   4 -> Bank data not defined
     *   5 -> Bank name not defined
     *   6 -> Invalid name
     *
     */
    private Result<Bank> createBank(User user, BankVO bankVO) {
        Enter(log, "createBank");

        if (user == null) {
            Exit(log, "createBank");
            return Result.create(2);
        }

        ValidationResult validationResult = validateBankCreationData(bankVO);
        if (!validationResult.isValid()) {
            ErrorWarning(log, "Error validating bank creation data.", validationResult.getErrCode(), validationResult.getErrMsg());
            int errCode = validationResult.getErrCode()+3;
            Exit(log, "createBank");
            return Result.create(errCode);
        }

        try {
            if (!userCanCreateBank(usersController.getUserPermissions(user))) {
                log.warn("User has not permission to create a bank.");
                Exit(log, "createBank");
                return Result.create(3);
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
            log.info("Bank created!");
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
        if(bankVO.getName().length() > Bank.NAME_MAX_LENGTH) return ValidationResult.error(3, "Bank name too long. (max length is 255)");

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
     *   2 -> Bank id not defined
     *   3 -> Bank does not exist
     *   4 -> User does not have permission to do this operation
     *   5 -> User not defined
     *
     */
    public Result<BankVO> getBankByIdVO(UserVO userVO, BankVO bankVO) {
        Enter(log, "getBankByIdVO");

        User user = em.find(User.class, userVO.getId());
        Result<Bank> getResult = getBankById(user, bankVO);

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
     *   2 -> Bank id not defined
     *   3 -> Bank does not exist
     *   4 -> User does not have permission to do this operation
     *   5 -> User not defined
     *
     */
    private Result<Bank> getBankById(User user, BankVO bankVO) {
        Enter(log, "getBankById");

        if (user == null) {
            Exit(log, "getBankById");
            return Result.create(5);
        }

        if (bankVO == null || bankVO.getId() == null) {
            Exit(log, "getBankById");
            return Result.create(2);
        }

        try {
            if (!userCanGetBank(usersController.getUserPermissions(user))) {
                log.warn("User has not permission to get bank info.");
                Exit(log, "getBankById");
                return Result.create(4);
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
            return Result.create(3);
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

    /**
     *
     * Deletes a bank
     *
     * @param userVO User that will delete the bank's data
     * @param bankVO Bank's model with the id
     * @return Bank to get or error code if an error has occurred.
     * Error codes:
     *  -1 -> Server error
     *   0 -> Ok
     *   1 -> General error
     *   2 -> Bank does not exist
     *   3 -> Bank id not defined
     *   4 -> User does not have permission to do this operation
     *   5 -> User not defined
     *
     */
    public Result<BankVO> deleteBankByIdVO(UserVO userVO, BankVO bankVO) {
        Enter(log, "deleteBankByIdVO");

        User user = em.find(User.class, userVO.getId());
        try {
            return deleteBankById(user, bankVO);
        } finally {
            Exit(log, "deleteBankByIdVO");
        }
    }

    /**
     *
     * Deletes a bank
     *
     * @param user User that will delete the bank
     * @param bankVO Bank's model with the id
     * @return Bank deleted or error code if an error has occurred.
     * Error codes:
     *  -1 -> Server error
     *   0 -> Ok
     *   1 -> General error
     *   2 -> Bank id not defined
     *   3 -> Bank does not exist
     *   4 -> User does not have permission to do this operation
     *   5 -> User not defined
     *
     */
    private Result<BankVO> deleteBankById(User user, BankVO bankVO) {
        Enter(log, "deleteBankById");

        if (user == null) {
            Exit(log, "deleteBankById");
            return Result.create(5);
        }

        if (bankVO == null || bankVO.getId() == null) {
            Exit(log, "deleteBankById");
            return Result.create(2);
        }

        try {
            if (!userCanDeleteBank(usersController.getUserPermissions(user))) {
                log.warn("User has not permission to delete the bank.");
                Exit(log, "deleteBankById");
                return Result.create(4);
            }
        } catch (ServerErrorException ex) {
            log.error("{}\n{}", ex.getMessage(), ex.getStackTrace());
            Error(log, "Error getting user permissions.", ex.getCode(), ex.getMessage());
            Exit(log, "deleteBankById");
            return Result.create(-1);
        }

        Bank bank = em.find(Bank.class, bankVO.getId());
        if (bank == null) {
            Exit(log, "deleteBankById");
            return Result.create(3);
        }

        BankVO deletedBankVO = bank.getVO();
        try {
            log.info("Deleting bank... (bank={})", bank);
            deleteBank(bank);
            log.info("Bank deleted!");
        } catch (Exception ex) {
            log.error("{}\n{}", ex.getMessage(), ex.getStackTrace());
            Error(log, "Error deleting bank.", null, ex.getMessage());
        }

        Exit(log, "deleteBankById");
        return Result.create(deletedBankVO);
    }

    private boolean userCanDeleteBank(List<Permission> permissions) {
        if (permissions.contains(Permission.SYSTEM)) return true;
        if (permissions.contains(Permission.ADMIN)) return true;
        if (permissions.contains(Permission.DELETE_BANK)) return true;

        return false;
    }

    /**
     * Deletes a bank and its dependencies
     *
     * ---> DO NOT USE THIS FUNCTION DIRECTLY <---
     *
     * @param bank Bank that will be removed
     */
    private void deleteBank(Bank bank) {
        em.remove(bank);
    }

    /**
     *
     * Edit bank info
     *
     * @param userVO User that will edit the bank's data
     * @param bankVO Bank's model with the new info and bank id
     * @return Bank to get or error code if an error has occurred.
     * Error codes:
     *  -1 -> Server error
     *   0 -> Ok
     *   1 -> General error
     *   2 -> User not defined
     *   3 -> User does not have permission to do this operation
     *   4 -> Bank does not exist
     *   5 -> Bank data not defined
     *   6 -> Bank id not defined
     *   7 -> Invalid bank name
     *
     */
    public Result<BankVO> editBankVO(UserVO userVO, BankVO bankVO) {
        Enter(log, "editBankVO");

        User user = em.find(User.class, userVO.getId());
        try {
            Result<Bank> editionResult = editBank(user, bankVO);
            if (!editionResult.isValid()) return Result.create(editionResult.getErrCode());
            return Result.create(editionResult.getResult().getVO());
        } finally {
            Exit(log, "editBankVO");
        }
    }

    /**
     *
     * Edit a bank
     *
     * @param user User that will edit the bank
     * @param bankVO Bank's model with the id
     * @return Bank deleted or error code if an error has occurred.
     * Error codes:
     *  -1 -> Server error
     *   0 -> Ok
     *   1 -> General error
     *   2 -> User not defined
     *   3 -> User does not have permission to do this operation
     *   4 -> Bank does not exist
     *   5 -> Bank data not defined
     *   6 -> Bank id not defined
     *   7 -> Bank name
     *
     */
    private Result<Bank> editBank(User user, BankVO bankVO) {
        Enter(log, "editBank");

        if (user == null) {
            Exit(log, "editBank");
            return Result.create(2);
        }

        try {
            ValidationResult validationResult = validateBankEditionData(bankVO);
            if (!validationResult.isValid()) {
                Exit(log, "editBank");
                Error(log, "Error validating bank data for an edition.", validationResult.getErrCode(), validationResult.getErrMsg());
                return Result.create(validationResult.getErrCode()+4);
            }

            if (!userCanEditBank(usersController.getUserPermissions(user))) {
                log.warn("User has not permission to edit the bank.");
                Exit(log, "editBank");
                return Result.create(3);
            }
        } catch (ServerErrorException ex) {
            log.error("{}\n{}", ex.getMessage(), ex.getStackTrace());
            Error(log, "Error getting user permissions.", ex.getCode(), ex.getMessage());
            Exit(log, "editBank");
            return Result.create(-1);
        }

        Bank bank = em.find(Bank.class, bankVO.getId());
        if (bank == null) {
            Exit(log, "editBank");
            return Result.create(4);
        }

        try {
            log.info("Editing bank...");
            if (bankVO.getName() != null) bank.setName(bankVO.getName());
            log.info("Bank edited!");
        } catch (Exception ex) {
            log.error("{}\n{}", ex.getMessage(), ex.getStackTrace());
            Error(log, "Error editing bank.", null, ex.getMessage());
        }

        Exit(log, "editBank");
        return Result.create(bank);
    }

    private ValidationResult validateBankEditionData(BankVO bankVO) {
        if (bankVO == null) return ValidationResult.error(1, "Bank data not defined.");
        if (bankVO.getId() == null) return ValidationResult.error(2, "Bank data id not defined.");

        if (bankVO.getName() != null) {
            if (bankVO.getName().isBlank()) return ValidationResult.error(3, "Bank name cannot be blank.");
            if(bankVO.getName().length() > Bank.NAME_MAX_LENGTH) return ValidationResult.error(3, "Bank name too long. (max length is 255)");
        }

        return ValidationResult.ok();
    }

    private boolean userCanEditBank(List<Permission> permissions) {
        if (permissions.contains(Permission.SYSTEM)) return true;
        if (permissions.contains(Permission.ADMIN)) return true;
        if (permissions.contains(Permission.EDIT_BANK)) return true;

        return false;
    }

}
