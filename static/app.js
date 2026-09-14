const money = (v) => (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
const pct = (v) => `${(v * 100).toFixed(2)}%`;

async function api(path, options) {
  const res = await fetch(path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
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

let latestPrices = {};
let isRunning = true;

async function refreshAll() {
  try {
    const [company, market, land, offers, loans, balance, dre, cashflow, gameState, competitors] = await Promise.all([
      api('/api/company'), api('/api/market'), api('/api/land'), api('/api/banks/offers'),
      api('/api/loans'), api('/api/finance/balance'), api('/api/finance/dre'),
      api('/api/finance/cashflow'), api('/api/game/state'), api('/api/competitors'),
    ]);

    document.getElementById('s-name').textContent = company.name;
    document.getElementById('s-cash').textContent = money(company.cash);
    document.getElementById('s-day').textContent = `Dia ${company.day}`;
    document.getElementById('s-score').textContent = company.credit_score;
    document.getElementById('inv-materia_prima').textContent = company.inventory.materia_prima?.toFixed(1) ?? '0';
    document.getElementById('inv-produto').textContent = company.inventory.produto?.toFixed(1) ?? '0';
    document.getElementById('factory-count').textContent = company.factory_count;

    latestPrices = {};
    market.forEach((g) => { latestPrices[g.name] = g.current_price; });
    document.getElementById('price-materia_prima').textContent = money(latestPrices.materia_prima);
    document.getElementById('price-produto').textContent = money(latestPrices.produto);

    isRunning = gameState.running;
    document.getElementById('btn-pause').textContent = isRunning ? 'Pausar' : 'Retomar';
    document.getElementById('sel-speed').value = String(gameState.speed_multiplier);

    renderLand(land);
    renderOffers(offers);
    renderLoans(loans);
    renderBalance(balance);
    renderDre(dre);
    renderCashflow(cashflow);
    renderCompetitors(competitors);
  } catch (err) {
    console.error(err);
  }
}

function renderLand(plots) {
  const tbody = document.querySelector('#tbl-land tbody');
  tbody.innerHTML = '';
  plots.forEach((p) => {
    const tr = document.createElement('tr');
    if (p.owned) tr.className = 'owned';
    tr.innerHTML = `
      <td>${p.name}<br><small>${p.region}</small></td>
      <td>${money(p.price + p.terraforming_cost)}</td>
      <td class="${p.logistics_bonus >= 0 ? 'positive' : 'negative'}">${pct(p.logistics_bonus)}</td>
      <td>${p.capacity}</td>
      <td></td>`;
    const actionCell = tr.lastElementChild;
    if (!p.owned) {
      const btn = document.createElement('button');
      btn.textContent = 'Comprar';
      btn.onclick = () => buyLand(p.id);
      actionCell.appendChild(btn);
    } else {
      const btn = document.createElement('button');
      btn.className = 'secondary';
      btn.textContent = 'Construir fábrica';
      btn.onclick = () => buildFactory(p.id);
      actionCell.appendChild(btn);
    }
    tbody.appendChild(tr);
  });
}

function renderOffers(offers) {
  const tbody = document.querySelector('#tbl-offers tbody');
  const select = document.getElementById('sel-bank');
  tbody.innerHTML = '';
  select.innerHTML = '';
  offers.forEach((o) => {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${o.bank_name}</td><td>${o.eligible ? pct(o.annual_rate) : '-'}</td>
      <td>${o.eligible ? money(o.max_principal) : '-'}</td>
      <td>${o.eligible ? o.max_term_months + ' meses' : o.reason}</td>`;
    tbody.appendChild(tr);
    if (o.eligible) {
      const opt = document.createElement('option');
      opt.value = o.bank_id;
      opt.textContent = `${o.bank_name} (${pct(o.annual_rate)})`;
      select.appendChild(opt);
    }
  });
}

function renderLoans(loans) {
  const tbody = document.querySelector('#tbl-loans tbody');
  tbody.innerHTML = '';
  loans.filter((l) => l.status === 'ACTIVE').forEach((l) => {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${l.bank}</td><td>${money(l.remaining_balance)}</td>
      <td>${l.payment_type === 'PRICE' ? money(l.monthly_payment ?? 0) : 'variável (SAC)'}</td>
      <td>${l.next_payment_day}</td><td>${l.status}</td><td></td>`;
    const btn = document.createElement('button');
    btn.className = 'secondary';
    btn.textContent = 'Quitar antecipado';
    btn.onclick = () => payoffLoan(l.id);
    tr.lastElementChild.appendChild(btn);
    tbody.appendChild(tr);
  });
}

function renderCompetitors(rows) {
  const tbody = document.querySelector('#tbl-competitors tbody');
  tbody.innerHTML = '';
  rows.forEach((r) => {
    const tr = document.createElement('tr');
    if (r.is_player) tr.style.fontWeight = '700';
    tr.innerHTML = `
      <td>${r.is_player ? '⭐ ' : ''}${r.name}</td>
      <td>${money(r.valuation)}</td>
      <td>${r.total_produced === null ? '-' : r.total_produced}</td>`;
    tbody.appendChild(tr);
  });
}

function renderBalance(b) {
  document.getElementById('tbl-balance').innerHTML = `
    <tr><td>Caixa</td><td>${money(b.cash)}</td></tr>
    <tr><td>Estoque</td><td>${money(b.inventory_value)}</td></tr>
    <tr><td>Terrenos</td><td>${money(b.land_value)}</td></tr>
    <tr><td>Fábricas</td><td>${money(b.factory_value)}</td></tr>
    <tr><th>Total de ativos</th><th>${money(b.total_assets)}</th></tr>
    <tr><td>Passivo (empréstimos)</td><td>${money(b.total_liabilities)}</td></tr>
    <tr><th>Patrimônio líquido</th><th>${money(b.equity)}</th></tr>`;
}

function renderDre(d) {
  document.getElementById('tbl-dre').innerHTML = `
    <tr><td>Receita de vendas</td><td>${money(d.revenue)}</td></tr>
    <tr><td>(-) Custo de matéria-prima</td><td>${money(d.cogs)}</td></tr>
    <tr><th>Lucro bruto</th><th>${money(d.gross_profit)}</th></tr>
    <tr><td>(-) Manutenção (opex)</td><td>${money(d.opex_upkeep)}</td></tr>
    <tr><th>Lucro operacional</th><th>${money(d.operating_profit)}</th></tr>
    <tr><td>(-) Juros de empréstimos</td><td>${money(d.interest_expense)}</td></tr>
    <tr><th>Lucro líquido</th><th>${money(d.net_profit)}</th></tr>`;
}

function renderCashflow(c) {
  document.getElementById('tbl-cashflow').innerHTML = `
    <tr><td>Atividades operacionais</td><td>${money(c.operating_activities)}</td></tr>
    <tr><td>Atividades de financiamento</td><td>${money(c.financing_activities)}</td></tr>
    <tr><td>Atividades de investimento</td><td>${money(c.investing_activities)}</td></tr>
    <tr><th>Fluxo de caixa líquido</th><th>${money(c.net_cash_flow)}</th></tr>`;
}

async function buyMaterial() {
  const quantity = Number(document.getElementById('qty-buy').value);
  try {
    await api('/api/production/buy-material', { method: 'POST', body: JSON.stringify({ quantity }) });
    showMsg('msg-production', 'Compra realizada.', false);
    refreshAll();
  } catch (err) { showMsg('msg-production', err.message, true); }
}

async function sellProduct() {
  const quantity = Number(document.getElementById('qty-sell').value);
  try {
    await api('/api/production/sell-product', { method: 'POST', body: JSON.stringify({ quantity }) });
    showMsg('msg-production', 'Venda realizada.', false);
    refreshAll();
  } catch (err) { showMsg('msg-production', err.message, true); }
}

async function buyLand(plotId) {
  try {
    await api(`/api/land/${plotId}/buy`, { method: 'POST' });
    showMsg('msg-land', 'Terreno comprado.', false);
    refreshAll();
  } catch (err) { showMsg('msg-land', err.message, true); }
}

async function buildFactory(plotId) {
  try {
    await api(`/api/production/build-factory/${plotId}`, { method: 'POST' });
    showMsg('msg-land', 'Fábrica construída.', false);
    refreshAll();
  } catch (err) { showMsg('msg-land', err.message, true); }
}

async function requestLoan() {
  const bank_id = Number(document.getElementById('sel-bank').value);
  const principal = Number(document.getElementById('loan-principal').value);
  const term_months = Number(document.getElementById('loan-term').value);
  const payment_type = document.getElementById('loan-type').value;
  try {
    await api('/api/loans', { method: 'POST', body: JSON.stringify({ bank_id, principal, term_months, payment_type }) });
    showMsg('msg-bank', 'Empréstimo aprovado.', false);
    refreshAll();
  } catch (err) { showMsg('msg-bank', err.message, true); }
}

async function payoffLoan(loanId) {
  try {
    await api(`/api/loans/${loanId}/payoff`, { method: 'POST' });
    showMsg('msg-bank', 'Empréstimo quitado.', false);
    refreshAll();
  } catch (err) { showMsg('msg-bank', err.message, true); }
}

document.getElementById('btn-pause').onclick = async () => {
  const multiplier = Number(document.getElementById('sel-speed').value);
  await api('/api/game/speed', { method: 'POST', body: JSON.stringify({ multiplier, running: !isRunning }) });
  refreshAll();
};

document.getElementById('sel-speed').onchange = async (e) => {
  await api('/api/game/speed', { method: 'POST', body: JSON.stringify({ multiplier: Number(e.target.value), running: isRunning }) });
};

document.getElementById('btn-advance-day').onclick = async () => {
  await api('/api/game/advance', { method: 'POST', body: JSON.stringify({ minutes: 1440 }) });
  refreshAll();
};

refreshAll();
setInterval(refreshAll, 2000);
