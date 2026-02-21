SET @userId = 3;

-- INSERT INTO householdeconomy.user_permission (user_id, permission) VALUES (@userId, 1); -- SYSTEM
-- INSERT INTO householdeconomy.user_permission (user_id, permission) VALUES (@userId, 2); -- ADMIN
INSERT INTO householdeconomy.user_permission (user_id, permission) VALUES (@userId, 3); -- ADD USER
INSERT INTO householdeconomy.user_permission (user_id, permission) VALUES (@userId, 4); -- ADD ALL USER
INSERT INTO householdeconomy.user_permission (user_id, permission) VALUES (@userId, 5); -- GET USER
INSERT INTO householdeconomy.user_permission (user_id, permission) VALUES (@userId, 6); -- GET ALL USER
INSERT INTO householdeconomy.user_permission (user_id, permission) VALUES (@userId, 7); -- DELETE USER
INSERT INTO householdeconomy.user_permission (user_id, permission) VALUES (@userId, 8); -- DELETE ALL USERS
INSERT INTO householdeconomy.user_permission (user_id, permission) VALUES (@userId, 9); -- EDIT USER
INSERT INTO householdeconomy.user_permission (user_id, permission) VALUES (@userId, 10); -- EDIT ALL USERS
INSERT INTO householdeconomy.user_permission (user_id, permission) VALUES (@userId, 11); -- SEND USER EMAIL VALIDATION CODE
INSERT INTO householdeconomy.user_permission (user_id, permission) VALUES (@userId, 12); -- SEND ALL USER EDIT VALIDATION CODE
INSERT INTO householdeconomy.user_permission (user_id, permission) VALUES (@userId, 13); -- ADD BANK
INSERT INTO householdeconomy.user_permission (user_id, permission) VALUES (@userId, 14); -- GET BANK
INSERT INTO householdeconomy.user_permission (user_id, permission) VALUES (@userId, 15); -- DELETE BANK
INSERT INTO householdeconomy.user_permission (user_id, permission) VALUES (@userId, 16); -- EDIT BANK