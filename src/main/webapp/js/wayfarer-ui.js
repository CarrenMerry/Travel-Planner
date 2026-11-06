/**
 * Wayfarer — Shared UI Components & Navigation Helper
 * Premium Tropical Travel Platform
 */

const WayfarerUI = {
  currentUser: null,

  // Authentic high-res coastal & tropical travel photography fallbacks
  DEFAULT_IMAGES: {
    relaxation: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80', // Tropical Turquoise Beach
    adventure: 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1200&q=80',  // Coastal Hiking & Cliffs
    cultural: 'https://images.unsplash.com/photo-1548013146-72479768bada?auto=format&fit=crop&w=1200&q=80',   // Historic Architecture
    historical: 'https://images.unsplash.com/photo-1552832230-c0197dd311b5?auto=format&fit=crop&w=1200&q=80', // Ancient Colosseum / Ruins
    nature: 'https://images.unsplash.com/photo-1432405972618-c60b0225b8f9?auto=format&fit=crop&w=1200&q=80',     // Tropical Lagoon
    default: 'https://images.unsplash.com/photo-1512100356356-de1b84283e18?auto=format&fit=crop&w=1200&q=80'    // Coastal Paradise
  },

  getDestinationImage(dest) {
    if (dest && dest.imageUrl && dest.imageUrl.trim().startsWith('http')) {
      return dest.imageUrl.trim();
    }
    const cat = (dest && dest.category) ? dest.category.toLowerCase() : 'default';
    return this.DEFAULT_IMAGES[cat] || this.DEFAULT_IMAGES.default;
  },

  async fetchCurrentUser() {
    try {
      const res = await WayfarerAPI.getMe();
      if (res && res.authenticated) {
        this.currentUser = res.user || {
          id: res.id,
          name: res.name,
          email: res.email,
          role: res.role,
          isAdmin: res.isAdmin
        };
        return this.currentUser;
      }
    } catch (e) {
      console.warn('Session check failed:', e);
    }
    this.currentUser = null;
    return null;
  },

  async initNavbar(activePage = '') {
    await this.fetchCurrentUser();

    const header = document.querySelector('header.navbar') || document.getElementById('mainNav');
    if (!header) return;

    const isAdmin = this.currentUser && (this.currentUser.role === 'ADMIN' || this.currentUser.isAdmin);
    const isAuth = !!this.currentUser;

    header.innerHTML = `
      <div class="nav-container">
        <a href="index.html" class="brand">
          <div class="brand-icon-wrap">
            <svg class="brand-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.3" stroke-linecap="round" stroke-linejoin="round">
              <path d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
            </svg>
          </div>
          <span>Wayfarer</span>
        </a>

        <button class="nav-toggle" id="navToggle" aria-label="Toggle navigation menu" aria-expanded="false">
          <span></span>
          <span></span>
          <span></span>
        </button>

        <ul class="nav-links" id="navLinks">
          <li><a href="index.html" class="nav-link ${activePage === 'home' ? 'active' : ''}">Home</a></li>
          <li><a href="explore.html" class="nav-link ${activePage === 'destinations' || activePage === 'explore' ? 'active' : ''}">Destinations</a></li>
          ${isAuth ? `
            <li><a href="itineraries.html" class="nav-link ${activePage === 'itineraries' ? 'active' : ''}">My Trips</a></li>
            <li><a href="itinerary-builder.html" class="nav-link ${activePage === 'builder' ? 'active' : ''}">Trip Planner</a></li>
          ` : `
            <li><a href="itinerary-builder.html" class="nav-link ${activePage === 'builder' ? 'active' : ''}">Plan a Trip</a></li>
          `}
          <li><a href="route-planner.html" class="nav-link ${activePage === 'routes' ? 'active' : ''}">Route Finder</a></li>
          <li><a href="recommendations.html" class="nav-link ${activePage === 'recommendations' ? 'active' : ''}">Inspiration</a></li>
          <li><a href="dsa.html" class="nav-link ${activePage === 'dsa' ? 'active' : ''}" style="font-weight: 600;">DSA Lab</a></li>
          ${isAdmin ? `
            <li><a href="admin.html" class="nav-link ${activePage === 'admin' ? 'active' : ''}">Admin</a></li>
          ` : ''}
        </ul>

        <div class="nav-actions">
          ${isAuth ? `
            <a href="profile.html" class="nav-user-chip" title="Traveler Profile & Settings">
              <div class="nav-user-avatar">${(this.currentUser.name || 'U').charAt(0).toUpperCase()}</div>
              <span class="nav-user-name">${this.escape(this.currentUser.name || 'User')}</span>
              ${isAdmin ? '<span class="badge badge-accent">Admin</span>' : ''}
            </a>
            <button id="logoutNavBtn" class="btn btn-secondary btn-sm" title="Sign Out">Sign Out</button>
          ` : `
            <a href="login.html" class="btn btn-secondary btn-sm">Sign In</a>
            <a href="login.html?mode=register" class="btn btn-primary btn-sm">Sign Up</a>
          `}
        </div>
      </div>
    `;

    // Transparent to solid frosted navbar scroll behavior
    if (header.classList.contains('navbar-transparent')) {
      const handleScroll = () => {
        if (window.scrollY > 40) {
          header.classList.add('scrolled');
        } else {
          header.classList.remove('scrolled');
        }
      };
      window.addEventListener('scroll', handleScroll, { passive: true });
      handleScroll(); // Initial check
    }

    // Mobile navigation toggle
    const toggleBtn = document.getElementById('navToggle');
    const navLinks = document.getElementById('navLinks');
    if (toggleBtn && navLinks) {
      toggleBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        const isOpen = navLinks.classList.toggle('mobile-open');
        toggleBtn.classList.toggle('open', isOpen);
        toggleBtn.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
      });

      // Close menu when clicking outside
      document.addEventListener('click', (e) => {
        if (!header.contains(e.target)) {
          navLinks.classList.remove('mobile-open');
          toggleBtn.classList.remove('open');
          toggleBtn.setAttribute('aria-expanded', 'false');
        }
      });
    }

    const logoutBtn = document.getElementById('logoutNavBtn');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', async () => {
        await WayfarerAPI.logout();
        this.currentUser = null;
        window.location.href = 'index.html';
      });
    }
  },

  initFooter() {
    const footer = document.querySelector('footer.footer');
    if (!footer) return;
    footer.innerHTML = `
      <div class="footer-container">
        <div>
          <div class="footer-brand">
            <div class="brand-icon-wrap" style="width: 32px; height: 32px; font-size: 0.9rem;">
              ✈️
            </div>
            <span>Wayfarer</span>
          </div>
          <p class="footer-brand-desc">
            Your modern holiday booking and smart travel platform. Discover tropical escapes, organize custom itineraries with pointer-ordered stops, and discover minimum-hop routes.
          </p>
          <div style="font-size: 0.82rem; color: #8CA0A7; margin-top: 1rem;">
            Powered by pure Java 17, Apache Tomcat 10.1, and MySQL 8.0.
          </div>
        </div>

        <div class="footer-links-col">
          <h4>Destinations</h4>
          <ul>
            <li><a href="explore.html?category=Relaxation">Tropical & Beaches</a></li>
            <li><a href="explore.html?category=Adventure">Adventure & Trails</a></li>
            <li><a href="explore.html?category=Cultural">Cultural Heritage</a></li>
            <li><a href="explore.html?category=Historical">Historic Wonders</a></li>
            <li><a href="explore.html">View All Destinations</a></li>
          </ul>
        </div>

        <div class="footer-links-col">
          <h4>Trip Planning</h4>
          <ul>
            <li><a href="itinerary-builder.html">Plan a Trip</a></li>
            <li><a href="itineraries.html">My Travel Plans</a></li>
            <li><a href="recommendations.html">Smart Inspiration</a></li>
            <li><a href="dashboard.html">Traveler Dashboard</a></li>
            <li><a href="profile.html">Profile & Settings</a></li>
          </ul>
        </div>

        <div class="footer-links-col">
          <h4>Academic & Tools</h4>
          <ul>
            <li><a href="route-planner.html">BFS Route Finder</a></li>
            <li><a href="dsa.html">DSA Demonstration Lab</a></li>
            <li><a href="dsa.html#sectionLinkedList">Custom Linked List</a></li>
            <li><a href="dsa.html#sectionGraphBFS">Travel Graph & BFS</a></li>
            <li><a href="admin.html">Admin Portal</a></li>
          </ul>
        </div>
      </div>

      <div class="footer-bottom">
        <div>
          &copy; ${new Date().getFullYear()} <strong>Wayfarer Travel Platform</strong>. All rights reserved.
        </div>
        <div style="display: flex; gap: 1.25rem;">
          <span>Custom Linked List (Stops)</span>
          <span>&bull;</span>
          <span>Graph BFS (Fewest Hops)</span>
          <span>&bull;</span>
          <span>Greedy Heuristic Engine</span>
        </div>
      </div>
    `;
  },

  async requireLogin() {
    let user = this.currentUser;
    if (!user) {
      user = await this.fetchCurrentUser();
    }
    if (!user) {
      const currentPath = window.location.pathname.split('/').pop() || 'index.html';
      window.location.href = 'login.html?redirect=' + encodeURIComponent(currentPath + window.location.search);
      return null;
    }
    return user;
  },

  async requireAdmin() {
    const user = await this.requireLogin();
    if (!user) return null;
    if (user.role !== 'ADMIN' && !user.isAdmin) {
      alert('Access Restricted: Administrator privileges required.');
      window.location.href = 'dashboard.html';
      return null;
    }
    return user;
  },

  escape(str) {
    if (str === null || str === undefined) return '';
    const div = document.createElement('div');
    div.textContent = String(str);
    return div.innerHTML;
  },

  formatMoney(num) {
    return '$' + (Number(num) || 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  },

  bindPasswordToggle(inputEl, btnEl) {
    if (!inputEl || !btnEl) return;
    btnEl.addEventListener('click', () => {
      const isPw = inputEl.type === 'password';
      inputEl.type = isPw ? 'text' : 'password';
      btnEl.textContent = isPw ? 'Hide' : 'Show';
    });
  }
};
