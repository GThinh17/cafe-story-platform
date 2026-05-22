export type AuthMode = "login" | "register";

export type AuthField = {
  autoComplete: string;
  label: string;
  name: string;
  placeholder: string;
  type: "email" | "password" | "text";
};

export type AuthFormCopy = {
  eyebrow: string;
  title: string;
  description: string;
  submitLabel: string;
  switchPrompt: string;
  switchHref: string;
  switchLabel: string;
};
