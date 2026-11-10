// Danielle: New hire setup logic (UC-5)

// Roles allowed to create employee records (REQ-1, REQ-14)
const ONBOARDING_ROLES = ['HR_ADMIN', 'SYS_ADMIN'];

const FIELDS = ['firstName', 'lastName', 'email', 'phone', 'department', 'jobTitle'];

document.addEventListener('DOMContentLoaded', async () => {
    if (!(await requireAuth())) return;
    displayCurrentUser();

    
    try {
        const employees = await apiRequest('/employees/supervisors');
        const supervisorDropdown = document.getElementById('supervisorId');

        employees.forEach(employee => {
            const option = document.createElement('option');
            option.value = employee.id;
            option.textContent = `${employee.firstName} ${employee.lastName}`;
            supervisorDropdown.appendChild(option);
        });
    } catch (err) {
        console.error('Unable to load supervisors:', err);
    }






    // Block the form for roles that can't create employees
    if (!(await canOnboard())) {
        showAccessDenied();
        return;
    }

    document.getElementById('onboardingForm').addEventListener('submit', handleSubmit);
});

/* --------------------- Access check --------------------------------------- */

async function canOnboard() {
    try {
        const user = await AuthAPI.currentUser();
        const roles = [].concat(user?.role ?? user?.roles ?? []);
        return roles.some(r => ONBOARDING_ROLES.includes(String(r).replace(/^ROLE_/, '')));
    } catch (err) {
        return false;
    }
}

function showAccessDenied() {
    document.getElementById('onboardingMain').innerHTML = `
        <h2>CREATE EMPLOYEE RECORD</h2>
        <p>Only HR administrators can create employee records.</p>
    `;
}

/* --------------------- Form handling -------------------------------------- */

async function handleSubmit(event) {
    event.preventDefault();
    clearErrors();

    const values = {};
    FIELDS.forEach(id => { values[id] = document.getElementById(id).value.trim(); });

    const errors = validate(values);
    if (Object.keys(errors).length > 0) {
        showErrors(errors);
        return;
    }

    const submitBtn = document.getElementById('submitBtn');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Saving...';

    try {
       const supervisorId = document.getElementById('supervisorId').value || null;

await apiRequest(
    `/employees${supervisorId ? '?supervisorId=' + encodeURIComponent(supervisorId) : ''}`,
    'POST',
    { ...values, employmentStatus: 'ACTIVE' }
);
        showMessage(`${values.firstName} ${values.lastName} was added to the employee directory.`, 'success');

        document.getElementById('onboardingForm').reset();
    } catch (err) {
        showMessage(friendlyError(err), 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Complete Onboarding';
    }
}

// Same rules as the Edit popup in the Employee Directory (NFR-5)
function validate(values) {
    const errors = {};

    if (!values.firstName) errors.firstName = 'Enter a first name.';
    if (!values.lastName) errors.lastName = 'Enter a last name.';

    if (!values.email) {
        errors.email = 'Enter an email address.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email)) {
        errors.email = 'Enter a valid email address, such as name@company.com.';
    }

    if (!values.phone) {
        errors.phone = 'Enter a phone number.';
    } else {
        const digits = values.phone.replace(/\D/g, '');
        if (!/^[0-9()+\-.\s]+$/.test(values.phone) || digits.length < 7 || digits.length > 15) {
            errors.phone = 'Enter a valid phone number, such as 555-0101.';
        }
    }

    if (!values.department) errors.department = 'Enter a department.';
    if (!values.jobTitle) errors.jobTitle = 'Enter a job title.';

    return errors;
}

function clearErrors() {
    FIELDS.forEach(id => {
        const input = document.getElementById(id);
        input.classList.remove('invalid');
        input.removeAttribute('aria-invalid');
        document.getElementById(`${id}Error`).textContent = '';
    });

    const message = document.getElementById('statusMessage');
    message.hidden = true;
    message.textContent = '';
}

function showErrors(errors) {
    Object.entries(errors).forEach(([id, text]) => {
        const input = document.getElementById(id);
        input.classList.add('invalid');
        input.setAttribute('aria-invalid', 'true');
        document.getElementById(`${id}Error`).textContent = text;
    });

    showMessage('Fix the highlighted fields, then submit again.', 'error');
    document.getElementById(Object.keys(errors)[0]).focus();
}

function showMessage(text, type) {
    const message = document.getElementById('statusMessage');
    message.className = type;
    message.setAttribute('role', type === 'error' ? 'alert' : 'status');
    message.textContent = text;
    message.hidden = false;
}

// apiRequest throws the raw response body; turn it into a readable message
function friendlyError(err) {
    let status = null;
    const text = err && err.message ? err.message : '';

    try {
        status = JSON.parse(text).status || null;
    } catch (_) {
        const match = text.match(/status (\d{3})/);
        if (match) status = Number(match[1]);
    }

    if (status === 401) return 'Your session has expired. Sign in again to continue.';
    if (status === 403) return 'Only HR administrators can create employee records.';
    if (status === 400) return 'Some information was not accepted. Check the fields and try again.';
    if (status === 409 || status === 500) return 'The employee could not be added. The email address may already belong to another employee.';

    return 'The employee could not be added. Check your connection and try again.';
}