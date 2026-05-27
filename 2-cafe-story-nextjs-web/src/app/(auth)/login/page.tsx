import { AuthCard } from "@/components/auth/auth-card";
import { AuthLayoutShell } from "@/components/auth/auth-layout-shell";
import { Suspense } from "react";

export default function LoginPage() {
  return (
    <AuthLayoutShell>
      <Suspense fallback={null}>
        <AuthCard mode="login" />
      </Suspense>
    </AuthLayoutShell>
  );
}
