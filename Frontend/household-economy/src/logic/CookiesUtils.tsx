/**
 * Sets a cookie.
 * @param key Cookie key
 * @param value Cookie value
 * @param expiresMs Time that the cookie will be valid in milliseconds.
 */
export function setCookie(key: string, value: string, expiresMs: number) {
	const date = new Date();
	date.setTime(date.getTime() + expiresMs);

	document.cookie =
		key + "=" + value + "; expires=" + date.toUTCString() + "; path=/";
}

/**
 * Get value of a cookie.
 * @param key Cookie key
 */
export function getCookie(key: string) {
	const parts = ("; " + document.cookie).split("; " + key + "=");

	if (parts && parts.length == 2) {
		const value = parts.pop();
		return value ? value.split(";").shift() : undefined;
	}
}

/**
 * Deletes a cookie.
 * @param key Cookie key
 */
export function deleteCookie(key: string) {
	const date = new Date();
	date.setTime(date.getTime() + -1 * 24 * 60 * 60 * 1000);

	document.cookie = key + "=; expires=" + date.toUTCString() + "; path=/";
}
