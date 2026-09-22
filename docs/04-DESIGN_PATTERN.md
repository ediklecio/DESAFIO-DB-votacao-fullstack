# Design Pattern — referência visual (Sicoob Credicom / Blog)

> Extraído de 2 capturas de tela (topo da página de post e rodapé). Cores medidas por amostragem de pixels; tipografia e medidas são estimativas visuais (viewport ≈ 1280px).
> Uso: referência de estilo para o frontend React do desafio de votação. Não copiar logotipo, marca ou textos institucionais.

---

## 1. Paleta de cores

| Token | Hex | Onde aparece |
|---|---|---|
| `--color-primary` | `#00AE9D` | Títulos de conteúdo (H1 do post), títulos de coluna do rodapé |
| `--color-primary-600` | `#00A091` | Barra superior (segmento), fundo do hero, bordas dos botões de acessibilidade |
| `--color-primary-700` | `#009285` | Aba ativa da barra superior ("Para você") |
| `--color-dark` | `#003641` | Header principal, rodapé, botões flutuantes de acessibilidade |
| `--color-accent` | `#7DB61C` | Destaques de texto sobre fundo escuro (ex.: cooperativa selecionada), botão de cookies |
| `--color-accent-light` | `#A1BE3E` | Detalhes do logotipo |
| `--color-text` | `#52535C` | Títulos de cards / texto corrido sobre branco |
| `--color-text-muted` | `#809AA0` | Ícones/estados inativos sobre fundo escuro |
| `--color-surface` | `#FFFFFF` | Fundo de conteúdo, cards |
| `--color-border` | `#F2F2F2` / `#F6F6F8` | Divisores internos de card |
| `--color-on-dark` | `#FFFFFF` | Texto de menu, links e telefones sobre `--color-dark` |

**Regras de uso**

- Duas "camadas" de marca: **teal** (`primary`) para identidade/ênfase e **petróleo** (`dark`) para estrutura (header/rodapé).
- Texto sobre `dark` é sempre branco; títulos de seção sobre `dark` usam `primary`.
- O verde-limão (`accent`) é usado com parcimônia — um destaque por contexto.
- Conteúdo principal sempre em fundo branco, com H1 em `primary`.

```css
:root {
  --color-primary: #00AE9D;
  --color-primary-600: #00A091;
  --color-primary-700: #009285;
  --color-dark: #003641;
  --color-accent: #7DB61C;
  --color-accent-light: #A1BE3E;
  --color-text: #52535C;
  --color-text-muted: #809AA0;
  --color-surface: #FFFFFF;
  --color-border: #F2F2F2;
  --color-on-dark: #FFFFFF;
}
```

---

## 2. Tipografia

- **Família:** sans-serif humanista, cantos suaves e peso forte nos títulos (visual próximo de *Asap* / *Mukta*). Fallback sugerido: `'Asap', 'Segoe UI', Roboto, sans-serif`.
- Sem serifas em toda a interface; itálico apenas para notas explicativas (ex.: aviso da Ouvidoria).

| Estilo | Tamanho aprox. | Peso | Cor | Exemplo |
|---|---|---|---|---|
| Display / hero | 48px | 700 | branco (com leve sombra) | "Blog" |
| H1 conteúdo | 32–34px | 700 | `primary` | "Você sabe o que são cooperativas?" |
| Título de card | 18–20px | 600 | `text` | "Por que contratar um seguro de vida?…" |
| Título de coluna (rodapé) | 17–18px | 700 | `primary` | "Links Úteis" |
| Menu principal | 16px | 500 | branco | "O Sicoob", "Produtos" |
| Corpo / links | 15–16px | 400–500 | branco (rodapé) / `text` | "Fale Conosco" |
| Dado de destaque | 15–16px | 700 | branco | "0800 724 4420" |
| Barra utilitária | 12–13px | 400–700 | branco | "Para você" (ativo em negrito) |

Line-height: ~1.5 para textos; ~1.3 para títulos de múltiplas linhas.

---

## 3. Layout e grid

- **Container:** centralizado, largura útil ≈ **940–1000px** (conteúdo inicia ~165px em viewport de 1280).
- Imagem de destaque do post ocupa a largura total do container.
- **Rodapé:** 5 colunas (3 de links estreitas + 2 de contato mais largas), alinhadas ao topo.
- **Cards de posts relacionados:** grid de 2–3 colunas, gap ≈ 24px.
- Espaçamento vertical generoso entre seções (≈ 64–120px); escala sugerida de 8px: `4, 8, 16, 24, 32, 48, 64, 96, 128`.

