// js/api.js

// Shared API configuration and fetch wrappers

const API_BASE_URL = 'http://localhost:8080/api';

async function apiRequest(endpoint, method = 'GET', body = null) {
    const options = {
        method,
        headers: { 'Content-Type': 'application/json' }
    };
    if (body) options.body = JSON.stringify(body);

    const response = await fetch(`${API_BASE_URL}${endpoint}`, options);

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || `Request failed with status ${response.status}`);
    }

    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

// HR / Employee Records
window.EmployeeAPI = {
    getAll: (search = '') => {
        const query = search ? `?search=${encodeURIComponent(search)}` : '';
        return apiRequest(`/employees${query}`, 'GET');
    },
    getById: (id) => apiRequest(`/employees/${id}`, 'GET'),
    create: (data) => apiRequest('/employees', 'POST', data),
    update: (id, data) => apiRequest(`/employees/${id}`, 'PUT', data),
    deactivate: (id) => apiRequest(`/employees/${id}/deactivate`, 'PATCH')
};