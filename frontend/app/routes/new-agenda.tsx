import { Form, redirect, useNavigation } from "react-router";
import type { Route } from "./+types/new-agenda";
import { Alert } from "~/components/Alert";
import { Button, ButtonLink } from "~/components/Button";
import { Container } from "~/components/Container";
import { TextAreaField, TextField } from "~/components/Field";
import { Hero } from "~/components/Hero";
import { api, ApiError } from "~/lib/api";

export function meta() {
  return [{ title: "Nova pauta · Votação Cooperativa" }];
}

export async function clientAction({ request }: Route.ClientActionArgs) {
  const form = await request.formData();
  const title = String(form.get("title") ?? "").trim();
  const description = String(form.get("description") ?? "").trim();

  if (!title) return { error: "Informe o título da pauta." };

  try {
    const created = await api.createAgenda(title, description);
    return redirect(`/pautas/${created.id}`);
  } catch (error) {
    return { error: error instanceof ApiError ? error.message : "Erro inesperado ao criar a pauta." };
  }
}

export default function NewAgendaPage({ actionData }: Route.ComponentProps) {
  const isSubmitting = useNavigation().state === "submitting";

  return (
    <>
      <Hero title="Nova pauta" subtitle="Cadastre o assunto que será deliberado em assembleia." />

      <Container className="pt-10 pb-16 md:pt-14 md:pb-22">
        <Form method="post" className="flex max-w-2xl flex-col gap-6">
          {actionData?.error && <Alert tone="error">{actionData.error}</Alert>}
          <TextField label="Título" name="title" required maxLength={255} placeholder="Ex.: Aprovação das contas do exercício 2025" autoFocus />
          <TextAreaField
            label="Descrição (opcional)"
            name="description"
            placeholder="Contexto da deliberação para os associados."
          />
          <div className="flex flex-wrap gap-3">
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Salvando…" : "Cadastrar pauta"}
            </Button>
            <ButtonLink to="/" variant="outline">
              Cancelar
            </ButtonLink>
          </div>
          <p className="text-sm text-muted">Depois de cadastrar, você poderá abrir a sessão de votação.</p>
        </Form>
      </Container>
    </>
  );
}
