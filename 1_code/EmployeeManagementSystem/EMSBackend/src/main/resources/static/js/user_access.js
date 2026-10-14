
// User Access - Role management

document.addEventListener('DOMContentLoaded', async () => {
    if (!(await requireAuth())) return;

    displayCurrentUser();

    const feedback = document.getElementById('accessFeedback');
    const select = document.getElementById('employeeSelect');
    const roleSelect = document.getElementById('roleSelect');
    const assignBtn = document.getElementById('assignBtn');

    if (!hasRole('HR_ADMIN', 'SYS_ADMIN')) {
        feedback.textContent = 'HR administrators only.';
        select.disabled = true;
        roleSelect.disabled = true;
        assignBtn.disabled = true;
        return;
    }

    async function loadUsers() {
        const users = await apiRequest('/users');

        select.replaceChildren();

        users.forEach(user => {
            const option = document.createElement('option');

            option.value = user.id;
            option.textContent =
                `${user.firstName} ${user.lastName} ` +
                `(${user.username}) - ${user.role}`;

            select.appendChild(option);
        });

        assignBtn.disabled = users.length === 0;

        if (users.length === 0) {
            feedback.textContent = 'No user accounts found.';
        }
    }

    try {
        await loadUsers();
    } catch (error) {
        feedback.textContent = 'Unable to load user accounts.';
        assignBtn.disabled = true;
        console.error(error);
    }

    assignBtn.addEventListener('click', async () => {
        const userId = select.value;
        const role = roleSelect.value;

        if (!userId) {
            feedback.textContent = 'Select a user account.';
            return;
        }

        assignBtn.disabled = true;
        feedback.textContent = '';

        try {
            await apiRequest(
                `/users/${userId}/role`,
                'PUT',
                { role }
            );

            feedback.textContent = 'User role updated successfully.';

            await loadUsers();

        } catch (error) {
            feedback.textContent = 'Unable to update user role.';
            console.error(error);

        } finally {
            assignBtn.disabled = false;
        }
    });
});
