package com.airondlph.economy.household.logic.financial;

import com.airondlph.economy.household.data.enumeration.OperationType;
import com.airondlph.economy.household.logic.data.Result;
import com.airondlph.economy.household.logic.users.UsersController;
import com.airondlph.economy.household.data.entity.financial.*;
import com.airondlph.economy.household.data.entity.user.User;
import com.airondlph.economy.household.data.enumeration.Permission;
import com.airondlph.economy.household.data.model.*;
import com.airondlph.economy.household.exception.ServerErrorException;
import com.airondlph.economy.household.util.ValidationResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.airondlph.economy.household.util.LogUtils.*;

/**
 * @author adriandlph / airondlph
 */
@Service
@Transactional
@Slf4j
public class FinancialControllerImpl implements FinancialController {

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
    @Override
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
            Error(log, "Error saving bank", ex);
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
    @Override
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
    @Override
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
    @Override
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

    @Override
    public Result<BankAccountVO> createBankAccountVO(UserVO userVO, BankAccountVO bankAccountVO, List<UserVO> ownersVO) {
        Enter(log, "createBankAccountVO");

        User user = (userVO == null || userVO.getId() == null) ? null : em.find(User.class, userVO.getId());
        Result<BankAccount> creationResult = createBankAccount(user, bankAccountVO, ownersVO);

        Exit(log, "createBankAccountVO");
        if (!creationResult.isValid()) return Result.create(creationResult.getErrCode());
        return Result.create(creationResult.getResult().getVO());
    }

    /**
     * Creates a bank account
     * @param user User that wants to create the bank account
     * @param bankAccountVO Bank account data
     * @return Bank account created or error code. Error codes:
     *      -1  -> Server error
     *       0  -> Undefined
     *       1  -> General error
     *       2  -> User not defined
     *       3  -> Bank account data not defined
     *       4  -> Bank account number not defined
     *       5  -> Bank account number not valid
     *       6  -> Bank account's currency not defined
     *       7  -> Bank account's currency not valid
     *       8  -> Bank account's bank not defined or does not exist
     *       9  -> Bank account's owners not defined or does not exist (must exist all owners for creation)
     *       10 -> User does not have permission to create this bank account
     *
     */
    private Result<BankAccount> createBankAccount(User user, BankAccountVO bankAccountVO, List<UserVO> ownersVO) {
        Enter(log, "createBankAccount");

        if (user == null) {
            Exit(log, "createBankAccount");
            return Result.create(2);
        }

        ValidationResult validationResult = validateBankAccountCreation(bankAccountVO, ownersVO);
        if (!validationResult.isValid()) {
            ErrorWarning(log, "Validating bank account data for creation.", validationResult.getErrCode(), validationResult.getErrMsg());
            Exit(log, "createBankAccount");
            return Result.create(validationResult.getErrCode() + 2);
        }

        try {
            if (!userCanCreateBankAccount(usersController.getUserPermissions(user))) {
                log.warn("User has not permission to create a bank account.");
                Exit(log, "createBankAccount");
                return Result.create(10);
            }
        } catch (ServerErrorException ex) {
            log.error("{}\n{}", ex.getMessage(), ex.getStackTrace());
            Error(log, "Error getting user permissions.", ex.getCode(), ex.getMessage());
            Exit(log, "createBankAccount");
            return Result.create(-1);
        }

        List<User> owners;
        try {
            List<Long> ownersIds = new ArrayList<>();
            ownersVO.forEach((o) -> { ownersIds.add(o.getId()); });
            Query query = em.createQuery("SELECT u FROM User u WHERE u.id in (:ids)").setParameter("ids", ownersIds);
            owners = (List<User>) query.getResultList();

        } catch (NoResultException ex) {
            owners = null;
        } catch (Exception ex) {
            log.error("{}\n{}", ex.getMessage(), ex.getStackTrace());
            Error(log, "Error searching bank account owners.", null, ex.getMessage());
            Exit(log, "createBankAccount");
            return Result.create(-1);
        }
        if (owners == null || owners.isEmpty()) {
            Exit(log, "createBankAccount");
            return Result.create(9);
        }

        Bank bank;
        try {
            bank = em.find(Bank.class, bankAccountVO.getBankVO().getId());
        } catch (Exception ex) {
            log.error("{}\n{}", ex.getMessage(), ex.getStackTrace());
            Error(log, "Error getting bank account's bank.", null, ex.getMessage());
            Exit(log, "createBankAccount");
            return Result.create(-1);
        }

        BankAccount bankAccount = BankAccount.builder()
            .bankAccountNumber(bankAccountVO.getBankAccountNumber())
            .balance(0L)
            .currency(bankAccountVO.getCurrency())
            .lastUpdate(LocalDateTime.now())
            .bank(bank)
            .build();
        try {
            log.info("Creating bank account...");
            em.persist(bankAccount);
            log.info("Bank account created!");
        } catch (Exception ex) {
            log.error("{}\n{}", ex.getMessage(), ex.getStackTrace());
            Error(log, "Error creating bank account.", null, ex.getMessage());
            Exit(log, "createBankAccount");
            return Result.create(-1);
        }

        try {
            log.info("Creating bank account owners...");
            for (User owner : owners) {
                BankAccountOwner bankAccountOwner = BankAccountOwner.builder().id(new BankAccountOwnerPK(bankAccount, owner)).build();
                em.persist(bankAccountOwner);
            }
            log.info("All bank account owners created!");

        } catch (Exception ex) {
            log.error("{}\n{}", ex.getMessage(), ex.getStackTrace());
            Error(log, "Error creating bank account owner.", null, ex.getMessage());

            log.info("Rolling back...");
            // TODO: ROLLBACK

            Exit(log, "createBankAccount");
            return Result.create(-1);
        }


        Exit(log, "createBankAccount");
        return Result.create(bankAccount);
    }

    /**
     * Validates bank account data for creation.
     *
     *  Validation result error codes:
     *      1 -> Bank account data not defined
     *      2 -> Bank account number not defined
     *      3 -> Bank account number not valid
     *      4 -> Bank account's currency not defined
     *      5 -> Bank account's currency not valid
     *      6 -> Bank account's bank not defined or does not exist
     *      7 -> Bank account's owners not defined or does not exist
     *
     * @param bankAccountVO Bank account data.
     * @return Validation result.
     */
    private ValidationResult validateBankAccountCreation(BankAccountVO bankAccountVO, List<UserVO> ownersVO) {
        if (bankAccountVO == null) return ValidationResult.error(1, "Bank account data not defined.");
        if (bankAccountVO.getBankAccountNumber() == null || bankAccountVO.getBankAccountNumber().isBlank()) return ValidationResult.error(2, "Bank account number not defined.");
        if (bankAccountVO.getCurrency() == null) return ValidationResult.error(4, "Bank account's currency not defined.");
        if (bankAccountVO.getBankVO() == null || bankAccountVO.getBankVO().getId() == null) return ValidationResult.error(6, "Bank account's bank not defined.");
        if (ownersVO == null || ownersVO.isEmpty()) return ValidationResult.error(7, "Bank account's owners not defined.");

        return ValidationResult.ok();
    }

    private boolean userCanCreateBankAccount(List<Permission> permissions) {
        if (permissions.contains(Permission.SYSTEM)) return true;
        if (permissions.contains(Permission.ADMIN)) return true;
        if (permissions.contains(Permission.ADD_BANK_ACCOUNT)) return true;

        return false;
    }

    @Override
    public Result<BankAccountCompleteVO> getBankAccountCompleteVO(UserVO userVO, BankAccountVO bankAccountVO) {
        Enter(log, "getBankAccountCompleteVO");
        User user = (userVO == null || userVO.getId() == null) ? null : em.find(User.class, userVO.getId());
        Result<BankAccountCompleteVO> getResult = getBankAccountComplete(user, bankAccountVO);
        Exit(log, "getBankAccountCompleteVO");
        return getResult;
    }

