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

// --- Sidebar collapsible groups ---
document.querySelectorAll('.nav-group-header').forEach((header) => {
  header.onclick = () => {
    header.classList.toggle('collapsed');
    document.querySelector(`[data-group-items="${header.dataset.group}"]`).classList.toggle('collapsed');
  };
});

let isRunning = true;
let recipesCache = [];

// Fetches every endpoint independently (instead of Promise.all) so that a
// single failing request — e.g. a stale local save missing a new column —
// can't blank out the entire UI. Each failure is logged and its dependent
// section is simply skipped for that refresh instead of aborting everything.
async function fetchOrNull(name, path) {
  try {
    return await api(path);
  } catch (err) {
    console.error(`Falha ao carregar ${name}:`, err);
    return null;
  }
}

async function refreshAll() {
  const [company, market, recipes, land, factories, offers, loans, balance, dre, cashflow, gameState, competitors, goalsList, retailOrders] =
    await Promise.all([
      fetchOrNull('company', '/api/company'),
      fetchOrNull('market', '/api/market'),
      fetchOrNull('recipes', '/api/market/recipes'),
      fetchOrNull('land', '/api/land'),
      fetchOrNull('factories', '/api/production/factories'),
      fetchOrNull('offers', '/api/banks/offers'),
      fetchOrNull('loans', '/api/loans'),
      fetchOrNull('balance', '/api/finance/balance'),
      fetchOrNull('dre', '/api/finance/dre'),
      fetchOrNull('cashflow', '/api/finance/cashflow'),
      fetchOrNull('gameState', '/api/game/state'),
      fetchOrNull('competitors', '/api/competitors'),
      fetchOrNull('goals', '/api/goals'),
      fetchOrNull('retailOrders', '/api/retail/orders'),
    ]);

  if (company) {
    document.getElementById('s-name').textContent = company.name;
    document.getElementById('s-cash').textContent = money(company.cash);
    document.getElementById('s-day').textContent = `Dia ${company.day}`;
    document.getElementById('s-score').textContent = company.credit_score;
    document.getElementById('s-level').textContent = company.company_level;
  }

  if (gameState) {
    latestGameMinutes = gameState.game_minutes;
    isRunning = gameState.running;
    document.getElementById('btn-pause').textContent = isRunning ? 'Pausar' : 'Retomar';
    document.getElementById('sel-speed').value = String(gameState.speed_multiplier);
    document.getElementById('s-boost-wrap').style.display = gameState.beginner_boost_active ? 'flex' : 'none';
    if (gameState.beginner_boost_active) {
      document.getElementById('s-boost').textContent = `2x produção (${gameState.beginner_boost_days_left}d restantes)`;
    }
  }

  if (recipes) recipesCache = recipes;
  if (market && company) { renderMarket(market, company); renderRetailGoodOptions(market); }
  if (retailOrders) renderRetailOrders(retailOrders);
  if (factories) { renderFactories(factories, 'tbl-factories'); renderFactories(factories, 'tbl-overview-factories'); }
  if (land && factories && recipes) renderBuildForm(land, factories, recipes);
  if (land && factories) { renderLand(land, factories); renderMap(land, factories); }
  if (offers) renderOffers(offers);
  if (loans) renderLoans(loans);
  if (balance) { renderBalance(balance, 'tbl-balance'); renderBalance(balance, 'tbl-overview-balance'); }
  if (dre) { renderDre(dre, 'tbl-dre'); renderDre(dre, 'tbl-overview-dre'); }
  if (cashflow) renderCashflow(cashflow);
  if (competitors) renderCompetitors(competitors);
  if (goalsList) renderGoals(goalsList);
}

let goodsLabelMap = {};

let previousPrices = {};

function renderTicker(goods) {
  const ticker = document.getElementById('price-ticker');
  ticker.innerHTML = '';
  goods.forEach((g) => {
    const prev = previousPrices[g.name];
    let arrow = '';
    let cls = '';
    if (prev !== undefined && g.current_price !== prev) {
      arrow = g.current_price > prev ? '▲' : '▼';
      cls = g.current_price > prev ? 'positive' : 'negative';
    }
    const item = document.createElement('div');
    item.className = 'ticker-item';
    item.innerHTML = `<span class="t-name">${g.label}</span><span class="t-price ${cls}">${money(g.current_price)} ${arrow}</span>`;
    ticker.appendChild(item);
  });
  goods.forEach((g) => { previousPrices[g.name] = g.current_price; });
}

