// login.js - Login form handling

document.addEventListener('DOMContentLoaded', () => {
    // If already logged in, redirect to dashboard
    if (localStorage.getItem('currentUser')) {
        window.location.href = 'index.html';
        return;
    }

    const form = document.getElementById('loginForm');
    const messageEl = document.getElementById('loginMessage');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        messageEl.className = '';
        messageEl.style.display = 'none';

        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;

        if (!username || !password) {
            showMessage('Please enter both username and password.', 'error');
            return;
        }

        try {
            const response = await fetch(API_BASE_URL + '/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            const data = await response.json();

            if (response.ok) {
                // Store user info in localStorage
                localStorage.setItem('currentUser', JSON.stringify(data));
                showMessage('Login successful! Redirecting...', 'success');
                
                // Redirect based on role after a brief delay
                setTimeout(() => {
                    if (data.role === 'MANAGER' || data.role === 'HR_ADMIN') {
                        window.location.href = 'manager_approvals.html';
                    } else {
                        window.location.href = 'index.html';
                    }
                }, 1000);
            } else {
                showMessage(data.error || 'Invalid username or password.', 'error');
            }
        } catch (err) {
            showMessage('Connection error. Please try again.', 'error');
            console.error('Login error:', err);
        }
    });

    function showMessage(text, type) {
        messageEl.textContent = text;
        messageEl.className = type;
        messageEl.style.display = 'block';
    }
});