    /**
     * Get a bank account
     * @param user User that wants to get the bank account
     * @param bankAccountVO Bank account data
     * @return Bank account created or error code. Error codes:
     *      -1 -> Server error
     *       0 -> Undefined
     *       1 -> General error
     *       2 -> User not defined
     *       3 -> Bank account's bank not defined or does not exist
     *       4 -> User does not have permission to get this bank account data
     *
     */
    private Result<BankAccountCompleteVO> getBankAccountComplete(User user, BankAccountVO bankAccountVO) {
        Enter(log, "getBankAccountComplete");

        if (user == null) {
            Exit(log, "getBankAccountComplete");
            log.warn("User not defined.");
            return Result.create(2);
        }

        if (bankAccountVO == null || bankAccountVO.getId() == null) {
            log.warn("Bank account not defined.");
            Exit(log, "getBankAccountComplete");
            return Result.create(3);
        }

        BankAccount bankAccount;
        try {
            bankAccount = em.find(BankAccount.class, bankAccountVO.getId());
        } catch (Exception ex) {
            log.error("{}\n{}", ex.getMessage(), ex.getStackTrace());
            Error(log, "Error getting bank account.", null, ex.getMessage());
            Exit(log, "getBankAccountComplete");
            return Result.create(-1);
        }

        if (bankAccount == null) {
            log.warn("Bank account does not exists.");
            Exit(log, "getBankAccountComplete");
            return Result.create(3);
        }

        List<User> owners;
        try {
            owners = getBankAccountOwners(bankAccount);
        } catch (Exception ex) {
            Error(log, "Error getting bank account owners.", null, ex.getMessage());
            Exit(log, "getBankAccountComplete");
            return Result.create(-1);
        }

        List<Permission> userPermissions;
        try {
            userPermissions = usersController.getUserPermissions(user);
        } catch (ServerErrorException ex) {
            Error(log, "Error getting user permissions.", ex.getCode(), ex.getMessage());
            Exit(log, "getBankAccountComplete");
            return Result.create(-1);
        }

        if (!userHasPermissionToGetBankAccounts(userPermissions, user, owners)) {
            log.warn("User does not have permission to get bank account data.");
            Exit(log, "getBankAccountComplete");
            return Result.create(4);
        }

        BankAccountVO aux = bankAccount.getVO();
        aux.setBankVO(bankAccount.getBank().getVO());
        BankAccountCompleteVO bankAccountCompleteVO = new BankAccountCompleteVO(aux);
        bankAccountCompleteVO.setOwnersVO(new ArrayList<>());
        owners.forEach((owner) -> bankAccountCompleteVO.getOwnersVO().add(owner.getVO()));

        Exit(log, "getBankAccountComplete");
        return Result.create(bankAccountCompleteVO);
    }

    private boolean userHasPermissionToGetBankAccounts(List<Permission> userPermissions, User user, List<User> bankAccountOwners) {
        if (userPermissions.contains(Permission.SYSTEM)) return true;
        if (userPermissions.contains(Permission.ADMIN)) return true;

        // Direct owner
        if (userPermissions.contains(Permission.GET_BANK_ACCOUNT) && bankAccountOwners.contains(user)) return true;

        // Hierarchical owner not allowed

        return false;
    }

    private List<User> getBankAccountOwners(BankAccount bankAccount) throws ServerErrorException {
        Query query = em.createQuery("SELECT bao.id.owner FROM BankAccountOwner bao WHERE bao.id.bankAccount=:bankAccount")
            .setParameter("bankAccount", bankAccount);
        try {
            return (List<User>) query.getResultList();
        } catch (Exception ex) {
            throw new ServerErrorException(1, "Error getting bank account owners.", ex);
        }
    }

    public Result<List<BankAccountVO>> getOwnerBankAccountsVO(UserVO userVO, UserVO ownerVO) {
        Enter(log, "getOwnerBankAccountsVO");

        User user = (userVO == null || userVO.getId() == null) ? null : em.find(User.class, userVO.getId());
        User owner = (ownerVO == null || ownerVO.getId() == null) ? null : em.find(User.class, ownerVO.getId());
        Result<List<BankAccount>> resultAccounts = getOwnerBankAccounts(user, owner);

        if (!resultAccounts.isValid()) {
            Exit(log, "getOwnerBankAccountsVO");
            return Result.create(resultAccounts.getErrCode());
        }

        List<BankAccountVO> result = new ArrayList<>();
        resultAccounts.getResult().forEach((bankAccount) -> result.add(bankAccount.getVO()));

        Exit(log, "getOwnerBankAccountsVO");
        return Result.create(result);
    }


    /**
     * Get owner bank accounts.
     * @param user User that want to get owner bank accounts
     * @param owner Owner
     *
     * @return List of owner's bank accounts or error code.
     *
     *  Error codes:
     *      -1 -> Server error
     *       0 -> Undefined
     *       1 -> General error
     *       2 -> User not defined or does not exist
     *       3 -> Owner not defined or does not exist
     *       4 -> User does not have permission to get this data
     */
    private Result<List<BankAccount>> getOwnerBankAccounts(User user, User owner) {
        Enter(log, "getOwnerBankAccounts");

        Query query = em.createQuery("SELECT bao.id.bankAccount FROM BankAccountOwner bao WHERE bao.id.owner=:owner")
            .setParameter("owner", owner);

        if (user == null) return Result.create(2);
        if (owner == null) return Result.create(3);

        try {
            if (!userCanGetOwnerBankAccounts(user, owner)) {
                log.warn("User does not have permission to get owner bank accounts.");
                return Result.create(4);
            }
        } catch (ServerErrorException ex) {
            log.error("{}\n{}");
            Error(log, "Error geting user permission to check if user can get owner bank accounts.", ex.getCode(), ex.getMessage());
            Exit(log, "getOwnerBankAccounts");
            return Result.create(-1);
        }

        try {
            return Result.create((List<BankAccount>)query.getResultList());
        } catch (Exception ex) {
            log.error("{}\n{}", ex.getMessage(), ex.getStackTrace());
            Error(log, "Error while getting owner bank accounts.", null, ex.getMessage());
            return Result.create(-1);
        } finally {
            Exit(log, "getOwnerBankAccounts");
        }
    }

    private boolean userCanGetOwnerBankAccounts(User user, User owner) throws ServerErrorException {
        List<Permission> permissions = usersController.getUserPermissions(user);
        if (permissions.contains(Permission.SYSTEM)) return true;
        if (permissions.contains(Permission.ADMIN)) return true;
        if (user.equals(owner) && permissions.contains(Permission.GET_BANK_ACCOUNT)) return true;

        return false;
    }

    @Override
    public Result<BankAccountVO> deleteBankAccountByIdVO(UserVO userVO, BankAccountVO bankAccountVO) {
        Enter(log, "deleteBankAccountVO");

        User user = (userVO == null || userVO.getId() == null) ? null : em.find(User.class, userVO.getId());
        Result<BankAccountVO> deletionResult = deleteBankAccountById(user, bankAccountVO);

        Exit(log, "deleteBankAccountVO");
        if (!deletionResult.isValid()) return Result.create(deletionResult.getErrCode());
        return Result.create(deletionResult.getResult());
    }

