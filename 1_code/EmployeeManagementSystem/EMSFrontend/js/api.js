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
    return response.json();
}