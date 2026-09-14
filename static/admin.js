async function api(path, options) {
  const res = await fetch(path, { headers: { 'Content-Type': 'application/json' }, ...options });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.detail || `Erro ${res.status}`);
  return data;
}

function showMsg(elementId, text, isError) {
  const el = document.getElementById(elementId);
  el.textContent = text;
  el.className = 'msg ' + (isError ? 'error' : 'ok');
  setTimeout(() => { el.textContent = ''; }, 4000);
}

async function loadState() {
  const state = await api('/api/admin/state');
  document.getElementById('adm-cash').value = state.company.cash;
  document.getElementById('adm-score').value = state.company.credit_score;
  document.getElementById('adm-minutes').value = state.clock.game_minutes;

  const marketGood = document.getElementById('adm-market-good').value;
  const market = state.market.find((m) => m.name === marketGood);
  if (market) document.getElementById('adm-market-price').value = market.current_price;

  const tbody = document.querySelector('#tbl-adm-competitors tbody');
  tbody.innerHTML = '';
  state.competitors.forEach((c) => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${c.name}</td>
      <td><input type="number" step="0.01" value="${c.cash}" id="comp-cash-${c.id}" style="width:110px"></td>
      <td><input type="number" step="0.1" value="${c.production_rate_per_hour}" id="comp-rate-${c.id}" style="width:80px"></td>
      <td></td>`;
    const btn = document.createElement('button');
    btn.className = 'secondary';
    btn.textContent = 'Salvar';
    btn.onclick = () => applyCompetitor(c.id);
    tr.lastElementChild.appendChild(btn);
    tbody.appendChild(tr);
  });
}

async function applyCompany() {
  try {
    await api('/api/admin/company', {
      method: 'POST',
      body: JSON.stringify({
        cash: Number(document.getElementById('adm-cash').value),
        credit_score: Number(document.getElementById('adm-score').value),
      }),
    });
    showMsg('msg-company', 'Aplicado.', false);
  } catch (err) { showMsg('msg-company', err.message, true); }
}

async function applyInventory() {
  const good = document.getElementById('adm-inv-good').value;
  const quantity = Number(document.getElementById('adm-inv-qty').value);
  try {
    await api(`/api/admin/company/inventory/${good}?quantity=${quantity}`, { method: 'POST' });
    showMsg('msg-inventory', 'Aplicado.', false);
  } catch (err) { showMsg('msg-inventory', err.message, true); }
}

async function applyMarket() {
  const good_name = document.getElementById('adm-market-good').value;
  const price = Number(document.getElementById('adm-market-price').value);
  try {
    await api('/api/admin/market', { method: 'POST', body: JSON.stringify({ good_name, price }) });
    showMsg('msg-market', 'Aplicado.', false);
  } catch (err) { showMsg('msg-market', err.message, true); }
}

async function applyGameTime() {
  const game_minutes = Number(document.getElementById('adm-minutes').value);
  try {
    await api('/api/admin/game', { method: 'POST', body: JSON.stringify({ game_minutes }) });
    showMsg('msg-game', 'Aplicado.', false);
  } catch (err) { showMsg('msg-game', err.message, true); }
}

async function applyCompetitor(id) {
  const cash = Number(document.getElementById(`comp-cash-${id}`).value);
  const production_rate_per_hour = Number(document.getElementById(`comp-rate-${id}`).value);
  try {
    await api(`/api/admin/competitors/${id}`, {
      method: 'POST', body: JSON.stringify({ cash, production_rate_per_hour }),
    });
    showMsg('msg-competitors', 'Aplicado.', false);
  } catch (err) { showMsg('msg-competitors', err.message, true); }
}

async function resetGame() {
  if (!confirm('Isso apaga todo o progresso salvo. Confirma?')) return;
  try {
    await api('/api/admin/reset', { method: 'POST' });
    showMsg('msg-reset', 'Jogo resetado.', false);
    loadState();
  } catch (err) { showMsg('msg-reset', err.message, true); }
}

document.getElementById('adm-market-good').onchange = loadState;

loadState();
