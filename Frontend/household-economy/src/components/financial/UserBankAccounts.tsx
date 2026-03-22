import { Card, Center, Flex, Heading } from "@chakra-ui/react";
import BasicWeb from "../BasicWeb";
import { API_URL } from "@/logic/data/URL";
import { getCookie } from "@/logic/CookiesUtils";
import { useEffect, useState } from "react";
import { Tooltip } from "../ui/tooltip";

interface BankAccount {
	id: number;
	bankAccountNumber: string;
	balance: number;
	currency: string;
	bankId: number;
}

function UserBankAccounts() {
	const [bankAccounts, setBankAccounts] = useState<BankAccount[]>();

	const getBankAccounts = async () => {
		let userDataStr = localStorage.getItem("loggedUser");
		if (!userDataStr) return;

		let userData = JSON.parse(userDataStr);
		if (!userData["id"]) return;

		const userToken = getCookie("userSession");
		if (!userToken) return;

		const response = await fetch(
			API_URL.GET_USER_BANK_ACCOUNTS.replace("{ownerId}", userData["id"]),
			{
				method: "GET",
				headers: [["Authorization", userToken]],
			}
		);

		const responseJSON = await response.json();
		const bankAccountsArr: BankAccount[] = [];

		console.log(responseJSON);

		if (responseJSON["code"] == 0) {
			responseJSON["data"].map((bankAccount: any) => {
				bankAccountsArr.push({
					id: bankAccount["id"],
					bankAccountNumber: bankAccount["bankAccountNumber"],
					balance: bankAccount["balance"],
					currency: bankAccount["currency"],
					bankId:
						bankAccount["bank"] && bankAccount["bank"]["id"]
							? bankAccount["bank"]["id"]
							: null,
				});
			});
		}

		setBankAccounts(bankAccountsArr);
	};

	useEffect(() => {
		getBankAccounts();
	}, []);

	return (
		<BasicWeb>
			<Center>
				<Flex
					gap="1"
					direction="column"
					align="center"
					w="2/3"
					mt="20px"
					pt="20px"
					pb="60px"
					minH="calc(80vh)"
					borderColor="orange.600"
					borderWidth="1px"
					borderStyle="solid"
					rounded="xl"
					shadow="2xl"
					backgroundColor="orange.300"
				>
					<Heading
						size="3xl"
						mb="25px"
					>
						Your Bank Accounts:{" "}
					</Heading>
					{bankAccounts?.map((bankAccount) => (
						<BankAccount
							name={bankAccount.bankAccountNumber}
							balance={bankAccount.balance}
							currency={bankAccount.currency}
							key={bankAccount.id}
						/>
					))}
				</Flex>
			</Center>
		</BasicWeb>
	);
}

export default UserBankAccounts;

interface BankAccountProps {
	name: string;
	balance: number;
	currency: string;
}

function BankAccount({ name, balance, currency }: BankAccountProps) {
	return (
		<Tooltip
			positioning={{ offset: { mainAxis: 4, crossAxis: 4 } }}
			openDelay={300}
			closeDelay={100}
			content="Click to view bank account details"
		>
			<Card.Root
				w="1/2"
				minW="3xl"
				maxH="100px"
				mt="5px"
				cursor="pointer"
				rounded="xl"
				borderColor="black"
				borderWidth="1px"
				overflow="hidden"
				shadow="2xl"
				onClick={() => {
					console.log("Bank account clicked: " + name);
				}}
			>
				<Card.Body
					pt="15px"
					pb="15px"
				>
					<p className="text-justify">{name}</p>
					<p className="text-right">
						{balance} {currency}
					</p>
				</Card.Body>
			</Card.Root>
		</Tooltip>
	);
}
