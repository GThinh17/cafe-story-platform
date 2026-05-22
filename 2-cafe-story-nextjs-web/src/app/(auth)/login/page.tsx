import { AuthCard } from "@/components/auth/auth-card";
import { AuthLayoutShell } from "@/components/auth/auth-layout-shell";

export default function LoginPage() {
  return (
    <AuthLayoutShell>
      <AuthCard mode="login" />
    </AuthLayoutShell>
  );
}
