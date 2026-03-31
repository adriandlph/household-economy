export interface Result<T> {
	data?: T;
	error?: Error;
}

export interface Error {
	code: number;
	message: string;
}
