const API_BASE = "http://localhost:9000";
export const API_URL = {
	BASE: API_BASE + "/",
	GET_LOGGED_USER: API_BASE + "/user/",
	LOGIN: API_BASE + "/user/login/",
	SIGN_UP: API_BASE + "/user/",
	GET_USER_BANK_ACCOUNTS:
		API_BASE + "/financial/bankAccount/owner/{ownerId}/",
};

const WEB_BASE = ""; // "http://localhost:5173";
export const WEB_URL = {
	BASE: WEB_BASE + "/",
	LOGIN: WEB_BASE + "/user/login/",
	SIGN_UP: WEB_BASE + "/user/signUp/",
};