    /**
     * Deletes a bank account by its ID
     * @param user
     * @param bankAccountVO
     * @return Deleted bank account or error code.
     *
     * Error codes:
     *      -1 -> Server error
     *       0 -> Undefined
     *       1 -> General error
     *       2 -> User not defined
     *       3 -> Bank account ID not defined
     *       4 -> Bank account does not exist
     *       5 -> User does not have permission to delete this bank account
     *
     */
    private Result<BankAccountVO> deleteBankAccountById(User user, BankAccountVO bankAccountVO) {
        Enter(log, "deleteBankAccountById");

        if (user == null) return Result.create(2);
        if (bankAccountVO == null || bankAccountVO.getId() == null) return Result.create(3);

        BankAccount bankAccount = em.find(BankAccount.class, bankAccountVO.getId());
        if (bankAccount == null) {
            log.info("Bank account does not exists.");
            Exit(log, "deleteBankAccountById");
            return Result.create(4);
        }

        try {
            if (!userCanDeleteBankAccount(user, bankAccount)) {
                log.warn("User cannot delete this bank account.");
                Exit(log, "deleteBankAccountById");
                return Result.create(5);
            }
        } catch (ServerErrorException ex) {
            log.error("{}", ex);
            Error(log, "Error checking if user can delete this bank account.", ex.getCode(), ex.getMessage());
            Exit(log, "deleteBankAccountById");
            return Result.create(-1);
        }

        BankAccountVO result = bankAccount.getVO();
        result.setBankVO(bankAccount.getBank().getVO());

        // Deletes bank account and its dependencies
        try {
            deleteBankAccount(bankAccount);
        } catch (Exception ex) {
            Error(log, "Error while deleting bank account.", ex);
            Exit(log, "deleteBankAccountById");
            return Result.create(-1);
        }

        Exit(log, "deleteBankAccountById");
        return Result.create(result);
    }

    private boolean userCanDeleteBankAccount(User user, BankAccount bankAccount) throws ServerErrorException {
        List<Permission> userPermissions = usersController.getUserPermissions(user);

        if (userPermissions.contains(Permission.SYSTEM)) return true;
        if (userPermissions.contains(Permission.ADMIN)) return true;

        // Direct owner
        if (userPermissions.contains(Permission.DELETE_BANK_ACCOUNT)) {
            List<User> bankAccountOwners = getBankAccountOwners(bankAccount);
            if (bankAccountOwners.contains(user)) return true;
        }

        // Hierarchical owner not allowed

        return false;
    }

    /**
     * Delete the bank account and its dependencies
     *
     * DO NOT USE DIRECTLY THIS METHOD!!!
     *   Use: deleteBankAccountById()
     *
     * @param bankAccount Bank account that will be deleted
     */
    private void deleteBankAccount(BankAccount bankAccount) {
        Enter(log, "deleteBankAccount");

        int rowsDeleted = 0;

        // Dependencies
        log.info("Deleting bank account dependencies...");

            rowsDeleted = em.createQuery("DELETE FROM BankAccountOwner bao WHERE bao.id.bankAccount=:bankAccount")
                .setParameter("bankAccount", bankAccount)
                .executeUpdate();
            log.info("\t- Bank account owners deleted: {}", rowsDeleted);

        log.info("All bank account dependencies deleted!");

        // Bank Account
        log.info("Deleting bank account...");
        em.remove(bankAccount);
        log.info("Bank account deleted!");

        Exit(log, "deleteBankAccount");
    }

    @Override
    public Result<List<UserVO>> getBankAccountOwnersVO(UserVO userVO, BankAccountVO bankAccountVO) {
        Enter(log, "getBankAccountOwnersVO");

        User user = (userVO == null || userVO.getId() == null) ? null : em.find(User.class, userVO.getId());
        Result<List<User>> bankAccountOwners = getBankAccountOwners(user, bankAccountVO);

        if (!bankAccountOwners.isValid()) {
            Exit(log, "getBankAccountOwnersVO");
            return Result.create(bankAccountOwners.getErrCode());
        }

        List<UserVO> bankAccountOwnersVO = new ArrayList<>();
        bankAccountOwners.getResult().forEach((owner) -> bankAccountOwnersVO.add(owner.getVO()));

        Exit(log, "getBankAccountOwnersVO");
        return Result.create(bankAccountOwnersVO);
    }

    /**
     * Get bank account owners.
     * @param user User that wants to get the owners.
     * @param bankAccountVO Bank account
     * @return List of owners or error code.
     *
     * Error codes:
     *       -1 -> Server error
     *        0 -> Undefined
     *        1 -> General error
     *        2 -> User not defined
     *        3 -> User does not have permission to do this operation
     *       10 -> Bank account not defined or does not exist
     *
     */
    private Result<List<User>> getBankAccountOwners(User user, BankAccountVO bankAccountVO) {
        Enter(log, "getBankAccountOwners");

        if (user == null) {
            log.info("User not defined.");
            Exit(log, "getBankAccountOwners");
            return Result.create(2);
        }

        BankAccount bankAccount = (bankAccountVO == null || bankAccountVO.getId() == null) ? null : em.find(BankAccount.class, bankAccountVO.getId());
        if (bankAccount == null) {
            log.info("Bank account not defined.");
            Exit(log, "getBankAccountOwners");
            return Result.create(10);
        }

        List<User> owners;
        try {
            owners = getBankAccountOwners(bankAccount);
        } catch (ServerErrorException ex) {
            Error(log, "Error getting bank account owners.", ex);
            Exit(log, "getBankAccountOwners");
            return Result.create(-1);
        }

        // Check permissions
        // This user is not one of the owners
        if (!owners.contains(user)) {

            List<Permission> userPermissions;
            try {
                userPermissions = usersController.getUserPermissions(user);
            } catch (ServerErrorException ex) {
                Error(log, "Error while getting user permission.", ex);
                Exit(log, "getBankAccountOwners");
                return Result.create(-1);
            }

            // If this user is not system and is not admin
            if (!userPermissions.contains(Permission.SYSTEM)
                && !userPermissions.contains(Permission.ADMIN)) {

                log.warn("This user cannot get bank account owner.");
                Exit(log, "getBankAccountOwners");
                return Result.create(3);
            }
        }

        Exit(log, "getBankAccountOwners");
        return Result.create(owners);
    }

    @Override
    public Result<Void> addBankAccountOwnerVO(UserVO userVO, BankAccountVO bankAccountVO, UserVO newOwnerVO) {
        Enter(log, "addBankAccountOwnerVO");

        User user = (userVO == null || userVO.getId() == null) ? null : em.find(User.class, userVO.getId());
        BankAccount bankAccount = (bankAccountVO == null || bankAccountVO.getId() == null) ? null : em.find(BankAccount.class, bankAccountVO.getId());
        User newOwner = (newOwnerVO == null || newOwnerVO.getId() == null) ? null : em.find(User.class, newOwnerVO.getId());

        Result<Void> result = addBankAccountOwner(user, bankAccount, newOwner);

        Exit(log, "addBankAccountOwnerVO");
        if (!result.isValid()) return Result.create(result.getErrCode());
        return Result.create(null);
    }

    /**
     * Add a new owner to the bank account.
     * @param user User that wants to add the owner.
     * @param bankAccount Bank account
     * @param newOwner User that will be added as a new owner of this bank account
     * @return List of bank account owners or error code.
     *
     * Error codes:
     *       -1 -> Server error
     *        0 -> Undefined
     *        1 -> General error
     *        2 -> User not defined
     *        3 -> User does not have permission to do this operation
     *       10 -> Bank account not defined
     *       11 -> New owner not defined
     *       12 -> New owner is already owner of this bank account
     *
     */
    private Result<Void> addBankAccountOwner(User user, BankAccount bankAccount, User newOwner) {
        Enter(log, "addBankAccountOwner");

        if (user == null) {
            log.info("User not defined.");
            Exit(log, "addBankAccountOwner");
            return Result.create(2);
        }

        if (bankAccount == null) {
            log.info("Bank account not defined.");
            Exit(log, "addBankAccountOwner");
            return Result.create(10);
        }

        if (newOwner == null) {
            log.info("New owner not defined.");
            Exit(log, "addBankAccountOwner");
            return Result.create(11);
        }

        // Check permissions
        try {
            if (!userCanAddBankAccountOwner(user, bankAccount)) {
                log.warn("This user cannot (does not have the permission) add a new owner to this bank account.");
                Exit(log, "addBankAccountOwner");
                return Result.create(3);
            }
        } catch (ServerErrorException ex) {
            Error(log, "Error while getting user permission.", ex);
            Exit(log, "addBankAccountOwner");
            return Result.create(-1);
        }

        BankAccountOwner bao = BankAccountOwner.builder()
            .id(new BankAccountOwnerPK(bankAccount, newOwner))
            .build();

        try {
            if (em.find(BankAccountOwner.class, bao.getId()) != null) {
                log.info("This user is already a owner of this bank account.");
                Exit(log, "addBankAccountOwner");
                return Result.create(12);
            }

            log.info("Adding bank account owner...");
            em.persist(bao);
            log.info("Bank account owner added!");
        } catch (Exception ex) {
            Error(log, "Error adding new owner to a bank account.", ex);
            Exit(log, "addBankAccountOwner");
            return Result.create(-1);
        }

        Exit(log, "addBankAccountOwner");
        return Result.create(null);
    }

