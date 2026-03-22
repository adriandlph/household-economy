import { createContext } from "react";

export interface LoggedUser {
	id: number;
	username: string;
	profilePictureUrl?: string;
	token: string;
}

export interface ContextMutableData<T> {
	data?: T;
	setData: (data: T | undefined) => void;
}

export const LoggedUserContext = createContext<ContextMutableData<LoggedUser>>({
	setData: (_data: LoggedUser | undefined) => {},
});
