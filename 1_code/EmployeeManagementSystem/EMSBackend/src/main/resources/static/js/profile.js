// Susan: Employee self-service edit logic (UC-2)

// Temporary employee ID for testing.
// Authentication will determine the logged-in employee in a later version.
const employeeId = 1;

let originalEmployee = null;

// Load employee information when the page opens.
async function loadProfile() {
    const message = document.getElementById('message');

    try {
        const employee = await apiRequest(`/employees/${employeeId}`);

        originalEmployee = employee;

        document.getElementById('firstName').value = employee.firstName || '';
        document.getElementById('lastName').value = employee.lastName || '';
        document.getElementById('department').value = employee.department || '';
        document.getElementById('jobTitle').value = employee.jobTitle || '';
        document.getElementById('email').value = employee.email || '';
        document.getElementById('phone').value = employee.phone || '';

        message.textContent = '';
    } catch (error) {
        console.error('Error loading employee profile:', error);
        message.textContent = 'Unable to load employee profile.';
    }
}

// Save permitted profile changes.
async function saveProfile(event) {
    event.preventDefault();

    const email = document.getElementById('email').value.trim();
    const phone = document.getElementById('phone').value.trim();
    const message = document.getElementById('message');

    if (email === '' || phone === '') {
        message.textContent = 'Email and phone are required.';
        return;
    }

    const updatedEmployee = {
        ...originalEmployee,
        email: email,
        phone: phone
    };

    try {
        const savedEmployee = await apiRequest(
            `/employees/${employeeId}`,
            'PUT',
            updatedEmployee
        );

        originalEmployee = savedEmployee;

        document.getElementById('email').value = savedEmployee.email || '';
        document.getElementById('phone').value = savedEmployee.phone || '';

        message.textContent = 'Profile updated successfully.';
    } catch (error) {
        console.error('Error updating employee profile:', error);
        message.textContent = 'Unable to update employee profile.';
    }
}

// Restore the original values if the employee selects Cancel.
function cancelChanges() {
    if (!originalEmployee) {
        return;
    }

    document.getElementById('email').value = originalEmployee.email || '';
    document.getElementById('phone').value = originalEmployee.phone || '';
    document.getElementById('message').textContent = 'Changes canceled.';
}

document.getElementById('profileForm').addEventListener('submit', saveProfile);
document.getElementById('cancelButton').addEventListener('click', cancelChanges);

loadProfile();