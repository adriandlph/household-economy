package com.airondlph.economy.household.controller.users;

import com.airondlph.economy.household.controller.data.Result;
import com.airondlph.economy.household.data.entity.User;
import com.airondlph.economy.household.data.entity.UserPermission;
import com.airondlph.economy.household.data.enumeration.Permission;
import com.airondlph.economy.household.data.model.UserVO;
import com.airondlph.economy.household.exception.ServerErrorException;
import com.airondlph.economy.household.exception.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import jdk.jfr.ValueDescriptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.airondlph.economy.household.util.LogUtils.*;


/**
 * @author adriandlph / airondlph
 */
@Controller
@Slf4j
@Transactional
public class UsersController {
    @Autowired
    private EntityManager em;
    @Autowired
    private SecurityController securityController;

    private static final Long SYSTEM_USER_ID = 1L;

    /**
     * Creates a user of the system
     */
    public Result<UserVO> createUserVO(UserVO userDataVO) {
        Enter(log, "createUserVO", "userDataVO");

        User user = User.builder()
            .username(userDataVO.getUsername())
            .password(userDataVO.getPassword())
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
     * Creates a user that depends on other user
     */
    public Result<UserVO> createUserOfOtherVO(Long operationUserId, UserVO userDataVO, Long parentUserId) {
        Enter(log, "createUserVO", "userDataVO");

        User operationUser = (operationUserId == null) ? null : em.find(User.class, operationUserId);
        User parentUser = (parentUserId == null) ? null : em.find(User.class, parentUserId);

        User user = User.builder()
            .username(userDataVO.getUsername())
            .password(userDataVO.getPassword())
            .firstName(userDataVO.getFirstName())
            .lastName(userDataVO.getLastName())
            .email(userDataVO.getEmail())
            .parentUser(parentUser)
            .build();

        Result<User> createUserResult = createUser(operationUser, user);

        Exit(log, "createUserVO");
        if (!createUserResult.isValid()) return Result.create(createUserResult.getErrCode());
        return Result.create(createUserResult.getResult().getVO());

    }

    /**
     * Creates a user of the system
     */
    private Result<User> createUser(User user) {
        Enter(log, "createUser", "user");

        User systemUser = em.find(User.class, SYSTEM_USER_ID);
        user.setParentUser(systemUser);

        try {
            return createUser(systemUser, user);
        } finally {
            Exit(log, "createUser");
        }

    }


    /**
     *
     * Create a new basic user (do not depend on other user)
     *
     * @param operationUser User that creates this user
     * @param user User's data
     *
     * @return Result with the created user or error code.
     * Error codes:
     * -1 -> Server error
     *  0 -> Undefined
     *  1 -> General error
     *  2 -> operationUser not defined
     *  3 -> userToCreate not defined
     *  4 -> User does not have permission to create this user
     *  5 -> User's username not defined
     *  6 -> User's username is not valid
     *  7 -> User's password not defined
     *  8 -> User's password not valid
     *  9 -> User's first name not defined
     *  10 -> User's email not defined
     *  11 -> User's email not valid
     *  12 -> User's username or email already registered.
     *
     */
    private Result<User> createUser(User operationUser, User user) {
        Enter(log, "createUser", "operationUser, user");

        List<Permission> userPermissions = null;
        try {
            userPermissions = getUserPermissions(operationUser);
        } catch (ServerErrorException ex) {
            Error(log,"Error getting user's permissions.", ex.getCode(), ex.getMessage());
            Exit(log, "createUser");
            return Result.create(-1);
        }

        try {
            log.info("Validating user's data...");
            validateUserCreation(operationUser, user, userPermissions);

        } catch (ValidationException ex) {
            ErrorWarning(log,"User's data not valid.", ex.getCode(), ex.getMessage());
            Exit(log, "createUser");
            return switch (ex.getCode()) {
                case 1 -> Result.create(2); // operationUser not defined
                case 2 -> Result.create(3); // user not defined
                case 3 -> Result.create(4); // User does not have permission to create this user
                case 4 -> Result.create(5); // Username not defined
                case 5 -> Result.create(6); // Username not valid
                case 6 -> Result.create(7); // password not defined
                case 7 -> Result.create(8); // password not valid
                case 8 -> Result.create(9); // First name not defined
                case 9 -> Result.create(10); // Email not defined
                case 10 -> Result.create(11); // Email not valid
                case 11 -> Result.create(12); // Username or email already registered.
                default -> Result.create(1); // General error
            };

        } catch (Exception ex) {
            Error(log, "Unexpected error", 1, ex.getMessage());
            Exit(log, "createUser");
            return Result.create(-1);
        }

        try {
            log.info("Creating user...");
            user.setPassword(securityController.encodeUserPassword(user.getPassword()));
            em.persist(user);

        } catch (Exception ex) {
            Error(log, "Error saving user", null, ex.getMessage());
            Exit(log, "createUser");
            return Result.create(-1); // Server error
        }

        Exit(log, "createUser");
        return Result.create(user);
    }

    /**
     *
     * Validate user data and check if operationUser can create this user.
     *
     * @param operationUser User that wants to create userToCreate
     * @param userToCreate User that will be created
     *
     * @throws ValidationException If any data of the user is invalid for user creation
     *  Exception codes:
     *   1 -> operationUser not defined
     *   2 -> userToCreate not defined
     *   3 -> User does not have permission to create this user
     *   4 -> User's username not defined
     *   5 -> User's username is not valid
     *   6 -> User's first name not defined
     *   7 -> User's email not defined
     *   8 -> User's email not valid
     *   9 -> User's username or email already registered.
     *
     */
    private void validateUserCreation(User operationUser, User userToCreate, List<Permission> operationUserPermissions) throws ValidationException {
        if (operationUser == null) throw new ValidationException(1, "Operation user's data not defined.");
        if (userToCreate == null) throw new ValidationException(2, "User's data not defined.");

        if (userToCreate.getParentUser() == null || userToCreate.getParentUser().getId() == null) throw new ValidationException(3, "You cannot create an independent user");

        // Check permissions
        if (!userHasCreationPermissions(operationUser, userToCreate, operationUserPermissions)) {
            throw new ValidationException(3, "You do not have permission to create this user.");
        }

        // Check user data
        if (userToCreate.getUsername() == null || userToCreate.getUsername().isBlank()) throw new ValidationException(4, "User's username is not defined.");
        if (userToCreate.getUsername().contains("@")) throw new ValidationException(5, "User's username cannot contain '@'");
        if (userToCreate.getPassword() == null || userToCreate.getPassword().isBlank()) throw new ValidationException(6, "User's password not defined");
        if (userToCreate.getPassword().length() < 10) throw new ValidationException(7, "User's password not valid");
        if (userToCreate.getFirstName() == null || userToCreate.getFirstName().isBlank()) throw new ValidationException(8, "User's first name is not defined.");
        if (userToCreate.getEmail() == null || userToCreate.getEmail().isBlank())  throw new ValidationException(9, "User's email is not defined.");
        if (!userToCreate.getEmail().contains("@")) throw new ValidationException(10, "User's email is not valid.");

        // Check if other users has this username or email
        Query query = em.createQuery("SELECT u FROM User u WHERE u.username=:username OR u.email=:email")
                .setParameter("username", userToCreate.getUsername())
                .setParameter("email", userToCreate.getEmail())
                .setMaxResults(1);
        if (!query.getResultList().isEmpty()) throw new ValidationException(11, "User's username or email already registered.");

    }


    /**
     *
     * @param operationUser User that wants to create the new user
     * @param userToCreate User that will be created
     * @param operationUserPermissions Operation user permissions
     * @return true if user can create this user, or else if cannot.
     *
     */
    private boolean userHasCreationPermissions(User operationUser, User userToCreate, List<Permission> operationUserPermissions) {
        // System or admin is the operation user
        if (operationUserPermissions.contains(Permission.SYSTEM) || operationUserPermissions.contains(Permission.ADMIN)) return true;

        // All users can delete its own users, except system
        if (operationUser.getId().equals(userToCreate.getId())) return true;
        // You have the permission to delete any user
        if (operationUserPermissions.contains(Permission.ADD_ALL_USER)) return true;
        // You have the permission to delete any user under your hierarchy
        if (operationUserPermissions.contains(Permission.ADD_USER) && isParent(operationUser, userToCreate.getParentUser())) return true;

        return false;
    }

    public Result<UserVO> getUserByIdVO(Long operationUserId, Long userId) {
        User operationUser = em.find(User.class, operationUserId);

        if (operationUser == null) return Result.create(1);

        Result<User> userDataResult = getUserById(operationUser, userId);
        if (!userDataResult.isValid()) return Result.create(userDataResult.getErrCode());

        return Result.create(userDataResult.getResult().getVO());
    }

    /**
     * Gets user data.
     * @param operationUser User that wants to access data.
     * @param userId User's id of user data that will ber returned.
     *
     * @return User data if error code == 0.
     *
     * Error codes:
     *   - -1 -> Server error.
     *   -  0 -> Ok.
     *   -  1 -> General error.
     *   -  2 -> UserId not defined.
     *   -  3 -> User does not have access to get this information.
     *   -  4 -> User does not exist.
     *
     */
    private Result<User> getUserById(User operationUser, Long userId) {
        Enter(log, "getUserById");

        if (userId == null) {
            ErrorWarning(log, "Error getting users data by id.", 1, "userId not defined.");
            Exit(log, "getUserById");
            return Result.create(2);
        }

        List<Permission> userPermissions = null;
        try {
            userPermissions = getUserPermissions(operationUser);
        } catch (ServerErrorException ex) {
            Error(log,"Error getting user's permissions.", ex.getCode(), ex.getMessage());
            Exit(log, "getUserById");
            return Result.create(-1);
        }


        // Gets user and check if we can access its data.
        // If the user does not exists: if the user has permission to see all user will be shown the error,
        // otherwise is a not auth error.
        User user = em.find(User.class, userId);
        boolean existsUser = user != null;
        if (!existsUser) user = User.builder().id(0L).parentUser(User.builder().id(SYSTEM_USER_ID).build()).build();
        if (!userHasGetUserPermission(operationUser, user, userPermissions)) {
            log.warn("User with id={} does not have permission to see user with id={} data.", user.getId(), userId);
            Exit(log, "getUserById");
            return Result.create(3);
        }

        Exit(log, "getUserById");
        if (!existsUser) return Result.create(4);
        return Result.create(user);
    }

    /**
     * Check if the operation user can get userToGet's user data
     *
     * @param operationUser User that wants to get data
     * @param userToGet User which data will be accesed
     * @param operationUserPermissions OperationUser's permissions
     *
     * @return true -> has permission | false -> does not have permission
     */
    private boolean userHasGetUserPermission(User operationUser, User userToGet, List<Permission> operationUserPermissions) {
        // System or admin is the operation user
        if (operationUserPermissions.contains(Permission.SYSTEM) || operationUserPermissions.contains(Permission.ADMIN)) return true;

        // All users can delete its own users, except system
        if (operationUser.getId().equals(userToGet.getId())) return true;
        // You have the permission to delete any user
        if (operationUserPermissions.contains(Permission.GET_ALL_USER)) return true;
        // You have the permission to delete any user under your hierarchy
        if (operationUserPermissions.contains(Permission.GET_USER) && isParent(operationUser, userToGet.getParentUser())) return true;

        return false;
    }


    public Result<UserVO> setUserVO(Long operationUserId, UserVO userToEditData) {
        User operationUser = (operationUserId == null) ? null : em.find(User.class, operationUserId);

        Result<User> userResult = setUser(operationUser, userToEditData);

        if (!userResult.isValid()) return Result.create(userResult.getErrCode());

        UserVO editedUserData = UserVO.builder()
            .id(userResult.getResult().getId())
            .username(userResult.getResult().getUsername())
            .firstName(userResult.getResult().getFirstName())
            .lastName(userResult.getResult().getLastName())
            .email(userResult.getResult().getEmail())
            .emailValidated(userResult.getResult().getEmailValidated())
            .parentUser(UserVO.builder().id(userResult.getResult().getParentUser() == null ? null : userResult.getResult().getParentUser().getId()).build())
            .build();

        return Result.create(editedUserData);
    }

    /**
     *
     * Create a new basic user (do not depend on other user)
     *
     * @param operationUser User that creates this user
     * @param userToEditData User's data
     *
     * @return Result with the created user or error code.
     * Error codes:
     * -1 -> Server error
     *  0 -> Undefined
     *  1 -> General error
     *  2 -> operationUser not defined
     *  3 -> userToEdit not defined
     *  4 -> operationUser does not have permission to edit this user
     *  5 -> User's username cannot be blank
     *  6 -> User's username not valid
     *  7 -> User's first name cannot be blank
     *  8 -> User's email cannot be blank
     *  9 -> User's email not valid
     *  10 -> User's username already in use
     *  11 -> User's email already in use
     *
     */
    private Result<User> setUser(User operationUser, UserVO userToEditData) {
        Enter(log, "setUser", "operationUser, userToEditData");

        if (operationUser == null) return Result.create(2);

        User userToEdit = (userToEditData.getId() == null) ? null : em.find(User.class, userToEditData.getId());
        if (userToEdit == null) return Result.create(3);


        List<Permission> userPermissions = null;
        try {
            userPermissions = getUserPermissions(operationUser);
        } catch (ServerErrorException ex) {
            Error(log,"Error getting user's permissions.", ex.getCode(), ex.getMessage());
            Exit(log, "setUser");
            return Result.create(-1);
        }

        try {
            log.info("Validating user's data...");
            validateUserEdition(operationUser, userToEdit, userToEditData, userPermissions);

        } catch (ValidationException ex) {
            ErrorWarning(log,"User's data not valid.", ex.getCode(), ex.getMessage());
            Exit(log, "setUser");
            return switch (ex.getCode()) {
                case -1 -> Result.create(-1); // Server error
                case 1 -> Result.create(2); // operationUser not defined
                case 2 -> Result.create(3); // user not defined
                case 3 -> Result.create(4); // operationUser cannot edit this user
                case 4 -> Result.create(5); // Username cannot be blank
                case 5 -> Result.create(6); // Username not valid
                case 6 -> Result.create(7); // First name cannot be blank
                case 7 -> Result.create(8); // email cannot be blank
                case 8 -> Result.create(9); // email not valid
                case 9 -> Result.create(10); // user's username already in use
                case 10 -> Result.create(11); // user's email already in use
                default -> Result.create(1); // General error
            };

        } catch (Exception ex) {
            Error(log, "Unexpected error", 1, ex.getMessage());
            Exit(log, "setUser");
            return Result.create(-1);
        }

        try {
            if (userToEditData.getUsername() != null) userToEdit.setUsername(userToEditData.getUsername());
            if (userToEditData.getFirstName() != null) userToEdit.setFirstName(userToEditData.getFirstName());
            if (userToEditData.getLastName() != null) userToEdit.setLastName(userToEditData.getLastName());
            if (userToEditData.getEmail() != null)  {
                userToEdit.setEmailValidated(false);
                userToEdit.setEmail(userToEditData.getEmail());
            }

            em.flush();

        } catch (Exception ex) {
            Error(log, "Error saving user", null, ex.getMessage());
            Exit(log, "setUser");
            return Result.create(-1); // Server error
        }

        Exit(log, "setUser");
        return Result.create(userToEdit);
    }

    /**
     *
     * Validate user data and check if operationUser can create this user.
     *
     * @param operationUser User that wants to create userToCreate
     * @param userToEdit User that will be created
     * @param operationUserPermissions permissions of operationUser
     *
     * @throws ValidationException If any data of the user is invalid for user creation
     *  Exception codes:
     *   1 -> operationUser not defined
     *   2 -> userToCreate not defined
     *   3 -> User does not have permission to edit this user
     *   4 -> User's username cannot be blank
     *   5 -> User's username is not valid
     *   6 -> User's first name cannot be blank
     *   7 -> User's email cannot be blank
     *   8 -> User's email not valid
     *   9 -> User's username already in use.
     *   10 -> User's email already in use.
     *
     */
    private void validateUserEdition(User operationUser, User userToEdit, UserVO userToEditVO, List<Permission> operationUserPermissions) throws ValidationException {
        if (operationUser == null) throw new ValidationException(1, "Operation user's data not defined.");
        if (userToEdit == null) throw new ValidationException(2, "User's data not defined.");

        // Check permissions
        if (!userHasEditionPermissions(operationUser, userToEdit, operationUserPermissions)) {
            throw new ValidationException(3, "You do not have permission to edit this user.");
        }

        // Check user data
        if (userToEditVO.getUsername() != null) {
            if (userToEditVO.getUsername().isBlank())
                throw new ValidationException(4, "User's username cannot be blank.");
            if (userToEditVO.getUsername().contains("@"))
                throw new ValidationException(5, "User's username cannot contain '@'");
        }

        if (userToEditVO.getFirstName() != null && userToEditVO.getFirstName().isBlank()) throw new ValidationException(6, "User's first name cannot be blank.");

        if (userToEditVO.getEmail() != null) {
            if (userToEditVO.getEmail().isBlank()) throw new ValidationException(7, "User's email cannot be blank.");
            if (!userToEditVO.getEmail().contains("@")) throw new ValidationException(8, "User's email is not valid.");
        }

        // Check if other users has this username or email
        Query query = em.createQuery("SELECT u FROM User u WHERE ((u.username=:username OR u.email=:email) AND (u.id != :id))")
            .setParameter("username", userToEditVO.getUsername())
            .setParameter("email", userToEditVO.getEmail())
            .setParameter("id", userToEditVO.getId())
            .setMaxResults(1);

        User user = null;
        try {
            user = (User) query.getResultList().getFirst();
        } catch (NoSuchElementException ignore) {
            // All is okey
        } catch (Exception ex) {
            log.error("{}", ex);
            Error(log, "Error checking if user's username or email is in use (for edition)", -1, ex.getMessage());
            throw new ValidationException(-1, "Server error.");
        }

        if (user != null) {
            if (user.getUsername().equals(userToEditVO.getUsername()))
                throw new ValidationException(9, "User's username already in use.");
            if (user.getEmail().equals(userToEditVO.getEmail()))
                throw new ValidationException(10, "User's email already in use.");
        }

    }


    /**
     *
     * @param operationUser User that wants to create the new user
     * @param userToEdit User that will be created
     * @param operationUserPermissions Operation user permissions
     * @return true if user can create this user, or else if cannot.
     *
     */
    private boolean userHasEditionPermissions(User operationUser, User userToEdit, List<Permission> operationUserPermissions) {
        // System or admin is the operation user
        if (operationUserPermissions.contains(Permission.SYSTEM) || operationUserPermissions.contains(Permission.ADMIN)) return true;

        // All users can delete its own users, except system
        if (operationUser.getId().equals(userToEdit.getId())) return true;
        // You have the permission to delete any user
        if (operationUserPermissions.contains(Permission.EDIT_ALL_USER)) return true;
        // You have the permission to delete any user under your hierarchy
        if (operationUserPermissions.contains(Permission.EDIT_USER) && isParent(operationUser, userToEdit.getParentUser())) return true;

        return false;
    }


    public Result<Void> deleteUserByIdVO(Long operationUserId, Long userId) {
        Enter(log, "deleteUserVO", "operationUserId, userId");

        Result<User> createUserResult = deleteUserById(operationUserId, userId);

        Exit(log, "deleteUserVO");
        if (!createUserResult.isValid()) return Result.create(createUserResult.getErrCode());
        return Result.create(null);
    }

    /**
     *
     * Deletes an user from database.
     *
     * @param operationUserId User that wants to do this action
     * @param userId User's ID of the user that will be removed
     *
     * @return Result of deletion.
     *   -1 -> Server error
     *    0 -> Undefined
     *    1 -> General error
     *    2 -> Operation user not found
     *    3 -> User to be deleted not found
     *    4 -> Operation user cannot remove this user (do not have permission)
     *
     */
    private Result<User> deleteUserById(Long operationUserId, Long userId) {
        Enter(log, "deleteUser", "operationUserId, userId");

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

        List<Permission> operationUserPermissions = null;
        try {
            operationUserPermissions = getUserPermissions(operationUser);
        } catch (ServerErrorException ex) {
            Error(log,"Error getting user's permissions.", ex.getCode(), ex.getMessage());
            Exit(log, "deleteUser");
            return Result.create(-1);
        }

        try {
            validateUserDeletion(operationUser, userToRemove, operationUserPermissions);
        } catch (ValidationException ex) {
            ErrorWarning(log, "Error validating if this user can be deleted", ex.getCode(), ex.getMessage());
            Exit(log, "deleteUser");
            return switch (ex.getCode()) {
                case 1 -> Result.create(2); // operationUser not exists
                case 2 -> Result.create(3); // user not exists
                case 3, 4 -> Result.create(4); // do not have permission to remove this user
                default -> Result.create(1); // general error
            };
        }

        try {
            Query deleteQuery;

            // Removing all user tokens
            log.info("Removing user's tokens...");
            deleteQuery = em.createQuery("DELETE FROM Token t WHERE t.user.id=:userId");
            deleteQuery.setParameter("userId", userToRemove.getId());
            log.info("{} tokens removed.", deleteQuery.executeUpdate());

            // Removing user
            em.remove(userToRemove);
            log.info("User removed: {}", userToRemove);
        } catch (Exception ex) {
            Error(log, "Error removing user", null, ex.getMessage());
            Exit(log, "deleteUser");
            return Result.create(-1);
        }

        Exit(log, "deleteUser");
        return Result.create(userToRemove);
    }

    /**
     * @param operationUser User that wants to do this action (null secure)
     * @param userToDelete User to be removed (null secure)
     *
     * @throws ValidationException If any data of the user is invalid for that operation
     *    1 -> operationUser not defined
     *    2 -> userToDelete not defined
     *    3 -> System user cannot been deleted
     *    4 -> operationUser cannot delete userToDelete
     */
    private void validateUserDeletion(User operationUser, User userToDelete, List<Permission> operationUserPermissions) throws ValidationException {
        if (operationUser == null) throw new ValidationException(1, "User's data not defined.");
        if (userToDelete == null) throw new ValidationException(2, "User's data not defined.");

        if (SYSTEM_USER_ID.equals(userToDelete.getId())) throw new ValidationException(3, "System user cannot been deleted.");

        // System or admin is the operation user
        if (operationUserPermissions.contains(Permission.SYSTEM) || operationUserPermissions.contains(Permission.ADMIN)) return;

        // All users can delete its own users, except system
        if (operationUser.getId().equals(userToDelete.getId())) return;
        // You have the permission to delete any user
        if (operationUserPermissions.contains(Permission.DELETE_ALL_USER)) return;
        // You have the permission to delete any user under your hierarchy
        if (operationUserPermissions.contains(Permission.DELETE_USER) && isParent(operationUser, userToDelete)) return;

        throw new ValidationException(4, "User does not have permission to remove this user.");
    }

    private List<Permission> getUserPermissions(User user) throws ServerErrorException {
        List<Permission> userPermissions = new ArrayList<>();

        Query query = em.createQuery("SELECT up FROM UserPermission up WHERE up.user = :user")
            .setParameter("user", user);

        try {
            ((List<UserPermission>) query.getResultList()).forEach(p -> userPermissions.add(p.getPermission()));
        } catch (Exception ex) {
            Error(log, "Getting user permissions.", null, ex.getMessage());
            throw new ServerErrorException("Error getting user permissions.");
        }

        return userPermissions;
    }

    private boolean isParent(User parent, User user) {
        if (user.equals(parent)) return true;

        User auxParent = user.getParentUser();

        while (auxParent != null && parent.getId() > 1) {
            if (auxParent.getId().equals(parent.getId())) return true;
            auxParent = auxParent.getParentUser();
        }

        return false;
    }



    // -------------------------------------------------------------------------------------------------------------



}
