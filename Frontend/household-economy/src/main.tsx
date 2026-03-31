import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App";
import { BrowserRouter, Routes, Route } from "react-router";
import { Provider } from "@/components/ui/provider";
import Login from "./components/user/Login";
import SignUp from "./components/user/SignUp";
import UserBankAccounts from "./components/financial/UserBankAccounts";
import { LoggedUserContext } from "./logic/data/Contexts";
import { useState } from "react";
import type { ContextMutableData, LoggedUser } from "./logic/data/Contexts";

function Root() {
	const [loggedUser, setLoggedUser] = useState<LoggedUser | undefined>();

	const loggedUserContext: ContextMutableData<LoggedUser> = {
		data: loggedUser,
		setData: setLoggedUser,
	};

	return (
		<BrowserRouter>
			<LoggedUserContext.Provider value={loggedUserContext}>
				<Provider>
					<Routes>
						<Route
							path="/"
							element={<App />}
						/>
						<Route path="user">
							<Route
								path="login"
								element={<Login />}
							/>
							<Route
								path="signUp"
								element={<SignUp />}
							/>
						</Route>

						<Route path="financial">
							<Route
								path="bankAccount"
								element={<UserBankAccounts />}
							/>
						</Route>
					</Routes>
				</Provider>
			</LoggedUserContext.Provider>
		</BrowserRouter>
	);
}

createRoot(document.getElementById("root")!).render(<Root />);
