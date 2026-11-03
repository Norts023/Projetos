# Plantinha da Taís 🌱

App web (PWA) para lembrar de beber água. Feito para iPhone, sem App Store e sem build: é um site estático.

## Arquivos

- `index.html`: o app inteiro (HTML, CSS e JavaScript)
- `sw.js`: service worker, que permite abrir o app sem internet
- `manifest.json`: informações de instalação
- `icon-192.png`, `icon-512.png`, `apple-touch-icon.png`: ícones
- `README.md`: este arquivo
- `DOCUMENTACAO.md`: documentação completa (regras, telas e testes)

Todos os arquivos devem ficar na mesma pasta.

## Como testar no computador

1. Abra a pasta `plantinha-da-tais` no Finder (Mac) ou no Explorador de Arquivos (Windows).
2. Dê um duplo clique em `index.html`. Ele abre no navegador.
3. Para simular um iPhone 16: no Chrome, aperte F12, depois o ícone de celular (Ctrl+Shift+M ou Cmd+Shift+M) e escolha um modelo de 393 × 852.

Observação: o modo offline (`sw.js`) só funciona quando o app é aberto por `http://localhost` ou `https://`. Abrindo com duplo clique (`file://`), o app funciona, mas sem offline. Para testar o offline, veja a seção "Como testar o modo offline" em `DOCUMENTACAO.md`.

Dica: para começar do zero, use ⚙️ > Avançado > Zerar dados (toque duas vezes).

## Como publicar (GitHub Pages)

1. Crie um repositório no GitHub chamado `app-agua-tais`.
2. Envie todos os arquivos da pasta `plantinha-da-tais` (inclusive `sw.js`) para a raiz do repositório.
3. No GitHub, vá em **Settings > Pages**.
4. Em **Build and deployment**, escolha **Deploy from a branch**, branch `main` e pasta `/ (root)`. Salve.
5. Em alguns minutos o site fica disponível em `https://SEU-USUARIO.github.io/app-agua-tais/`.

Depois de cada alteração no app, aumente o número `VERSAO_CACHE` em `sw.js` (por exemplo, de `plantinha-tais-v2` para `plantinha-tais-v3`). Assim o celular recebe a versão nova.

## Como instalar no iPhone da Taís

1. Abra o link no **Safari** (não funciona direito no Chrome do iPhone).
2. Toque no botão de compartilhar (quadrado com seta para cima).
3. Escolha **Adicionar à Tela de Início**.
4. Confirme. O ícone da plantinha aparece na tela inicial e abre em tela cheia.

Se o app for aberto pelo Safari em vez do ícone, aparece uma faixa lembrando de abrir pelo ícone.

## Backup do progresso

Em ⚙️ > Avançado:

- **Exportar progresso**: copia o progresso para a área de transferência e baixa um arquivo `.json`.
- **Importar progresso**: escolha um arquivo `.json` exportado antes. Depois toque de novo para confirmar. Isso substitui o progresso atual.

## Onde editar

- **Prêmios**: array `RECOMPENSAS` no topo do script. Troque os textos entre colchetes.
- **Frases**: objeto `FRASES`, com as chaves `feliz`, `com_sede`, `murchando`, `florindo`, `garrafa_vazia`, `meta_batida` e `dormindo`.
- **Níveis e estágios**: arrays `NIVEIS` e `ESTAGIOS`.
- **Valores padrão**: `META_PADRAO` (2000 ml), `GARRAFA_PADRAO` (500 ml), `DORMIR_PADRAO` (23:00) e `ACORDAR_PADRAO` (07:00).

## Observações

- Os dados ficam só no aparelho (localStorage). Se apagar os dados do Safari, o progresso some. Use o backup para guardar uma cópia.
- Ainda não há notificações. Lembretes falados com Atalhos do iOS são uma etapa futura.
