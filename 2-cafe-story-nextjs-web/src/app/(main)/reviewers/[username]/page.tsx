import { notFound } from "next/navigation";

type ReviewerProfileRouteProps = {
  params: Promise<{
    username: string;
  }>;
};

export function generateStaticParams() {
  return [];
}

export default async function ReviewerProfileRoute({
  params,
}: ReviewerProfileRouteProps) {
  await params;
  notFound();
}