    private boolean userCanAddBankAccountOwner(User user, BankAccount bankAccount) throws ServerErrorException {
        List<Permission> userPermissions = usersController.getUserPermissions(user);

        if (userPermissions.contains(Permission.SYSTEM)) return true;
        if (userPermissions.contains(Permission.ADMIN)) return true;

        List<User> owners = null;
        if (userPermissions.contains(Permission.ADD_BANK_ACCOUNT_OWNER)) {
            owners = getBankAccountOwners(bankAccount);
            // Check direct owner
            if (owners.contains(user)) return true;
            // Hierarchical owners
            if (owners.stream().anyMatch((owner) -> usersController.userDepends(user, owner))) return true;
        }

        return false;
    }

    @Override
    public Result<Void> removeBankAccountOwnerVO(UserVO userVO, BankAccountVO bankAccountVO, UserVO ownerVO) {
        Enter(log, "removeBankAccountOwnerVO");

        User user = (userVO == null || userVO.getId() == null) ? null : em.find(User.class, userVO.getId());
        BankAccount bankAccount = (bankAccountVO == null || bankAccountVO.getId() == null) ? null : em.find(BankAccount.class, bankAccountVO.getId());
        User owner = (ownerVO == null || ownerVO.getId() == null) ? null : em.find(User.class, ownerVO.getId());

        Result<Void> result = removeBankAccountOwner(user, bankAccount, owner);

        Exit(log, "removeBankAccountOwnerVO");
        if (!result.isValid()) return Result.create(result.getErrCode());
        return Result.create(null);
    }

    /**
     * Remove this owner of the bank account.
     * @param user User that wants to remove the owner
     * @param bankAccount Bank account
     * @param owner User that will be added as a new owner of this bank account
     * @return List of bank account owners or error code.
     *
     * Error codes:
     *       -1 -> Server error
     *        0 -> Undefined
     *        1 -> General error
     *        2 -> User not defined
     *        3 -> User does not have permission to do this operation
     *       10 -> Bank account not defined
     *       11 -> Owner not defined
     *       12 -> Owner is not owner of this bank account
     *
     */
    private Result<Void> removeBankAccountOwner(User user, BankAccount bankAccount, User owner) {
        Enter(log, "removeBankAccountOwner");

        if (user == null) {
            log.info("User not defined.");
            Exit(log, "removeBankAccountOwner");
            return Result.create(2);
        }

        if (bankAccount == null) {
            log.info("Bank account not defined.");
            Exit(log, "removeBankAccountOwner");
            return Result.create(10);
        }

        if (owner == null) {
            log.info("Owner not defined.");
            Exit(log, "removeBankAccountOwner");
            return Result.create(11);
        }

        // Check permissions
        try {
            if (!userCanAddBankAccountOwner(user, bankAccount)) {
                log.warn("This user cannot (does not have the permission) add a new owner to this bank account.");
                Exit(log, "removeBankAccountOwner");
                return Result.create(3);
            }
        } catch (ServerErrorException ex) {
            Error(log, "Error while getting user permission.", ex);
            Exit(log, "removeBankAccountOwner");
            return Result.create(-1);
        }

        try {
            BankAccountOwner bao;
            if ((bao = em.find(BankAccountOwner.class, new BankAccountOwnerPK(bankAccount, owner))) == null) {
                log.info("This user is not a owner of this bank account.");
                Exit(log, "removeBankAccountOwner");
                return Result.create(12);
            }

            log.info("Removing bank account owner...");
            em.remove(bao);
            log.info("Bank account owner removed!");
        } catch (Exception ex) {
            Error(log, "Error removing owner of the bank account.", ex);
            Exit(log, "removeBankAccountOwner");
            return Result.create(-1);
        }

        Exit(log, "removeBankAccountOwner");
        return Result.create(null);
    }

    private boolean userCanRemoveBankAccountOwner(User user, BankAccount bankAccount) throws ServerErrorException {
        List<Permission> userPermissions = usersController.getUserPermissions(user);

        if (userPermissions.contains(Permission.SYSTEM)) return true;
        if (userPermissions.contains(Permission.ADMIN)) return true;

        List<User> owners = null;
        if (userPermissions.contains(Permission.REMOVE_BANK_ACCOUNT_OWNER)) {
            owners = getBankAccountOwners(bankAccount);
            // Check direct owner
            if (owners.contains(user)) return true;
            // Hierarchical owners
            if (owners.stream().anyMatch((owner) -> usersController.userDepends(user, owner))) return true;
        }

        return false;
    }

    @Override
    public Result<BankTransferVO> getBankTransferByIdVO(UserVO userVO, BankTransferVO bankTransferVO) {
        Enter(log, "getBankTransferByIdVO");

        User user = (userVO == null || userVO.getId() == null) ? null : em.find(User.class, userVO.getId());
        Result<BankTransfer> bankTransferResult = getBankTransferById(user, bankTransferVO);

        Exit(log, "getBankTransferByIdVO");
        if (!bankTransferResult.isValid()) return Result.create(bankTransferResult.getErrCode());

        BankTransferVO result = bankTransferResult.getResult().getVO();
        result.setMe(bankTransferResult.getResult().getMe().getVO());
        result.getMe().setBankVO(bankTransferResult.getResult().getMe().getBank().getVO());
        result.setOther(bankTransferResult.getResult().getOther().getVO());
        result.getOther().setBankVO(bankTransferResult.getResult().getOther().getBank().getVO());

        return Result.create(bankTransferResult.getResult().getVO());
    }

    /**
     * Get bank transfer.
     *
     * @param user User that wants to do this operation.
     * @param bankTransferVO Bank transfer's id
     *
     * @return Bank transfer or error code.
     *
     * Error codes:
     *       -1 -> Server error
     *        0 -> Undefined
     *        1 -> General error
     *        2 -> User does not exist or not defined
     *        3 -> User does not have permission to get this bank account transfer
     *       10 -> Bank transfer id not defined or bank transfer does not exist.
     */
    public Result<BankTransfer> getBankTransferById(User user, BankTransferVO bankTransferVO) {
        Enter(log, "getBankTransferById");

        if (user == null) {
            log.info("User not defined.");
            Exit(log, "getBankTransferById");
            return Result.create(2);
        }

        BankTransfer bankTransfer = (bankTransferVO == null || bankTransferVO.getId() == null) ? null : em.find(BankTransfer.class, bankTransferVO.getId());
        if (bankTransfer == null) {
            log.info("Bank transfer not defined or does not exists.");
            Exit(log, "getBankTransferById");
            return Result.create(10);
        }

        try {
            if (!userCanGetBankTransfer(user, bankTransfer)) {
                log.warn("User does not have permission to get this bank account transfer data.");
                Exit(log, "getBankTransferById");
                return Result.create(3);
            }
        } catch (ServerErrorException ex) {
            Error(log, "Error checking if user can get bank transfer.", ex);
            Exit(log, "getBankTransferById");
            return Result.create(-1);
        }

        Exit(log, "getBankTransferById");
        return Result.create(bankTransfer);
    }

    private boolean userCanGetBankTransfer(User user, BankTransfer bankTransfer) throws ServerErrorException {
        List<Permission> userPermission = usersController.getUserPermissions(user);

        if (userPermission.contains(Permission.SYSTEM)) return true;
        if (userPermission.contains(Permission.ADMIN)) return true;

        if (userPermission.contains(Permission.GET_INCOME_OPERATION)) {
            List<User> ownersMe = getBankAccountOwners(bankTransfer.getMe());
            if (ownersMe.stream().anyMatch((owner) -> usersController.userDepends(user, owner))) return true;
        }

        return false;
    }

