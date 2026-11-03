# Plantinha da Taís — Documentação completa

Estado do app, regras do jogo, telas, armazenamento, backup, modo offline e testes.

---

## 1. Visão geral

**Plantinha da Taís** é um web app (PWA) em português do Brasil, feito para uma pessoa só (a Taís), que usa iPhone 16 com iOS 18 ou mais novo. Ele lembra de beber água por meio de uma plantinha que cresce quando a pessoa bebe água e murcha quando fica sem beber. Durante o horário de dormir, a plantinha dorme.

- **Tipo:** site estático. Um `index.html` com HTML, CSS e JavaScript embutidos, mais um `sw.js` (service worker) para o modo offline.
- **Instalação:** pela tela de início do Safari ("Adicionar à Tela de Início"). Não passa pela App Store e não tem build.
- **Dados:** tudo fica salvo no navegador do aparelho (localStorage). Não há servidor, conta nem sincronização. O backup manual é feito pelo próprio app (seção 10).
- **Idioma:** português do Brasil em todos os textos e datas.

---

## 2. Estado atual do projeto

| Item | Situação |
|---|---|
| Jogo, visual e animações | Pronto |
| Desfazer o último registro (6 s) | Pronto |
| Bebi fora da garrafa | Pronto |
| Horário de dormir (plantinha dorme) | Pronto |
| Aviso para abrir pelo ícone (só iPhone/iPad) | Pronto |
| Sequência calculada pelos dias com meta batida | Pronto |
| Modo offline (service worker) | Pronto (testado em servidor local) |
| Exportar e importar progresso | Pronto |
| Frases sem repetir seguidas | Pronto |
| Testado no Chromium (393 × 852) | Sim, sem erros de console |
| Testado no Safari/iPhone real | **Não** |
| Publicado no GitHub Pages | **Não** (falta criar o repositório `app-agua-tais`) |
| Notificações faladas (Atalhos) | **Não existem** (etapa futura) |

### Arquivos da pasta `plantinha-da-tais/`

| Arquivo | Função |
|---|---|
| `index.html` | O app inteiro: HTML, CSS e JavaScript. Registra o `sw.js`. |
| `sw.js` | Service worker: guarda os arquivos do app no cache para abrir sem internet. Versão do cache: `plantinha-tais-v2`. |
| `manifest.json` | Metadados de instalação: nome, cores, ícones, `start_url` e `scope` (ambos `"./"`), `display: standalone`, `orientation: portrait`. |
| `icon-192.png`, `icon-512.png` | Ícones do manifest (plantinha em fundo verde-claro, com gotinha azul). |
| `apple-touch-icon.png` | Ícone de 180×180 usado pelo iOS na tela de início. |
| `README.md` | Passo a passo para testar, publicar e instalar. |
| `DOCUMENTACAO.md` | Este documento. |

### Caminhos e publicação

