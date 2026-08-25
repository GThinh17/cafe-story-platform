import { redirect } from "next/navigation";

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

  redirect(paymentId
    ? `/ads?paymentId=${encodeURIComponent(paymentId)}`
    : "/ads");
}
