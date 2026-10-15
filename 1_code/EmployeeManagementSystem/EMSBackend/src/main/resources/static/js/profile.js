
// Employee self-service profile management.

let originalEmployee = null;

// Load the authenticated employee's profile.
async function loadProfile() {
    if (!(await requireAuth())) return;

    displayCurrentUser();

    const message = document.getElementById('message');

    try {
        const employee = await apiRequest('/employees/me');

        originalEmployee = employee;

        document.getElementById('firstName').value =
            employee.firstName || '';

        document.getElementById('lastName').value =
            employee.lastName || '';

        document.getElementById('department').value =
            employee.department || '';

        document.getElementById('jobTitle').value =
            employee.jobTitle || '';

        document.getElementById('email').value =
            employee.email || '';

        document.getElementById('phone').value =
            employee.phone || '';

        message.textContent = '';

    } catch (error) {
        console.error('Error loading employee profile:', error);
        message.textContent = 'Unable to load employee profile.';
    }
}

// Save only the permitted contact information.
async function saveProfile(event) {
    event.preventDefault();

    const message = document.getElementById('message');

    const email = document.getElementById('email').value.trim();
    const phone = document.getElementById('phone').value.trim();

    if (!email || !phone) {
        message.textContent = 'Email and phone are required.';
        return;
    }

    try {
        const savedEmployee = await apiRequest(
            '/employees/me/contact',
            'PUT',
            {
                email: email,
                phone: phone
            }
        );

        originalEmployee = savedEmployee;

        document.getElementById('email').value =
            savedEmployee.email || '';

        document.getElementById('phone').value =
            savedEmployee.phone || '';

        message.textContent = 'Profile updated successfully.';

    } catch (error) {
        console.error('Error updating employee profile:', error);
        message.textContent = 'Unable to update employee profile.';
    }
}

// Restore the original contact information.
function cancelChanges() {
    if (!originalEmployee) return;

    document.getElementById('email').value =
        originalEmployee.email || '';

    document.getElementById('phone').value =
        originalEmployee.phone || '';

    document.getElementById('message').textContent =
        'Changes canceled.';
}

document.getElementById('profileForm')
    .addEventListener('submit', saveProfile);

document.getElementById('cancelButton')
    .addEventListener('click', cancelChanges);

loadProfile();
