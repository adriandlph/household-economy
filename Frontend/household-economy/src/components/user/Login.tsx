import { Button, Card, Container, Field, Input } from "@chakra-ui/react";
import BasicWeb from "../BasicWeb";
import { useForm } from "react-hook-form";
import { API_URL, WEB_URL } from "../../logic/data/URL";
import { useEffect, useState } from "react";
import { getCookie, setCookie } from "@/logic/CookiesUtils";
import { Days2Milliseconds } from "@/logic/DateUtils";
import { useNavigate } from "react-router";
import { PasswordInput } from "../ui/password-input";

import "@/logic/i18n";
import { useTranslation } from "react-i18next";

interface LoginFormValues {
	username: string;
	password: string;
}

interface LoginError {
	title: string;
	description: string;
}

function Login() {
	const navigate = useNavigate();
	const { register, handleSubmit } = useForm<LoginFormValues>();
	const [loginError, setLoginError] = useState<LoginError>();
	const { t, i18n } = useTranslation();

	useEffect(() => {
		i18n.changeLanguage(navigator.language);
	}, []);

	if (getCookie("userSession")) navigate(WEB_URL.BASE);

	const onSubmit = handleSubmit((data: LoginFormValues) => {
		(async () => {
			const userData =
				'{"username":"' +
				data.username +
				'", "password":"' +
				data.password +
				'"}';

			const response = await fetch(API_URL.LOGIN, {
				method: "POST",
				headers: {
					Accept: "application/json",
					"Content-Type": "application/json",
				},
				body: userData,
			});

			const responseJSON = await response.json();
			switch (responseJSON["code"]) {
				case 0: // Ok
					setCookie(
						"userSession",
						responseJSON["data"]["token"],
						Days2Milliseconds(7)
					);
					navigate("/");
					break;
				default:
					setLoginError({
						title: t("loginError"),
						description: responseJSON["message"],
					});
					break;
			}

			console.log("responseJSON: " + JSON.stringify(responseJSON));
		})();
	});

	return (
		<BasicWeb>
			<Container
				maxW="lg"
				pt="20"
			>
				<Card.Root
					maxW="lg"
					overflow="hidden"
					shadow="2xl"
				>
					<form onSubmit={onSubmit}>
						<Card.Header fontWeight="bold">
							{t("login")}
						</Card.Header>
						<Card.Body>
							<Field.Root>
								<Field.Label></Field.Label>
								<Input
									type="text"
									placeholder={t("login")}
									{...register("username")}
									size="md"
									colorPalette="orange"
								/>
							</Field.Root>
							<Field.Root>
								<Field.Label></Field.Label>
								<PasswordInput
									{...register("password")}
									placeholder={t("password")}
									size="md"
									colorPalette="orange"
								/>
							</Field.Root>

							{loginError && (
								<Field.Root>
									<Field.Label></Field.Label>
									<Field.Label></Field.Label>
									<Field.Label
										className="text-sm text-red-500"
										fontStyle="italic"
									>
										{loginError.description}
									</Field.Label>
								</Field.Root>
							)}
						</Card.Body>
						<Card.Footer>
							<Button
								colorPalette="orange"
								flex="1"
								/* isLoading={props.isSubmitting}*/
								type="submit"
							>
								{t("login")}
							</Button>
						</Card.Footer>
					</form>
				</Card.Root>
			</Container>
		</BasicWeb>
	);
}

export default Login;