    @Override
    public Result<BankTransferVO> createBankTransferVO(UserVO userVO, BankTransferVO bankTransferVO) {
        Enter(log, "createBankTransferVO");

        User user = (userVO == null || userVO.getId() == null) ? null : em.find(User.class, userVO.getId());
        Result<BankTransfer> bankTransferResult = createBankTransfer(user, bankTransferVO);

        Exit(log, "createBankTransferVO");
        if (!bankTransferResult.isValid()) return Result.create(bankTransferResult.getErrCode());

        var result = bankTransferResult.getResult().getVO();
        result.setMe(bankTransferResult.getResult().getMe().getVO());
        result.getMe().setBankVO(bankTransferResult.getResult().getMe().getBank().getVO());
        result.setOther(bankTransferResult.getResult().getOther().getVO());
        result.getOther().setBankVO(bankTransferResult.getResult().getOther().getBank().getVO());

        return Result.create(result);
    }

    /**
     * Get bank transfer.
     *
     * @param user User that wants to do this operation.
     * @param bankTransferVO Bank transfer's id
     *
     * @return Bank transfer or error code.
     *
     * Error codes:
     *       -1 -> Server error.
     *        0 -> Undefined.
     *        1 -> General error.
     *        2 -> User does not exist or not defined.
     *        3 -> User does not have permission to add this bank account transfer.
     *       10 -> Bank account transfer data not defined.
     *       11 -> Bank account 'me' not defined.
     *       12 -> Bank account 'other' not defined.
     *       13 -> Bank transfer concept not valid or not defined.
     *       14 -> Bank transfer value not defined.
     *       15 -> Bank transfer from currency not defined.
     *       16 -> Bank transfer conversion of different currencies not defined.
     *       17 -> Bank transfer conversion not valid.
     *       18 -> Bank transfer operation date not defined.
     *       19 -> Bank transfer operation type not defined.
     *
     */
    public Result<BankTransfer> createBankTransfer(User user, BankTransferVO bankTransferVO) {
        Enter(log, "createBankTransfer");

        if (user == null) {
            log.info("User not defined.");
            Exit(log, "createBankTransfer");
            return Result.create(2);
        }

        ValidationResult validationResult = isBankTransferCreationValid(bankTransferVO);
        if (!validationResult.isValid()) {
            log.info("BankTransfer not valid: {}", validationResult.getErrMsg());
            Exit(log, "createBankTransfer");
            return Result.create(9+validationResult.getErrCode());
        }

        BankAccount me = em.find(BankAccount.class, bankTransferVO.getMe().getId());
        if (me == null) {
            log.info("Bank account 'me' does not exists.");
            Exit(log, "createBankTransfer");
            return Result.create(11);
        }

        BankAccount other = em.find(BankAccount.class, bankTransferVO.getOther().getId());
        if (other == null) {
            log.info("Bank account 'me' does not exists.");
            Exit(log, "createBankTransfer");
            return Result.create(11);
        }

        try {
            if (!userCanCreateBankTransfer(user, me, bankTransferVO)) {
                log.warn("User does not have permission to add this bank transfer.");
                Exit(log, "createBankTransfer");
                return Result.create(3);
            }
        } catch (ServerErrorException ex) {
            Error(log, "Error checking if user can get bank transfer.", ex);
            Exit(log, "createBankTransfer");
            return Result.create(-1);
        }


        BankTransfer bankTransfer = BankTransfer.builder()
            .concept(bankTransferVO.getConcept())
            .description(bankTransferVO.getDescription())
            .operationType(bankTransferVO.getOperationType())
            .value(bankTransferVO.getValue())
            .fromCurrency(bankTransferVO.getFromCurrency())
            .toCurrency(bankTransferVO.getToCurrency())
            .conversion(bankTransferVO.getConversion())
            .madeWhen(bankTransferVO.getMadeWhen())
            .applyWhen(bankTransferVO.getApplyWhen())
            .me(me)
            .other(other)
            .lastModification(LocalDateTime.now())
            .build();

        try {
            em.persist(bankTransfer);
        } catch (Exception ex) {
            Error(log, "Error saving bank transfer in DB.", ex);
            Exit(log, "createBankTransfer");
            return Result.create(-1);
        }

        Exit(log, "createBankTransfer");
        return Result.create(bankTransfer);
    }

    /**
     * 1 -> Bank transfer data not defined
     * 2 -> Bank account 'me' does not exist
     * 3 -> Bank account 'other' does not exist
     * 4 -> Bank transfer concept not valid or not defined.
     * 5 -> Bank transfer value not defined
     * 6 -> Bank transfer from currency not defined
     * 7 -> Bank transfer conversion of different currencies not defined
     * 8 -> Bank transfer conversion not valid
     * 9 -> Bank transfer operation date not defined.
     * 10 -> Bank transfer operation type not defined.
     */
    private ValidationResult isBankTransferCreationValid(BankTransferVO bankTransferVO) {

        if (bankTransferVO == null) return ValidationResult.error(1, "Bank transfer data not defined.");
        if (bankTransferVO.getMe() == null || bankTransferVO.getMe().getId() == null) return ValidationResult.error(2, "Bank account 'me' data not defined.");
        if (bankTransferVO.getOther() == null || bankTransferVO.getOther().getId() == null) return ValidationResult.error(3, "Bank account 'other' data not defined.");
        if (bankTransferVO.getConcept() == null) return ValidationResult.error(4, "Concept cannot be null.");
        if (bankTransferVO.getDescription() != null) {
            if (bankTransferVO.getDescription().isBlank()) bankTransferVO.setDescription(null);
        }

        if (bankTransferVO.getValue() == null) return ValidationResult.error(5, "Bank transfer value cannot be null.");
        if (bankTransferVO.getFromCurrency() == null) return ValidationResult.error(6, "Bank transfer from currency cannot be null.");
        if (bankTransferVO.getToCurrency() == null || bankTransferVO.getFromCurrency().equals(bankTransferVO.getToCurrency())) {
            bankTransferVO.setToCurrency(bankTransferVO.getFromCurrency());
            bankTransferVO.setConversion(1.0f);
        } else {
            if (bankTransferVO.getConversion() == null) return ValidationResult.error(7, "You must specify conversion between currencies.");
            if (bankTransferVO.getConversion().equals(0.0f)) return ValidationResult.error(8, "Conversion cannot be 0.");
            if (bankTransferVO.getConversion() < 0.0f) return ValidationResult.error(8, "Conversion cannot be negative.");
            if ((bankTransferVO.getConversion() >= Float.MAX_VALUE)
                || bankTransferVO.getConversion().equals(Float.NEGATIVE_INFINITY)
                || bankTransferVO.getConversion().equals(Float.POSITIVE_INFINITY)
                || bankTransferVO.getConversion().equals(Float.NaN)) return ValidationResult.error(8, "Conversion value not valid.");
        }

        if (bankTransferVO.getMadeWhen() == null) return ValidationResult.error(9, "Operation date not defined.");
        if (bankTransferVO.getApplyWhen() == null) bankTransferVO.setApplyWhen(bankTransferVO.getMadeWhen());
        if (bankTransferVO.getOperationType() == null) return ValidationResult.error(10, "Operation type not defined.");

        return ValidationResult.ok();
    }

