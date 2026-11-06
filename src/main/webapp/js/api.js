/**
 * API Client Module for Smart Travel Planner.
 * Communicates with Java REST endpoints deployed on embedded Apache Tomcat.
 */

const API = {
  // Authentication & Session
  async getMe() {
    const res = await fetch('/api/auth/me');
    return await res.json();
  },

  async login(username, password) {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    return await res.json();
  },

  async logout() {
    const res = await fetch('/api/auth/logout', {
      method: 'POST'
    });
    return await res.json();
  },

  async register(username, password, email, fullName) {
    const res = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password, email, fullName })
    });
    return await res.json();
  },

  // Dashboard
  async getDashboard() {
    const res = await fetch('/api/dashboard');
    return await res.json();
  },

  // Destinations
  async getDestinations() {
    const res = await fetch('/api/destinations');
    return await res.json();
  },

  async getDestination(id) {
    const res = await fetch(`/api/destinations?id=${encodeURIComponent(id)}`);
    return await res.json();
  },

  async createDestination(data) {
    const res = await fetch('/api/destinations', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    return await res.json();
  },

  async updateDestination(data) {
    const res = await fetch('/api/destinations', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    return await res.json();
  },

  async deleteDestination(id) {
    const res = await fetch(`/api/destinations?id=${encodeURIComponent(id)}`, {
      method: 'DELETE'
    });
    return await res.json();
  },

  // Itinerary (Linked List & Stack Undo)
  async getItinerary() {
    const res = await fetch('/api/itinerary');
    return await res.json();
  },

  async addToItinerary(destinationId, index = null) {
    const payload = { destinationId };
    if (index !== null) payload.index = index;
    const res = await fetch('/api/itinerary', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    return await res.json();
  },

  async removeFromItinerary(index) {
    const res = await fetch(`/api/itinerary?index=${index}`, {
      method: 'DELETE'
    });
    return await res.json();
  },

  async reorderItinerary(fromIndex, toIndex) {
    const res = await fetch('/api/itinerary/reorder', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fromIndex, toIndex })
    });
    return await res.json();
  },

  async undoItinerary() {
    const res = await fetch('/api/itinerary/undo', {
      method: 'POST'
    });
    return await res.json();
  },

  async clearItinerary() {
    const res = await fetch('/api/itinerary/clear', {
      method: 'POST'
    });
    return await res.json();
  },

  // Graph (BFS, DFS, Dijkstra)
  async getGraph() {
    const res = await fetch('/api/graph');
    return await res.json();
  },

  async runBfs(startId) {
    const res = await fetch('/api/graph/bfs', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ startId })
    });
    return await res.json();
  },

  async runDfs(startId) {
    const res = await fetch('/api/graph/dfs', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ startId })
    });
    return await res.json();
  },

  async runDijkstra(startId, targetId) {
    const res = await fetch('/api/graph/dijkstra', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ startId, targetId })
    });
    return await res.json();
  },

  async addEdge(fromId, toId, distanceKm, travelCost) {
    const res = await fetch('/api/graph/edge', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fromId, toId, distanceKm, travelCost })
    });
    return await res.json();
  },

  async removeEdge(fromId, toId) {
    const res = await fetch(`/api/graph/edge?fromId=${encodeURIComponent(fromId)}&toId=${encodeURIComponent(toId)}`, {
      method: 'DELETE'
    });
    return await res.json();
  },

  // Searching & Sorting
  async search(query, algo = 'linear') {
    const res = await fetch(`/api/search-sort/search?query=${encodeURIComponent(query)}&algo=${algo}`);
    return await res.json();
  },

  async sort(by = 'cost', order = 'asc', algo = 'mergesort') {
    const res = await fetch(`/api/search-sort/sort?by=${by}&order=${order}&algo=${algo}`);
    return await res.json();
  },

  // Optimization (Greedy, DP, Backtracking)
  async runGreedy(startId, maxStops = 5) {
    const res = await fetch('/api/optimize/greedy', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ startId, maxStops })
    });
    return await res.json();
  },

  async runDp(budget = 1200) {
    const res = await fetch('/api/optimize/dp', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ budget })
    });
    return await res.json();
  },

  async runBacktrack(maxBudget = 3000, maxDuration = 10, minStops = 3) {
    const res = await fetch('/api/optimize/backtrack', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ maxBudget, maxDuration, minStops })
    });
    return await res.json();
  },

  // Request Queue
  async getQueue() {
    const res = await fetch('/api/queue');
    return await res.json();
  },

  async enqueueRequest(travelerName, requestType, destinationId, destinationName) {
    const res = await fetch('/api/queue', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ travelerName, requestType, destinationId, destinationName })
    });
    return await res.json();
  },

  async processNextRequest() {
    const res = await fetch('/api/queue/process', {
      method: 'POST'
    });
    return await res.json();
  },

  // DSA Lab
  async evaluateExpression(expression) {
    const res = await fetch('/api/dsa-lab/evaluate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ expression })
    });
    return await res.json();
  },

  async getBstData() {
    const res = await fetch('/api/dsa-lab');
    return await res.json();
  },

  async searchBst(name) {
    const res = await fetch('/api/dsa-lab/bst', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'search', destinationName: name })
    });
    return await res.json();
  },

  async runTreeTraversals(nodeLabels = null) {
    const payload = nodeLabels ? { nodeLabels } : {};
    const res = await fetch('/api/dsa-lab/tree', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    return await res.json();
  }
};
