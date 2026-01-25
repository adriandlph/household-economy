/* 
 *  @author adriandlph / airondlph
 */

INSERT INTO householdeconomy.user (id, email_validated, username, password, first_name, email, last_name) VALUES (1, 1, 'system', 'system', 'SYSTEM', ' ', ' ');
INSERT INTO householdeconomy.user (id, email_validated, username, password, first_name, email, last_name, parent_user_id) VALUES (2, 1, 'admin', 'admin', 'ADMIN', ' ', ' ', 1);


/* System permission */
INSERT INTO householdeconomy.user_permission (user_id, permission) VALUES (1, 0);

/* Admin permission */
INSERT INTO householdeconomy.user_permission (user_id, permission) VALUES (2, 1);