- Todos os caminhos são relativos (`./...`), para funcionar dentro da subpasta do GitHub Pages: `https://SEU-USUARIO.github.io/app-agua-tais/`.
- O `sw.js` também usa caminhos relativos e o cache inclui `./`, `./index.html`, `./manifest.json`, os três ícones.
- Meta tags de iPhone no `<head>`: `viewport` com `viewport-fit=cover`, `apple-mobile-web-app-capable`, `apple-mobile-web-app-status-bar-style` (default), `apple-mobile-web-app-title` ("Plantinha"), `theme-color` (#8ED1E8).
- Pendências para publicar: criar o repositório `app-agua-tais`, enviar todos os arquivos da pasta para a raiz da branch `main` (inclusive `sw.js`) e ativar *Settings > Pages* (branch `main`, pasta `/ (root)`).

---

## 3. Como usar (fluxo da Taís)

1. Abre o app pelo ícone na tela de início.
2. Vê a plantinha, o balão com uma frase, o estado atual e o horário de dormir.
3. Bebe água e toca em **+ Gole** (50 ml) ou **+ Copo** (250 ml). A água sai da garrafa virtual e conta como consumo do dia. Um aviso aparece com o botão **Desfazer** por 6 segundos.
4. Se bebeu água sem usar a garrafa, toca em **Bebi fora da garrafa** e escolhe Gole, Copo ou Garrafa. Isso conta no dia e no XP, mas não mexe na garrafa virtual.
5. Quando a garrafa virtual acaba, os botões de garrafa ficam desligados e a plantinha pede para encher. Toca em **Encher garrafa** para voltar ao cheio (não conta como consumo).
6. Acompanha meta do dia, XP, nível, estágio, sequência, histórico e prêmios.
7. Ajusta meta, garrafa e horário de dormir em **⚙️ Configurações**. Backup e zerar ficam em **Avançado**.

---

## 4. Tela e navegação

### 4.1 Faixa para abrir pelo ícone (só iPhone/iPad)
- Aparece no topo quando o app é aberto no Safari (fora do modo instalado) em iPhone ou iPad.
- Texto: "Abre pelo ícone da Plantinha na tela de início para não perder seu progresso 🌱".
- Fecha com ✕. Não volta até o app ser aberto de novo.
- Não aparece no computador.

### 4.2 Cabeçalho
- Título "Plantinha da Taís" e botão ⚙️ (abre as configurações).
- Fixo no topo, com respeito à área segura do iPhone.

### 4.3 Barra de abas (inferior, fixa)

| Aba | Ícone | Conteúdo |
|---|---|---|
| Hoje | 🌱 | Cena da plantinha, estado, XP, água do dia e garrafa. |
| Histórico | 📊 | Gráfico dos últimos 7 dias e lista. |
| Prêmios | 🎁 | Lista de recompensas (desbloqueadas e bloqueadas). |

### 4.4 Aba Hoje (de cima para baixo)

**a) Cena da plantinha**
- Céu com gradiente azul para verde-claro (à noite, fica azul-escuro), duas nuvens com movimento lento, chão de grama e terra.
- **Balão de fala** com a frase atual. O texto aparece letra por letra. Com "Reduzir movimento" ativo, aparece de uma vez.
- **Plantinha** em SVG, com o rosto no vaso (seção 7).
- Tocar na plantinha sorteia outra frase do estado atual. Também funciona com Enter ou Espaço.
- **Garrafa de pedido** (à direita da planta): aparece só no estado "garrafa vazia".

**b) Cartão de informações**
- Nome do estado (ex.: "Feliz da vida", "Com sede...", "Murchando!", "Florindo!", "Garrafa vazia", "Meta batida!", "Dormindo... Zzz").
- "Estágio: ..." (um dos 6 estágios da planta).
- Barra de XP, "Nível N · Nome" e o texto de XP (quanto falta para o próximo nível).
- "Dorme às HH:MM · acorda às HH:MM" (horário de dormir configurado).

**c) Cartão "Água de hoje"**
- Data por extenso (ex.: "terça-feira, 3 de novembro").
- Total do dia e meta: "X ml de Y ml", ou "Meta batida! X ml 🎉".
- Barra de progresso da meta (limitada a 100%).
- Sequência: "🔥 N dias seguidos batendo a meta" ou "🍃 Bata a meta para começar uma sequência".
- "Melhor sequência: N dias" (recorde guardado).

**d) Cartão da garrafa**
- Garrafa desenhada com nível de água animado e ondulação (a ondulação some quando está vazia).
- Texto "X ml de Y ml na garrafa", ou "Garrafa vazia! Enche ela de verdade 💧".
- Botões: **+ Gole** (50 ml), **+ Copo** (250 ml) e **Encher garrafa** (largura total). Os dois primeiros ficam desligados com a garrafa vazia.
- Botão **Bebi fora da garrafa** (largura total). Ao tocar, abre um painel com três opções: **Gole 50 ml**, **Copo 250 ml** e **Garrafa** (com o tamanho atual da garrafa), mais **Cancelar**. Funciona mesmo com a garrafa vazia.

