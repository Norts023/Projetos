// Service worker da Plantinha da Taís: permite abrir o app sem internet.
// Ao mudar algum arquivo do app, aumente o número da versão abaixo.
const VERSAO_CACHE = 'plantinha-tais-v2';

// Caminhos relativos a este arquivo, para funcionar dentro da subpasta do GitHub Pages.
const ARQUIVOS = [
  './',
  './index.html',
  './manifest.json',
  './icon-192.png',
  './icon-512.png',
  './apple-touch-icon.png'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(VERSAO_CACHE)
      .then((cache) => cache.addAll(ARQUIVOS))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys()
      .then((chaves) => Promise.all(
        chaves
          .filter((chave) => chave.startsWith('plantinha-tais-') && chave !== VERSAO_CACHE)
          .map((chave) => caches.delete(chave))
      ))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  const requisicao = event.request;
  if (requisicao.method !== 'GET') return;

  const url = new URL(requisicao.url);
  if (url.origin !== self.location.origin) return;

  const ehPagina = requisicao.mode === 'navigate'
    || url.pathname.endsWith('/')
    || url.pathname.endsWith('/index.html');

  // index.html: rede primeiro (para as atualizações chegarem); sem rede, usa o cache.
  if (ehPagina) {
    event.respondWith(
      fetch(requisicao)
        .then((resposta) => {
          if (resposta.ok) {
            const copia = resposta.clone();
            caches.open(VERSAO_CACHE).then((cache) => cache.put(requisicao, copia));
          }
          return resposta;
        })
        .catch(() => caches.match(requisicao).then((r) => r || caches.match('./index.html')))
    );
    return;
  }

  // Demais arquivos: cache primeiro, depois rede.
  event.respondWith(
    caches.match(requisicao).then((cacheado) => cacheado || fetch(requisicao))
  );
});
