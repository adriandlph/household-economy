import Header from "./Header";
import Footer from "./Footer";
import type { ReactNode } from "react";
import { Box } from "@chakra-ui/react";

interface BasicWebProps {
	children?: ReactNode | ReactNode[];
}

function BasicWeb({ children }: BasicWebProps) {
	const headerHeight = "60px";
	return (
		<>
			<Header height={headerHeight} />
			<Box
				minHeight="calc(100vh)"
				backgroundColor="#fafafa"
			>
				{children}
			</Box>
			<Footer />
		</>
	);
}

export default BasicWeb;