### 4.5 Aba Histórico
- Gráfico de barras com os últimos 7 dias (o mais antigo à esquerda, "Hoje" à direita).
- Barras em tons de verde nos dias em que a meta foi batida (dias de `bonusDias`), e em azul nos outros.
- Linha tracejada laranja marcando a meta atual, com o rótulo "meta".
- Lista abaixo, do dia mais recente para o mais antigo, com o total de cada dia e ✅ quando a meta foi batida.

### 4.6 Aba Prêmios
- Texto de apoio: "Bata a meta e suba de nível para desbloquear."
- Cada prêmio mostra a condição e o texto.
- Prêmio **bloqueado**: texto "Segredo" e selo cinza "bloqueado".
- Prêmio **desbloqueado**: texto real e selo verde "Desbloqueado".

### 4.7 Configurações (painel inferior)
Abre pelo ⚙️. Fecha pelo ✕ ou tocando fora do painel.

- **Meta diária (ml):** de 500 a 6000 ml, passos de 50.
- **Tamanho da garrafa (ml):** de 100 a 3000 ml. Trocar o tamanho enche a garrafa de novo.
- **Dormir às** e **Acordar às:** campos de hora (padrão 23:00 e 07:00), com fonte de 16 px. Não podem ser iguais.
- **Salvar:** valida os valores. Se algo estiver errado, mostra um aviso e não salva.
- **Avançado** (recolhido por padrão):
  - **Exportar progresso** (seção 10).
  - **Importar progresso** (seção 10).
  - **Zerar dados**. O primeiro toque muda o texto para "Toque de novo para confirmar". Sem segundo toque em 4 segundos, volta ao normal. Com o segundo toque, apaga tudo.

---

## 5. Regras do jogo

### 5.1 Garrafa virtual
- O registro pela garrafa consome `min(quantidade pedida, garrafa atual)`.
- Com a garrafa vazia, os botões de garrafa não registram. Aparece o aviso "A garrafa está vazia. Encha ela primeiro!".
- **Encher garrafa** restaura a garrafa ao tamanho configurado. Não soma nada ao consumo.
- **Bebi fora da garrafa** registra no dia e no XP, sem mexer na garrafa. Não tem limite de garrafa.

### 5.2 Registros e desfazer
- Todo registro (pela garrafa ou fora dela) mostra um aviso com o botão **Desfazer** por **6 segundos**.
- Só o **último** registro pode ser desfeito. Ele fica guardado apenas na memória do app (some ao recarregar a página).
- Desfazer:
  - devolve a água à garrafa (se o registro veio da garrafa, sem passar do tamanho da garrafa);
  - tira a água do dia e do total de água (base do XP);
  - remove o bônus de meta do dia, se esse registro foi o que bateu a meta;
  - volta o "último registro" para o valor anterior (o estado da planta volta a ser calculado a partir dele).
- Depois de 6 segundos, o botão some e o registro não pode mais ser desfeito.

### 5.3 Meta diária
- Padrão: 2000 ml. Configurável de 500 a 6000 ml.
- A meta é comparada com o total do **dia local** (fuso do aparelho).
- Quando a meta é batida pela primeira vez no dia, o dia entra em `bonusDias` e ganha 50 XP (uma vez por dia).

### 5.4 XP e níveis
- **XP por água:** 1 XP a cada 10 ml registrados (`floor(total de ml na vida / 10)`). Ex.: 100 ml = 10 XP.
- **Bônus de meta:** +50 XP por dia em que a meta foi batida (`bonusDias`).
- **XP total** = XP por água + 50 × número de dias em `bonusDias`.

**Níveis (8):**

| Nível | Nome | XP mínimo |
|---|---|---|
| 1 | Semente | 0 |
| 2 | Broto | 100 |
| 3 | Muda | 250 |
| 4 | Roseira | 450 |
| 5 | Girassol | 700 |
| 6 | Jardineira | 1000 |
| 7 | Mestre do Jardim | 1400 |
| 8 | Lenda do Jardim | 1900 |

### 5.5 Estágios da planta (visual)
Limites de XP próprios, separados dos níveis:

