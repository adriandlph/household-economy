import { Flex } from "@chakra-ui/react";

function Footer() {
	return (
		<>
			<Flex
				background="black"
				borderTopWidth="1px"
				borderTopColor="black"
				borderTopStyle="solid"
				color="white"
			>
				<ul>
					<li>Contact</li>
					<li>Copyrights</li>
				</ul>
			</Flex>
		</>
	);
}

export default Footer;