async function openHistory(query, title) {
  document.getElementById('history-modal-title').textContent = title;
  document.getElementById('history-modal').classList.remove('hidden');
  const tbody = document.querySelector('#tbl-history tbody');
  tbody.innerHTML = '<tr><td colspan="4">Carregando...</td></tr>';
  try {
    const rows = await api(`/api/finance/ledger?q=${encodeURIComponent(query)}&limit=50`);
    tbody.innerHTML = '';
    if (!rows.length) { tbody.innerHTML = '<tr><td colspan="4">Nenhuma transação encontrada ainda.</td></tr>'; return; }
    rows.forEach((r) => {
      const tr = document.createElement('tr');
      const cls = r.amount >= 0 ? 'positive' : 'negative';
      tr.innerHTML = `<td>Dia ${r.day}</td><td>${r.entry_type}</td><td>${r.description}</td><td class="${cls}">${money(r.amount)}</td>`;
      tbody.appendChild(tr);
    });
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="4">${err.message}</td></tr>`;
  }
}

function closeHistoryModal() {
  document.getElementById('history-modal').classList.add('hidden');
}

function renderMarket(goods, company) {
  goods.forEach((g) => { goodsLabelMap[g.name] = g.label; });
  renderTicker(goods);
  const container = document.getElementById('market-tiers');
  // The quantity inputs are rebuilt below; remember what the player already
  // typed so the periodic refresh doesn't wipe it out mid-edit.
  const previousQuantities = {};
  container.querySelectorAll('input[id^="qty-"]').forEach((input) => {
    previousQuantities[input.id] = input.value;
  });
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
        <td><input type="number" min="1" value="${previousQuantities[`qty-${g.name}`] ?? 50}" id="qty-${g.name}" style="width:80px"></td>
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
      const histBtn = document.createElement('button');
      histBtn.className = 'link';
      histBtn.textContent = 'Histórico';
      histBtn.onclick = () => openHistory(g.label, `Histórico — ${g.label}`);
      actionCell.appendChild(buyBtn);
      actionCell.appendChild(sellBtn);
      actionCell.appendChild(histBtn);
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

let retailGoodOptionsBuilt = false;

function renderRetailGoodOptions(goods) {
  if (retailGoodOptionsBuilt) return;
  const select = document.getElementById('retail-good');
  goods.forEach((g) => {
    const opt = document.createElement('option');
    opt.value = g.name;
    opt.textContent = g.label;
    select.appendChild(opt);
  });
  retailGoodOptionsBuilt = true;
  updateRetailEstimate();
}

async function updateRetailEstimate() {
  const good_name = document.getElementById('retail-good').value;
  const price = Number(document.getElementById('retail-price').value);
  const el = document.getElementById('retail-estimate');
  if (!good_name || !price) { el.textContent = ''; return; }
  try {
    const est = await api(`/api/retail/estimate?good_name=${good_name}&price=${price}`);
    el.textContent = `Preço de mercado: ${money(est.market_price)} — velocidade estimada: ${est.estimated_rate_per_hour} un/h`;
  } catch (err) { el.textContent = ''; }
}
document.getElementById('retail-price').addEventListener('input', updateRetailEstimate);
document.getElementById('retail-good').addEventListener('change', updateRetailEstimate);

async function createRetailOrder() {
  const good_name = document.getElementById('retail-good').value;
  const quantity = Number(document.getElementById('retail-qty').value);
  const price_per_unit = Number(document.getElementById('retail-price').value);
  try {
    await api('/api/retail/orders', { method: 'POST', body: JSON.stringify({ good_name, quantity, price_per_unit }) });
    showMsg('msg-retail', 'Pedido colocado à venda.', false);
    refreshAll();
  } catch (err) { showMsg('msg-retail', err.message, true); }
}

async function cancelRetailOrder(orderId) {
  try {
    await api(`/api/retail/orders/${orderId}/cancel`, { method: 'POST' });
    showMsg('msg-retail', 'Pedido cancelado, estoque devolvido.', false);
    refreshAll();
  } catch (err) { showMsg('msg-retail', err.message, true); }
}

function renderRetailOrders(orders) {
  const tbody = document.querySelector('#tbl-retail-orders tbody');
  tbody.innerHTML = '';
  orders.forEach((o) => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${o.good_label}</td>
      <td>${money(o.price_per_unit)}</td>
      <td>${o.quantity_remaining} / ${o.quantity_original}</td>
      <td>${o.estimated_rate_per_hour} un/h</td>
      <td>${o.estimated_hours_left !== null ? o.estimated_hours_left + 'h' : '-'}</td>
      <td></td>`;
    const btn = document.createElement('button');
    btn.className = 'secondary';
    btn.textContent = 'Cancelar';
    btn.onclick = () => cancelRetailOrder(o.id);
    tr.lastElementChild.appendChild(btn);
    tbody.appendChild(tr);
  });
}

