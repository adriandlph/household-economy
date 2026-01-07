package com.airondlph.economy.household.controller.users;

import com.airondlph.economy.household.controller.data.Result;
import com.airondlph.economy.household.data.entity.User;
import com.airondlph.economy.household.data.model.UserVO;
import com.airondlph.economy.household.exception.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import static com.airondlph.economy.household.controller.LogUtils.*;


/**
 * @author adriandlph / airondlph
 */
@Controller
@Slf4j
@Transactional
public class UsersController {
    @Autowired
    private EntityManager em;

    public Result<UserVO> createUserVO(UserVO userDataVO) {
        Enter(log, "createUserVO");

        User user = User.builder()
            .username(userDataVO.getUsername())
            .firstName(userDataVO.getFirstName())
            .lastName(userDataVO.getLastName())
            .email(userDataVO.getEmail())
            .build();

        Result<User> createUserResult = createUser(user);

        Exit(log, "createUserVO");
        if (!createUserResult.isValid()) return Result.create(createUserResult.getErrCode());
        return Result.create(createUserResult.getResult().getVO());
    }

    /**
     * Create a new user
     *
     * @param user User's data
     *
     * @return Result with the created user or error code.
     * Error codes:
     * -1 -> Server error
     *  0 -> Ok
     *  1 -> General user's data error
     *  2 -> User's first name error
     *  3 -> User's email error
     *  4 -> User's email already registered
     *
     */
    private Result<User> createUser(User user) {
        Enter(log, "createUser");

        try {
            log.info("Validating user's data...");
            validateUserCreation(user);
        } catch (ValidationException ex) {
            ErrorWarning(log,"User's data not valid.", ex.getCode(), ex.getMessage());
            Exit(log, "createUser");
            return switch (ex.getCode()) {
                case 2 -> Result.create(2); // User data not defined
                case 3 -> Result.create(3); // Username not defined
                case 4 -> Result.create(4); // Username not valid
                case 5 -> Result.create(5); // First name not defined
                case 6 -> Result.create(6); // Email not defined
                case 7 -> Result.create(7); // Email not valid
                case 8 -> Result.create(8); // User's username or email already registered
                default -> Result.create(1); // General error
            };
        } catch (Exception ex) {
            Error(log, "Unexpected error", 1, ex.getMessage());
            Exit(log, "createUser");
            return Result.create(1);
        }

        try {
            log.info("Creating user...");
            em.persist(user);
        } catch (Exception ex) {
            Error(log, "Error saving user", null, ex.getMessage());
            log.error("{}", ex);
            Exit(log, "createUser");
            return Result.create(-1); // Server error
        }

        Exit(log, "createUser");
        return Result.create(user);
    }

    /**
     * @param user (null secure)
     * @throws ValidationException If any data of the user is invalid for user creation
     */
    private void validateUserCreation(User user) throws ValidationException {
        if (user == null) throw new ValidationException(2, "User's data not defined.");
        if (user.getUsername() == null || user.getUsername().isBlank()) throw new ValidationException(3, "User's username is not defined.");
        if (user.getUsername().contains("@")) throw new ValidationException(4, "User's username cannot contain '@'");
        if (user.getFirstName() == null || user.getFirstName().isBlank()) throw new ValidationException(5, "User's first name is not defined.");
        if (user.getEmail() == null || user.getEmail().isBlank())  throw new ValidationException(6, "User's email is not defined.");
        if (!user.getEmail().contains("@")) throw new ValidationException(7, "User's email is not valid.");

        Query query = em.createQuery("SELECT u FROM user u WHERE u.username=:username OR u.email=:email")
                .setParameter("username", user.getUsername())
                .setParameter("email", user.getEmail())
                .setMaxResults(1);
        if (!query.getResultList().isEmpty()) throw new ValidationException(8, "User's username or email already registered.");

    }

    public Result<Void> deleteUserByIdVO(Long operationUserId, Long userId) {
        Enter(log, "deleteUserVO");

        Result<User> createUserResult = deleteUserById(operationUserId, userId);

        Exit(log, "deleteUserVO");
        if (!createUserResult.isValid()) return Result.create(createUserResult.getErrCode());
        return Result.create(null);
    }

    /**
     * Deletes an user from database.
     * @param operationUserId User that wants to do this action
     * @param userId User's ID of the user that will be removed
     * @return Result of deletion.
     *   -1 -> Server error
     *    0 -> Ok
     *    1 -> General error
     *    2 -> Operation user not found
     *    3 -> User to be deleted not found
     *    4 -> Operation user cannot remove this user
     */
    private Result<User> deleteUserById(Long operationUserId, Long userId) {
        Enter(log, "deleteUser");

        if (operationUserId == null) {
            ErrorWarning(log, "Error deleting user", null, "operationUserId not defined.");
            Exit(log, "deleteUser");
            return Result.create(2);
        }

        if (userId == null) {
            ErrorWarning(log, "Error deleting user", null, "userId not defined.");
            Exit(log, "deleteUser");
            return Result.create(3);
        }

        User operationUser = em.find(User.class, operationUserId);
        if (operationUser == null) {
            ErrorWarning(log, "Error deleting user", null, "operationUser not defined.");
            Exit(log, "deleteUser");
            return Result.create(2);
        }

        User userToRemove = em.find(User.class, userId);
        if (userToRemove == null) {
            ErrorWarning(log, "Error deleting user", null, "userToRemove not defined.");
            Exit(log, "deleteUser");
            return Result.create(3);
        }

        try {
            validateUserDeletion(operationUser, userToRemove);
        } catch (ValidationException ex) {
            ErrorWarning(log, "Error validating if this user can be deleted", ex.getCode(), ex.getMessage());
            int errorCode = switch (ex.getCode()) {
                case 1 -> 2;
                case 2 -> 3;
                case 3 -> 4;
                default -> 1;
            };

            return Result.create(errorCode);
        }

        try {
            em.remove(userToRemove);
            log.info("User removed: {}", userToRemove);
        } catch (Exception ex) {
            Error(log, "Error removing user", null, ex.getMessage());
            return Result.create(-1);
        }

        return Result.create(userToRemove);
    }

    /**
     * @param operationUser User that wants to do this action (null secure)
     * @param userToDelete User to be removed (null secure)
     *
     * @throws ValidationException If any data of the user is invalid for that operation
     *    1 -> operationUser not defined
     *    2 -> userToDelete not defined
     *    3 -> operationUser cannot delete userToDelete
     */
    private void validateUserDeletion(User operationUser, User userToDelete) throws ValidationException {
        if (operationUser == null) throw new ValidationException(1, "User's data not defined.");
        if (userToDelete == null) throw new ValidationException(2, "User's data not defined.");

        // All users can delete its own users
        if (operationUser.getId().equals(userToDelete.getId())) return;

        // TODO: Check if the registered user can delete this user (has permission to delete lower-level users (depends on them or someone lower in their hierarchy)).

    }



    // -------------------------------------------------------------------------------------------------------------



}
