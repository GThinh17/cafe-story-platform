"use client";

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
import { useRouter, useSearchParams } from "next/navigation";
import { FormEvent, useState } from "react";
import { ApiError } from "@/lib/api/client";
import { login, register } from "@/lib/api/auth";
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
    autoComplete: "username",
    label: "Email or username",
    name: "identifier",
    placeholder: "hello@cafestory.com",
    type: "text",
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
    name: "userFullName",
    placeholder: "John Doe",
    type: "text",
  },
  {
    autoComplete: "email",
    label: "Email address",
    name: "userEmail",
    placeholder: "hello@cafestory.com",
    type: "email",
  },
  {
    autoComplete: "username",
    label: "Username",
    name: "userName",
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
    return registerFields;
  }

  return sharedFields;
}

function getSafeNextPath(nextPath: string | null) {
  if (!nextPath || !nextPath.startsWith("/") || nextPath.startsWith("//")) {
    return "/";
  }

  return nextPath;
}

function buildSwitchHref(
  baseHref: string,
  nextPath: string,
  reason: string | null,
) {
  const params = new URLSearchParams();

  if (reason) {
    params.set("reason", reason);
  }

  if (nextPath !== "/") {
    params.set("next", nextPath);
  }

  const query = params.toString();

  return query ? `${baseHref}?${query}` : baseHref;
}

export function AuthCard({ mode }: AuthCardProps) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const copy = authCopy[mode];
  const fields = getFields(mode);
  const [errorMessage, setErrorMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const reason = searchParams.get("reason");
  const nextPath = getSafeNextPath(searchParams.get("next"));
  const switchHref = buildSwitchHref(copy.switchHref, nextPath, reason);
  const shouldShowAuthNotice = reason === "auth_required";

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErrorMessage("");
    setIsSubmitting(true);

    const formData = new FormData(event.currentTarget);
    const password = String(formData.get("password") ?? "");

    try {
      if (mode === "login") {
        await login({
          identifier: String(formData.get("identifier") ?? ""),
          password,
        });
      } else {
        const userEmail = String(formData.get("userEmail") ?? "");

        await register({
          userName: String(formData.get("userName") ?? ""),
          userFullName: String(formData.get("userFullName") ?? ""),
          userEmail,
          password,
        });
        await login({
          identifier: userEmail,
          password,
        });
      }

      router.push(nextPath);
      router.refresh();
    } catch (error) {
      setErrorMessage(
        error instanceof ApiError
          ? error.message
          : "Something went wrong. Please try again.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

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
      {shouldShowAuthNotice ? (
        <p className="mt-6 rounded border border-primary/20 bg-primary/10 px-4 py-3 text-sm font-bold leading-6 text-primary-strong">
          Please sign in to continue.
        </p>
      ) : null}

      <form className="mt-10 space-y-6" onSubmit={handleSubmit}>
        <div className="space-y-6">
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
        {errorMessage ? (
          <p className="rounded border border-accent/25 bg-accent/10 px-4 py-3 text-sm font-medium text-espresso">
            {errorMessage}
          </p>
        ) : null}

        <button
          className="mt-2 flex h-[62px] w-full items-center justify-center gap-3 rounded bg-espresso px-10 text-sm font-black uppercase tracking-[0.08em] text-white shadow-[0_20px_25px_-5px_rgba(39,19,16,0.05),0_8px_10px_-6px_rgba(39,19,16,0.05)] transition hover:bg-[#3a201b] focus:outline-none focus:ring-4 focus:ring-espresso/15 disabled:cursor-not-allowed disabled:opacity-65"
          disabled={isSubmitting}
          type="submit"
        >
          {isSubmitting ? "Please wait..." : copy.submitLabel}
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
        <Link className="font-black text-espresso no-underline" href={switchHref}>
          {copy.switchLabel}
        </Link>
      </div>
    </section>
  );
}