function formatDuration(minutes) {
  if (minutes <= 0) return 'pronto';
  const h = Math.floor(minutes / 60);
  const m = Math.round(minutes % 60);
  return h > 0 ? `${h}h ${m}min` : `${m}min`;
}

let latestGameMinutes = 0;

function renderFactories(factories, tableId) {
  const tbody = document.querySelector(`#${tableId} tbody`);
  tbody.innerHTML = '';

  if (tableId === 'tbl-overview-factories') {
    factories.forEach((f) => {
      const tr = document.createElement('tr');
      tr.innerHTML = `<td>${f.recipe_label}</td><td>${f.output_label}</td><td>${f.land_plot}</td>`;
      tbody.appendChild(tr);
    });
    return;
  }

  factories.forEach((f) => {
    const tr = document.createElement('tr');
    const inputsText = f.inputs.map((i) => `${i.label}: ${i.stock}`).join(', ') || '-';
    let statusText = 'Ativa';
    if (f.status !== 'ACTIVE') {
      const remaining = f.busy_until_minutes - latestGameMinutes;
      const verb = f.status === 'BUILDING' ? 'Construindo' : 'Melhorando';
      statusText = `${verb} — ${formatDuration(remaining)}`;
    }
    tr.innerHTML = `
      <td>${f.recipe_label}</td>
      <td>${f.level}</td>
      <td>${f.land_plot}</td>
      <td title="${inputsText}">${inputsText}</td>
      <td>${statusText}</td>
      <td></td>`;
    const actionCell = tr.lastElementChild;
    if (f.status === 'ACTIVE') {
      const upgradeBtn = document.createElement('button');
      upgradeBtn.className = 'secondary';
      const plan = f.upgrade_plan;
      upgradeBtn.textContent = 'Melhorar';
      if (plan) {
        const materialsText = Object.entries(plan.materials)
          .map(([good, qty]) => `${qty} ${goodsLabelMap[good] || good}`).join(', ');
        upgradeBtn.title = `${money(plan.cash_cost)} + ${materialsText} — pronto em ${formatDuration(plan.minutes)}`;
      }
      upgradeBtn.onclick = () => upgradeFactory(f.id);
      actionCell.appendChild(upgradeBtn);
    } else {
      const rushBtn = document.createElement('button');
      rushBtn.textContent = 'Apressar (pagar)';
      rushBtn.onclick = () => rushFactory(f.id);
      actionCell.appendChild(rushBtn);
    }
    const histBtn = document.createElement('button');
    histBtn.className = 'link';
    histBtn.textContent = 'Histórico';
    histBtn.onclick = () => openHistory(f.output_label, `Histórico — ${f.recipe_label}`);
    actionCell.appendChild(histBtn);
    tbody.appendChild(tr);
  });
}

async function upgradeFactory(factoryId) {
  try {
    const res = await api(`/api/production/factories/${factoryId}/upgrade`, { method: 'POST' });
    showMsg('msg-build', `Melhoria iniciada, pronta em ${formatDuration(res.plan.minutes)}.`, false);
    refreshAll();
  } catch (err) { showMsg('msg-build', err.message, true); }
}

async function rushFactory(factoryId) {
  try {
    const res = await api(`/api/production/factories/${factoryId}/rush`, { method: 'POST' });
    showMsg('msg-build', `Pago ${money(res.paid)} para apressar.`, false);
    refreshAll();
  } catch (err) { showMsg('msg-build', err.message, true); }
}

let selectedRecipeId = null;

