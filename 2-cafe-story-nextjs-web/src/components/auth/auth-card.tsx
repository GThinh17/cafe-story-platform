import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Field,
  FieldContent,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import type { AuthField, AuthFormCopy, AuthMode } from "@/types/auth";

type AuthCardProps = {
  mode: AuthMode;
};

const authCopy: Record<AuthMode, AuthFormCopy> = {
  login: {
    eyebrow: "",
    title: "Welcome back",
    description: "Please enter your details to access your curated reviews.",
    submitLabel: "Sign in",
    switchPrompt: "Don't have an account?",
    switchHref: "/register",
    switchLabel: "Join the club",
  },
  register: {
    eyebrow: "Cafe Story",
    title: "Create your account",
    description: "Start your journey through the world's best cafes.",
    submitLabel: "Create account",
    switchPrompt: "Already have an account?",
    switchHref: "/login",
    switchLabel: "Sign in",
  },
};

const sharedFields: AuthField[] = [
  {
    autoComplete: "email",
    label: "Email address",
    name: "email",
    placeholder: "hello@cafestory.com",
    type: "email",
  },
  {
    autoComplete: "current-password",
    label: "Password",
    name: "password",
    placeholder: "Enter your password",
    type: "password",
  },
];

const registerFields: AuthField[] = [
  {
    autoComplete: "name",
    label: "Full name",
    name: "name",
    placeholder: "John Doe",
    type: "text",
  },
  {
    autoComplete: "username",
    label: "Username",
    name: "username",
    placeholder: "cafestory_user",
    type: "text",
  },
  {
    autoComplete: "new-password",
    label: "Password",
    name: "password",
    placeholder: "Create a password",
    type: "password",
  },
];

function getFields(mode: AuthMode) {
  if (mode === "register") {
    return [
      registerFields[0],
      sharedFields[0],
      registerFields[1],
      registerFields[2],
    ];
  }

  return sharedFields;
}

export function AuthCard({ mode }: AuthCardProps) {
  const copy = authCopy[mode];
  const fields = getFields(mode);

  return (
    <section className="w-full">
      <div className="flex flex-col gap-2">
        {copy.eyebrow ? (
          <p className="text-lg font-black text-espresso">{copy.eyebrow}</p>
        ) : null}
        <h1 className="text-3xl font-black leading-tight text-espresso sm:text-[32px]">
          {copy.title}
        </h1>
        <p className="max-w-[360px] text-base leading-6 text-coffee-muted">
          {copy.description}
        </p>
      </div>

      <form className="mt-10 flex flex-col gap-6">
        <FieldGroup>
          {fields.map((field) => (
            <Field key={field.name}>
              <div className="flex items-center justify-between gap-3">
                <FieldLabel
                  className="text-xs font-black uppercase tracking-[0.08em] text-coffee-muted"
                  htmlFor={field.name}
                >
                {field.label}
                </FieldLabel>
                {mode === "login" && field.name === "password" ? (
                  <a
                    className="text-xs font-bold normal-case tracking-normal text-coffee-muted no-underline transition hover:text-espresso"
                    href="#"
                  >
                    Forgot Password?
                  </a>
                ) : null}
              </div>
              <Input
                autoComplete={field.autoComplete}
                className="rounded-none border-0 border-b border-line-soft bg-transparent px-0 text-base text-espresso placeholder:text-line-soft focus:border-espresso"
                id={field.name}
                name={field.name}
                placeholder={field.placeholder}
                required
                type={field.type}
              />
            </Field>
          ))}
        </FieldGroup>

        {mode === "register" ? (
          <Field orientation="horizontal">
            <Checkbox
              className="mt-0.5"
              name="terms"
              required
            />
            <FieldContent>
              <FieldLabel className="text-xs leading-5 text-coffee-muted">
                I agree to the Terms of Service and Privacy Policy.
              </FieldLabel>
            </FieldContent>
          </Field>
        ) : null}

        <Button
          className="mt-2 flex h-[62px] w-full items-center justify-center gap-3 rounded bg-espresso px-10 text-sm font-black uppercase tracking-[0.08em] text-white shadow-[0_20px_25px_-5px_rgba(39,19,16,0.05),0_8px_10px_-6px_rgba(39,19,16,0.05)] transition hover:bg-[#3a201b] focus:outline-none focus:ring-4 focus:ring-espresso/15"
          type="submit"
        >
          {copy.submitLabel}
          <span aria-hidden="true">-&gt;</span>
        </Button>
      </form>

      <div className="my-10 flex items-center gap-4">
        <Separator className="flex-1" />
        <span className="text-xs uppercase text-coffee-muted">
          Or continue with
        </span>
        <Separator className="flex-1" />
      </div>

      <div className="grid grid-cols-2 gap-4">
        <Button
          className="flex h-[54px] items-center justify-center gap-3 rounded border border-line-soft bg-surface text-sm font-black uppercase tracking-[0.08em] text-foreground transition hover:border-espresso"
          type="button"
          variant="outline"
        >
          <span className="grid size-5 place-items-center rounded-full border border-line-soft text-xs">
            G
          </span>
          Google
        </Button>
        <Button
          className="flex h-[54px] items-center justify-center gap-3 rounded border border-line-soft bg-surface text-sm font-black uppercase tracking-[0.08em] text-foreground transition hover:border-espresso"
          type="button"
          variant="outline"
        >
          <span className="text-xs font-black">iOS</span>
          Apple
        </Button>
      </div>

      <div className="mt-16 flex items-center justify-center gap-1 text-center text-base text-coffee-muted">
        <span>{copy.switchPrompt}</span>
        <Link className="font-black text-espresso no-underline" href={copy.switchHref}>
          {copy.switchLabel}
        </Link>
      </div>
    </section>
  );
}