| Estágio | XP mínimo | Visual |
|---|---|---|
| Semente | 0 | Grão pequeno sobre a terra. |
| Broto | 100 | Duas folhinhas saindo do solo. |
| Plantinha | 300 | Caule médio com 4 folhas. |
| Muda Forte | 600 | Caule mais grosso, folhas maiores. |
| Flor | 1000 | Botão de flor rosa no topo. |
| Jardim Florido | 1600 | Flor completa e flores menores ao redor. |

- A planta cresce um pouco dentro de cada estágio a cada registro.
- Quando o estágio sobe: "🌱 Sua plantinha virou ...". Quando só o nível sobe: "⭐ Novo nível: ...".

### 5.6 Sequência de dias e recorde (baseados em `bonusDias`)
- **Sequência atual:** dias seguidos presentes em `bonusDias`, contando de hoje. Se hoje ainda não bateu a meta, conta de ontem.
- **Recorde:** a maior sequência já registrada no histórico de `bonusDias`, guardada em `melhorSeq`. O recorde **nunca diminui**, então prêmios já liberados continuam liberados.
- **Mudar a meta não altera dias passados.** A sequência não compara o histórico com a meta atual: ela usa só os dias em que a meta foi batida de verdade, na hora do registro.

### 5.7 Horário de dormir
- Configurado em Configurações: **Dormir às** (padrão 23:00) e **Acordar às** (padrão 07:00). O intervalo pode passar da meia-noite.
- **Durante o horário de dormir**, a plantinha mostra o estado "Dormindo... Zzz", com rosto dormindo, letras "Zzz" e cena noturna. Usa frases de boa noite (`FRASES.dormindo`).
- **O tempo de sono não conta para a sede.** As horas de "acordado" desde o último registro são calculadas descontando o período de sono, inclusive quando ele passa da meia-noite.
  - Exemplo: último registro às 22:30, sono das 23:00 às 07:00. Às 08:00 da manhã, passaram 1,5 h acordada (22:30–23:00 e 07:00–08:00). Por isso ela está feliz, e não murcha.

### 5.8 Estados da plantinha
Ordem de prioridade (o primeiro que se aplicar vence):

| Ordem | Estado | Condição | Rótulo na tela |
|---|---|---|---|
| 1 | `dormindo` | agora está dentro do horário de dormir | "Dormindo... Zzz" |
| 2 | `murchando` | 4 horas ou mais acordada desde o último registro | "Murchando!" |
| 3 | `com_sede` | 2 horas ou mais acordada desde o último registro | "Com sede..." |
| 4 | `garrafa_vazia` | garrafa com 0 ml | "Garrafa vazia" |
| 5 | `meta_batida` | total de hoje ≥ meta | "Meta batida!" |
| 6 | `florindo` | estágio Flor ou Jardim Florido (XP ≥ 1000) | "Florindo!" |
| 7 | `feliz` | nenhum dos anteriores | "Feliz da vida" |

- "Último registro" é a última vez que a Taís registrou água (inclusive fora da garrafa). Na primeira abertura, conta como registro.

### 5.9 Prêmios
- Lista `RECOMPENSAS` no topo do script. Hoje tem 4 exemplos, com textos entre colchetes para substituir:
  - `{ nivel: 3, texto: "[Vale um jantar com o Marcos]" }`
  - `{ nivel: 5, texto: "[Vale uma massagem]" }`
  - `{ dias: 3, texto: "[Uma mensagem de amor]" }`
  - `{ dias: 7, texto: "[Vale escolher o filme da noite]" }`
- **Por nível:** desbloqueia quando o nível atual é igual ou maior que `nivel`.
- **Por dias:** desbloqueia quando o recorde (`melhorSeq`) é igual ou maior que `dias`.
- Ao desbloquear pela primeira vez, aparece o aviso "🎁 Você desbloqueou um prêmio! Veja na aba Prêmios." Não se repete.
- O aviso é identificado pela condição (ex.: `nivel-3`). Trocar só o texto não gera novo aviso.

---

## 6. Frases da plantinha

