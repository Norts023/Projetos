const money = (v) => (v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
const pct = (v) => `${(v * 100).toFixed(2)}%`;

const TIER_LABELS = {
  0: 'Tier 0 — Matérias-primas',
  1: 'Tier 1 — Processados',
  2: 'Tier 2 — Componentes',
  3: 'Tier 3 — Produtos finais',
};

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

// --- Sidebar navigation ---
document.querySelectorAll('.nav-btn').forEach((btn) => {
  btn.onclick = () => {
    document.querySelectorAll('.nav-btn').forEach((b) => b.classList.remove('active'));
    document.querySelectorAll('.page').forEach((p) => p.classList.remove('active'));
    btn.classList.add('active');
    document.getElementById(`page-${btn.dataset.page}`).classList.add('active');
  };
});

let isRunning = true;
let recipesCache = [];

async function refreshAll() {
  try {
    const [company, market, recipes, land, factories, offers, loans, balance, dre, cashflow, gameState, competitors] =
      await Promise.all([
        api('/api/company'), api('/api/market'), api('/api/market/recipes'), api('/api/land'),
        api('/api/production/factories'), api('/api/banks/offers'), api('/api/loans'),
        api('/api/finance/balance'), api('/api/finance/dre'), api('/api/finance/cashflow'),
        api('/api/game/state'), api('/api/competitors'),
      ]);
    recipesCache = recipes;

    document.getElementById('s-name').textContent = company.name;
    document.getElementById('s-cash').textContent = money(company.cash);
    document.getElementById('s-day').textContent = `Dia ${company.day}`;
    document.getElementById('s-score').textContent = company.credit_score;

    isRunning = gameState.running;
    document.getElementById('btn-pause').textContent = isRunning ? 'Pausar' : 'Retomar';
    document.getElementById('sel-speed').value = String(gameState.speed_multiplier);

    renderMarket(market, company);
    renderFactories(factories, 'tbl-factories');
    renderFactories(factories, 'tbl-overview-factories');
    renderBuildForm(land, factories, recipes);
    renderLand(land, factories);
    renderOffers(offers);
    renderLoans(loans);
    renderBalance(balance, 'tbl-balance');
    renderBalance(balance, 'tbl-overview-balance');
    renderDre(dre, 'tbl-dre');
    renderDre(dre, 'tbl-overview-dre');
    renderCashflow(cashflow);
    renderCompetitors(competitors);
  } catch (err) {
    console.error(err);
  }
}

function renderMarket(goods, company) {
  const container = document.getElementById('market-tiers');
  container.innerHTML = '';
  const byTier = {};
  goods.forEach((g) => { (byTier[g.tier] ||= []).push(g); });

  Object.keys(byTier).sort().forEach((tier) => {
    const heading = document.createElement('h3');
    heading.className = 'tier-heading';
    heading.textContent = TIER_LABELS[tier] || `Tier ${tier}`;
    container.appendChild(heading);

    const table = document.createElement('table');
    table.innerHTML = '<thead><tr><th>Bem</th><th>Preço</th><th>Seu estoque</th><th>Quantidade</th><th></th></tr></thead>';
    const tbody = document.createElement('tbody');
    byTier[tier].forEach((g) => {
      const stock = company.inventory[g.name] ?? 0;
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td>${g.label}${g.has_recipe ? '' : '<span class="tier-badge">bruto</span>'}</td>
        <td>${money(g.current_price)}</td>
        <td>${stock.toFixed(1)}</td>
        <td><input type="number" min="1" value="50" id="qty-${g.name}" style="width:80px"></td>
        <td></td>`;
      const actionCell = tr.lastElementChild;
      const buyBtn = document.createElement('button');
      buyBtn.textContent = 'Comprar';
      buyBtn.onclick = () => tradeGood(g.name, 'buy');
      const sellBtn = document.createElement('button');
      sellBtn.className = 'secondary';
      sellBtn.textContent = 'Vender';
      sellBtn.style.marginLeft = '4px';
      sellBtn.onclick = () => tradeGood(g.name, 'sell');
      actionCell.appendChild(buyBtn);
      actionCell.appendChild(sellBtn);
      tbody.appendChild(tr);
    });
    table.appendChild(tbody);
    container.appendChild(table);
  });
}

async function tradeGood(goodName, action) {
  const quantity = Number(document.getElementById(`qty-${goodName}`).value);
  try {
    await api(`/api/production/${action}`, { method: 'POST', body: JSON.stringify({ good_name: goodName, quantity }) });
    showMsg('msg-market', action === 'buy' ? 'Compra realizada.' : 'Venda realizada.', false);
    refreshAll();
  } catch (err) { showMsg('msg-market', err.message, true); }
}

function renderFactories(factories, tableId) {
  const tbody = document.querySelector(`#${tableId} tbody`);
  tbody.innerHTML = '';
  const showLevel = tableId === 'tbl-factories';
  factories.forEach((f) => {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${f.recipe_label}</td><td>${f.output_label}</td><td>${f.land_plot}</td>` +
      (showLevel ? `<td>${f.level}</td>` : '');
    tbody.appendChild(tr);
  });
}

function renderBuildForm(land, factories, recipes) {
  const plotSelect = document.getElementById('sel-build-plot');
  const recipeSelect = document.getElementById('sel-build-recipe');
  plotSelect.innerHTML = '';
  recipeSelect.innerHTML = '';

  land.filter((p) => p.owned).forEach((p) => {
    const used = factories.filter((f) => f.land_plot_id === p.id).length;
    const free = p.capacity - used;
    const opt = document.createElement('option');
    opt.value = p.id;
    opt.textContent = `${p.name} (${free}/${p.capacity} livre)`;
    opt.disabled = free <= 0;
    plotSelect.appendChild(opt);
  });

  recipes.forEach((r) => {
    const opt = document.createElement('option');
    opt.value = r.id;
    opt.textContent = `${r.label} — ${money(r.build_cost)} (Tier ${r.tier})`;
    recipeSelect.appendChild(opt);
  });
}

async function buildFactory() {
  const plotId = Number(document.getElementById('sel-build-plot').value);
  const recipeId = document.getElementById('sel-build-recipe').value;
  if (!plotId) { showMsg('msg-build', 'Você precisa possuir um terreno com capacidade livre.', true); return; }
  try {
    await api(`/api/production/build-factory/${plotId}`, { method: 'POST', body: JSON.stringify({ recipe_id: recipeId }) });
    showMsg('msg-build', 'Fábrica construída.', false);
    refreshAll();
  } catch (err) { showMsg('msg-build', err.message, true); }
}

function renderLand(plots, factories) {
  const tbody = document.querySelector('#tbl-land tbody');
  tbody.innerHTML = '';
  plots.forEach((p) => {
    const used = factories.filter((f) => f.land_plot_id === p.id).length;
    const tr = document.createElement('tr');
    if (p.owned) tr.className = 'owned';
    tr.innerHTML = `
      <td>${p.name}<br><small>${p.region}</small></td>
      <td>${money(p.price + p.terraforming_cost)}</td>
      <td class="${p.logistics_bonus >= 0 ? 'positive' : 'negative'}">${pct(p.logistics_bonus)}</td>
      <td>${p.capacity}</td>
      <td>${p.owned ? `${used}/${p.capacity}` : '-'}</td>
      <td></td>`;
    const actionCell = tr.lastElementChild;
    if (!p.owned) {
      const btn = document.createElement('button');
      btn.textContent = 'Comprar';
      btn.onclick = () => buyLand(p.id);
      actionCell.appendChild(btn);
    }
    tbody.appendChild(tr);
  });
}

async function buyLand(plotId) {
  try {
    await api(`/api/land/${plotId}/buy`, { method: 'POST' });
    showMsg('msg-land', 'Terreno comprado.', false);
    refreshAll();
  } catch (err) { showMsg('msg-land', err.message, true); }
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

function renderBalance(b, tableId) {
  document.getElementById(tableId).innerHTML = `
    <tr><td>Caixa</td><td>${money(b.cash)}</td></tr>
    <tr><td>Estoque</td><td>${money(b.inventory_value)}</td></tr>
    <tr><td>Terrenos</td><td>${money(b.land_value)}</td></tr>
    <tr><td>Fábricas</td><td>${money(b.factory_value)}</td></tr>
    <tr><th>Total de ativos</th><th>${money(b.total_assets)}</th></tr>
    <tr><td>Passivo (empréstimos)</td><td>${money(b.total_liabilities)}</td></tr>
    <tr><th>Patrimônio líquido</th><th>${money(b.equity)}</th></tr>`;
}

function renderDre(d, tableId) {
  document.getElementById(tableId).innerHTML = `
    <tr><td>Receita de vendas</td><td>${money(d.revenue)}</td></tr>
    <tr><td>(-) Custo de mercadorias</td><td>${money(d.cogs)}</td></tr>
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

function renderCompetitors(rows) {
  const tbody = document.querySelector('#tbl-competitors tbody');
  tbody.innerHTML = '';
  rows.forEach((r) => {
    const tr = document.createElement('tr');
    if (r.is_player) tr.style.fontWeight = '700';
    tr.innerHTML = `
      <td>${r.is_player ? '⭐ ' : ''}${r.name}</td>
      <td>${r.product_label ?? '-'}</td>
      <td>${money(r.valuation)}</td>
      <td>${r.total_produced === null ? '-' : r.total_produced}</td>`;
    tbody.appendChild(tr);
  });
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
