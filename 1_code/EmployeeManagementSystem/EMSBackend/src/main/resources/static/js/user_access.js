// User Access - Role management (UC-7)

document.addEventListener('DOMContentLoaded', async () => {
    if (!(await requireAuth())) return;

    displayCurrentUser();

    const feedback = document.getElementById('accessFeedback');
    const select = document.getElementById('employeeSelect');
    const roleSelect = document.getElementById('roleSelect');
    const assignBtn = document.getElementById('assignBtn');
    const cancelBtn = document.getElementById('cancelBtn');

    const permissionBoxes = document.querySelectorAll('[data-perm]');

    // Permissions included in each role, from least to most access
    const ROLE_ORDER = ['EMPLOYEE', 'MANAGER', 'HR_ADMIN'];
    const ROLE_PERMISSIONS = {
        EMPLOYEE: [],
        MANAGER: ['viewRecords', 'approveTimeOff'],
        HR_ADMIN: ['viewRecords', 'approveTimeOff', 'deactivateRecords',
                   'createEditRecords', 'securitySettings']
    };

    // Users loaded from the server, kept so we know each user's saved role
    let users = [];

    function updatePreview() {
        const allowed = ROLE_PERMISSIONS[roleSelect.value] || [];
        permissionBoxes.forEach(box => {
            box.checked = allowed.includes(box.dataset.perm);
        });
    }

    // Set the role dropdown to the selected user's saved role
    function showSavedRole() {
        const user = users.find(u => String(u.id) === select.value);
        if (user) {
            roleSelect.value = user.role;
        }
        updatePreview();
    }

    if (!hasRole('HR_ADMIN', 'SYS_ADMIN')) {
        feedback.textContent = 'HR administrators only.';
        select.disabled = true;
        roleSelect.disabled = true;
        assignBtn.disabled = true;
        cancelBtn.disabled = true;
        permissionBoxes.forEach(box => { box.disabled = true; });
        return;
    }

    async function loadUsers() {
        const previousSelection = select.value;

        users = await apiRequest('/users');

        select.replaceChildren();

        users.forEach(user => {
            const option = document.createElement('option');

            option.value = user.id;
            option.textContent =
                `${user.firstName} ${user.lastName} (${user.username})`;

            select.appendChild(option);
        });

        // Keep the same user selected after a save
        if (previousSelection) {
            select.value = previousSelection;
        }

        assignBtn.disabled = users.length === 0;
        cancelBtn.disabled = users.length === 0;

        if (users.length === 0) {
            feedback.textContent = 'No user accounts found.';
        }

        showSavedRole();
    }

    try {
        await loadUsers();
    } catch (error) {
        feedback.textContent = 'Unable to load user accounts.';
        assignBtn.disabled = true;
        console.error(error);
    }

    // Changing the user shows that user's saved role
    select.addEventListener('change', () => {
        feedback.textContent = '';
        showSavedRole();
    });

    // Changing the role updates the permissions preview
    roleSelect.addEventListener('change', updatePreview);

    // Clicking a permission switches to the role that matches it.
    // Checking a box picks the lowest role that includes that permission;
    // unchecking a box picks the highest role that does not include it.
    permissionBoxes.forEach(box => {
        box.addEventListener('change', () => {
            const perm = box.dataset.perm;
            let newRole;

            if (box.checked) {
                newRole = ROLE_ORDER.find(role =>
                    ROLE_PERMISSIONS[role].includes(perm));
            } else {
                newRole = [...ROLE_ORDER].reverse().find(role =>
                    !ROLE_PERMISSIONS[role].includes(perm));
            }

            roleSelect.value = newRole;
            updatePreview();
            feedback.textContent = '';
        });
    });

    // Cancel discards the unsaved role change
    cancelBtn.addEventListener('click', () => {
        feedback.textContent = '';
        showSavedRole();
    });

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

            await loadUsers();

            feedback.textContent = 'User role updated successfully.';

        } catch (error) {
            feedback.textContent = 'Unable to update user role.';
            console.error(error);

        } finally {
            assignBtn.disabled = false;
        }
    });
});