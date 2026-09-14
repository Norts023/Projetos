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

### Jogar pelo celular (mesma rede Wi-Fi)

O jogo continua rodando no computador, mas dá pra acessar pelo navegador
do celular se os dois estiverem na mesma rede Wi-Fi:

1. Rode o servidor aceitando conexões de outros dispositivos:
   ```bash
   uvicorn app.main:app --host 0.0.0.0 --port 8000
   ```
2. Descubra o IP local do computador:
   - Windows: `ipconfig` (campo "Endereço IPv4")
   - Mac/Linux: `ifconfig` ou `ip addr` (algo como `192.168.0.x`)
3. No celular, com o Wi-Fi conectado à mesma rede, abra no navegador:
   `http://<IP-do-computador>:8000` (ex: `http://192.168.0.42:8000`)
4. Se não conectar, o firewall do computador pode estar bloqueando a
   porta 8000 — libere o acesso na primeira vez que o sistema pedir.

Isso expõe o jogo para qualquer aparelho na sua rede local (normal em
casa; evite fazer isso em Wi-Fi público/compartilhado).

### Rodando 100% no Android (Termux, sem PC)

Também dá pra rodar o jogo direto no celular/tablet Android, sem
depender de um computador, usando o [Termux](https://f-droid.org/en/packages/com.termux/)
(instale pela F-Droid, não pela Play Store — a versão de lá está
desatualizada):

```bash
pkg update && pkg upgrade -y
pkg install python git -y
git clone https://github.com/Norts023/Projetos.git
cd Projetos
git checkout claude/business-simulation-game-mzaeq0
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

Depois abra `http://localhost:8000` no navegador do próprio aparelho.

Por isso as dependências (`requirements.txt`) são fixadas em versões
mais antigas do FastAPI/Pydantic: as versões mais novas usam um
componente escrito em Rust (`pydantic-core`) que não tem pacote pronto
para Android, e o Termux tenta compilar na hora e falha. As versões
fixadas aqui são 100% Python puro e instalam sem precisar compilar nada
— funcionam igual tanto no PC quanto no Android.

Dicas para manter rodando no Android:
- Deixe o Termux e o navegador abertos ao mesmo tempo (tela dividida),
  senão o Android pode suspender o processo quando o Termux for para
  segundo plano sozinho.
- Desative a otimização de bateria do Termux em Configurações → Apps →
  Termux → Bateria → "Sem restrições".

A interface é organizada em abas com menu lateral (Visão Geral, Produção,
Mercado, Varejo, Terrenos, Bancos, Financeiro, Concorrência, Metas), no
estilo do painel do Sim Companies.

## Mecânicas implementadas (v1)

- **Cadeias de produção multi-tier** (como no Sim Companies): 40 bens em 4
  tiers, em 5 setores — Industrial (Minério de Ferro → Aço → Peças
  Mecânicas → Eletrodomésticos), Petroquímico/Têxtil (Petróleo → Plástico,
  Algodão → Tecido → Roupas), Agropecuária/Alimentos (Vacas/Cereal → Leite,
  Bife, Farinha → Queijo, Pão → Hamburguer), Construção (Argila, Calcário,
  Madeira → Tijolos, Cimento, Tábuas, Vigas de Aço → Concreto Armado),
  Eletrônicos (Silício, Minerais → Processadores, Baterias, Telas,
  Componentes → Smartphones → Robôs) e Automóvel (Carroceria, Motor,
  Interior → Motor Elétrico, Computador de Bordo → Carro Econômico/Elétrico).
  Vários bens (Aço, Plástico, Tecido, Processadores...) são reaproveitados
  como insumo em mais de uma cadeia, exatamente como no jogo original. Cada
  bem processado tem sua própria receita (fábrica/insumos/taxa de produção)
  e pode ser comprado ou vendido no mercado a qualquer momento ("produzir
  vs. comprar pronto").
- **Mercado dinâmico**: cada um dos 40 bens tem preço próprio que reage à
  oferta (produção/vendas) e demanda (compras + demanda de base simulada
  por tier), com tendência de retorno ao preço-base ao longo do tempo.
  Vender pela Bolsa (aba Mercado) é instantâneo mas cobra 3% de taxa,
  seguindo o modelo do Sim Companies.
- **Venda no varejo**: alternativa à Bolsa — você define preço e
  quantidade de um lote, sem taxa, e ele vende aos poucos ao longo do
  tempo. Preço acima do mercado vende mais devagar, abaixo vende mais
  rápido; a aba Varejo mostra a velocidade estimada antes de confirmar.
- **Calculadora de lucro por receita**: ao escolher o que construir, o
  formulário mostra o lucro/hora estimado de cada fábrica com os preços
  atuais de mercado.
- **Bônus de iniciante**: nos primeiros 7 dias de jogo, toda produção
  roda em dobro, para dar um empurrão inicial (como no jogo original).
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
- **Concorrentes simulados (IA leve)**: 5 empresas fictícias, cada uma
  com sua própria cadeia de produção (Aço, Roupas, Eletrodomésticos,
  Hamburguer, Carro Econômico), produzem e vendem no mesmo mercado que
  você e crescem aos poucos; painel de "Concorrência" ranqueia todo mundo
  por valorização estimada, como um placar do Sim Companies.
- **Painel admin para testes** (`/admin.html`): editar caixa, score,
  estoque, preços de mercado, o relógio do jogo e os concorrentes
  diretamente, ou resetar o jogo do zero.
- **Metas/objetivos opcionais**: 10 marcos de progresso (primeira fábrica,
  diversificação industrial, quitar uma dívida, virar líder de mercado,
  primeiro milhão, etc.), cada um com uma recompensa em dinheiro paga uma
  única vez ao ser concluído. Acompanhados na aba "Metas".
- **Construção com materiais e tempo** (como no Sim Companies): construir ou
  melhorar uma fábrica agora custa metade em dinheiro e metade nos 4
  materiais universais de construção (Concreto Armado, Vigas de Aço,
  Tábuas, Vidro — todos produzíveis ou compráveis no mercado), e leva um
  tempo real pra ficar pronta (de 1h a 24h, proporcional ao custo). Dá pra
  pagar um valor extra a qualquer momento para apressar e terminar na hora.
- **Upgrade de nível de fábrica**: melhorar uma fábrica existente para o
  próximo nível aumenta sua taxa de produção, usando o mesmo sistema de
  materiais + tempo (+ apressar) da construção.
- **Juros por valor emprestado**: além do score de crédito, pedir um
  empréstimo muito acima do seu caixa disponível aumenta a taxa oferecida
  (as ofertas mostram uma faixa de taxa mín.–máx.).
- Tela de Produção mostra o estoque atual de cada insumo necessário por
  fábrica, e o status (ativa / construindo / melhorando, com tempo restante).
- **Mapa** (tela inicial): visão geral em blocos visuais de todos os
  terrenos — os seus mostram as fábricas construídas (ícone do setor, nível,
  status/tempo restante) e os espaços livres para construir; os terrenos
  disponíveis aparecem com borda tracejada, preço e botão de compra direto
  no bloco.
- **Catálogo de construção em grade**: a aba Produção mostra as receitas
  disponíveis como cards ilustrados agrupados por setor (Industrial,
  Petroquímico, Têxtil, Agropecuária, Construção, Eletrônicos, Automóvel),
  com ícone, custo e lucro/hora estimado — no estilo do catálogo "Novo
  edifício" do Sim Companies, em vez de uma lista simples.
- **Faixa de preços (ticker)** no topo do Mercado, com seta de alta/baixa
  em relação à última atualização.
- **Histórico de transações** por bem (aba Mercado) e por fábrica (aba
  Produção), num modal com as últimas movimentações do livro-razão.
- Menu lateral reorganizado com o Mapa como tela principal, seguido de
  Visão Geral e dos grupos com submenus (Operações/Finanças/Progresso).
- **Nível da Empresa**: indicador separado do Score de Crédito bancário,
  começa em 100 e sobe conforme você paga suas próprias contas em dia
  (manutenção dos terrenos e parcelas de empréstimo); parcela perdida
  derruba o nível, igual acontece com o score.

## Ideias para próximos passos (não implementadas ainda)

- Eventos aleatórios de economia (crises, escassez, picos de demanda)
- Reputação de mercado além do score de crédito bancário
- Mercado como um livro de ofertas real (cada concorrente posta sua
  própria oferta de venda) em vez do preço único simulado atual
- Mais categorias do Sim Companies ainda de fora: Moda (Couro, Vestido,
  Bolsas...), Energia (Gasolina, Diesel, Etanol...), Aeroespacial (a cadeia
  mais longa do jogo original, até foguetes), Pesquisa (árvore de
  tecnologia) e itens Sazonais (eventos por época do ano)

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