    private boolean userCanCreateBankTransfer(User user, BankAccount bankAccountMe, BankTransferVO bankTransferVO) throws ServerErrorException {
        List<Permission> userPermission = usersController.getUserPermissions(user);

        if (userPermission.contains(Permission.SYSTEM)) return true;
        if (userPermission.contains(Permission.ADMIN)) return true;

        List<User> ownersMe = null;
        if (OperationType.INCOME.equals(bankTransferVO.getOperationType()) && userPermission.contains(Permission.ADD_INCOME_OPERATION)) {
            ownersMe = getBankAccountOwners(bankAccountMe);
            if (ownersMe.stream().anyMatch((owner) -> usersController.userDepends(user, owner))) return true;
        }

        if (OperationType.OUTCOME.equals(bankTransferVO.getOperationType()) && userPermission.contains(Permission.ADD_OUTCOME_OPERATION)) {
            if (ownersMe == null) ownersMe = getBankAccountOwners(bankAccountMe);
            if (ownersMe.stream().anyMatch((owner) -> usersController.userDepends(user, owner))) return true;
        }

        return false;
    }

    @Override
    public Result<CreditCardVO> getCreditCardByIdVO(UserVO userVO, CreditCardVO creditCardVO) {
        Enter(log, "getCreditCardByIdVO");

        User user = (userVO == null || userVO.getId() == null) ? null : em.find(User.class, userVO.getId());
        Result<CreditCard> creationResult = getCreditCardById(user, creditCardVO);

        Exit(log, "getCreditCardByIdVO");
        if (!creationResult.isValid()) return Result.create(creationResult.getErrCode());

        CreditCardVO result = creationResult.getResult().getVO();
        result.setOwnerVO(creationResult.getResult().getOwner().getVO());
        result.setBankAccountVO(creationResult.getResult().getBankAccount().getVO());

        return Result.create(result);
    }

    /**
     * Gets credit card data
     * @param operationUser User that wants to do this operation
     * @param creditCardVO Credit card id
     * @return Credit card data or error code.
     *
     * Error codes:
     *       -1 -> Server error
     *        0 -> Undefined
     *        1 -> General error
     *        2 -> Operation user not defined
     *        3 -> Operation user does not have permission to get this data.
     *       10 -> Credit card not defined
     *       11 -> Credit card does not exist
     */
    private Result<CreditCard> getCreditCardById(User operationUser, CreditCardVO creditCardVO) {
        Enter(log, "getCreditCardById");

        if (operationUser == null) {
            log.warn("Operation user not defined.");
            Exit(log, "getCreditCardById");
            return Result.create(2);
        }

        if (creditCardVO == null || creditCardVO.getId() == null) {
            log.warn("Credit card data not defined.");
            Exit(log, "getCreditCardById");
            return Result.create(10);
        }

        CreditCard creditCard = em.find(CreditCard.class, creditCardVO.getId());
        if (creditCard == null) {
            log.warn("Credit card does not exists.");
            Exit(log, "getCreditCardById");
            return Result.create(11);
        }

        try {
            if (!userCanGetCreditCard(operationUser, creditCard)) {
                log.warn("Operation user does not have permission to get credit card data.");
                Exit(log, "getCreditCardById");
                return Result.create(3);
            }
        } catch (ServerErrorException ex) {
            Error(log, "Error checking if user has permission to get this credit card data.", ex);
            Exit(log, "getCreditCardById");
            return Result.create(-1);
        }

        Exit(log, "getCreditCardById");
        return Result.create(creditCard);
    }

    private boolean userCanGetCreditCard(User operationUser, CreditCard creditCard) throws ServerErrorException {
        List<Permission> userPermission = usersController.getUserPermissions(operationUser);

        if (userPermission.contains(Permission.SYSTEM)) return true;
        if (userPermission.contains(Permission.ADMIN)) return true;

        if (userPermission.contains(Permission.GET_CREDIT_CARD)) {
            if (creditCard.getOwner().equals(operationUser)) return true;
        }

        return false;
    }

    @Override
    public Result<CreditCardVO> createCreditCardVO(UserVO userVO, CreditCardVO creditCardVO) {
        Enter(log, "createCreditCardVO");

        User user = (userVO == null || userVO.getId() == null) ? null : em.find(User.class, userVO.getId());
        Result<CreditCard> creationResult = createCreditCard(user, creditCardVO);

        Exit(log, "createCreditCardVO");
        if (!creationResult.isValid()) return Result.create(creationResult.getErrCode());

        CreditCardVO result = creationResult.getResult().getVO();
        result.setOwnerVO(creationResult.getResult().getOwner().getVO());
        result.setBankAccountVO(creationResult.getResult().getBankAccount().getVO());

        return Result.create(creationResult.getResult().getVO());
    }

    /**
     * Creates a credit card
     *
     * @param user User that wants to create the credit card
     * @param creditCardVO Credit card data
     *
     * @return
     *    Credit card created or error code. Error codes:
     *        -1 -> Server error
     *         0 -> Undefined
     *         1 -> General error
     *         2 -> User not defined
     *        10 -> Credit card not defined
     *        11 -> Card number not defined
     *        12 -> Card expiration date not defined
     *        13 -> Card's owner not defined or does not exist
     *        14 -> Card's bank account not defined or does not exist
     *
     */
    private Result<CreditCard> createCreditCard(User user, CreditCardVO creditCardVO) {
        Enter(log, "createCreditCard");

        if (user == null) {
            log.warn("User not defined.");
            Exit(log, "createCreditCard");
            return Result.create(2);
        }

        ValidationResult validationResult = validateCreditCardCreation(creditCardVO);
        if (!validationResult.isValid()) {
            ErrorWarning(log, "Validating credit card data for creation.", validationResult.getErrCode(), validationResult.getErrMsg());
            Exit(log, "createCreditCard");
            return Result.create(validationResult.getErrCode()+9);
        }

        User owner = em.find(User.class, creditCardVO.getOwnerVO().getId());
        if (owner == null) {
            Exit(log, "createCreditCard");
            return Result.create(13);
        }

        BankAccount bankAccount = em.find(BankAccount.class, creditCardVO.getBankAccountVO().getId());
        if (bankAccount == null) {
            Exit(log, "createCreditCard");
            return Result.create(14);
        }

        try {
            if (!userCanCreateCreditCard(user, bankAccount)) {
                log.warn("Operation user does not have permission to create this credit card.");
                Exit(log, "createCreditCard");
                return Result.create(3);
            }
        } catch (ServerErrorException ex) {
            Error(log, "Error checking if user can create this credit card or not.", ex);
            Exit(log, "createCreditCard");
            return Result.create(-1);
        }

        CreditCard creditCard = CreditCard.builder()
            .cardNumber(creditCardVO.getCardNumber())
            .expires(creditCardVO.getExpires())
            .owner(owner)
            .bankAccount(bankAccount)
            .build();

        if (creditCardVO.getCcv() != null) creditCard.setCcv(creditCardVO.getCcv());
        if (creditCardVO.getPin() != null) creditCard.setPin(creditCardVO.getPin());

        try {
            log.info("Creating credit card...");
            em.persist(creditCard);
            log.info("Credit card created!");
        } catch (Exception ex) {
            log.error("{}\n{}");
            Error(log, "Error creating credit card.", null, ex.getMessage());
            return Result.create(-1);
        }

        Exit(log, "createCreditCard");
        return Result.create(creditCard);
    }

    private ValidationResult validateCreditCardCreation(CreditCardVO creditCardVO) {
        // Bank card data validation
        ValidationResult validationResult = validateBankCardCreation(creditCardVO);
        if (!validationResult.isValid()) return validationResult;

        // Credit card data validation
        // Nothing

        return ValidationResult.ok();
    }

    private ValidationResult validateBankCardCreation(BankCardVO bankCardVO) {
        if (bankCardVO == null) return ValidationResult.error(1, "Bank card data not defined.");
        if (bankCardVO.getCardNumber() == null) return ValidationResult.error(2, "Card number not defined.");
        if (bankCardVO.getExpires() == null) return ValidationResult.error(3, "Card expiration date not defined.");
        if (bankCardVO.getOwnerVO() == null || bankCardVO.getOwnerVO().getId() == null) return ValidationResult.error(4, "Card's owner not defined.");
        if (bankCardVO.getBankAccountVO() == null || bankCardVO.getBankAccountVO().getId() == null) return ValidationResult.error(5, "Card's bank account not defined.");
        return ValidationResult.ok();
    }

