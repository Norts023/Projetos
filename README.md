# Business Sim

Jogo de simulação empresarial single-player, inspirado em jogos como *Sim
Companies*, mas com mecânicas próprias: empréstimos negociados diretamente
com bancos (prazo, taxa e forma de pagamento) e compra livre de terrenos
(sem desbloqueio por nível). Roda 100% local, sem depender de servidor de
terceiros.

## Stack

- Python 3.11+, FastAPI + Uvicorn (API REST + serve o frontend)
- SQLAlchemy + SQLite (`data/game.db`)
- Frontend em HTML/CSS/JS puro (sem build step, sem dependências externas)

## Como rodar

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload
```

Acesse `http://localhost:8000` no navegador. O relógio do jogo roda em
tempo real (1 dia de jogo ≈ 144 segundos reais em velocidade 1x, ajustável
até 8x, com pausa e um botão de avançar 1 dia manualmente).

Os dados ficam salvos em `data/game.db` (SQLite, ignorado pelo git).
Para recomeçar o jogo do zero, apague esse arquivo com o servidor parado
(ou use o botão "Resetar jogo" no painel admin).

Em `http://localhost:8000/admin.html` fica um painel para desenvolvedor
editar diretamente caixa, score de crédito, estoque, preços de mercado,
o relógio do jogo e os concorrentes — útil para testes, sem passar pelas
regras normais do jogo.

A interface é organizada em abas com menu lateral (Visão Geral, Produção,
Mercado, Terrenos, Bancos, Financeiro, Concorrência), no estilo do painel
do Sim Companies.

## Mecânicas implementadas (v1)

- **Cadeias de produção multi-tier** (como no Sim Companies): 9 bens em 4
  tiers — matérias-primas (Minério de Ferro, Petróleo, Algodão) → processados
  (Aço, Plástico, Tecido) → componentes (Peças Mecânicas, Roupas) → produto
  final (Eletrodomésticos). Cada bem processado tem sua própria receita
  (fábrica/insumos/taxa de produção) e pode ser comprado ou vendido no
  mercado a qualquer momento ("produzir vs. comprar pronto").
- **Mercado dinâmico**: cada um dos 9 bens tem preço próprio que reage à
  oferta (produção/vendas) e demanda (compras + demanda de base simulada
  por tier), com tendência de retorno ao preço-base ao longo do tempo.
- **Terrenos**: compra livre a qualquer momento, com preço, custo de
  terraplanagem, bônus logístico (positivo ou negativo) e capacidade de
  fábricas por atributos próprios de cada terreno.
- **Bancos e empréstimos**: 3 bancos com taxas e limites diferentes;
  negociação de principal, prazo e forma de pagamento (PRICE = parcela
  fixa, SAC = parcela decrescente); score de crédito que sobe com
  pagamentos em dia e cai com atrasos, afetando taxas e limites futuros;
  quitação antecipada com desconto.
- **Painel financeiro nativo**: DRE, Balanço Patrimonial e Fluxo de Caixa
  calculados a partir do livro-razão (ledger) de transações, mais
  exportação do histórico completo em CSV.
- **API REST**: todos os módulos acima são expostos via `/api/...` para
  consulta e análise externa dos seus dados (ex: puxar `/api/finance/dre`
  ou o CSV para montar seus próprios relatórios).
- **Concorrentes simulados (IA leve)**: 3 empresas fictícias, cada uma
  com sua própria cadeia de produção (Aço, Roupas, Eletrodomésticos),
  produzem e vendem no mesmo mercado que você e crescem aos poucos;
  painel de "Concorrência" ranqueia todo mundo por valorização estimada,
  como um placar do Sim Companies.
- **Painel admin para testes** (`/admin.html`): editar caixa, score,
  estoque, preços de mercado, o relógio do jogo e os concorrentes
  diretamente, ou resetar o jogo do zero.

## Ideias para próximos passos (não implementadas ainda)

- Eventos aleatórios de economia (crises, escassez, picos de demanda)
- Metas/objetivos opcionais (marcos de progresso)
- Reputação de mercado além do score de crédito bancário
- Upgrades de nível de fábrica (aumentar taxa de produção)

## Estrutura do projeto

```
app/
  config.py          Constantes de balanceamento do jogo
  models.py           Modelos SQLAlchemy
  seed.py             Dados iniciais (bancos, terrenos, mercado)
  simulation/          Motor de simulação (economia, bancos, terrenos, financeiro, relógio)
  api/                 Rotas FastAPI por módulo
  main.py              Ponto de entrada da aplicação
static/                Frontend (HTML/CSS/JS)
data/                  Banco de dados SQLite (gerado em runtime)
```