O objeto `FRASES` tem 7 listas, com 6 frases cada (42 no total):
- `feliz`, `com_sede`, `murchando`, `florindo`, `garrafa_vazia`, `meta_batida`, `dormindo`.

Quando a frase é sorteada:
- Ao mudar de estado.
- Ao registrar água (sempre sorteia de novo, mesmo sem mudança de estado).
- Ao desfazer, ao encher a garrafa (sorteia de `feliz`).
- Ao tocar na plantinha (sorteia do estado atual).
- Ao tentar beber com a garrafa vazia (sorteia de `garrafa_vazia`).
- A cada 30 segundos, se o estado tiver mudado.

**Sem repetição seguida:** a frase sorteada nunca é igual à anterior do mesmo estado.

---

## 7. A plantinha

### 7.1 Desenho
- SVG em um `viewBox` de 200 × 220, com 6 desenhos (um por estágio).
- O vaso tem um rosto (olhos e boca) que muda conforme o estado. Todos os grupos do rosto ficam no SVG, e só o do estado atual aparece.
- Estados `florindo` mostram estrelinhas ao redor. Estado `dormindo` mostra "Zzz".

### 7.2 Rostos por estado

| Estado | Rosto |
|---|---|
| feliz | Olhos abertos e sorriso. |
| com_sede | Olhos semicerrados, boca reta e gota de suor que pinga. |
| murchando | Sobrancelhas tristes, olhos menores e boca para baixo. |
| florindo | Olhos grandes com brilho, bochechas rosadas e sorriso aberto. |
| garrafa_vazia | Olhos olhando para cima, boca pequena em "o". Ao lado, a garrafa de pedido. |
| meta_batida | Olhos fechados em arco feliz, bochechas e sorriso aberto. |
| dormindo | Olhos fechados em arco, boca pequena e "Zzz" flutuando. |

### 7.3 Animações

| Animação | Quando acontece | Detalhes |
|---|---|---|
| Balanço | Sempre | 4 s, ±1,2°, em loop. |
| Piscada | A cada 3–5 s, aleatório | Os olhos fecham por 150 ms. |
| Pulinho | Ao registrar água | 0,9 s. |
| Crescimento | Ao mudar de estágio | 0,6 s, escala de 0,6 para 1 com fade-in. |
| Gotinha | Ao registrar água | Cai de cima até a planta, em 0,95 s. |
| Folhas caindo | `murchando` (queda de 42°) e `com_sede` (cerca de 40% disso) | Transição de 0,9 s. Ao receber água, sobem de volta. |
| Flor pendendo | `murchando` e `com_sede` (estágios Flor e Jardim Florido) | Transição de 0,9 s. |
| Partículas | Ao entrar em `florindo` | 7 pétalas e folhinhas, caindo em loop (6 a 10 s). |
| Confete | Ao entrar em `meta_batida` vindo de outro estado | 26 peças, somem em cerca de 3,8 s. |
| Brilho das estrelas | `florindo` | 1,8 s em loop. |
| Suor pingando | `com_sede` | 1,6 s em loop. |
| "Zzz" | `dormindo` | 2,6 s em loop, com atraso no segundo "z". |
| Cena noturna | `dormindo` | Céu escurece com transição de 1,2 s. |
| Garrafa de pedido | `garrafa_vazia` | Balanço de 1,6 s em loop. |
| Nuvens | Sempre | 22 s e 28 s, de ida e volta. |
| Ondulação da água | Sempre que a garrafa tem água | 1,6 s em loop. |

### 7.4 Movimento reduzido
Com "Reduzir movimento" ativo no iPhone (Ajustes > Acessibilidade > Movimento):
- O CSS desliga todas as animações e transições, e esconde a camada de efeitos.
- O JavaScript não cria gotinha, partículas, confete nem piscada, e o balão mostra a frase de uma vez.
- A plantinha continua mudando de estado e de estágio, sem animar.

---

## 8. Mensagens na tela (avisos)

