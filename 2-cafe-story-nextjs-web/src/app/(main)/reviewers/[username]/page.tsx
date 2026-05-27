import { notFound } from "next/navigation";

export function generateStaticParams() {
  return [];
}

export default async function ReviewerProfileRoute({
  params,
}: PageProps<"/reviewers/[username]">) {
  await params;
  notFound();
}
