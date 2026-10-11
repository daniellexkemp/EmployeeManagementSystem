// auth.js - Authentication utilities for all pages

/**
 * Get the currently logged-in user from localStorage.
 * Returns null if no user is stored.
 */
function getCurrentUser() {
    const raw = localStorage.getItem('currentUser');
    if (!raw) return null;
    try {
        return JSON.parse(raw);
    } catch {
        return null;
    }
}

/**
 * Clear the current user session from localStorage.
 */
function logout() {
    localStorage.removeItem('currentUser');
    window.location.href = 'login.html';
}

/**
 * Check if the user is authenticated.
 * If not, redirect to login page.
 */
function requireAuth() {
    if (!getCurrentUser()) {
        window.location.href = 'login.html';
        return false;
    }
    return true;
}

/**
 * Display the current user's name in the header bar.
 * Called by each page's JS on DOMContentLoaded.
 */
function displayCurrentUser() {
    const user = getCurrentUser();
    const el = document.getElementById('currentUser');
    if (el && user) {
        el.textContent = `${user.firstName} ${user.lastName} (${user.role})`;
    }
}

/**
 * Check role permissions.
 * Returns true if user has one of the required roles.
 */
function hasRole(...allowedRoles) {
    const user = getCurrentUser();
    if (!user) return false;
    return allowedRoles.includes(user.role);
}