- Aparecem no topo, por 3,2 segundos. Os avisos de registro ficam 6 segundos, com o botão **Desfazer**.
- Um aviso novo substitui o anterior.
- Prioridade ao registrar água: 1) prêmio desbloqueado; 2) estágio subiu; 3) nível subiu; 4) "+X ml · +Y XP" (com "(fora da garrafa)" quando for o caso e "· Meta batida: +50 XP!" se bateu a meta).
- Outros avisos: "Registro desfeito...", "Garrafa cheia de novo! Vamos crescer 💧", "Configurações salvas!", "Tudo zerado. Começo novinho!", "Progresso importado!", mensagens de validação de configurações e de backup.
- Aviso de armazenamento indisponível: "Este navegador não guarda dados. O progresso some ao fechar a página." (aparece 0,6 s após abrir).
- Falha ao salvar: "Não consegui salvar o progresso neste navegador."

---

## 9. Armazenamento

- Chave única no localStorage: `plantinha-tais-v1`. Guarda o objeto inteiro em JSON.

| Campo | Tipo | Significado |
|---|---|---|
| `metaMl` | número | Meta diária. Padrão 2000. |
| `garrafaMl` | número | Tamanho da garrafa. Padrão 500. |
| `garrafaAtual` | número | Água na garrafa agora. |
| `totalMl` | número | Total de água registrado na vida (base do XP). |
| `dias` | objeto | `AAAA-MM-DD` → ml bebidos no dia. |
| `bonusDias` | lista | Dias em que a meta foi batida. Base da sequência e do bônus de XP. |
| `melhorSeq` | número | Recorde de dias seguidos. Nunca diminui. |
| `ultimoRegistro` | número (timestamp) | Último registro, ou primeira abertura. Base do estado murcho. |
| `premiosVistos` | lista | Prêmios que já geraram aviso (ex.: `nivel-3`). |
| `dormirAs` | texto `HH:MM` | Hora de dormir. Padrão `23:00`. |
| `acordarAs` | texto `HH:MM` | Hora de acordar. Padrão `07:00`. |

- **Mesclagem com o padrão:** ao abrir, os dados salvos são combinados com os valores padrão. Campos novos (como `dormirAs` e `acordarAs`) entram com o padrão, e nenhum dado antigo é perdido. Isso foi testado com um progresso salvo sem os campos novos.
- Se o JSON estiver corrompido, o app começa do zero sem travar.
- Sem localStorage (modo privado antigo ou bloqueio), o app funciona só em memória e avisa.
- Se gravar falhar (ex.: cota cheia), o app avisa e para de tentar salvar nesta sessão.
- O desfazer não é salvo: ele existe só na memória, por 6 segundos.
- `navigator.storage.persist()` é pedido ao abrir, quando disponível, para o Safari não apagar o cache e os dados com facilidade.

---

## 10. Backup: exportar e importar

Ficam em **⚙️ > Avançado**.

### 10.1 Exportar progresso
- Copia o progresso (JSON) para a área de transferência e baixa um arquivo `plantinha-tais-backup-AAAA-MM-DD.json`.
- No iPhone, o download abre a opção de salvar em Arquivos.
- Formato do arquivo: `{ "app": "plantinha-da-tais", "formato": 1, "exportadoEm": "...", "progresso": { ... } }`.

### 10.2 Importar progresso
1. Toca em **Importar progresso** e escolhe um arquivo `.json`.
2. O app valida o arquivo: confere o campo `app`, os intervalos (meta, garrafa), o formato das datas e os tipos dos campos.
3. Se estiver válido, mostra um resumo (dias de histórico e XP) e o botão muda para **"Toque de novo para substituir"**.
4. Toca de novo dentro de **4 segundos** para confirmar. A importação substitui o progresso atual.
5. Se o arquivo for inválido, aparece uma mensagem explicando o problema e nada muda.

**Atenção:** importar substitui o progresso atual. Exporte antes, se quiser guardar o atual.

---

## 11. Modo offline (service worker)