function renderBuildForm(land, factories, recipes) {
  const plotSelect = document.getElementById('sel-build-plot');
  // Rebuilding the <select> options wipes out whatever the player had
  // clicked, so remember the selection and restore it afterwards instead
  // of always snapping back to the first option.
  const previousPlot = plotSelect.value;
  plotSelect.innerHTML = '';

  land.filter((p) => p.owned).forEach((p) => {
    const used = factories.filter((f) => f.land_plot_id === p.id).length;
    const free = p.capacity - used;
    const opt = document.createElement('option');
    opt.value = p.id;
    opt.textContent = `${p.name} (${free}/${p.capacity} livre)`;
    opt.disabled = free <= 0;
    plotSelect.appendChild(opt);
  });
  if ([...plotSelect.options].some((o) => o.value === previousPlot)) plotSelect.value = previousPlot;

  const wrap = document.getElementById('build-catalog-wrap');
  wrap.innerHTML = '';
  const bySector = {};
  recipes.forEach((r) => { (bySector[r.sector] = bySector[r.sector] || []).push(r); });

  Object.keys(bySector).sort().forEach((sector) => {
    const heading = document.createElement('div');
    heading.className = 'sector-heading';
    heading.innerHTML = `<span>${bySector[sector][0].sector_icon}</span><span>${sector}</span>`;
    wrap.appendChild(heading);

    const catalog = document.createElement('div');
    catalog.className = 'build-catalog';
    bySector[sector].forEach((r) => {
      const card = document.createElement('div');
      card.className = 'build-card' + (r.id === selectedRecipeId ? ' selected' : '');
      card.dataset.recipeId = r.id;
      const profitClass = r.profit_per_hour >= 0 ? 'positive' : 'negative';
      const profitLabel = r.profit_per_hour >= 0
        ? `+${money(r.profit_per_hour)}/h`
        : `${money(r.profit_per_hour)}/h`;
      card.innerHTML = `
        <div class="bc-icon">${r.sector_icon}</div>
        <div class="bc-name">${r.label}<span class="tier-badge">T${r.tier}</span></div>
        <div class="bc-meta">Custo: ${money(r.build_cost)}</div>
        <div class="bc-profit ${profitClass}">${profitLabel}</div>`;
      card.onclick = () => selectBuildRecipe(r.id);
      catalog.appendChild(card);
    });
    wrap.appendChild(catalog);
  });

  updateBuildPlanPreview();
}

function selectBuildRecipe(recipeId) {
  selectedRecipeId = recipeId;
  document.querySelectorAll('.build-card').forEach((c) => {
    c.classList.toggle('selected', c.dataset.recipeId === recipeId);
  });
  updateBuildPlanPreview();
}

async function updateBuildPlanPreview() {
  const el = document.getElementById('build-plan-preview');
  if (!selectedRecipeId) { el.textContent = ''; return; }
  try {
    const plan = await api(`/api/production/build-plan?recipe_id=${selectedRecipeId}`);
    const materials = Object.entries(plan.materials)
      .map(([good, qty]) => `${qty} ${goodsLabelMap[good] || good}`).join(', ');
    el.innerHTML = `Custo: ${money(plan.cash_cost)} + ${materials} — pronto em ${formatDuration(plan.minutes)} `;
    const btn = document.createElement('button');
    btn.textContent = 'Construir';
    btn.style.marginLeft = '8px';
    btn.onclick = buildFactory;
    el.appendChild(btn);
  } catch (err) { el.textContent = ''; }
}

