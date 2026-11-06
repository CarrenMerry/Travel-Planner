/**
 * Admin Portal Controller
 * Manages administrative authentication, destination catalogue, graph edges, and system status.
 */

const AdminApp = {
  currentView: 'dashboard',
  allDestinations: [],
  allEdges: [],
  currentUser: null,
  editingDestId: null,
  auditLog: [],

  async init() {
    this.bindEvents();
    await this.checkAuthAndLoad();
  },

  bindEvents() {
    // Navigation
    document.querySelectorAll('.admin-nav-link[data-view]').forEach(link => {
      link.addEventListener('click', () => {
        const view = link.getAttribute('data-view');
        this.navigateTo(view);
      });
    });

    // Login Form
    const loginForm = document.getElementById('admin-login-form');
    if (loginForm) {
      loginForm.addEventListener('submit', (e) => {
        e.preventDefault();
        this.handleLogin();
      });
    }

    // Logout
    const logoutBtn = document.getElementById('btn-admin-logout');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', () => this.handleLogout());
    }

    // Destination search filter in admin table
    const searchInput = document.getElementById('admin-dest-search');
    if (searchInput) {
      searchInput.addEventListener('input', () => this.filterDestinationsTable());
    }
  },

  async checkAuthAndLoad() {
    try {
      const auth = await API.getMe();
      if (auth.authenticated && auth.isAdmin) {
        this.currentUser = auth;
        this.showAdminLayout();
        await this.loadAllAdminData();
      } else {
        this.showLoginGate();
      }
    } catch (err) {
      console.error('Auth verification failed:', err);
      this.showLoginGate();
    }
  },

  showLoginGate() {
    document.getElementById('admin-auth-gate').style.display = 'flex';
  },

  showAdminLayout() {
    document.getElementById('admin-auth-gate').style.display = 'none';
    if (this.currentUser) {
      document.getElementById('admin-sidebar-name').textContent = this.currentUser.displayName || this.currentUser.username;
      document.getElementById('admin-sidebar-avatar').textContent = (this.currentUser.username || 'A').charAt(0).toUpperCase();
    }
  },

  async handleLogin() {
    const user = document.getElementById('admin-login-user').value.trim();
    const pass = document.getElementById('admin-login-pass').value.trim();
    const errEl = document.getElementById('admin-login-error');

    errEl.style.display = 'none';

    try {
      const res = await API.login(user, pass);
      if (res.success && res.user && res.user.isAdmin) {
        this.currentUser = res.user;
        this.recordAudit(`Administrator '${res.user.username}' signed in securely.`);
        this.showAdminLayout();
        await this.loadAllAdminData();
        this.showToast('Authentication successful. Welcome, ' + res.user.displayName);
      } else if (res.success && (!res.user || !res.user.isAdmin)) {
        errEl.textContent = 'Account authenticated, but lacks Administrator privileges.';
        errEl.style.display = 'block';
      } else {
        errEl.textContent = res.error || 'Invalid administrator credentials.';
        errEl.style.display = 'block';
      }
    } catch (e) {
      errEl.textContent = 'Network or server error during authentication.';
      errEl.style.display = 'block';
    }
  },

  async handleLogout() {
    await API.logout();
    this.currentUser = null;
    this.showToast('Admin session terminated.');
    this.showLoginGate();
  },

  navigateTo(viewId) {
    this.currentView = viewId;
    document.querySelectorAll('.admin-nav-link[data-view]').forEach(link => {
      link.classList.toggle('active', link.getAttribute('data-view') === viewId);
    });

    document.querySelectorAll('.admin-view-section').forEach(sec => {
      sec.classList.toggle('active', sec.id === `admin-view-${viewId}`);
    });

    const titleMap = {
      'dashboard': 'Admin Dashboard',
      'destinations': 'Destination Catalogue Management',
      'connections': 'Graph Transit & Route Connections',
      'system': 'System & DSA Memory State'
    };

    if (titleMap[viewId]) {
      document.getElementById('admin-view-title').textContent = titleMap[viewId];
    }
  },

  async loadAllAdminData() {
    await Promise.all([
      this.loadDashboard(),
      this.loadDestinations(),
      this.loadConnections(),
      this.loadSystemStatus()
    ]);
  },

  // ==========================================
  // DASHBOARD
  // ==========================================
  async loadDashboard() {
    const data = await API.getDashboard();
    document.getElementById('admin-stat-dest-count').textContent = data.totalDestinations || '0';
    document.getElementById('admin-stat-edge-count').textContent = data.graphEdgeCount || '0';
    document.getElementById('admin-stat-avg-rating').textContent = `★ ${(data.averageRating || 0).toFixed(1)}`;

    if (this.auditLog.length === 0) {
      this.recordAudit('System initialized and ready for administrative management.');
      this.recordAudit('Destination catalogue verified with persistent storage (data/destinations.json).');
    }
    this.renderAuditLog();
  },

  recordAudit(action) {
    const timestamp = new Date().toLocaleTimeString();
    this.auditLog.unshift(`[${timestamp}] ${action}`);
    if (this.auditLog.length > 20) this.auditLog.pop();
    this.renderAuditLog();
  },

  renderAuditLog() {
    const list = document.getElementById('admin-audit-log-list');
    if (!list) return;

    list.innerHTML = this.auditLog.map(item => `
      <div style="background: #f8fafc; border-left: 3px solid var(--admin-accent); padding: 8px 12px; border-radius: 0 var(--radius-sm) var(--radius-sm) 0; font-size: 13px;">
        ${item}
      </div>
    `).join('');
  },

  // ==========================================
  // DESTINATIONS MANAGEMENT
  // ==========================================
  async loadDestinations() {
    const res = await API.getDestinations();
    this.allDestinations = res.destinations || [];
    this.renderDestinationsTable(this.allDestinations);
    this.populateEdgeDropdowns();
  },

  renderDestinationsTable(list) {
    const tbody = document.getElementById('admin-dest-tbody');
    const countEl = document.getElementById('admin-dest-filter-count');
    if (!tbody) return;

    if (countEl) countEl.textContent = `Showing ${list.length} of ${this.allDestinations.length} destinations`;

    if (list.length === 0) {
      tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding:32px; color:var(--admin-text-muted);">No destinations matching filter.</td></tr>`;
      return;
    }

    tbody.innerHTML = list.map(d => `
      <tr>
        <td>
          <img class="table-thumb" src="${d.imageUrl}" alt="${d.name}" onerror="this.src='https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=100&q=80'">
        </td>
        <td><strong style="font-family:var(--font-mono); font-size:12px;">${d.id}</strong></td>
        <td><strong>${d.name}</strong></td>
        <td>${d.location}</td>
        <td style="color:var(--admin-accent); font-weight:700;">$${d.estimatedCost.toLocaleString()}</td>
        <td>${d.visitDuration} Days</td>
        <td><span class="badge badge-warning">★ ${d.rating.toFixed(1)}</span></td>
        <td style="text-align: right; white-space: nowrap;">
          <button class="btn btn-secondary btn-sm" onclick="AdminApp.openEditDestModal('${d.id}')">Edit</button>
          <button class="btn btn-danger btn-sm" onclick="AdminApp.deleteDestination('${d.id}')">Delete</button>
        </td>
      </tr>
    `).join('');
  },

  filterDestinationsTable() {
    const q = document.getElementById('admin-dest-search').value.toLowerCase().trim();
    if (!q) {
      this.renderDestinationsTable(this.allDestinations);
      return;
    }
    const filtered = this.allDestinations.filter(d =>
      d.id.toLowerCase().includes(q) ||
      d.name.toLowerCase().includes(q) ||
      d.location.toLowerCase().includes(q)
    );
    this.renderDestinationsTable(filtered);
  },

  openAddDestModal() {
    this.editingDestId = null;
    document.getElementById('admin-modal-dest-title').textContent = 'Add New Destination';
    document.getElementById('admin-form-id').value = `DEST-${this.allDestinations.length + 1 < 10 ? '0' : ''}${this.allDestinations.length + 1}`;
    document.getElementById('admin-form-id').disabled = false;
    document.getElementById('admin-form-name').value = '';
    document.getElementById('admin-form-location').value = '';
    document.getElementById('admin-form-cost').value = 1200;
    document.getElementById('admin-form-duration').value = 4;
    document.getElementById('admin-form-rating').value = 4.8;
    document.getElementById('admin-form-image').value = 'https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=800&q=80';
    document.getElementById('admin-form-desc').value = '';

    document.getElementById('admin-modal-dest').classList.add('active');
  },

  openEditDestModal(id) {
    const d = this.allDestinations.find(item => item.id === id);
    if (!d) return;

    this.editingDestId = d.id;
    document.getElementById('admin-modal-dest-title').textContent = `Edit Destination (${d.id})`;
    document.getElementById('admin-form-id').value = d.id;
    document.getElementById('admin-form-id').disabled = true;
    document.getElementById('admin-form-name').value = d.name;
    document.getElementById('admin-form-location').value = d.location;
    document.getElementById('admin-form-cost').value = d.estimatedCost;
    document.getElementById('admin-form-duration').value = d.visitDuration;
    document.getElementById('admin-form-rating').value = d.rating;
    document.getElementById('admin-form-image').value = d.imageUrl;
    document.getElementById('admin-form-desc').value = d.description;

    document.getElementById('admin-modal-dest').classList.add('active');
  },

  closeDestModal() {
    document.getElementById('admin-modal-dest').classList.remove('active');
    this.editingDestId = null;
  },

  async saveDestination() {
    const data = {
      id: document.getElementById('admin-form-id').value.trim(),
      name: document.getElementById('admin-form-name').value.trim(),
      location: document.getElementById('admin-form-location').value.trim(),
      estimatedCost: parseFloat(document.getElementById('admin-form-cost').value) || 0,
      visitDuration: parseFloat(document.getElementById('admin-form-duration').value) || 1,
      rating: parseFloat(document.getElementById('admin-form-rating').value) || 4.5,
      imageUrl: document.getElementById('admin-form-image').value.trim(),
      description: document.getElementById('admin-form-desc').value.trim()
    };

    let res;
    if (this.editingDestId) {
      res = await API.updateDestination(data);
    } else {
      res = await API.createDestination(data);
    }

    if (res.success) {
      this.showToast(res.message);
      this.recordAudit(`${this.editingDestId ? 'Updated' : 'Created'} destination: ${data.name} (${data.id})`);
      this.closeDestModal();
      await this.loadDestinations();
      await this.loadDashboard();
      await this.loadConnections();
    } else {
      this.showToast(res.error || 'Validation error saving destination.', true);
    }
  },

  async deleteDestination(id) {
    if (!confirm(`Are you sure you want to permanently delete destination '${id}'? This will also remove all associated transit graph connections.`)) {
      return;
    }

    const res = await API.deleteDestination(id);
    if (res.success) {
      this.showToast(res.message);
      this.recordAudit(`Deleted destination: ${id}`);
      await this.loadDestinations();
      await this.loadDashboard();
      await this.loadConnections();
    } else {
      this.showToast(res.error || 'Failed to delete destination.', true);
    }
  },

  // ==========================================
  // CONNECTIONS & ROUTE MANAGEMENT
  // ==========================================
  async loadConnections() {
    const graphData = await API.getGraph();
    this.allEdges = graphData.edges || [];
    this.renderEdgesTable(this.allEdges);
  },

  renderEdgesTable(edges) {
    const tbody = document.getElementById('admin-edge-tbody');
    if (!tbody) return;

    if (edges.length === 0) {
      tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; padding:32px; color:var(--admin-text-muted);">No transit edges defined in graph.</td></tr>`;
      return;
    }

    tbody.innerHTML = edges.map(e => {
      const srcDest = this.allDestinations.find(d => d.id === e.source);
      const tgtDest = this.allDestinations.find(d => d.id === e.target);
      return `
        <tr>
          <td><strong>${e.source}</strong> <span style="font-size:12px; color:var(--admin-text-secondary);">(${srcDest ? srcDest.name : 'Hub'})</span></td>
          <td><strong>${e.target}</strong> <span style="font-size:12px; color:var(--admin-text-secondary);">(${tgtDest ? tgtDest.name : 'Hub'})</span></td>
          <td style="font-family:var(--font-mono); font-weight:600;">${Math.round(e.distanceKm)} km</td>
          <td style="color:var(--admin-accent); font-weight:700;">$${Math.round(e.travelCost)}</td>
          <td style="text-align: right;">
            <button class="btn btn-danger btn-sm" onclick="AdminApp.deleteEdge('${e.source}', '${e.target}')">Remove Edge</button>
          </td>
        </tr>
      `;
    }).join('');
  },

  populateEdgeDropdowns() {
    const fromSelect = document.getElementById('admin-edge-from');
    const toSelect = document.getElementById('admin-edge-to');
    if (!fromSelect || !toSelect) return;

    const options = this.allDestinations.map(d => `<option value="${d.id}">${d.name} (${d.id})</option>`).join('');
    fromSelect.innerHTML = options;
    toSelect.innerHTML = options;
    if (this.allDestinations.length > 1) {
      toSelect.selectedIndex = 1;
    }
  },

  openAddEdgeModal() {
    this.populateEdgeDropdowns();
    document.getElementById('admin-modal-edge').classList.add('active');
  },

  closeEdgeModal() {
    document.getElementById('admin-modal-edge').classList.remove('active');
  },

  async saveEdge() {
    const fromId = document.getElementById('admin-edge-from').value;
    const toId = document.getElementById('admin-edge-to').value;
    const dist = parseFloat(document.getElementById('admin-edge-dist').value) || 1000;
    const cost = parseFloat(document.getElementById('admin-edge-cost').value) || 150;

    if (fromId === toId) {
      this.showToast('Source and Target transit hubs cannot be the same vertex.', true);
      return;
    }

    const res = await API.addEdge(fromId, toId, dist, cost);
    if (res.success) {
      this.showToast(res.message);
      this.recordAudit(`Added connection edge: ${fromId} ↔ ${toId} (${dist} km, $${cost})`);
      this.closeEdgeModal();
      await this.loadConnections();
      await this.loadDashboard();
    } else {
      this.showToast(res.error || 'Failed to add connection edge.', true);
    }
  },

  async deleteEdge(fromId, toId) {
    if (!confirm(`Are you sure you want to remove the transit edge between ${fromId} and ${toId}?`)) return;

    const res = await API.removeEdge(fromId, toId);
    if (res.success) {
      this.showToast(res.message);
      this.recordAudit(`Removed connection edge: ${fromId} ↔ ${toId}`);
      await this.loadConnections();
      await this.loadDashboard();
    } else {
      this.showToast(res.error || 'Failed to remove edge.', true);
    }
  },

  // ==========================================
  // SYSTEM & DSA STATUS
  // ==========================================
  async loadSystemStatus() {
    try {
      const summary = await fetch('/api/dsa-lab/all-structures').then(r => r.json());
      document.getElementById('sys-graph-v').textContent = summary.graphVertices || '0';
      document.getElementById('sys-graph-e').textContent = this.allEdges.length;
      document.getElementById('sys-bst-nodes').textContent = summary.bstSize || '0';
      document.getElementById('sys-bst-height').textContent = summary.bstHeight || '0';
    } catch (e) {
      console.warn('System status check error:', e);
    }
  },

  showToast(message, isError = false) {
    const container = document.getElementById('admin-toast-container');
    if (!container) return;
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
  AdminApp.init();
});
