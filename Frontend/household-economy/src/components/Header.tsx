import {
	AvatarGroup,
	Avatar,
	Flex,
	defineStyle,
	Button,
	Spacer,
	Image,
	HStack,
	Box,
} from "@chakra-ui/react";
import { useNavigate, type To } from "react-router";
import hero from "../assets/hero.png";
import { useContext, useEffect, type Key } from "react";
import { API_URL, WEB_URL } from "../logic/data/URL";
import { deleteCookie, getCookie } from "@/logic/CookiesUtils";
import { LoggedUserContext } from "@/logic/data/Contexts";

import "@/logic/i18n";
import { useTranslation } from "react-i18next";

interface HeaderProp {
	height: string;
}

function Header({ height }: HeaderProp) {
	const navigate = useNavigate();
	const { t, i18n } = useTranslation();

	const navElements = [
		{ name: t("home"), path: "/" },
		{ name: t("dashboard"), path: "/dashboard" },
		{ name: t("myBankAccounts"), path: "/financial/bankAccount" },
	];

	useEffect(() => {
		i18n.changeLanguage(navigator.language);
	}, []);

	return (
		<>
			<Flex
				minH={height}
				maxH={height}
				height={height}
				borderBottom="sm"
				background="white"
			>
				<Box className="w-xs"></Box>
				<Image
					src={hero}
					aspectRatio={25 / 9}
					className="h-full cursor-pointer"
					onClick={() => navigate(WEB_URL.BASE)}
				/>

				<Flex
					justify="start"
					gap="8"
					className="w-full"
				>
					<HStack>
						<Spacer />
						{navElements.map((elem) => (
							<NavButton
								key={elem.name}
								name={elem.name}
								path={elem.path}
							/>
						))}
					</HStack>
				</Flex>

				<Spacer />

				<UserHeaderSection />

				<Box className="w-xs"></Box>
			</Flex>
		</>
	);
}

export default Header;

function UserHeaderSection() {
	const loggedUserContext = useContext(LoggedUserContext);
	const navigate = useNavigate();
	const { t, i18n } = useTranslation();

	useEffect(() => {
		i18n.changeLanguage(navigator.language);

		const loadAvatar = async () => {
			try {
				const userToken =
					loggedUserContext.data && loggedUserContext.data.token
						? loggedUserContext.data.token
						: getCookie("userSession");

				if (userToken) {
					const response = await fetch(API_URL.GET_LOGGED_USER, {
						method: "GET",
						headers: [["Authorization", userToken]],
					});
					const responseJSON = await response.json();

					if (responseJSON["code"] == 0) {
						const userData = responseJSON["data"];

						loggedUserContext.setData({
							id: userData["id"],
							username: userData["username"],
							profilePictureUrl: userData["profilePictureUrl"],
							token: userToken,
						});
					} else {
						console.log(
							"Error (" +
								responseJSON["code"] +
								"): " +
								responseJSON["message"]
						);

						loggedUserContext.setData(undefined);
					}
				}
			} catch (err: any) {
				console.log("Exception: " + err.stack);
			}
		};

		(async () => await loadAvatar())();
	}, []);

	const avatarOutline = defineStyle({
		outlineWidth: "1px",
		outlineColor: "orange",
		outlineOffset: "0px",
		outlineStyle: "solid",
	});

	return (
		<>
			{loggedUserContext.data ? (
				<AvatarGroup>
					<Avatar.Root
						onClick={() => {
							deleteCookie("userSession");
							loggedUserContext.setData(undefined);
						}}
						shape="rounded"
						colorPalette="orange"
						css={avatarOutline}
					>
						<Avatar.Fallback
							name={loggedUserContext.data.username}
						/>
						<Avatar.Image
							src={loggedUserContext.data.profilePictureUrl}
						/>
					</Avatar.Root>
				</AvatarGroup>
			) : (
				<HStack>
					<Button
						variant="outline"
						colorPalette="orange"
						onClick={() => navigate("/user/login")}
					>
						{t("login")}
					</Button>
					<Button
						variant="solid"
						colorPalette="orange"
						onClick={() => navigate("/user/signUp")}
					>
						{t("signUp")}
					</Button>
				</HStack>
			)}
		</>
	);
}

interface NavButtonProps {
	name: Key;
	path: To;
}

function NavButton({ name, path }: NavButtonProps) {
	const navigate = useNavigate();

	const _hover = {
		color: "orange.600",
		_after: {
			width: "100%", // Expand to full width
		},
	};

	const _after = {
		content: '""',
		position: "absolute",
		bottom: 0,
		left: "50%", // Start from middle
		width: "0%", // Hidden initially
		height: "2px",
		bg: "orange.600",
		transition: "all 0.2s ease-in-out",
		transform: "translateX(-50%)", // Ensure it expands to sides
	};

	return (
		<Button
			variant="plain"
			rounded="0"
			_after={_after}
			_hover={_hover}
			onClick={() => navigate(path)}
		>
			{name}
		</Button>
	);
}