    private boolean userCanCreateCreditCard(User operationUser, BankAccount bankAccount) throws ServerErrorException {
        List<Permission> userPermissions = usersController.getUserPermissions(operationUser);

        if (userPermissions.contains(Permission.SYSTEM)) return true;
        if (userPermissions.contains(Permission.ADMIN)) return true;

        if (userPermissions.contains(Permission.CREATE_CREDIT_CARD)) {
            List<User> owners = getBankAccountOwners(bankAccount);
            if (owners.contains(operationUser)) return true;
        }

        return false;
    }

    @Override
    public Result<CreditCardVO> deleteCreditCardVO(UserVO userVO, CreditCardVO creditCardVO) {
        Enter(log, "deleteCreditCardVO");

        User user = (userVO == null || userVO.getId() == null) ? null : em.find(User.class, userVO.getId());
        Result<CreditCardVO> deletionResult = deleteCreditCard(user, creditCardVO);

        Exit(log, "deleteCreditCardVO");
        return deletionResult;
    }

    /**
     * Delete credit card data
     * @param operationUser User that wants to do this operation
     * @param creditCardVO Credit card id
     * @return Credit card deleted or error code.
     *
     * Error codes:
     *       -1 -> Server error
     *        0 -> Undefined
     *        1 -> General error
     *        2 -> Operation user not defined
     *        3 -> Operation user does not have permission to get this data.
     *       10 -> Credit card not defined
     *       11 -> Credit card does not exist
     */
    private Result<CreditCardVO> deleteCreditCard(User operationUser, CreditCardVO creditCardVO) {
        Enter(log, "deleteCreditCard");

        if (operationUser == null) {
            log.warn("Operation user not defined.");
            Exit(log, "deleteCreditCard");
            return Result.create(2);
        }

        if (creditCardVO == null || creditCardVO.getId() == null) {
            log.warn("Credit card data not defined.");
            Exit(log, "deleteCreditCard");
            return Result.create(10);
        }

        CreditCard creditCard = em.find(CreditCard.class, creditCardVO.getId());
        if (creditCard == null) {
            log.warn("Credit card does not exists.");
            Exit(log, "deleteCreditCard");
            return Result.create(11);
        }

        try {
            if (!userCanDeleteCreditCard(operationUser, creditCard)) {
                log.warn("Operation user does not have permission to delete credit card.");
                Exit(log, "deleteCreditCard");
                return Result.create(3);
            }
        } catch (ServerErrorException ex) {
            Error(log, "Error checking if user has permission to delete this credit card.", ex);
            Exit(log, "deleteCreditCard");
            return Result.create(-1);
        }

        CreditCardVO result = creditCard.getVO();
        try {
            deleteCreditCard(creditCard);
        } catch (ServerErrorException ex) {
            Error(log, "Error deleting credit card.", ex);
            Exit(log, "deleteCreditCard");
            Result.create(-1);
        }

        Exit(log, "deleteCreditCard");
        return Result.create(result);
    }

    private boolean userCanDeleteCreditCard(User operationUser, CreditCard creditCard) throws ServerErrorException {
        List<Permission> userPermission = usersController.getUserPermissions(operationUser);

        if (userPermission.contains(Permission.SYSTEM)) return true;
        if (userPermission.contains(Permission.ADMIN)) return true;

        if (userPermission.contains(Permission.DELETE_CREDIT_CARD)) {
            if (creditCard.getOwner().equals(operationUser)) return true;
        }

        return false;
    }

    /**
     *
     * Delete credit card and its dependencies.
     *
     * DO NOT USE THIS METHOD DIRECTLY!!!
     *
     * ServerErrorException codes:
     *  1 -> Error deleting credit card
     *  2 -> Error deleting credit card's operations.
     *
     */
    private void deleteCreditCard(CreditCard creditCard) throws ServerErrorException {
        Enter(log, "deleteCreditCard");
        Query query;
        int n;

        // Delete credit card operation
        try {
            query = em.createQuery("DELETE FROM CreditCardOperation op WHERE op.me =:creditCard")
                    .setParameter("creditCard", creditCard);

            log.info("Deleting credit card operations...");
            n = query.executeUpdate();
            log.info("{} credit card operations deleted.", n);
        } catch (Exception ex) {
            Exit(log, "deleteCreditCard");
            throw new ServerErrorException(2, "Error deleting credit card operations.", ex);
        }

        try {
            log.info("Deleting credit card.");
            em.remove(creditCard);
            log.info("Credit card deleted!");
        } catch (Exception ex) {
            Exit(log, "deleteCreditCard");
            throw new ServerErrorException(1, "Error deleting credit card.", ex);
        }

        Exit(log, "deleteCreditCard");
    }

    @Override
    public Result<DebitCardVO> getDebitCardByIdVO(UserVO userVO, DebitCardVO debitCardVO) {
        Enter(log, "getDebitCardByIdVO");

        User user = (userVO == null || userVO.getId() == null) ? null : em.find(User.class, userVO.getId());
        Result<DebitCard> creationResult = getDebitCardById(user, debitCardVO);

        Exit(log, "getDebitCardByIdVO");
        if (!creationResult.isValid()) return Result.create(creationResult.getErrCode());

        DebitCardVO result = creationResult.getResult().getVO();
        result.setOwnerVO(creationResult.getResult().getOwner().getVO());
        result.setBankAccountVO(creationResult.getResult().getBankAccount().getVO());

        return Result.create(result);
    }

    /**
     * Gets debit card data
     * @param operationUser User that wants to do this operation
     * @param debitCardVO Debit card id
     * @return Debit card data or error code.
     *
     * Error codes:
     *       -1 -> Server error
     *        0 -> Undefined
     *        1 -> General error
     *        2 -> Operation user not defined
     *        3 -> Operation user does not have permission to get this data.
     *       10 -> Debit card not defined
     *       11 -> Debit card does not exist
     */
    private Result<DebitCard> getDebitCardById(User operationUser, DebitCardVO debitCardVO) {
        Enter(log, "getDebitCardById");

        if (operationUser == null) {
            log.warn("Operation user not defined.");
            Exit(log, "getDebitCardById");
            return Result.create(2);
        }

        if (debitCardVO == null || debitCardVO.getId() == null) {
            log.warn("Debit card data not defined.");
            Exit(log, "getDebitCardById");
            return Result.create(10);
        }

        DebitCard debitCard = em.find(DebitCard.class, debitCardVO.getId());
        if (debitCard == null) {
            log.warn("Debit card does not exists.");
            Exit(log, "getDebitCardById");
            return Result.create(11);
        }

        try {
            if (!userCanGetDebitCard(operationUser, debitCard)) {
                log.warn("Operation user does not have permission to get debit card data.");
                Exit(log, "getDebitCardById");
                return Result.create(3);
            }
        } catch (ServerErrorException ex) {
            Error(log, "Error checking if user has permission to get this debit card data.", ex);
            Exit(log, "getDebitCardById");
            return Result.create(-1);
        }

        Exit(log, "getDebitCardById");
        return Result.create(debitCard);
    }

    private boolean userCanGetDebitCard(User operationUser, DebitCard debitCard) throws ServerErrorException {
        List<Permission> userPermission = usersController.getUserPermissions(operationUser);

        if (userPermission.contains(Permission.SYSTEM)) return true;
        if (userPermission.contains(Permission.ADMIN)) return true;

        if (userPermission.contains(Permission.GET_DEBIT_CARD)) {
            if (debitCard.getOwner().equals(operationUser)) return true;
        }

        return false;
    }

    @Override
    public Result<DebitCardVO> createDebitCardVO(UserVO userVO, DebitCardVO debitCardVO) {
        Enter(log, "createDebitCardVO");

        User user = (userVO == null || userVO.getId() == null) ? null : em.find(User.class, userVO.getId());
        Result<DebitCard> creationResult = createDebitCard(user, debitCardVO);

        Exit(log, "createDebitCardVO");
        if (!creationResult.isValid()) return Result.create(creationResult.getErrCode());

        DebitCardVO result = creationResult.getResult().getVO();
        result.setOwnerVO(creationResult.getResult().getOwner().getVO());
        result.setBankAccountVO(creationResult.getResult().getBankAccount().getVO());

        return Result.create(creationResult.getResult().getVO());
    }

