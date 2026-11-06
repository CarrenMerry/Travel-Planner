/**
 * Wayfarer — Common UI Controller & Auth Guard
 */

const Wayfarer = {
  currentUser: null,

  async init(requiredRole = null) {
    try {
      const auth = await WayfarerAPI.getMe();
      if (auth.authenticated) {
        this.currentUser = auth;
        this.updateUserUI(auth);

        if (requiredRole === 'ADMIN' && !auth.isAdmin) {
          window.location.href = '/user/dashboard.html';
          return;
        }
      } else {
        if (requiredRole) {
          window.location.href = '/login.html';
          return;
        }
      }
    } catch (e) {
      console.warn('Session check failed:', e);
      if (requiredRole) {
        window.location.href = '/login.html';
      }
    }
  },

  updateUserUI(user) {
    const nameEls = document.querySelectorAll('.wf-current-user-name');
    const emailEls = document.querySelectorAll('.wf-current-user-email');
    const avatarEls = document.querySelectorAll('.wf-current-user-avatar');

    nameEls.forEach(el => el.textContent = user.name || user.email);
    emailEls.forEach(el => el.textContent = user.email);
    avatarEls.forEach(el => {
      const initial = (user.name || user.email || 'W').charAt(0).toUpperCase();
      el.textContent = initial;
    });
  },

  async logout() {
    try {
      await WayfarerAPI.logout();
    } catch (e) {
      console.error(e);
    }
    window.location.href = '/login.html';
  },

  showToast(message, isError = false) {
    let container = document.getElementById('wf-toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'wf-toast-container';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `wf-toast ${isError ? 'error' : ''}`;
    toast.innerHTML = `
      <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
        ${isError ? '<circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>' : '<path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>'}
      </svg>
      <span>${message}</span>
    `;
    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(10px)';
      toast.style.transition = 'all 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  }
};
