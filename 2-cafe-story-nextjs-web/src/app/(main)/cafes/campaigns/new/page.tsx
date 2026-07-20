import { SettingsShell } from "@/components/settings/settings-shell";
import { CampaignCreateForm } from "@/components/cafe/campaign-create-form";

type CampaignCreateSearchParams = {
  paymentId?: string | string[];
};

type CampaignCreatePageProps = {
  searchParams: Promise<CampaignCreateSearchParams>;
};

export default async function CampaignCreatePage({
  searchParams,
}: CampaignCreatePageProps) {
  const params = await searchParams;
  const paymentId = Array.isArray(params.paymentId)
    ? params.paymentId[0]
    : params.paymentId;

  return (
    <SettingsShell>
      <CampaignCreateForm paymentId={paymentId ?? ""} />
    </SettingsShell>
  );
}