---

## 4. Componentes

### 4.1 Barra utilitária (topo)
- Altura ≈ 32px, fundo `primary-600`.
- Abas à esquerda ("Para você" / "Para sua Empresa"); aba ativa com fundo `primary-700` e texto em negrito.
- À direita: link de acesso à conta com ícone de linha (chave).

### 4.2 Header principal
- Fundo `dark`, altura ≈ 130px, duas linhas:
  1. Logotipo + seletor de contexto (ícone de pin + rótulo branco em negrito + valor em `accent` + chevron).
  2. Menu horizontal (itens brancos, espaçamento ≈ 40px) + ícone de busca (lupa, traço fino).
- Sem sombras; separação por cor sólida.

### 4.3 Hero / banner de página
- Fundo `primary-600` com **padrão geométrico de triângulos** em traço fino branco semitransparente (~15–20% opacidade), triângulos de cantos arredondados sobrepostos.
- Altura ≈ 260px; título display branco alinhado ao container, à esquerda.
- O mesmo padrão reaparece como faixa final abaixo do rodapé — é o elemento gráfico assinatura.

### 4.4 Cabeçalho de conteúdo
- Botão "voltar" (chevron `<` fino, cor `dark`) alinhado à esquerda, acima do título.
- H1 centralizado em `primary`, seguido da imagem de destaque colada ao título (≈ 12px).

### 4.5 Card
- Fundo branco, `border-radius` ≈ 6–8px.
- Sombra suave: `0 2px 6px rgba(0, 0, 0, 0.15)`; estado hover com sombra levemente mais forte.
- Padding interno ≈ 24px; título em até 3 linhas; divisor horizontal fino (`border`) antes do rodapé do card.

### 4.6 Rodapé
- Fundo `dark`, padding vertical ≈ 64px.
- Colunas com título `primary` + lista de links brancos (espaçamento ≈ 20px entre itens, indentação leve de 8px).
- Blocos de contato: número em negrito + descrição em regular na linha abaixo.
- Ícones de redes sociais monocromáticos brancos, ≈ 16px, em linha.

### 4.7 Elementos flutuantes
- **Barra de acessibilidade** fixa à direita, centro vertical: 3 botões quadrados ≈ 34px empilhados, fundo `dark`, borda `primary-600`, raio 6px à esquerda, ícones brancos (Libras, audiodescrição, alto contraste/ocultar).
- **Botão de cookies** fixo no canto inferior esquerdo: círculo ≈ 48px, fundo `accent`, borda branca, ícone branco.

---

## 5. Iconografia

- Ícones **de linha (outline)**, traço fino (~1.5px), monocromáticos (branco ou `dark`).
- Chevrons finos para navegação e dropdown.
- Sugestão de biblioteca equivalente: Lucide / Feather / Tabler.

---

## 6. Princípios de estilo

1. **Contraste estrutural:** blocos sólidos de `dark` emolduram o conteúdo branco.
2. **Cor como hierarquia:** títulos sempre em `primary`, nunca em preto.
3. **Assinatura geométrica:** padrão de triângulos só em áreas de marca (hero e faixa final) — nunca sob texto corrido.
4. **Acessibilidade visível:** controles de acessibilidade sempre à mostra; texto branco sobre `dark` (contraste ≈ 13:1).
5. **Superfícies simples:** poucas sombras, raios pequenos, sem gradientes.

> ⚠️ Contraste: branco sobre `#00A091` fica ≈ 3.2:1 — ok apenas para texto grande (≥ 24px ou 18.7px bold). Para texto pequeno sobre teal, usar `dark` ou escurecer o fundo.

---

## 7. Mapeamento sugerido para o frontend (React)

| Tela do desafio | Padrão aplicado |
|---|---|
| Layout base | Barra utilitária + header `dark` + rodapé `dark` com faixa geométrica |
| Lista de pautas | Hero teal com título "Pautas" + grid de cards |
| Detalhe da pauta / sessão | Botão voltar + H1 `primary` centralizado |
| Votar (Sim/Não) | Botão primário `primary` (texto `dark` ou branco em ≥ 18px bold); secundário com contorno `dark` |
| Resultado | Card com números em negrito; `accent` para o resultado vencedor |
| Estados/feedback | Sucesso `accent`, informação `primary`, erro — definir vermelho próprio (não presente na referência) |
