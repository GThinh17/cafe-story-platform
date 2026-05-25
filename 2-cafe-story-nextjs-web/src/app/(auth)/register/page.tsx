import { AuthCard } from "@/components/auth/auth-card";
import { AuthLayoutShell } from "@/components/auth/auth-layout-shell";

export default function RegisterPage() {
  return (
    <AuthLayoutShell>
      <AuthCard mode="register" />
    </AuthLayoutShell>
  );
}
