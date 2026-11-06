/**
 * Wayfarer — API Client
 * Centralized REST client communicating with Java Servlets.
 */

const WayfarerAPI = {
  async _fetch(url, options = {}) {
    try {
      const res = await fetch(url, options);
      const text = await res.text();
      let data = {};
      try {
        data = text ? JSON.parse(text) : {};
      } catch (jsonErr) {
        data = { success: false, error: text || 'Invalid JSON response from server' };
      }

      if (typeof data === 'object' && data !== null && !Array.isArray(data)) {
        if (!data.message && data.error) {
          data.message = data.error;
        } else if (!data.error && data.message) {
          data.error = data.message;
        }
        if (!res.ok && data.success === undefined) {
          data.success = false;
        }
      }
      return data;
    } catch (netErr) {
      return {
        success: false,
        error: netErr.message || 'Network connection failed. Please verify server is running.',
        message: netErr.message || 'Network connection failed. Please verify server is running.'
      };
    }
  },

  // Authentication
  async getMe() {
    return await this._fetch('/api/auth/me');
  },

  async login(email, password) {
    return await this._fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
  },

  async register(name, email, password) {
    return await this._fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, password })
    });
  },

  async logout() {
    return await this._fetch('/api/auth/logout', { method: 'POST' });
  },

  // Destinations
  async getDestinations(query = '', category = '') {
    const params = new URLSearchParams();
    if (query) params.append('query', query);
    if (category && category !== 'ALL') params.append('category', category);
    const queryString = params.toString();
    const url = queryString ? `/api/destinations?${queryString}` : '/api/destinations';
    return await this._fetch(url);
  },

  async getDestination(id) {
    return await this._fetch(`/api/destinations?id=${encodeURIComponent(id)}`);
  },

  async getCategories() {
    return await this._fetch('/api/destinations/categories');
  },

  async createDestination(data) {
    return await this._fetch('/api/destinations', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
  },

  async updateDestination(data) {
    return await this._fetch('/api/destinations', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
  },

  async deleteDestination(id) {
    return await this._fetch(`/api/destinations?id=${encodeURIComponent(id)}`, {
      method: 'DELETE'
    });
  },

  // Route Finder (BFS)
  async findBfsRoute(sourceCode, targetCode) {
    return await this._fetch('/api/routes/bfs', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ sourceCode, targetCode })
    });
  },

  // Recommendations (Greedy)
  async getGreedyRecommendations(params) {
    return await this._fetch('/api/recommendations/greedy', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(params)
    });
  },

  // Itineraries
  async getUserItineraries() {
    return await this._fetch('/api/itineraries');
  },

  async getItinerary(id) {
    return await this._fetch(`/api/itineraries?id=${encodeURIComponent(id)}`);
  },

  async createItinerary(name, budget) {
    return await this._fetch('/api/itineraries', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, budget })
    });
  },

  async updateItinerary(id, name, budget, status) {
    return await this._fetch('/api/itineraries', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id, name, budget, status })
    });
  },

  async deleteItinerary(id) {
    return await this._fetch(`/api/itineraries?id=${encodeURIComponent(id)}`, {
      method: 'DELETE'
    });
  },

  async addStop(itineraryId, destinationId, insertIndex = null, notes = '') {
    return await this._fetch('/api/itineraries/stops', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ itineraryId, destinationId, insertIndex, notes })
    });
  },

  async removeStop(itineraryId, index) {
    return await this._fetch(`/api/itineraries/stops?itineraryId=${encodeURIComponent(itineraryId)}&index=${encodeURIComponent(index)}`, {
      method: 'DELETE'
    });
  },

  async reorderStops(itineraryId, fromIndex, toIndex) {
    return await this._fetch('/api/itineraries/reorder', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ itineraryId, fromIndex, toIndex })
    });
  },

  async moveStopUp(itineraryId, index) {
    return await this._fetch('/api/itineraries/move-up', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ itineraryId, index })
    });
  },

  async moveStopDown(itineraryId, index) {
    return await this._fetch('/api/itineraries/move-down', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ itineraryId, index })
    });
  },

  // Admin Endpoints
  async getAdminStats() {
    return await this._fetch('/api/admin/stats');
  },

  async getAdminUsers() {
    return await this._fetch('/api/admin/users');
  },

  async getAdminItineraries() {
    return await this._fetch('/api/admin/itineraries');
  },

  async getConnections() {
    return await this._fetch('/api/connections');
  },

  async createConnection(data) {
    return await this._fetch('/api/connections', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
  },

  async updateConnection(data) {
    return await this._fetch('/api/connections', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
  },

  async deleteConnection(id) {
    return await this._fetch(`/api/connections?id=${encodeURIComponent(id)}`, {
      method: 'DELETE'
    });
  }
};
