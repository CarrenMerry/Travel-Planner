/**
 * Smart Travel Planner & Route Optimizer
 * Main Application Logic & Interactive UI Controller
 */

const App = {
  currentView: 'dashboard',
  allDestinations: [],
  itinerary: [],
  graphData: { vertices: [], edges: [] },
  editingDestId: null,

  init() {
    this.bindEvents();
    this.checkAuth();
    this.loadAllData();
  },

  bindEvents() {
    // Navigation
    document.querySelectorAll('.nav-link').forEach(link => {
      link.addEventListener('click', (e) => {
        const view = link.getAttribute('data-view');
        this.navigateTo(view);
      });
    });

    // Top actions
    document.getElementById('btn-quick-queue').addEventListener('click', () => {
      this.navigateTo('dsa-lab');
    });

    // Destination Details Modal (User-Facing)
    document.getElementById('btn-close-detail-modal').addEventListener('click', () => this.closeDetailModal());
    document.getElementById('btn-cancel-detail-modal').addEventListener('click', () => this.closeDetailModal());
    document.getElementById('btn-add-from-modal').addEventListener('click', () => {
      if (this.currentDetailId) {
        this.addToItinerary(this.currentDetailId);
        this.closeDetailModal();
      }
    });

    // Explore Page
    document.getElementById('btn-apply-sort').addEventListener('click', () => this.applySort());
    const searchInput = document.getElementById('explore-search-input');
    searchInput.addEventListener('keyup', (e) => {
      if (e.key === 'Enter') this.applySearch();
    });

    // Itinerary Page
    document.getElementById('btn-undo-action').addEventListener('click', () => this.undoItinerary());
    document.getElementById('btn-itinerary-undo').addEventListener('click', () => this.undoItinerary());
    document.getElementById('btn-clear-itinerary').addEventListener('click', () => this.clearItinerary());
    document.getElementById('btn-quick-eval').addEventListener('click', () => this.evaluateQuickBudget());

    // Route Planner Page
    document.getElementById('btn-run-bfs').addEventListener('click', () => this.runBfs());
    document.getElementById('btn-run-dfs').addEventListener('click', () => this.runDfs());
    document.getElementById('btn-run-dijkstra').addEventListener('click', () => this.runDijkstra());

    // Optimization Tabs
    document.querySelectorAll('.tab-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
        btn.classList.add('active');
        const targetId = btn.getAttribute('data-tab');
        document.getElementById(targetId).classList.add('active');
      });
    });

    document.getElementById('btn-run-greedy').addEventListener('click', () => this.runGreedy());
    document.getElementById('btn-run-dp').addEventListener('click', () => this.runDp());
    document.getElementById('btn-run-backtrack').addEventListener('click', () => this.runBacktrack());

    // DSA Laboratory
    document.getElementById('btn-lab-eval').addEventListener('click', () => this.labEvaluateExpression());
    document.getElementById('btn-lab-bst-search').addEventListener('click', () => this.labSearchBst());
    document.getElementById('btn-lab-bst-traversals').addEventListener('click', () => this.labBstTraversals());
    document.getElementById('btn-lab-tree-traversals').addEventListener('click', () => this.labTreeTraversals());
    document.getElementById('btn-lab-enqueue').addEventListener('click', () => this.labEnqueue());
    document.getElementById('btn-lab-dequeue').addEventListener('click', () => this.labDequeue());
    document.getElementById('btn-lab-run-sort-bench').addEventListener('click', () => this.labRunSortBench());
    document.getElementById('btn-lab-run-search-bench').addEventListener('click', () => this.labRunSearchBench());
  },

  navigateTo(viewId) {
    this.currentView = viewId;
    document.querySelectorAll('.nav-link').forEach(link => {
      link.classList.toggle('active', link.getAttribute('data-view') === viewId);
    });

    document.querySelectorAll('.view-section').forEach(sec => {
      sec.classList.toggle('active', sec.id === `view-${viewId}`);
    });

    // Update Header Titles
    const titleMap = {
      'dashboard': { title: 'Dashboard Overview', sub: 'Algorithmic Travel Management & Performance Dashboard' },
      'explore': { title: 'Explore Destinations', sub: 'Search & Sort Destination Catalog with Explicit Java Algorithms' },
      'itinerary': { title: 'My Itinerary Planner', sub: 'Ordered Linked List Navigation with Stack-Based Undo' },
      'route': { title: 'Transit Route Planner', sub: 'Custom Graph Visualization with BFS, DFS, and Dijkstra Exploration' },
      'optimization': { title: 'Smart Route Optimization', sub: 'Greedy Heuristic, Dynamic Programming Knapsack, and Constraint Backtracking' },
      'dsa-lab': { title: 'Academic DSA Laboratory', sub: 'Direct Verification & Interactive Exploration of All 11 Core Concepts' }
    };

    if (titleMap[viewId]) {
      document.getElementById('top-page-title').textContent = titleMap[viewId].title;
      document.getElementById('top-page-subtitle').textContent = titleMap[viewId].sub;
    }

    if (viewId === 'route') {
      setTimeout(() => this.renderGraph(), 100);
    }
  },

  async loadAllData() {
    try {
      await Promise.all([
        this.loadDashboard(),
        this.loadDestinations(),
        this.loadItinerary(),
        this.loadGraphData(),
        this.loadQueueData()
      ]);
    } catch (err) {
      console.error('Error loading initial data:', err);
    }
  },

  // ==========================================
  // DASHBOARD
  // ==========================================
  async loadDashboard() {
    const data = await API.getDashboard();
    document.getElementById('dash-dest-count').textContent = data.totalDestinations || '0';
    document.getElementById('dash-itinerary-count').textContent = data.itineraryCount || '0';
    document.getElementById('dash-total-cost').textContent = '$' + (data.totalCost || 0).toLocaleString();
    document.getElementById('dash-total-duration').textContent = (data.totalDuration || 0) + ' Days';
    document.getElementById('header-queue-badge').textContent = data.pendingQueueCount || '0';

    // Render popular destinations
    const popularContainer = document.getElementById('dash-popular-grid');
    if (popularContainer && data.popularDestinations) {
      popularContainer.innerHTML = data.popularDestinations.map(d => this.renderDestinationCardHtml(d, true)).join('');
    }

    // Render recent activities
    const actContainer = document.getElementById('dash-recent-activity');
    if (actContainer && data.recentActivities) {
      actContainer.innerHTML = data.recentActivities.map((act, idx) => `
        <div class="timeline-step">
          <span>${act}</span>
          <span style="font-size:11px; color:var(--text-muted);">Step #${data.recentActivities.length - idx}</span>
        </div>
      `).join('');
    }
  },

  // ==========================================
  // DESTINATIONS & EXPLORE
  // ==========================================
  async loadDestinations() {
    const res = await API.getDestinations();
    this.allDestinations = res.destinations || [];
    this.renderDestinationsGrid(this.allDestinations);
    this.populateDestinationDropdowns();
  },

  renderDestinationsGrid(destinations) {
    const container = document.getElementById('explore-dest-grid');
    if (!container) return;

    if (!destinations || destinations.length === 0) {
      container.innerHTML = `
        <div style="grid-column: 1/-1; text-align: center; padding: 48px 20px; background: #fff; border-radius: var(--radius-lg); border: 1px dashed var(--border-light);">
          <h4 style="font-size: 16px; color: var(--text-main); margin-bottom: 6px;">No destinations found</h4>
          <p style="font-size: 13px; color: var(--text-secondary);">Try a different query or reset sorting parameters.</p>
        </div>
      `;
      return;
    }

    container.innerHTML = destinations.map(d => this.renderDestinationCardHtml(d, false)).join('');
  },

  renderDestinationCardHtml(d, isMini = false) {
    const activitiesCount = d.activities ? d.activities.length : 0;
    return `
      <div class="dest-card" id="card-${d.id}">
        <div class="dest-image-container">
          <img class="dest-image" src="${d.imageUrl}" alt="${d.name}" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=800&q=80'">
          <span class="dest-id-badge">${d.id}</span>
          <span class="dest-rating-badge">★ ${d.rating.toFixed(1)}</span>
        </div>
        <div class="dest-content">
          <h4 class="dest-name">${d.name}</h4>
          <div class="dest-location">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
            ${d.location}
          </div>
          <p class="dest-desc">${d.description}</p>
          <div class="dest-meta">
            <div>
              <span class="dest-cost">$${d.estimatedCost.toLocaleString()}</span>
              <span style="font-size:12px; color:var(--text-muted);"> / visit</span>
            </div>
            <span class="dest-duration">${d.visitDuration} days • ${activitiesCount} activities</span>
          </div>
          <div class="dest-actions">
            <button class="btn btn-primary btn-sm" onclick="App.addToItinerary('${d.id}')">
              <svg viewBox="0 0 24 24" width="13" height="13" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
              Add to Itinerary
            </button>
            <button class="btn btn-secondary btn-sm" onclick="App.viewDetails('${d.id}')">
              View Details
            </button>
          </div>
        </div>
      </div>
    `;
  },

  populateDestinationDropdowns() {
    const startSelect = document.getElementById('graph-start-node');
    const targetSelect = document.getElementById('graph-target-node');
    const greedySelect = document.getElementById('greedy-start-hub');
    const edgeFrom = document.getElementById('edge-form-from');
    const edgeTo = document.getElementById('edge-form-to');

    const options = this.allDestinations.map(d => `<option value="${d.id}">${d.name} (${d.location})</option>`).join('');

    if (startSelect) startSelect.innerHTML = options;
    if (targetSelect) {
      targetSelect.innerHTML = options;
      if (this.allDestinations.length > 2) targetSelect.selectedIndex = 2;
    }
    if (greedySelect) greedySelect.innerHTML = options;
    if (edgeFrom) edgeFrom.innerHTML = options;
    if (edgeTo) {
      edgeTo.innerHTML = options;
      if (this.allDestinations.length > 1) edgeTo.selectedIndex = 1;
    }
  },

  async applySort() {
    const by = document.getElementById('explore-sort-by').value;
    const order = document.getElementById('explore-sort-order').value;
    const algo = document.getElementById('explore-sort-algo').value;

    const res = await API.sort(by, order, algo);
    if (res && res.primary) {
      this.renderDestinationsGrid(res.primary.sortedList);

      // Show Performance Metrics Banner
      const banner = document.getElementById('explore-algo-metrics');
      const title = document.getElementById('algo-metric-title');
      const details = document.getElementById('algo-metric-details');
      const pills = document.getElementById('algo-metric-pills');

      banner.style.display = 'block';
      title.textContent = `Java ${res.primary.algorithm} Executed`;
      details.textContent = `Sorted ${res.primary.recordCount} destinations by ${by.toUpperCase()} (${order.toUpperCase()}).`;

      const timeMicros = (res.primary.executionTimeNanos / 1000).toFixed(2);
      const altTimeMicros = (res.alternative.executionTimeNanos / 1000).toFixed(2);

      pills.innerHTML = `
        <span class="complexity-pill" style="background:#ccfbf1; color:#0f766e;">Time: ${timeMicros} µs (${res.primary.comparisons} comparisons)</span>
        <span class="complexity-pill" style="background:#f1f5f9; color:#475569;">Alternative ${res.alternative.algorithm}: ${altTimeMicros} µs</span>
        <span class="complexity-pill">${res.primary.timeComplexity}</span>
      `;

      this.showToast(`Sorted catalog via Java ${res.primary.algorithm} (${timeMicros} µs)`);
    }
  },

  async applySearch() {
    const query = document.getElementById('explore-search-input').value;
    const algo = document.getElementById('explore-search-algo').value;

    if (!query || query.trim() === '') {
      this.renderDestinationsGrid(this.allDestinations);
      document.getElementById('explore-algo-metrics').style.display = 'none';
      return;
    }

    const res = await API.search(query, algo);
    const banner = document.getElementById('explore-algo-metrics');
    const title = document.getElementById('algo-metric-title');
    const details = document.getElementById('algo-metric-details');
    const pills = document.getElementById('algo-metric-pills');

    banner.style.display = 'block';
    title.textContent = `Java ${res.algorithm} Executed`;
    details.textContent = res.explanation;

    const timeMicros = (res.executionTimeNanos / 1000).toFixed(2);
    pills.innerHTML = `
      <span class="complexity-pill" style="background:#ccfbf1; color:#0f766e;">Comparisons: ${res.totalComparisons}</span>
      <span class="complexity-pill" style="background:#e0f2fe; color:#0369a1;">Time: ${timeMicros} µs</span>
      <span class="complexity-pill">${res.found ? 'Status: MATCH FOUND' : 'Status: NOT FOUND'}</span>
    `;

    if (res.found && res.destination) {
      this.renderDestinationsGrid([res.destination]);
      this.showToast(`Found destination '${res.destination.name}' in ${res.totalComparisons} comparisons.`);
    } else {
      this.renderDestinationsGrid([]);
      this.showToast(`No matches found for '${query}'.`, true);
    }
  },

  // ==========================================
  // ITINERARY & LINKED LIST
  // ==========================================
  async loadItinerary() {
    const res = await API.getItinerary();
    this.itinerary = res.itinerary || [];
    const stats = res.stats || { totalStops: 0, totalCost: 0, totalDuration: 0, averageRating: 0, undoCount: 0 };

    this.renderItineraryList(this.itinerary);
    this.renderLinkedListFlow(this.itinerary);

    document.getElementById('itin-sum-stops').textContent = stats.totalStops;
    document.getElementById('itin-sum-duration').textContent = `${stats.totalDuration} Days`;
    document.getElementById('itin-sum-cost').textContent = `$${stats.totalCost.toLocaleString()}`;
    document.getElementById('itin-sum-rating').textContent = `★ ${stats.averageRating.toFixed(1)}`;

    // Undo banner state
    const undoBanner = document.getElementById('itinerary-undo-banner');
    const undoSize = document.getElementById('undo-stack-size');
    if (stats.undoCount > 0) {
      undoBanner.style.display = 'flex';
      undoSize.textContent = stats.undoCount;
    } else {
      undoBanner.style.display = 'none';
    }
  },

  renderItineraryList(items) {
    const container = document.getElementById('itinerary-items-list');
    if (!container) return;

    if (!items || items.length === 0) {
      container.innerHTML = `
        <div style="text-align: center; padding: 48px 20px; background: #fff; border-radius: var(--radius-lg); border: 1px dashed var(--border-light);">
          <h4 style="font-size: 16px; color: var(--text-main); margin-bottom: 6px;">Your itinerary is empty</h4>
          <p style="font-size: 13px; color: var(--text-secondary); margin-bottom: 16px;">Add destinations from the catalog to build your custom schedule.</p>
          <button class="btn btn-primary btn-sm" onclick="App.navigateTo('explore')">Explore Destinations</button>
        </div>
      `;
      return;
    }

    container.innerHTML = items.map((item, idx) => `
      <div class="itinerary-item">
        <div class="item-index-badge">${idx + 1}</div>
        <img class="item-thumb" src="${item.imageUrl}" alt="${item.name}">
        <div class="item-details">
          <h4 class="item-name">${item.name}</h4>
          <div class="item-subtext">
            <span>${item.location}</span>
            <span>•</span>
            <span style="color:var(--accent-primary); font-weight:600;">$${item.estimatedCost.toLocaleString()}</span>
            <span>•</span>
            <span>${item.visitDuration} Days</span>
            <span>•</span>
            <span style="color:#d97706;">★ ${item.rating.toFixed(1)}</span>
          </div>
        </div>
        <div class="item-actions">
          <button class="btn btn-secondary btn-sm" onclick="App.reorderItinerary(${idx}, ${idx - 1})" ${idx === 0 ? 'disabled style="opacity:0.4; cursor:default;"' : ''} title="Move Up (CustomLinkedList.move)">
            ↑
          </button>
          <button class="btn btn-secondary btn-sm" onclick="App.reorderItinerary(${idx}, ${idx + 1})" ${idx === items.length - 1 ? 'disabled style="opacity:0.4; cursor:default;"' : ''} title="Move Down (CustomLinkedList.move)">
            ↓
          </button>
          <button class="btn btn-danger btn-sm" onclick="App.removeFromItinerary(${idx})" title="Remove from Itinerary">
            Remove
          </button>
        </div>
      </div>
    `).join('');
  },

  renderLinkedListFlow(items) {
    const container = document.getElementById('itinerary-nodes-flow');
    if (!container) return;

    if (!items || items.length === 0) {
      container.innerHTML = `
        <div style="font-size: 13px; color: var(--text-muted); font-family: var(--font-mono); padding: 8px 12px;">
          head = null (Empty Linked List)
        </div>
      `;
      return;
    }

    let html = `<div style="font-family:var(--font-mono); font-size:12px; font-weight:bold; color:var(--text-secondary); margin-right:4px;">HEAD →</div>`;

    items.forEach((item, idx) => {
      html += `
        <div class="flow-node">
          <div class="node-title">${item.name}</div>
          <div class="node-meta">${item.id} • $${item.estimatedCost}</div>
        </div>
        <div class="flow-pointer">→</div>
      `;
    });

    html += `<div style="font-family:var(--font-mono); font-size:12px; font-weight:bold; color:var(--text-muted);">NULL</div>`;
    container.innerHTML = html;
  },

  async addToItinerary(destId) {
    const res = await API.addToItinerary(destId);
    if (res.success) {
      this.showToast(res.message);
      this.documentUndoBanner(res.message);
      await this.loadItinerary();
      await this.loadDashboard();
    } else {
      this.showToast(res.error || 'Failed to add destination', true);
    }
  },

  async removeFromItinerary(index) {
    const res = await API.removeFromItinerary(index);
    if (res.success) {
      this.showToast(res.message);
      this.documentUndoBanner(res.message);
      await this.loadItinerary();
      await this.loadDashboard();
    } else {
      this.showToast(res.error || 'Failed to remove destination', true);
    }
  },

  async reorderItinerary(fromIdx, toIdx) {
    const res = await API.reorderItinerary(fromIdx, toIdx);
    if (res.success) {
      this.showToast(res.message);
      this.documentUndoBanner(res.message);
      await this.loadItinerary();
    }
  },

  async undoItinerary() {
    const res = await API.undoItinerary();
    if (res.success) {
      this.showToast(res.message);
      this.documentUndoBanner(res.message);
      await this.loadItinerary();
      await this.loadDashboard();
    } else {
      this.showToast(res.error || 'Undo stack empty', true);
    }
  },

  async clearItinerary() {
    if (!confirm('Are you sure you want to clear your entire itinerary? (You can still undo via Custom Stack)')) return;
    const res = await API.clearItinerary();
    if (res.success) {
      this.showToast('Itinerary cleared. Stack snapshot recorded.');
      this.documentUndoBanner('Cleared itinerary (Snapshot saved)');
      await this.loadItinerary();
      await this.loadDashboard();
    }
  },

  documentUndoBanner(msg) {
    const banner = document.getElementById('itinerary-undo-banner');
    const msgEl = document.getElementById('undo-banner-msg');
    if (banner && msgEl) {
      msgEl.textContent = msg;
      banner.style.display = 'flex';
    }
  },

  async evaluateQuickBudget() {
    const expr = document.getElementById('quick-calc-expr').value;
    const resultBox = document.getElementById('quick-calc-result');
    try {
      const res = await API.evaluateExpression(expr);
      if (res.success) {
        resultBox.textContent = `Result: $${res.result.toFixed(2)}`;
        resultBox.style.color = 'var(--accent-primary)';
      } else {
        resultBox.textContent = `Error: ${res.error}`;
        resultBox.style.color = 'var(--accent-rose)';
      }
    } catch (e) {
      resultBox.textContent = `Error: ${e.message}`;
      resultBox.style.color = 'var(--accent-rose)';
    }
  },

  // ==========================================
  // GRAPH & ROUTE PLANNER
  // ==========================================
  async loadGraphData() {
    const data = await API.getGraph();
    this.graphData = data;
    this.renderGraph();
  },

  renderGraph(highlightedNodes = [], highlightedEdges = []) {
    const svg = document.getElementById('graph-svg');
    if (!svg) return;

    svg.innerHTML = '';
    const width = svg.clientWidth || 900;
    const height = 520;

    const vertices = this.graphData.vertices || [];
    const edges = this.graphData.edges || [];

    if (vertices.length === 0) return;

    // Distribute vertices in a pleasing organic layout
    const nodeCoords = {};
    const radius = Math.min(width, height) * 0.38;
    const centerX = width / 2;
    const centerY = height / 2;

    vertices.forEach((v, i) => {
      const angle = (i / vertices.length) * 2 * Math.PI - Math.PI / 2;
      // slight alternating offset for organic look
      const r = radius * (i % 2 === 0 ? 1 : 0.85);
      nodeCoords[v.id] = {
        x: centerX + r * Math.cos(angle),
        y: centerY + r * Math.sin(angle),
        vertex: v
      };
    });

    // Draw Edges
    edges.forEach(edge => {
      const p1 = nodeCoords[edge.source];
      const p2 = nodeCoords[edge.target];
      if (!p1 || !p2) return;

      const isHighlighted = highlightedEdges.some(e =>
        (e[0] === edge.source && e[1] === edge.target) ||
        (e[1] === edge.source && e[0] === edge.target)
      );

      const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
      line.setAttribute('x1', p1.x);
      line.setAttribute('y1', p1.y);
      line.setAttribute('x2', p2.x);
      line.setAttribute('y2', p2.y);
      line.setAttribute('stroke', isHighlighted ? '#f59e0b' : '#cbd5e1');
      line.setAttribute('stroke-width', isHighlighted ? '3.5' : '1.5');
      if (isHighlighted) {
        line.setAttribute('stroke-dasharray', '4');
      }
      svg.appendChild(line);

      // Distance Label
      const midX = (p1.x + p2.x) / 2;
      const midY = (p1.y + p2.y) / 2;
      const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
      text.setAttribute('x', midX);
      text.setAttribute('y', midY - 3);
      text.setAttribute('text-anchor', 'middle');
      text.setAttribute('font-size', '10');
      text.setAttribute('font-family', 'var(--font-mono)');
      text.setAttribute('fill', isHighlighted ? '#b45309' : '#94a3b8');
      text.textContent = `${Math.round(edge.distanceKm)}km`;
      svg.appendChild(text);
    });

    // Draw Vertices
    vertices.forEach(v => {
      const p = nodeCoords[v.id];
      if (!p) return;

      const isHighlighted = highlightedNodes.includes(v.id);

      const g = document.createElementNS('http://www.w3.org/2000/svg', 'g');
      g.style.cursor = 'pointer';
      g.addEventListener('click', () => {
        document.getElementById('graph-start-node').value = v.id;
        App.showToast(`Selected '${v.name}' as Start Hub.`);
      });

      // Outer circle
      const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
      circle.setAttribute('cx', p.x);
      circle.setAttribute('cy', p.y);
      circle.setAttribute('r', isHighlighted ? '22' : '18');
      circle.setAttribute('fill', isHighlighted ? '#0d9488' : '#ffffff');
      circle.setAttribute('stroke', isHighlighted ? '#0f766e' : '#0d9488');
      circle.setAttribute('stroke-width', isHighlighted ? '3.5' : '2.5');
      g.appendChild(circle);

      // Vertex ID text
      const idText = document.createElementNS('http://www.w3.org/2000/svg', 'text');
      idText.setAttribute('x', p.x);
      idText.setAttribute('y', p.y + 4);
      idText.setAttribute('text-anchor', 'middle');
      idText.setAttribute('font-size', '10');
      idText.setAttribute('font-weight', 'bold');
      idText.setAttribute('fill', isHighlighted ? '#ffffff' : '#0f172a');
      idText.textContent = v.id.replace('DEST-', '');
      g.appendChild(idText);

      // Label below
      const labelText = document.createElementNS('http://www.w3.org/2000/svg', 'text');
      labelText.setAttribute('x', p.x);
      labelText.setAttribute('y', p.y + (p.y > centerY ? 32 : -24));
      labelText.setAttribute('text-anchor', 'middle');
      labelText.setAttribute('font-size', '11');
      labelText.setAttribute('font-weight', '600');
      labelText.setAttribute('fill', '#1e293b');
      labelText.textContent = v.name.split(' ')[0];
      g.appendChild(labelText);

      svg.appendChild(g);
    });
  },

  async runBfs() {
    const startId = document.getElementById('graph-start-node').value;
    const res = await API.runBfs(startId);
    this.displayTraversalResults(res, 'BFS (Breadth-First Search)');

    // Highlight traversed nodes
    this.renderGraph(res.traversalOrder, this.buildConsecutiveEdges(res.traversalOrder));
  },

  async runDfs() {
    const startId = document.getElementById('graph-start-node').value;
    const res = await API.runDfs(startId);
    this.displayTraversalResults(res, 'DFS (Depth-First Search)');

    // Highlight traversed nodes
    this.renderGraph(res.traversalOrder, this.buildConsecutiveEdges(res.traversalOrder));
  },

  async runDijkstra() {
    const startId = document.getElementById('graph-start-node').value;
    const targetId = document.getElementById('graph-target-node').value;

    const res = await API.runDijkstra(startId, targetId);
    if (!res.pathFound) {
      this.showToast(`No connected path found between ${startId} and ${targetId}.`, true);
      return;
    }

    const box = document.getElementById('traversal-result-box');
    const title = document.getElementById('traversal-title');
    const theory = document.getElementById('traversal-theory');
    const pills = document.getElementById('traversal-order-pills');
    const stepsList = document.getElementById('traversal-steps-list');

    box.style.display = 'block';
    title.textContent = `Dijkstra's Shortest Weighted Path: ${startId} → ${targetId}`;
    theory.textContent = `${res.explanation} Total genuine distance: ${res.totalDistance.toLocaleString()} km.`;

    pills.innerHTML = res.path.map((nodeId, idx) => `
      <span class="complexity-pill" style="background:#ccfbf1; color:#0f766e; font-size:12px;">${idx + 1}. ${nodeId}</span>
    `).join(' <span style="color:var(--text-muted);">→</span> ');

    stepsList.innerHTML = `
      <div class="timeline-step">
        <span>Computed shortest flight corridor through ${res.path.length} vertices: ${res.path.join(' ➔ ')}</span>
        <strong>${res.totalDistance} km total</strong>
      </div>
    `;

    // Highlight Dijkstra Path
    this.renderGraph(res.path, this.buildConsecutiveEdges(res.path));
    this.showToast(`Found shortest route: ${res.path.length} stops, ${res.totalDistance} km.`);
  },

  buildConsecutiveEdges(nodeList) {
    const edges = [];
    for (let i = 0; i < nodeList.length - 1; i++) {
      edges.push([nodeList[i], nodeList[i + 1]]);
    }
    return edges;
  },

  displayTraversalResults(res, algoName) {
    const box = document.getElementById('traversal-result-box');
    const title = document.getElementById('traversal-title');
    const theory = document.getElementById('traversal-theory');
    const pills = document.getElementById('traversal-order-pills');
    const stepsList = document.getElementById('traversal-steps-list');

    box.style.display = 'block';
    title.textContent = `${algoName} Traversal Sequence from ${res.startVertexId}`;
    theory.textContent = res.explanation;

    pills.innerHTML = res.traversalOrder.map((nodeId, idx) => `
      <span class="complexity-pill" style="background:#ccfbf1; color:#0f766e; font-size:12px;">${idx + 1}. ${nodeId}</span>
    `).join('');

    stepsList.innerHTML = res.steps.map((step, idx) => `
      <div class="timeline-step">
        <div>
          <strong>Step ${idx + 1} (${step.currentVertexId}):</strong> ${step.action}
        </div>
        <div style="font-family:var(--font-mono); font-size:11px; color:var(--text-secondary);">
          Frontier State: [${step.frontierState.join(', ')}]
        </div>
      </div>
    `).join('');

    this.showToast(`Completed ${algoName}. Visited ${res.traversalOrder.length} reachable destinations.`);
  },

  // ==========================================
  // SMART OPTIMIZATION
  // ==========================================
  async runGreedy() {
    const startId = document.getElementById('greedy-start-hub').value;
    const maxStops = parseInt(document.getElementById('greedy-max-stops').value) || 5;

    const res = await API.runGreedy(startId, maxStops);
    const container = document.getElementById('greedy-output-container');
    const accordion = document.getElementById('greedy-steps-accordion');

    container.style.display = 'block';
    accordion.innerHTML = res.steps.map(step => `
      <div style="background:var(--bg-canvas); border:1px solid var(--border-light); border-radius:var(--radius-md); padding:16px;">
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
          <h5 style="font-size:14px; font-weight:700;">Step #${step.stepIndex}: Origin Hub '${step.fromDestinationName}'</h5>
          <span class="complexity-pill" style="background:#ccfbf1; color:#0f766e;">Selected: ${step.chosenDestination ? step.chosenDestination.destinationName : 'None'}</span>
        </div>
        <p style="font-size:13px; color:var(--text-secondary); margin-bottom:12px;">${step.rationale}</p>
        <div style="font-size:12px; font-weight:600; color:var(--text-muted); margin-bottom:6px;">Evaluated Candidates (${step.candidatesConsidered.length}):</div>
        <div style="display:grid; grid-template-columns:repeat(auto-fill, minmax(220px, 1fr)); gap:8px;">
          ${step.candidatesConsidered.map(c => `
            <div style="background:#fff; border:1px solid ${c.destinationId === step.chosenDestination.destinationId ? 'var(--accent-primary)' : 'var(--border-light)'}; border-radius:var(--radius-sm); padding:8px 10px; font-size:12px;">
              <strong>${c.destinationName}</strong>
              <div style="font-family:var(--font-mono); font-size:11px; color:var(--text-secondary); margin-top:2px;">
                Formula: ${c.evaluationFormula}
              </div>
            </div>
          `).join('')}
        </div>
      </div>
    `).join('');

    this.showToast(`Greedy route selected: ${res.selectedRoute.length} stops, $${res.totalCost.toLocaleString()}`);
  },

  async runDp() {
    const budget = parseFloat(document.getElementById('dp-budget-input').value) || 1000;
    const res = await API.runDp(budget);

    const container = document.getElementById('dp-output-container');
    container.style.display = 'block';

    document.getElementById('dp-total-value').textContent = `${res.totalTravelValue} pts`;
    document.getElementById('dp-total-cost').textContent = `$${res.totalCost.toFixed(2)}`;
    document.getElementById('dp-remaining-budget').textContent = `$${res.remainingBudget.toFixed(2)}`;

    // Selected Activities
    const listEl = document.getElementById('dp-selected-activities');
    listEl.innerHTML = res.selectedActivities.map(a => `
      <div style="background:var(--bg-canvas); border:1px solid var(--border-light); border-radius:var(--radius-md); padding:12px 16px; display:flex; justify-content:space-between; align-items:center;">
        <div>
          <strong style="color:var(--text-main); font-size:14px;">${a.name}</strong>
          <span style="font-size:12px; color:var(--text-secondary); margin-left:8px;">ID: ${a.id}</span>
        </div>
        <div style="display:flex; gap:12px; align-items:center;">
          <span style="font-weight:700; color:var(--accent-primary);">$${a.cost}</span>
          <span class="complexity-pill" style="background:#fef3c7; color:#b45309;">${a.travelValue} pts</span>
        </div>
      </div>
    `).join('');

    // Render DP Matrix Table Slice
    const matrixContainer = document.getElementById('dp-matrix-container');
    if (res.dpTableSample && res.tableColHeaders) {
      let tableHtml = `<table class="dp-matrix"><thead><tr><th>Activity \\ Budget ($)</th>`;
      res.tableColHeaders.forEach(col => {
        tableHtml += `<th>$${col}</th>`;
      });
      tableHtml += `</tr></thead><tbody>`;

      res.tableRowHeaders.forEach((rowName, rIdx) => {
        tableHtml += `<tr><td style="text-align:left; font-weight:600;">${rowName}</td>`;
        for (let cIdx = 0; cIdx < res.tableColHeaders.length; cIdx++) {
          const val = res.dpTableSample[rIdx][cIdx];
          tableHtml += `<td>${val}</td>`;
        }
        tableHtml += `</tr>`;
      });

      tableHtml += `</tbody></table>`;
      matrixContainer.innerHTML = tableHtml;
    }

    this.showToast(`Optimal DP combination: ${res.selectedActivities.length} activities (${res.totalTravelValue} pts).`);
  },

  async runBacktrack() {
    const budget = parseFloat(document.getElementById('bt-budget-input').value) || 3000;
    const duration = parseFloat(document.getElementById('bt-duration-input').value) || 10;
    const minStops = parseInt(document.getElementById('bt-min-stops').value) || 3;

    const res = await API.runBacktrack(budget, duration, minStops);
    const container = document.getElementById('bt-output-container');
    container.style.display = 'block';

    document.getElementById('bt-states-count').textContent = res.totalStatesExplored.toLocaleString();
    document.getElementById('bt-pruned-count').textContent = res.branchesPruned.toLocaleString();
    document.getElementById('bt-valid-count').textContent = res.validItineraries.length;

    // Render Valid Itineraries
    const validList = document.getElementById('bt-valid-itineraries-list');
    if (res.validItineraries.length === 0) {
      validList.innerHTML = `<p style="font-size:13px; color:var(--accent-rose);">No itineraries found within constraints. Try relaxing the budget or duration limits.</p>`;
    } else {
      validList.innerHTML = res.validItineraries.map((it, idx) => `
        <div style="background:var(--bg-canvas); border:1px solid var(--border-light); border-radius:var(--radius-md); padding:16px;">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
            <strong style="color:var(--text-main); font-size:14px;">Option #${idx + 1} (${it.destinations.length} Stops)</strong>
            <span style="font-size:13px; font-weight:700; color:var(--accent-primary);">$${it.totalCost.toLocaleString()} • ${it.totalDuration} Days • ★ ${it.averageRating.toFixed(1)}</span>
          </div>
          <div style="display:flex; gap:8px; flex-wrap:wrap;">
            ${it.destinations.map(d => `<span class="complexity-pill" style="background:#fff; border:1px solid var(--border-light); font-size:12px;">${d.name}</span>`).join(' ➔ ')}
          </div>
        </div>
      `).join('');
    }

    // Render Pruning Log Table
    const tbody = document.getElementById('bt-pruning-log-tbody');
    tbody.innerHTML = res.pruningLogs.map(log => `
      <tr>
        <td style="font-family:var(--font-mono); font-size:11px;">#${log.stepNumber}</td>
        <td style="font-size:12px;">${log.partialItinerary}</td>
        <td style="font-weight:600;">${log.candidateDestination}</td>
        <td><span class="prune-badge">${log.rejectionReason}</span></td>
      </tr>
    `).join('');

    this.showToast(`Backtracking complete: ${res.validItineraries.length} feasible itineraries found.`);
  },

  // ==========================================
  // DSA LABORATORY
  // ==========================================
  async labEvaluateExpression() {
    const expr = document.getElementById('lab-expr-input').value;
    const output = document.getElementById('lab-eval-output');

    try {
      const res = await API.evaluateExpression(expr);
      if (res.success) {
        let traceText = `Evaluation Success!\nResult: ${res.result}\n\nStack Tracing Steps:\n`;
        res.traces.forEach((t, i) => {
          traceText += `[${i + 1}] Token '${t.token}': ${t.action}\n    Operands: [${t.operandStack.join(', ')}]\n    Operators: [${t.operatorStack.join(', ')}]\n`;
        });
        output.textContent = traceText;
        this.showToast(`Expression evaluated to: ${res.result}`);
      } else {
        output.textContent = `Syntax Error: ${res.error}`;
      }
    } catch (e) {
      output.textContent = `Error: ${e.message}`;
    }
  },

  async labSearchBst() {
    const query = document.getElementById('lab-bst-input').value;
    const output = document.getElementById('lab-bst-output');

    const res = await API.searchBst(query);
    let txt = `BST Search for '${res.targetName}':\n`;
    txt += `Found: ${res.found}\n`;
    txt += `Comparisons: ${res.comparisons}\n`;
    txt += `Path Visited in Tree:\n`;
    res.searchPath.forEach((node, i) => {
      txt += `  Step ${i + 1}: ${node.name} (Key ID: ${node.id})\n`;
    });
    output.textContent = txt;
  },

  async labBstTraversals() {
    const output = document.getElementById('lab-bst-output');
    const res = await API.getBstData();
    let txt = `Binary Search Tree (Size: ${res.size}, Height: ${res.height})\n\n`;
    txt += `Inorder Traversal (Alphabetically Sorted):\n`;
    res.inorder.forEach(d => {
      txt += `  • ${d.name} ($${d.estimatedCost})\n`;
    });
    output.textContent = txt;
  },

  async labTreeTraversals() {
    const output = document.getElementById('lab-tree-output');
    const res = await API.runTreeTraversals();

    let txt = `Regional Binary Tree (Height: ${res.height})\n\n`;
    txt += `1. Preorder Traversal (Root -> Left -> Right):\n   ${res.preorder.join(' -> ')}\n\n`;
    txt += `2. Inorder Traversal (Left -> Root -> Right):\n   ${res.inorder.join(' -> ')}\n\n`;
    txt += `3. Postorder Traversal (Left -> Right -> Root):\n   ${res.postorder.join(' -> ')}\n`;
    output.textContent = txt;
  },

  async loadQueueData() {
    const data = await API.getQueue();
    document.getElementById('header-queue-badge').textContent = data.pendingCount;

    const output = document.getElementById('lab-queue-output');
    if (output) {
      let txt = `Pending Queue Size: ${data.pendingCount} | Processed: ${data.processedCount}\n\n`;
      txt += `Pending Requests (FIFO Order):\n`;
      if (data.pending.length === 0) {
        txt += `  [Queue Empty]\n`;
      } else {
        data.pending.forEach((req, i) => {
          txt += `  [${i + 1}] ${req.id}: ${req.travelerName} - ${req.requestType} for ${req.destinationName}\n`;
        });
      }

      txt += `\nRecently Processed:\n`;
      data.processed.slice(0, 3).forEach(p => {
        txt += `  ✓ ${p.id} (${p.travelerName}): ${p.resultSummary}\n`;
      });
      output.textContent = txt;
    }
  },

  async labEnqueue() {
    const names = ['Margaret Hamilton', 'Donald Knuth', 'Claude Shannon', 'Barbara Liskov'];
    const randomName = names[Math.floor(Math.random() * names.length)];
    const dest = this.allDestinations[Math.floor(Math.random() * this.allDestinations.length)];

    const res = await API.enqueueRequest(randomName, 'Route Exploration', dest.id, dest.name);
    this.showToast(`Enqueued request for ${randomName}`);
    await this.loadQueueData();
  },

  async labDequeue() {
    const res = await API.processNextRequest();
    if (res.success) {
      this.showToast(`Processed: ${res.message}`);
      await this.loadQueueData();
    } else {
      this.showToast(res.error || 'Queue is empty', true);
    }
  },

  async labRunSortBench() {
    const output = document.getElementById('lab-sort-output');
    output.textContent = 'Executing MergeSort and QuickSort on backend...';

    const res = await API.sort('cost', 'asc', 'mergesort');
    const merge = res.primary.algorithm === 'Merge Sort' ? res.primary : res.alternative;
    const quick = res.primary.algorithm === 'Quick Sort' ? res.primary : res.alternative;

    let txt = `Sorting Performance Benchmark (Dataset Size: ${merge.recordCount} items)\n\n`;
    txt += `[Merge Sort]\n`;
    txt += `  Comparisons: ${merge.comparisons}\n`;
    txt += `  Merges/Swaps: ${merge.swapsOrMerges}\n`;
    txt += `  Execution Time: ${(merge.executionTimeNanos / 1000).toFixed(2)} µs (${merge.executionTimeNanos} ns)\n`;
    txt += `  Complexity: ${merge.timeComplexity} | Space: ${merge.spaceComplexity}\n\n`;

    txt += `[Quick Sort]\n`;
    txt += `  Comparisons: ${quick.comparisons}\n`;
    txt += `  Swaps: ${quick.swapsOrMerges}\n`;
    txt += `  Execution Time: ${(quick.executionTimeNanos / 1000).toFixed(2)} µs (${quick.executionTimeNanos} ns)\n`;
    txt += `  Complexity: ${quick.timeComplexity} | Space: ${quick.spaceComplexity}\n`;

    output.textContent = txt;
  },

  async labRunSearchBench() {
    const query = document.getElementById('lab-search-input').value;
    const output = document.getElementById('lab-search-output');

    const lin = await API.search(query, 'linear');
    const bin = await API.search(query, 'binary');

    let txt = `Search Benchmark for Target: '${query}'\n\n`;
    txt += `[Linear Search - Unsorted Sequence]\n`;
    txt += `  Found: ${lin.found}\n`;
    txt += `  Comparisons: ${lin.totalComparisons}\n`;
    txt += `  Execution Time: ${(lin.executionTimeNanos / 1000).toFixed(2)} µs (${lin.executionTimeNanos} ns)\n\n`;

    txt += `[Binary Search - Pre-sorted BST/Array]\n`;
    txt += `  Found: ${bin.found}\n`;
    txt += `  Comparisons: ${bin.totalComparisons}\n`;
    txt += `  Execution Time: ${(bin.executionTimeNanos / 1000).toFixed(2)} µs (${bin.executionTimeNanos} ns)\n`;

    output.textContent = txt;
  },

  // ==========================================
  // MODALS & CRUD
  // ==========================================
  openDestModal(dest = null) {
    this.editingDestId = dest ? dest.id : null;
    document.getElementById('modal-dest-title').textContent = dest ? `Edit Destination ${dest.id}` : 'Add New Destination';
    document.getElementById('dest-form-id').value = dest ? dest.id : `DEST-${this.allDestinations.length + 1 < 10 ? '0' : ''}${this.allDestinations.length + 1}`;
    document.getElementById('dest-form-id').disabled = !!dest;
    document.getElementById('dest-form-name').value = dest ? dest.name : '';
    document.getElementById('dest-form-location').value = dest ? dest.location : '';
    document.getElementById('dest-form-cost').value = dest ? dest.estimatedCost : 1200;
    document.getElementById('dest-form-duration').value = dest ? dest.visitDuration : 4;
    document.getElementById('dest-form-rating').value = dest ? dest.rating : 4.8;
    document.getElementById('dest-form-image').value = dest ? dest.imageUrl : 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80';
    document.getElementById('dest-form-desc').value = dest ? dest.description : '';

    document.getElementById('modal-destination').classList.add('active');
  },

  closeDestModal() {
    document.getElementById('modal-destination').classList.remove('active');
  },

  currentDetailId: null,

  viewDetails(id) {
    const d = this.allDestinations.find(item => item.id === id);
    if (!d) return;

    this.currentDetailId = d.id;
    document.getElementById('detail-modal-title').textContent = `${d.name} (${d.id})`;
    document.getElementById('detail-modal-id').textContent = d.id;
    document.getElementById('detail-modal-rating').textContent = `★ ${d.rating.toFixed(1)}`;
    document.getElementById('detail-modal-name').textContent = d.name;
    document.getElementById('detail-modal-location').textContent = d.location;
    document.getElementById('detail-modal-desc').textContent = d.description;
    document.getElementById('detail-modal-cost').textContent = `$${d.estimatedCost.toLocaleString()}`;
    document.getElementById('detail-modal-duration').textContent = `${d.visitDuration} Days`;
    document.getElementById('detail-modal-img').src = d.imageUrl;

    const actList = document.getElementById('detail-modal-activities');
    if (d.activities && d.activities.length > 0) {
      actList.innerHTML = d.activities.map(a => `
        <div style="background:#fff; border:1px solid var(--border-light); border-radius:var(--radius-sm); padding:10px 14px; display:flex; justify-content:space-between; align-items:center;">
          <div>
            <strong style="color:var(--text-main); font-size:13px;">${a.name}</strong>
            <div style="font-size:11px; color:var(--text-secondary); font-family:var(--font-mono);">${a.id}</div>
          </div>
          <div style="display:flex; gap:10px; align-items:center;">
            <span style="font-weight:700; color:var(--accent-primary);">$${a.cost}</span>
            <span class="complexity-pill" style="background:#fef3c7; color:#b45309;">${a.travelValue} pts</span>
          </div>
        </div>
      `).join('');
    } else {
      actList.innerHTML = `<p style="font-size:13px; color:var(--text-muted);">No individual experiences specified for this destination.</p>`;
    }

    document.getElementById('modal-dest-details').classList.add('active');
  },

  closeDetailModal() {
    document.getElementById('modal-dest-details').classList.remove('active');
    this.currentDetailId = null;
  },

  async checkAuth() {
    try {
      const user = await API.getMe();
      const nameEl = document.getElementById('sidebar-display-name');
      const avatarEl = document.getElementById('sidebar-avatar');
      if (user && user.authenticated) {
        this.currentUser = user;
        if (nameEl) nameEl.textContent = user.displayName || user.username;
        if (avatarEl) avatarEl.textContent = (user.displayName || user.username || 'U').charAt(0).toUpperCase();
      } else {
        if (nameEl) nameEl.textContent = 'Guest Traveler';
        if (avatarEl) avatarEl.textContent = 'G';
      }
    } catch (e) {
      console.warn('Session check fallback:', e);
    }
  },

  async logout() {
    try {
      await API.logout();
    } catch (e) {
      console.error(e);
    }
    window.location.href = 'login.html';
  },

  showToast(message, isError = false) {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${isError ? 'toast-error' : ''}`;
    toast.innerHTML = `
      <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="${isError ? '#f43f5e' : '#0d9488'}" stroke-width="2">
        ${isError ? '<circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>' : '<path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>'}
      </svg>
      <span>${message}</span>
    `;
    container.appendChild(toast);
    setTimeout(() => {
      toast.remove();
    }, 4000);
  }
};

window.addEventListener('DOMContentLoaded', () => {
  App.init();
});
