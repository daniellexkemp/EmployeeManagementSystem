
// login.js - Login form handling

document.addEventListener('DOMContentLoaded', async () => {
    const form = document.getElementById('loginForm');
    const messageEl = document.getElementById('loginMessage');

    // Do not trust an old localStorage login.
    localStorage.removeItem('currentUser');

    // Check whether the server already has an authenticated session.
    try {
        const existingUser = await AuthAPI.currentUser();

        if (existingUser) {
            redirectForRole(existingUser.role);
            return;
        }
    } catch (error) {
        // No valid session; allow the user to log in.
    }

    if (!form || !messageEl) {
        console.error('Login form elements not found.');
        return;
    }

    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        messageEl.className = '';
        messageEl.style.display = 'none';

        const username =
            document.getElementById('username').value.trim();

        const password =
            document.getElementById('password').value;

        if (!username || !password) {
            showMessage(
                'Please enter both username and password.',
                'error'
            );
            return;
        }

        const submitButton =
            form.querySelector('button[type="submit"]');

        if (submitButton) {
            submitButton.disabled = true;
        }

        try {
            const user = await AuthAPI.login(username, password);

            showMessage(
                'Login successful! Redirecting...',
                'success'
            );

            redirectForRole(user.role);

        } catch (error) {
            console.error('Login error:', error);

            showMessage(
                'Login failed. Check your credentials and try again.',
                'error'
            );

        } finally {
            if (submitButton) {
                submitButton.disabled = false;
            }
        }
    });

    function showMessage(message, type) {
        messageEl.textContent = message;
        messageEl.className = type;
        messageEl.style.display = 'block';
    }

    function redirectForRole(role) {
        if (role === 'MANAGER' || role === 'HR_ADMIN') {
            window.location.replace('manager_approvals.html');
        } else {
            window.location.replace('index.html');
        }
    }
});