async function buildFactory() {
  const plotId = Number(document.getElementById('sel-build-plot').value);
  if (!plotId) { showMsg('msg-build', 'Você precisa possuir um terreno com capacidade livre.', true); return; }
  if (!selectedRecipeId) { showMsg('msg-build', 'Escolha uma fábrica no catálogo.', true); return; }
  try {
    await api(`/api/production/build-factory/${plotId}`, { method: 'POST', body: JSON.stringify({ recipe_id: selectedRecipeId }) });
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

async function buyLandFromMap(plotId) {
  try {
    await api(`/api/land/${plotId}/buy`, { method: 'POST' });
    showMsg('msg-map', 'Terreno comprado.', false);
    refreshAll();
  } catch (err) { showMsg('msg-map', err.message, true); }
}

function goToBuildPlot(plotId) {
  document.querySelector('.nav-btn[data-page="production"]').click();
  document.getElementById('sel-build-plot').value = String(plotId);
  updateBuildPlanPreview();
}

function renderMap(plots, factories) {
  const grid = document.getElementById('map-grid');
  grid.innerHTML = '';
  plots
    .slice()
    .sort((a, b) => (a.owned === b.owned ? a.name.localeCompare(b.name) : a.owned ? -1 : 1))
    .forEach((p) => {
      const tile = document.createElement('div');
      if (!p.owned) {
        tile.className = 'map-tile empty';
        tile.onclick = () => buyLandFromMap(p.id);
        tile.innerHTML = `
          <div class="mt-name">🏞️ ${p.name}</div>
          <div class="mt-region">${p.region}</div>
          <div class="mt-price">${money(p.price + p.terraforming_cost)}</div>
          <div class="mt-bonus ${p.logistics_bonus >= 0 ? 'positive' : 'negative'}">Logística: ${pct(p.logistics_bonus)}</div>
          <button class="mt-buy-btn">Comprar</button>`;
        grid.appendChild(tile);
        return;
      }

      const plotFactories = factories.filter((f) => f.land_plot_id === p.id);
      tile.className = 'map-tile';
      tile.innerHTML = `
        <div class="mt-header">
          <div><div class="mt-name">${p.name}</div><div class="mt-region">${p.region}</div></div>
          <div class="mt-capacity">${plotFactories.length}/${p.capacity}</div>
        </div>
        <div class="mt-bonus ${p.logistics_bonus >= 0 ? 'positive' : 'negative'}">Logística: ${pct(p.logistics_bonus)}</div>
        <div class="mt-factories"></div>`;
      const factoriesRow = tile.querySelector('.mt-factories');

      plotFactories.forEach((f) => {
        const fEl = document.createElement('div');
        fEl.className = 'map-factory';
        let statusHtml = '<span class="mf-status">Ativa</span>';
        let title = `${f.recipe_label} (nível ${f.level})`;
        if (f.status !== 'ACTIVE') {
          const remaining = f.busy_until_minutes - latestGameMinutes;
          const verb = f.status === 'BUILDING' ? 'Construindo' : 'Melhorando';
          const cls = f.status === 'BUILDING' ? 'building' : 'upgrading';
          statusHtml = `<span class="mf-status ${cls}">${formatDuration(remaining)}</span>`;
          title += ` — ${verb}: ${formatDuration(remaining)}`;
        }
        fEl.title = title;
        fEl.innerHTML = `<span class="mf-icon">${f.sector_icon || '🏭'}</span><span class="mf-level">Nv ${f.level}</span>${statusHtml}`;
        factoriesRow.appendChild(fEl);
      });

      for (let i = plotFactories.length; i < p.capacity; i++) {
        const slot = document.createElement('div');
        slot.className = 'mt-slot-empty';
        slot.textContent = '+';
        slot.title = 'Construir fábrica aqui';
        slot.onclick = (ev) => { ev.stopPropagation(); goToBuildPlot(p.id); };
        factoriesRow.appendChild(slot);
      }

      grid.appendChild(tile);
    });
}

function renderOffers(offers) {
  const tbody = document.querySelector('#tbl-offers tbody');
  const select = document.getElementById('sel-bank');
  const previousBank = select.value;
  tbody.innerHTML = '';
  select.innerHTML = '';
  offers.forEach((o) => {
    const tr = document.createElement('tr');
    const rateLabel = o.eligible
      ? (o.max_annual_rate > o.annual_rate ? `${pct(o.annual_rate)} – ${pct(o.max_annual_rate)}` : pct(o.annual_rate))
      : '-';
    tr.innerHTML = `<td>${o.bank_name}</td><td>${rateLabel}</td>
      <td>${o.eligible ? money(o.max_principal) : '-'}</td>
      <td>${o.eligible ? o.max_term_months + ' meses' : o.reason}</td>`;
    tbody.appendChild(tr);
    if (o.eligible) {
      const opt = document.createElement('option');
      opt.value = o.bank_id;
      opt.textContent = `${o.bank_name} (${rateLabel})`;
      select.appendChild(opt);
    }
  });
  if ([...select.options].some((o) => o.value === previousBank)) select.value = previousBank;
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
    <tr><td>(-) Taxas de mercado</td><td>${money(d.market_fees)}</td></tr>
    <tr><th>Lucro operacional</th><th>${money(d.operating_profit)}</th></tr>
    <tr><td>(-) Juros de empréstimos</td><td>${money(d.interest_expense)}</td></tr>
    <tr><td>(+) Recompensas de metas</td><td>${money(d.goal_rewards)}</td></tr>
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

function renderGoals(goalsList) {
  const tbody = document.querySelector('#tbl-goals tbody');
  tbody.innerHTML = '';
  goalsList.forEach((g) => {
    const tr = document.createElement('tr');
    if (g.achieved) tr.style.opacity = '0.6';
    tr.innerHTML = `
      <td>${g.achieved ? '✅' : '⬜'}</td>
      <td>${g.label}</td>
      <td>${g.description}${g.achieved ? ` <small>(dia ${g.achieved_day})</small>` : ''}</td>
      <td>${money(g.reward_cash)}</td>`;
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