    /**
     * Creates a debit card
     *
     * @param user User that wants to create the debit card
     * @param debitCardVO Debit card data
     *
     * @return
     *    Debit card created or error code. Error codes:
     *        -1 -> Server error
     *         0 -> Undefined
     *         1 -> General error
     *         2 -> User not defined
     *        10 -> Debit card not defined
     *        11 -> Card number not defined
     *        12 -> Card expiration date not defined
     *        13 -> Card's owner not defined or does not exist
     *        14 -> Card's bank account not defined or does not exist
     *
     */
    private Result<DebitCard> createDebitCard(User user, DebitCardVO debitCardVO) {
        Enter(log, "createDebitCard");

        if (user == null) {
            log.warn("User not defined.");
            Exit(log, "createDebitCard");
            return Result.create(2);
        }

        ValidationResult validationResult = validateDebitCardCreation(debitCardVO);
        if (!validationResult.isValid()) {
            ErrorWarning(log, "Validating debit card data for creation.", validationResult.getErrCode(), validationResult.getErrMsg());
            Exit(log, "createDebitCard");
            return Result.create(validationResult.getErrCode()+9);
        }

        User owner = em.find(User.class, debitCardVO.getOwnerVO().getId());
        if (owner == null) {
            Exit(log, "createDebitCard");
            return Result.create(13);
        }

        BankAccount bankAccount = em.find(BankAccount.class, debitCardVO.getBankAccountVO().getId());
        if (bankAccount == null) {
            Exit(log, "createDebitCard");
            return Result.create(14);
        }

        try {
            if (!userCanCreateDebitCard(user, bankAccount)) {
                log.warn("Operation user does not have permission to create this debit card.");
                Exit(log, "createDebitCard");
                return Result.create(3);
            }
        } catch (ServerErrorException ex) {
            Error(log, "Error checking if user can create this debit card or not.", ex);
            Exit(log, "createDebitCard");
            return Result.create(-1);
        }

        DebitCard debitCard = DebitCard.builder()
                .cardNumber(debitCardVO.getCardNumber())
                .expires(debitCardVO.getExpires())
                .owner(owner)
                .bankAccount(bankAccount)
                .build();

        if (debitCardVO.getCcv() != null) debitCard.setCcv(debitCardVO.getCcv());
        if (debitCardVO.getPin() != null) debitCard.setPin(debitCardVO.getPin());

        try {
            log.info("Creating debit card...");
            em.persist(debitCard);
            log.info("Debit card created!");
        } catch (Exception ex) {
            log.error("{}\n{}");
            Error(log, "Error creating debit card.", null, ex.getMessage());
            return Result.create(-1);
        }

        Exit(log, "createDebitCard");
        return Result.create(debitCard);
    }

    private ValidationResult validateDebitCardCreation(DebitCardVO debitCardVO) {
        // Bank card data validation
        ValidationResult validationResult = validateBankCardCreation(debitCardVO);
        if (!validationResult.isValid()) return validationResult;

        // Debit card data validation
        // Nothing

        return ValidationResult.ok();
    }

    private boolean userCanCreateDebitCard(User operationUser, BankAccount bankAccount) throws ServerErrorException {
        List<Permission> userPermissions = usersController.getUserPermissions(operationUser);

        if (userPermissions.contains(Permission.SYSTEM)) return true;
        if (userPermissions.contains(Permission.ADMIN)) return true;

        if (userPermissions.contains(Permission.CREATE_DEBIT_CARD)) {
            List<User> owners = getBankAccountOwners(bankAccount);
            if (owners.contains(operationUser)) return true;
        }

        return false;
    }

    @Override
    public Result<DebitCardVO> deleteDebitCardVO(UserVO userVO, DebitCardVO debitCardVO) {
        Enter(log, "deleteDebitCardVO");

        User user = (userVO == null || userVO.getId() == null) ? null : em.find(User.class, userVO.getId());
        Result<DebitCardVO> deletionResult = deleteDebitCard(user, debitCardVO);

        Exit(log, "deleteDebitCardVO");
        return deletionResult;
    }

    /**
     * Delete debit card data
     * @param operationUser User that wants to do this operation
     * @param debitCardVO Debit card id
     * @return Debit card deleted or error code.
     *
     * Error codes:
     *       -1 -> Server error
     *        0 -> Undefined
     *        1 -> General error
     *        2 -> Operation user not defined
     *        3 -> Operation user does not have permission to get this data.
     *       10 -> Debit card not defined
     *       11 -> Debit card does not exist
     */
    private Result<DebitCardVO> deleteDebitCard(User operationUser, DebitCardVO debitCardVO) {
        Enter(log, "deleteDebitCard");

        if (operationUser == null) {
            log.warn("Operation user not defined.");
            Exit(log, "deleteDebitCard");
            return Result.create(2);
        }

        if (debitCardVO == null || debitCardVO.getId() == null) {
            log.warn("Debit card data not defined.");
            Exit(log, "deleteDebitCard");
            return Result.create(10);
        }

        DebitCard debitCard = em.find(DebitCard.class, debitCardVO.getId());
        if (debitCard == null) {
            log.warn("Debit card does not exists.");
            Exit(log, "deleteDebitCard");
            return Result.create(11);
        }

        try {
            if (!userCanDeleteDebitCard(operationUser, debitCard)) {
                log.warn("Operation user does not have permission to delete debit card.");
                Exit(log, "deleteDebitCard");
                return Result.create(3);
            }
        } catch (ServerErrorException ex) {
            Error(log, "Error checking if user has permission to delete this debit card.", ex);
            Exit(log, "deleteDebitCard");
            return Result.create(-1);
        }

        DebitCardVO result = debitCard.getVO();
        try {
            deleteDebitCard(debitCard);
        } catch (ServerErrorException ex) {
            Error(log, "Error deleting debit card.", ex);
            Exit(log, "deleteDebitCard");
            Result.create(-1);
        }

        Exit(log, "deleteDebitCard");
        return Result.create(result);
    }

    private boolean userCanDeleteDebitCard(User operationUser, DebitCard debitCard) throws ServerErrorException {
        List<Permission> userPermission = usersController.getUserPermissions(operationUser);

        if (userPermission.contains(Permission.SYSTEM)) return true;
        if (userPermission.contains(Permission.ADMIN)) return true;

        if (userPermission.contains(Permission.DELETE_DEBIT_CARD)) {
            if (debitCard.getOwner().equals(operationUser)) return true;
        }

        return false;
    }

    /**
     *
     * Delete debit card and its dependencies.
     *
     * DO NOT USE THIS METHOD DIRECTLY!!!
     *
     * ServerErrorException codes:
     *  1 -> Error deleting debit card
     *  2 -> Error deleting debit card's operations.
     *
     */
    private void deleteDebitCard(DebitCard debitCard) throws ServerErrorException {
        Enter(log, "deleteDebitCard");
        Query query;
        int n;

        // Delete debit card operation
        try {
            query = em.createQuery("DELETE FROM DebitCardOperation op WHERE op.me =:debitCard")
                    .setParameter("debitCard", debitCard);

            log.info("Deleting debit card operations...");
            n = query.executeUpdate();
            log.info("{} debit card operations deleted.", n);
        } catch (Exception ex) {
            Exit(log, "deleteDebitCard");
            throw new ServerErrorException(2, "Error deleting debit card operations.", ex);
        }

        try {
            log.info("Deleting debit card.");
            em.remove(debitCard);
            log.info("Debit card deleted!");
        } catch (Exception ex) {
            Exit(log, "deleteDebitCard");
            throw new ServerErrorException(1, "Error deleting debit card.", ex);
        }

        Exit(log, "deleteDebitCard");
    }

}
