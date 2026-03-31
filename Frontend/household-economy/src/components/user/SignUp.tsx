import { useForm } from "react-hook-form";
import BasicWeb from "../BasicWeb";
import {
	Card,
	Field,
	Input,
	Button,
	Container,
	Group,
	Spacer,
} from "@chakra-ui/react";
import { API_URL, WEB_URL } from "@/logic/data/URL";
import { useNavigate } from "react-router";
import { useEffect, useState } from "react";
import { getCookie } from "@/logic/CookiesUtils";
import { PasswordInput } from "../ui/password-input";
import "@/logic/i18n";
import { useTranslation } from "react-i18next";

interface SignUpFormValues {
	firstName: string;
	lastName: string;
	email: string;
	username: string;
	password: string;
}

interface SignUpError {
	title: string;
	description: string;
}

function SignUp() {
	const navigate = useNavigate();
	const { register, handleSubmit } = useForm<SignUpFormValues>();
	const [signUpError, setSignUpError] = useState<SignUpError>();

	const [firstNameError, setFirstNameError] = useState<string>();
	const [lastNameError, setLastNameError] = useState<string>();
	const [emailError, setEmailError] = useState<string>();
	const [usernameError, setUsernameError] = useState<string>();
	const [passwordError, setPasswordError] = useState<string>();

	const { t, i18n } = useTranslation();

	useEffect(() => {
		i18n.changeLanguage(navigator.language);
	}, []);

	if (getCookie("userSession")) navigate(WEB_URL.BASE);

	const onSubmit = handleSubmit((data: SignUpFormValues) => {
		let notValidData: boolean = false;

		if (data.firstName.length === 0) {
			setFirstNameError("First name is required");
			notValidData = true;
		} else {
			setFirstNameError(undefined);
		}

		if (data.lastName.length === 0) {
			setLastNameError("Last name is required");
			notValidData = true;
		} else {
			setLastNameError(undefined);
		}

		if (data.email.length === 0) {
			setEmailError("Email is required");
			notValidData = true;
		} else {
			if (!data.email.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)) {
				setEmailError("Email is not valid");
				notValidData = true;
			} else {
				setEmailError(undefined);
			}
		}

		if (data.username.length === 0) {
			setUsernameError("Username is required");
			notValidData = true;
		} else {
			setUsernameError(undefined);
		}

		if (data.password.length === 0) {
			setPasswordError("Password is required");
			notValidData = true;
		} else {
			setPasswordError(undefined);
		}

		if (notValidData) return;

		(async () => {
			const userData =
				'{"firstName":"' +
				data.firstName +
				'", "lastName":"' +
				data.lastName +
				'", "email":"' +
				data.email +
				'", "username":"' +
				data.username +
				'", "password":"' +
				data.password +
				'"}';

			const response = await fetch(API_URL.SIGN_UP, {
				method: "POST",
				headers: {
					Accept: "application/json",
					"Content-Type": "application/json",
				},
				body: userData,
			});

			response.json().then((responseJSON) => {
				switch (responseJSON["code"]) {
					case 0:
						navigate(WEB_URL.LOGIN);
						break;
					case 5:
						setUsernameError("Username not defined.");
						break;
					case 6:
						setUsernameError("Username not valid.");
						break;
					case 7:
						setPasswordError("Password not defined.");
						break;
					case 8:
						setPasswordError("Password not valid.");
						break;
					case 9:
						setFirstNameError("First name not defined.");
						break;
					case 10:
						setEmailError("Email not defined.");
						break;
					case 11:
						setEmailError("Email not valid.");
						break;
					case 12:
						setSignUpError({
							title: "Sign Up error!",
							description: "Username or email already exists.",
						});
						break;
					default:
						setSignUpError({
							title: "Sign Up error!",
							description: responseJSON["message"],
						});
						break;
				}
			});
		})();
	});

	return (
		<BasicWeb>
			<Container
				maxW="2xl"
				pt="20"
			>
				<Card.Root
					maxW="2xl"
					overflow="hidden"
					shadow="2xl"
				>
					<form onSubmit={onSubmit}>
						<Card.Header fontWeight="bold">
							{t("signUp")}
						</Card.Header>
						<Card.Body>
							<Group>
								<Field.Root width="45%">
									<Field.Label>{t("firstName")}</Field.Label>
									<Input
										type="text"
										{...register("firstName")}
										placeholder={t("firstName")}
										size="md"
										colorPalette="orange"
									/>
									{firstNameError && (
										<ValidationError
											errorMsg={firstNameError}
										/>
									)}
								</Field.Root>
								<Field.Root>
									<Field.Label>{t("lastName")}</Field.Label>
									<Input
										type="text"
										{...register("lastName")}
										placeholder={t("lastName")}
										size="md"
										colorPalette="orange"
									/>
									{lastNameError && (
										<ValidationError
											errorMsg={lastNameError}
										/>
									)}
								</Field.Root>
							</Group>

							<Field.Root>
								<Spacer />
								<Spacer />
								<Spacer />
								<Spacer />
								<Field.Label>{t("email")}</Field.Label>
								<Input
									type="text" // Email validation is done in submit handler, so type is text to avoid default email validation and use custom error messages
									{...register("email")}
									placeholder={t("emailExample")}
									size="md"
									colorPalette="orange"
								/>
								{emailError && (
									<ValidationError errorMsg={emailError} />
								)}
							</Field.Root>
							<Field.Root>
								<Spacer />
								<Spacer />
								<Spacer />
								<Spacer />
								<Field.Label>{t("username")}</Field.Label>
								<Input
									type="text"
									{...register("username")}
									placeholder={t("username")}
									size="md"
									colorPalette="orange"
								/>
								{usernameError && (
									<ValidationError errorMsg={usernameError} />
								)}
							</Field.Root>
							<Field.Root>
								<Spacer />
								<Spacer />
								<Field.Label>{t("password")}</Field.Label>
								<PasswordInput
									{...register("password")}
									placeholder={t("password")}
									size="md"
									colorPalette="orange"
								/>
								{passwordError && (
									<ValidationError errorMsg={passwordError} />
								)}
							</Field.Root>
							{signUpError && (
								<Field.Root>
									<Field.Label></Field.Label>
									<Field.Label></Field.Label>
									<Field.Label
										className="text-sm text-red-500"
										fontStyle="italic"
									>
										{signUpError.description}
									</Field.Label>
								</Field.Root>
							)}
						</Card.Body>
						<Card.Footer>
							<Button
								colorPalette="orange"
								flex="1"
								type="submit"
							>
								{t("signUp")}
							</Button>
						</Card.Footer>
					</form>
				</Card.Root>
			</Container>
		</BasicWeb>
	);
}

export default SignUp;

interface ValidationErrorProps {
	errorMsg: string;
}

function ValidationError({ errorMsg }: ValidationErrorProps) {
	return (
		<Field.Label
			className="text-sm text-red-500"
			fontStyle="italic"
		>
			{errorMsg}
		</Field.Label>
	);
}
