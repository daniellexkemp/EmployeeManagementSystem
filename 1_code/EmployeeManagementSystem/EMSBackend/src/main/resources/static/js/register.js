// register.js - Registration form handling

document.addEventListener('DOMContentLoaded', () => {
    // If already logged in, redirect to dashboard
    if (localStorage.getItem('currentUser')) {
        window.location.href = 'index.html';
        return;
    }

    const form = document.getElementById('registerForm');
    const messageEl = document.getElementById('registerMessage');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        messageEl.className = '';
        messageEl.style.display = 'none';

        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        const employeeId = document.getElementById('employeeId').value;

        // Validation
        if (!username || !password || !employeeId) {
            showMessage('Please fill in all fields.', 'error');
            return;
        }

        if (password !== confirmPassword) {
            showMessage('Passwords do not match.', 'error');
            return;
        }

        if (password.length < 6) {
            showMessage('Password must be at least 6 characters.', 'error');
            return;
        }

        try {
            const response = await fetch(API_BASE_URL + '/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password, employeeId: parseInt(employeeId) })
            });

            const data = await response.json();

            if (response.ok) {
                showMessage('Registration successful! Redirecting to login...', 'success');
                setTimeout(() => {
                    window.location.href = 'login.html';
                }, 2000);
            } else {
                showMessage(data.error || 'Registration failed.', 'error');
            }
        } catch (err) {
            showMessage('Connection error. Please try again.', 'error');
            console.error('Registration error:', err);
        }
    });

    function showMessage(text, type) {
        messageEl.textContent = text;
        messageEl.className = type;
        messageEl.style.display = 'block';
    }
});
