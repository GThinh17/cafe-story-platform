import type { FormEventHandler } from "react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import type { ReviewComposerModel, ReviewDraftHint } from "@/types/review";

type CreatePostFormProps = {
  composer: ReviewComposerModel;
  hints: ReviewDraftHint[];
  onSubmit?: FormEventHandler<HTMLFormElement>;
};

const ratingValues = [1, 2, 3, 4, 5];

export function CreatePostForm({
  composer,
  hints,
  onSubmit,
}: CreatePostFormProps) {
  return (
    <Card asChild>
      <form className="overflow-hidden" onSubmit={onSubmit}>
      <CardHeader className="flex items-start justify-between gap-4 border-b border-border px-6 py-5">
        <div className="min-w-0">
          <p className="text-xs font-black uppercase tracking-[0.12em] text-primary">
            Reviewer
          </p>
          <CardTitle className="mt-2 text-2xl font-black text-foreground">
            {composer.title}
          </CardTitle>
          <CardDescription className="mt-1 max-w-[440px] text-sm leading-6">
            {composer.subtitle}
          </CardDescription>
        </div>
        <Badge className="shrink-0 px-3 py-2 text-xs font-black" variant="secondary">
          Draft
        </Badge>
      </CardHeader>

      <CardContent className="space-y-6 p-6">
        <Card className="overflow-hidden bg-background shadow-none">
          <img
            alt={`${composer.selectedCafe} cafe preview`}
            className="aspect-[16/10] w-full object-cover"
            decoding="async"
            src={composer.previewImage}
          />
          <div className="flex flex-wrap items-center justify-between gap-3 px-4 py-3">
            <div className="min-w-0">
              <p className="truncate text-sm font-black">
                {composer.selectedCafe}
              </p>
              <p className="text-xs text-muted">{composer.location}</p>
            </div>
            <Button
              className="text-xs font-black"
              size="sm"
              variant="outline"
              type="button"
            >
              Change photo
            </Button>
          </div>
        </Card>

        <FieldGroup className="grid gap-4 sm:grid-cols-2">
          <Field className="sm:col-span-2">
            <FieldLabel className="text-sm font-black">Cafe</FieldLabel>
            <Input
              defaultValue={composer.selectedCafe}
              type="text"
            />
          </Field>

          <Field>
            <FieldLabel className="text-sm font-black">Visit type</FieldLabel>
            <Input
              defaultValue={composer.visitType}
              type="text"
            />
          </Field>

          <Field>
            <FieldLabel className="text-sm font-black">Spend</FieldLabel>
            <Input
              defaultValue={composer.spend}
              type="text"
            />
          </Field>
        </FieldGroup>

        <section className="space-y-3">
          <p className="text-sm font-black">Rating</p>
          <div className="grid grid-cols-5 gap-2">
            {ratingValues.map((rating) => (
              <Badge
                className={`flex h-11 items-center justify-center rounded-md border text-sm font-black transition ${
                  rating <= composer.rating
                    ? "border-rating bg-rating/15 text-rating"
                    : "border-border bg-surface text-muted"
                }`}
                key={rating}
                variant="outline"
              >
                {rating}
              </Badge>
            ))}
          </div>
        </section>

        <Field>
          <FieldLabel className="text-sm font-black">Review</FieldLabel>
          <Textarea
            className="min-h-36"
            defaultValue={composer.caption}
          />
        </Field>

        <section className="space-y-3">
          <div className="flex items-center gap-2">
            <Badge className="grid size-6 place-items-center rounded-md bg-primary/10 p-0 text-xs font-black text-primary">
              AI
            </Badge>
            <h2 className="text-sm font-black">AI tags</h2>
          </div>
          <div className="flex flex-wrap gap-2">
            {composer.aiTags.map((tag) => (
              <Badge className="px-3 py-2 text-xs font-black" key={tag} variant="secondary">
                {tag}
              </Badge>
            ))}
          </div>
        </section>

        <FieldGroup className="grid grid-cols-2 gap-3">
          {hints.map((hint) => (
            <Field key={hint.label}>
              <FieldLabel className="text-xs font-black uppercase tracking-[0.08em] text-muted">
                {hint.label}
              </FieldLabel>
              <Input
                className="h-11 px-3"
                defaultValue={hint.value}
                type="text"
              />
            </Field>
          ))}
        </FieldGroup>

        <section className="flex flex-wrap items-center justify-between gap-3 rounded-md border border-border bg-background px-4 py-3">
          <div className="min-w-0">
            <p className="text-sm font-black">{composer.moderation.label}</p>
            <p className="text-xs text-muted">{composer.moderation.status}</p>
          </div>
          <Badge className="bg-primary/10 px-3 py-2 text-xs font-black text-primary">
            Public
          </Badge>
        </section>
      </CardContent>

      <CardFooter className="flex flex-wrap items-center justify-end gap-3 border-t border-border bg-background px-6 py-4">
        <Button
          className="h-11 text-sm font-black"
          variant="outline"
          type="button"
        >
          Save draft
        </Button>
        <Button
          className="h-11 px-5 text-sm font-black"
          type="submit"
        >
          Publish review
        </Button>
      </CardFooter>
      </form>
    </Card>
  );
}
