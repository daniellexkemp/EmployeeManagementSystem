
// auth.js - Authentication utilities for EMS pages

let currentUser = null;

// Return the user verified by the server during this page load.
function getCurrentUser() {
    return currentUser;
}

// Verify the current session with the backend.
async function requireAuth() {
    try {
        currentUser = await AuthAPI.currentUser();

        displayCurrentUser();
        return true;
    } catch (error) {
        currentUser = null;
        window.location.replace('login.html');
        return false;
    }
}

// Log out of the server-side session.
async function logout() {
    try {
        await AuthAPI.logout();
    } catch (error) {
        console.error('Logout failed:', error);
        alert('Unable to log out. Please try again.');
        return;
    }

    currentUser = null;
    localStorage.removeItem('currentUser');
    window.location.replace('login.html');
}

// Display the authenticated user's name.
function displayCurrentUser() {
    const el = document.getElementById('currentUser');

    if (el && currentUser) {
        el.textContent =
            `${currentUser.firstName} ${currentUser.lastName} (${currentUser.role})`;
    }
}

// Frontend convenience check only.
// Backend endpoints must independently enforce permissions.
function hasRole(...allowedRoles) {
    return currentUser !== null &&
        allowedRoles.includes(currentUser.role);
}
