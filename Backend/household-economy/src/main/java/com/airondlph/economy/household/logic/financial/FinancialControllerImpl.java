package com.airondlph.economy.household.logic.financial;

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
        List<User> bankAccountOwners = getBankAccountOwners(bankAccount);
        if (userPermissions.contains(Permission.DELETE_BANK_ACCOUNT) && bankAccountOwners.contains(user)) return true;

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
    public Result<BankAccountVO> editBankAccountVO(UserVO userVO, BankAccountVO bankAccountVO) {
        Enter(log, "editBankAccountVO");

        User user = (userVO == null || userVO.getId() == null) ? null : em.find(User.class, userVO.getId());
        Result<BankAccount> editionResult = editBankAccount(user, bankAccountVO);

        Exit(log, "editBankAccountVO");
        if (!editionResult.isValid()) return Result.create(editionResult.getErrCode());
        return Result.create(editionResult.getResult().getVO());
    }

    private Result<BankAccount> editBankAccount(User user, BankAccountVO bankAccountVO) {
        // TODO
        return null;
    }

    @Override
    public Result<CreditCardVO> createCreditCardVO(UserVO userVO, CreditCardVO creditCardVO) {
        Enter(log, "createCreditCardVO");

        User user = (userVO == null || userVO.getId() == null) ? null : em.find(User.class, userVO.getId());
        Result<CreditCard> creationResult = createCreditCard(user, creditCardVO);

        Exit(log, "createCreditCardVO");
        if (!creationResult.isValid()) return Result.create(creationResult.getErrCode());
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
     *       -1 -> Server error
     *        0 -> Undefined
     *        1 -> General error
     *        2 -> User not defined
     *        3 -> Credit card not defined
     *        4 -> Card number not defined
     *        5 -> Card expiration date not defined
     *        6 -> Card's owner not defined or does not exists
     *        7 -> Card's bank account not defined or does not exists
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
            return Result.create(validationResult.getErrCode()+2);
        }


        User owner;
        try {
            owner = em.find(User.class, creditCardVO.getOwnerVO().getId());
        } catch (Exception ex) {
            log.error("{}\n{}", ex.getMessage(), ex.getStackTrace());
            Error(log, "Error getting credit card's owner.", null, ex.getMessage());
            Exit(log, "createCreditCard");
            return Result.create(-1);
        }
        if (owner == null) {
            Exit(log, "createCreditCard");
            return Result.create(6);
        }

        BankAccount bankAccount;
        try {
            bankAccount = em.find(BankAccount.class, creditCardVO.getBankAccountVO().getId());
        } catch (Exception ex) {
            log.error("{}\n{}", ex.getMessage(), ex.getStackTrace());
            Error(log, "Error getting credit card's bank account.", null, ex.getMessage());
            Exit(log, "createCreditCard");
            return Result.create(-1);
        }

        if (bankAccount == null) {
            Exit(log, "createCreditCard");
            return Result.create(7);
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
        if (bankCardVO.getCardNumber() == null) ValidationResult.error(2, "Card number not defined.");
        if (bankCardVO.getExpires() == null) ValidationResult.error(3, "Card expiration date not defined.");
        if (bankCardVO.getOwnerVO() == null || bankCardVO.getOwnerVO().getId() == null) ValidationResult.error(4, "Card's owner not defined.");
        if (bankCardVO.getBankAccountVO() == null || bankCardVO.getBankAccountVO().getId() == null) ValidationResult.error(5, "Card's bank account not defined.");
        return ValidationResult.ok();
    }


}
