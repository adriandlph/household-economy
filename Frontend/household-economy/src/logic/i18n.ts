import i18n from "i18next";
import { initReactI18next } from "react-i18next";

i18n.use(initReactI18next).init({
    debug: true,
    fallbackLng: "es",
    resources: {
        en: {
            translation: {
                login: "Login",
                signUp: "Sign Up",
                username: "Username",
                password: "Password",
                email: "Email",
                firstName: "First name",
                lastName: "Last name",
                emailExample: "myEmail@company.com",
                loginError: "Login error!",
                home: "Home",
                dashboard: "Dashboard",
                myBankAccounts: "My bank accounts",
            }
        },
        es: {
            translation: {
                login: "Iniciar sesión",
                signUp: "Registrarse",
                username: "Nombre de usuario",
                password: "Contraseña",
                email: "Correo electrónico",
                firstName: "Nombre",
                lastName: "Apellidos",
                emailExample: "miCorreo@empresa.com",
                loginError: "Error de inicio de sesión",
                home: "Inicio",
                dashboard: "Dashboard",
                myBankAccounts: "Mis cuentas bancarias",
            }
        },

    }
});

export default i18n;