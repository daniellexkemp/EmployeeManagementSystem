
// api.js - Shared API configuration and request helpers

const API_BASE_URL = '/api';

let csrfToken = null;

// Retrieve a CSRF token from Spring Security.
async function refreshCsrfToken() {
    const response = await fetch(`${API_BASE_URL}/auth/csrf`, {
        method: 'GET',
        credentials: 'same-origin'
    });

    if (!response.ok) {
        throw new Error('Unable to retrieve security token.');
    }

    const data = await response.json();
    csrfToken = data.token;

    return csrfToken;
}

// Make an API request with session cookies and CSRF protection.
async function apiRequest(endpoint, method = 'GET', body = null) {
    const normalizedMethod = method.toUpperCase();

    const options = {
        method: normalizedMethod,
        credentials: 'same-origin',
        headers: {
            'Accept': 'application/json'
        }
    };

    if (body !== null) {
        options.headers['Content-Type'] = 'application/json';
        options.body = JSON.stringify(body);
    }

    if (!['GET', 'HEAD', 'OPTIONS'].includes(normalizedMethod)) {
        if (!csrfToken) {
            await refreshCsrfToken();
        }

        options.headers['X-CSRF-TOKEN'] = csrfToken;
    }

    const response = await fetch(`${API_BASE_URL}${endpoint}`, options);

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(
            errorText || `Request failed with status ${response.status}`
        );
    }

    const text = await response.text();

    return text ? JSON.parse(text) : null;
}

// Authentication API
window.AuthAPI = {
    login: async (username, password) => {
        const user = await apiRequest(
            '/auth/login',
            'POST',
            { username, password }
        );

        // Refresh the token after login because authentication
        // can invalidate the previous CSRF token.
        csrfToken = null;
        await refreshCsrfToken();

        return user;
    },

    currentUser: () => apiRequest('/auth/me'),

    logout: async () => {
        const result = await apiRequest('/auth/logout', 'POST');
        csrfToken = null;
        return result;
    },

    register: (username, password, employeeId) =>
        apiRequest('/auth/register', 'POST', {
            username,
            password,
            employeeId
        }),

    changePassword: (oldPassword, newPassword) =>
        apiRequest('/auth/change-password', 'POST', {
            oldPassword,
            newPassword
        })
};

// HR / Employee Records API
window.EmployeeAPI = {
    getAll: (search = '') => {
        const query = search
            ? `?search=${encodeURIComponent(search)}`
            : '';

        return apiRequest(`/employees${query}`);
    },

    getById: (id) =>
        apiRequest(`/employees/${id}`),

    create: (data) =>
        apiRequest('/employees', 'POST', data),

    update: (id, data) =>
        apiRequest(`/employees/${id}`, 'PUT', data),

    deactivate: (id) =>
        apiRequest(`/employees/${id}/deactivate`, 'PATCH')
};