### 11.1 Como funciona
- O `index.html` registra o `sw.js` ao abrir (quando o navegador suporta).
- Na instalação, o `sw.js` guarda no cache: `./`, `./index.html`, `./manifest.json`, `./icon-192.png`, `./icon-512.png` e `./apple-touch-icon.png`.
- **index.html:** estratégia "rede primeiro". Quando há internet, busca a versão nova e atualiza o cache. Sem internet, usa o cache.
- **Demais arquivos:** estratégia "cache primeiro", com a rede como reserva.
- **Versão do cache:** `VERSAO_CACHE = 'plantinha-tais-v2'` em `sw.js`. Ao mudar qualquer arquivo do app, aumente esse número. Versões antigas do cache são apagadas na ativação.
- O service worker só funciona em `https://` (GitHub Pages) ou `http://localhost`. Abrindo com duplo clique (`file://`), o app funciona, mas sem offline.

### 11.2 Atualização
- Quando o app recebe uma versão nova, ela aparece na próxima abertura com internet.
- Se o celular estiver sem internet, continua usando a versão que está no cache.

---

## 12. Instalação no iPhone

1. Abrir o link publicado no **Safari** (não no Chrome do iPhone).
2. Botão de compartilhar > **Adicionar à Tela de Início**.
3. O ícone usa `apple-touch-icon.png`. O nome na tela é "Plantinha".
4. Abre em tela cheia, sem barra do Safari.
5. Se for aberto pelo Safari em vez do ícone, aparece a faixa de aviso (seção 4.1).

---

## 13. Como testar

### 13.1 No computador (básico)
1. Abrir `plantinha-da-tais/index.html` no navegador (duplo clique).
2. Simular o iPhone: F12 > ícone de celular > tamanho 393 × 852.

### 13.2 Como testar cada melhoria

**Desfazer (6 s)**
1. Toque em **+ Gole**. O aviso mostra o botão **Desfazer**.
2. Toque em **Desfazer** dentro de 6 segundos: a água volta para a garrafa e o total do dia diminui.
3. Espere 6 segundos sem tocar: o botão some e não dá mais para desfazer.

**Bebi fora da garrafa**
1. Esvazie a garrafa (toque em **+ Copo** até os botões ficarem desligados).
2. Toque em **Bebi fora da garrafa** > **Copo 250 ml**. O total do dia sobe 250 ml, a garrafa continua vazia.

**Horário de dormir (simular agora)**
1. Abra ⚙️ e coloque **Dormir às** uma hora antes da hora atual e **Acordar às** uma hora depois. Salve.
2. A plantinha mostra "Dormindo... Zzz", a cena fica noturna e as frases são de boa noite.
3. Para ver o cálculo de "acordada" sem esperar: use o relógio simulado do DevTools (Sensors ou Playwright), ou apenas conte que o tempo de sono não entra na sede.

**Aviso para abrir pelo ícone**
1. No computador, a faixa não aparece.
2. Para simular o iPhone: no DevTools, mude o user agent para um iPhone (Network conditions > User agent) e recarregue. A faixa aparece. Fecha com ✕.

**Sequência e recorde**
1. Bata a meta em dias seguidos (ou use **Importar** com um arquivo que tenha `bonusDias`).
2. Mude a meta em Configurações: a sequência e o recorde não mudam.

**Backup**
1. ⚙️ > Avançado > **Exportar progresso**. Um arquivo `.json` é baixado.
2. Altere alguma coisa, toque em **Importar progresso**, escolha o arquivo, toque de novo para confirmar. O progresso volta ao do arquivo.

**Frases**
- Toque na plantinha várias vezes: a frase nunca se repete na sequência.

### 13.3 Como testar o modo offline
Use um servidor local (o `sw.js` não funciona com duplo clique):

1. Na pasta `plantinha-da-tais`, rode `python3 -m http.server 8000`.
2. Abra `http://localhost:8000/` no Chrome.
3. Confira o service worker: F12 > Application > Service Workers (deve aparecer ativo) e Cache Storage (deve ter `plantinha-tais-v2`).
4. Marque **Offline** em Application > Service Workers (ou em Network > Offline).
5. Recarregue a página: o app deve abrir normalmente.

