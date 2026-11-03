# Plantinha da Taís 🌱

App web (PWA) para lembrar de beber água. Feito para iPhone, sem App Store e sem build: é só um site estático.

## Arquivos

- `index.html`: o app inteiro (HTML, CSS e JavaScript)
- `manifest.json`: informações de instalação
- `icon-192.png`, `icon-512.png`, `apple-touch-icon.png`: ícones
- `README.md`: este arquivo

## Como testar no computador

1. Abra a pasta `plantinha-da-tais` no Finder (Mac) ou no Explorador de Arquivos (Windows).
2. Dê um duplo clique em `index.html`. Ele abre no navegador.
3. Para simular um iPhone 16: no Chrome, aperte F12, depois o ícone de celular (Ctrl+Shift+M ou Cmd+Shift+M) e escolha um modelo de 393 × 852.

Dica: como o progresso fica salvo no navegador, para começar do zero use o botão ⚙️ > Avançado > Zerar dados (toque duas vezes).

## Como publicar (GitHub Pages)

1. Crie um repositório no GitHub (por exemplo `plantinha-tais`).
2. Envie os arquivos da pasta `plantinha-da-tais` para a raiz do repositório.
3. No GitHub, vá em **Settings > Pages**.
4. Em **Build and deployment**, escolha **Deploy from a branch**, branch `main` e pasta `/ (root)`. Salve.
5. Em alguns minutos o site fica disponível em `https://SEU-USUARIO.github.io/plantinha-tais/`.

## Como instalar no iPhone da Taís

1. Abra o link no **Safari** (não funciona direito no Chrome do iPhone).
2. Toque no botão de compartilhar (quadrado com seta para cima).
3. Escolha **Adicionar à Tela de Início**.
4. Confirme. O ícone da plantinha aparece na tela inicial e abre em tela cheia.

## Onde editar

- **Prêmios**: array `RECOMPENSAS` no topo do script. Troque os textos entre colchetes.
- **Frases**: objeto `FRASES`, com as chaves `feliz`, `com_sede`, `murchando`, `florindo`, `garrafa_vazia` e `meta_batida`.
- **Níveis e estágios**: arrays `NIVEIS` e `ESTAGIOS`.
- **Valores padrão**: `META_PADRAO` (2000 ml) e `GARRAFA_PADRAO` (500 ml).

## Observações

- Os dados ficam só no aparelho (localStorage). Se apagar os dados do Safari, o progresso some.
- Ainda não há modo offline (service worker) nem notificações. Veja a lista de próximos passos no resumo do pull request ou da conversa.
