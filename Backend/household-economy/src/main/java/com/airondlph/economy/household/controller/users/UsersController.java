package com.airondlph.economy.household.controller.users;

import com.airondlph.economy.household.controller.data.Result;
import com.airondlph.economy.household.data.entity.User;
import com.airondlph.economy.household.data.enumeration.EntityOperationEnum;
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
            validateUser(user, EntityOperationEnum.CREATE);
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
     * @param operation Operation that will be done
     * @throws ValidationException If any data of the user is invalid for that operation
     */
    private void validateUser(User user, EntityOperationEnum operation) throws ValidationException {

        if (EntityOperationEnum.CREATE.equals(operation)) {
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

            return;
        }

        throw new ValidationException(4, "Operation not defined.");
    }

    // -------------------------------------------------------------------------------------------------------------



}