### 13.4 Testes já feitos
- Bateria automatizada no Chromium (393 × 852), com 36 verificações: desfazer (inclusive bônus e 6 segundos), bebi fora (inclusive garrafa vazia), dormindo (rosto, noite e cálculo que cruza a meia-noite), sequência não mudando com a meta, frases sem repetição seguida, exportar e importar (com validação e confirmação), faixa só em iPhone, service worker registrado e app aberto offline. Sem erros de JavaScript.
- Dados antigos (sem `dormirAs`/`acordarAs`) carregados com os valores padrão e sem perda.

### 13.5 Testes que faltam
- Safari no iPhone real: instalação, tela cheia, ícone, toque, rolagem, área segura, download do backup e atualização do service worker.
- Publicação no GitHub Pages: caminhos, ícones e service worker dentro da subpasta.
- Teste de longa duração: virada de dia real, dias seguidos reais.

---

## 14. Limitações conhecidas

- **Dados só no aparelho.** Trocar de iPhone ou apagar os dados do Safari perde o progresso. Use o backup para guardar uma cópia.
- **Desfazer só vale para o último registro e por 6 segundos.** Se o app for fechado nesse intervalo, não dá para desfazer.
- **Importar substitui tudo.** Não há mesclagem: o que estava no app antes é perdido após a confirmação.
- **Atualização do offline depende da próxima abertura com internet.** Até lá, a versão do cache é usada.
- **Estados em horas dependem do app ser atualizado.** O cálculo acontece a cada 30 segundos ou ao voltar ao app.
- **Dia = dia local do aparelho.** Viajar de fuso pode alterar a contagem do dia.
- **Histórico cresce sem limite.** O objeto `dias` não é podado. Não é problema prático para uso diário.
- **A faixa de instalar some só ao fechar com ✕ nesta abertura.** Ela volta na próxima vez que o app for aberto pelo Safari.
- **Prêmios de exemplo.** Os textos entre colchetes precisam ser trocados pela Taís.
- **Horário de dormir é fixo por dia.** Não há horários diferentes para fins de semana.
- **Ícones em PNG gerados por script.** O estilo é simples; pode ser refeito.

---

## 15. Histórico de mudanças desta versão

1. **Desfazer:** botão de 6 segundos após cada registro, só para o último registro.
2. **Bebi fora da garrafa:** registra no dia e no XP sem mexer na garrafa; funciona com a garrafa vazia.
3. **Horário de dormir:** configurável; plantinha dorme (rosto, "Zzz", cena noturna, frases próprias); tempo de sono não conta para sede e murcha.
4. **Aviso para abrir pelo ícone:** faixa no topo em iPhone/iPad fora do app instalado, com ✕.
5. **Sequência e recorde a partir de `bonusDias`:** mudar a meta não altera dias passados; o recorde nunca diminui.
6. **Modo offline:** `sw.js` com cache versionado (`plantinha-tais-v2`), rede primeiro para o `index.html` e cache primeiro para o resto; registro no `index.html` e `navigator.storage.persist()`.
7. **Backup:** exportar (área de transferência e arquivo `.json`) e importar (validação e confirmação com toque duplo).
8. **Frases:** nunca repetem a anterior no mesmo estado (nova frase de `dormindo` incluída).
9. **README:** removida a menção a "resumo do pull request ou da conversa"; instruções atualizadas com `sw.js`, backup e versão do cache.
10. **Esta documentação:** atualizada com todas as mudanças. Também passou a existir dentro do projeto.

---

## 16. Próximas etapas

1. **Publicação (Etapa 3):** criar o repositório `app-agua-tais`, enviar a pasta para a raiz da branch `main` e ativar o GitHub Pages.
2. **Teste no iPhone:** instalar pela tela de início no Safari e conferir ícone, tela cheia, toques, área segura, backup e atualização do offline.
3. **Notificações faladas:** automações com Atalhos do iOS para lembrar de beber água com voz.
4. **Prêmios reais:** a Taís (ou você) troca os textos de exemplo em `RECOMPENSAS`.
5. **Visual avançado:** ilustrações mais elaboradas e animações adicionais, se quiser.
