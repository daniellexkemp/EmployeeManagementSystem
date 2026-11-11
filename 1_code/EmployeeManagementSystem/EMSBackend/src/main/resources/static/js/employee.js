// Danielle: Employee Directory Controller logic (UC-1)

let allEmployees = [];
let currentEmployee = null;     // employee currently shown in the modal
let lastFocusedElement = null;  // returns keyboard focus after the modal closes
let currentList = [];           // rows currently shown, used by Export

// Deactivated records may be saved as either value; treat both as inactive.
const INACTIVE_STATUSES = ['INACTIVE', 'DEACTIVATED'];
// Statuses HR can choose in the Edit form. Deactivation stays on its own button.
const EDITABLE_STATUSES = ['ACTIVE', 'ON_LEAVE'];
// Roles allowed to use the Employee Directory (REQ-14)
const DIRECTORY_ROLES = ['HR_ADMIN', 'SYS_ADMIN', 'MANAGER'];

document.addEventListener('DOMContentLoaded', async () => {
    // Only run on the directory page
    if (!document.getElementById('employeeTableBody')) return;

    if (!(await requireAuth())) return;
    displayCurrentUser();

    
    const addEmployeeLink = document.getElementById('addEmployeeLink');
    
    if (addEmployeeLink && (hasRole('HR_ADMIN') || hasRole('SYS_ADMIN'))) {

    addEmployeeLink.style.display = '';
}


    // Show the Time-Off Report navigation link to HR administrators.
    const reportLink = document.getElementById('timeOffReportLink');
    if (reportLink && hasRole('HR_ADMIN')) {
        reportLink.hidden = false;
    }

    // Block the page for roles that aren't allowed to see employee records
    if (!(await canUseDirectory())) {
        showAccessDenied();
        return;
    }


    await loadEmployees();

    // Filtering
    document.getElementById('searchBtn').addEventListener('click', filterTable);
    document.getElementById('searchInput').addEventListener('keyup', (e) => {
        if (e.key === 'Enter') filterTable();
    });
    document.getElementById('departmentFilter').addEventListener('change', filterTable);
    document.getElementById('statusFilter').addEventListener('change', filterTable);

    const addEmployeeBtn = document.getElementById('addEmployeeBtn');

if (addEmployeeBtn) {
    if (hasRole('HR_ADMIN') || hasRole('SYS_ADMIN')) {
        addEmployeeBtn.addEventListener('click', () => {
            window.location.href = 'onboarding.html';
        });
    } else {
        addEmployeeBtn.hidden = true;
    }
}
   

    document.getElementById('exportBtn').addEventListener('click', exportCsv);

    // Row buttons (View / Edit / Deactivate / Reactivate)
    document.getElementById('employeeTableBody').addEventListener('click', handleRowAction);

    // Modal: close button, click outside, Escape key
    document.getElementById('modalCloseBtn').addEventListener('click', closeModal);
    document.getElementById('employeeModal').addEventListener('click', (e) => {
        if (e.target.id === 'employeeModal') closeModal();
    });
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && !document.getElementById('employeeModal').hidden) {
            closeModal();
        }
    });
});

/* --------------------- Helpers --------------------------------------------- */

// Check the signed-in user's role before loading any employee data
async function canUseDirectory() {
    try {
        const user = await AuthAPI.currentUser();
        const roles = [].concat(user?.role ?? user?.roles ?? []);
        return roles.some(r => DIRECTORY_ROLES.includes(String(r).replace(/^ROLE_/, '')));
    } catch (err) {
        return false;
    }
}

// Replace the directory with a message, matching the Manager Approvals pattern
function showAccessDenied() {
    document.querySelector('main .container').innerHTML = `
        <h1>EMPLOYEE DIRECTORY</h1>
        <p>Only authorized administrators and managers can view the employee directory.</p>
    `;
}

// Prevent names or other text from being read as HTML
function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function getStatus(employee) {
    return employee.employmentStatus || 'ACTIVE';
}

function isInactive(status) {
    return INACTIVE_STATUSES.includes(status);
}

function formatStatus(status) {
    return status.replace(/_/g, ' ');
}

function statusClass(status) {
    if (status === 'ACTIVE') return 'status-approved';
    if (isInactive(status)) return 'status-denied';
    return 'status-pending';
}

function fullName(employee) {
    return `${employee.firstName || ''} ${employee.lastName || ''}`.trim();
}

function getDepartments() {
    return [...new Set(allEmployees.map(e => e.department).filter(Boolean))].sort();
}

/* ------------------------ Directory table  ------------------------------------------ */

// Fetch all employees from the Spring Boot backend / MySQL database
async function loadEmployees() {
    const tbody = document.getElementById('employeeTableBody');
    tbody.innerHTML = '<tr><td colspan="6">Loading records from database...</td></tr>';

    try {
        allEmployees = await EmployeeAPI.getAll();
        populateDepartmentDropdown();
        filterTable(); // keeps the current search and filters after a reload
    } catch (err) {
        tbody.innerHTML = '<tr><td colspan="6">Employee records could not be loaded. Refresh the page to try again.</td></tr>';
    }
}

// Populate department dropdown dynamically based on existing data
function populateDepartmentDropdown() {
    const deptSelect = document.getElementById('departmentFilter');
    const currentVal = deptSelect.value;

    deptSelect.innerHTML = '<option value="">All Departments</option>' +
        getDepartments()
            .map(dept => `<option value="${escapeHtml(dept)}">${escapeHtml(dept)}</option>`)
            .join('');

    deptSelect.value = currentVal;
}

// Filter employees based on search input, department, and status selection
function filterTable() {
    const nameQuery = document.getElementById('searchInput').value.toLowerCase();
    const deptQuery = document.getElementById('departmentFilter').value;
    const statusQuery = document.getElementById('statusFilter').value;

    const filtered = allEmployees.filter(e => {
        const searchText = `${fullName(e)} ${e.email || ''}`.toLowerCase();
        const status = getStatus(e);

        const matchesName = searchText.includes(nameQuery);
        const matchesDept = !deptQuery || e.department === deptQuery;
        const matchesStatus = !statusQuery ||
            (statusQuery === 'INACTIVE' ? isInactive(status) : status === statusQuery);

        return matchesName && matchesDept && matchesStatus;
    });

    currentList = filtered;
    renderTable(filtered);
}

// Render the employee rows into the HTML table
function renderTable(list) {
    const tbody = document.getElementById('employeeTableBody');

    if (!list || list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6">No matching employee records found.</td></tr>';
        return;
    }


    tbody.innerHTML = list.map(e => {
        const status = getStatus(e);
        const name = escapeHtml(fullName(e));
        const statusButton = isInactive(status)
            ? `<button type="button" data-action="reactivate" data-id="${e.id}" aria-label="Reactivate ${name}">Reactivate</button>`
            : `<button type="button" data-action="deactivate" data-id="${e.id}" aria-label="Deactivate ${name}">Deactivate</button>`;

        return `
            <tr>
                <td>${e.id}</td>
                <td>${name}</td>
                <td>${escapeHtml(e.department)}</td>
                <td>${escapeHtml(e.jobTitle)}</td>
                <td><span class="${statusClass(status)}">${escapeHtml(formatStatus(status))}</span></td>
                <td class="actions-cell">
                    <button type="button" data-action="view" data-id="${e.id}" aria-label="View ${name}">View</button>
                    ${(hasRole('HR_ADMIN') || hasRole('SYS_ADMIN')) ? `<button type="button" data-action="edit" data-id="${e.id}" aria-label="Edit ${name}">Edit</button>` : ''}
                    ${(hasRole('HR_ADMIN') || hasRole('SYS_ADMIN')) ? statusButton : ''}
                </td>
            </tr>
        `;
    }).join('');
}

// One listener handles every row button
function handleRowAction(event) {
    const button = event.target.closest('button[data-action]');
    if (!button) return;

    const id = Number(button.dataset.id);

    switch (button.dataset.action) {
        case 'view':       openEmployee(id, 'view'); break;
        case 'edit':       openEmployee(id, 'edit'); break;
        case 'deactivate': handleDeactivate(id); break;
        case 'reactivate': handleReactivate(id); break;
    }

}

// Handle employee deactivation action
async function handleDeactivate(id) {
    const employee = allEmployees.find(e => e.id === id);
    const name = employee ? fullName(employee) : 'this employee';

    if (!confirm(`Deactivate ${name}? Their record and history will be kept.`)) return;

    try {
        await EmployeeAPI.deactivate(id);
        await loadEmployees();
    } catch (err) {
        alert(friendlyError(err, 'deactivate'));
    }
}

// Handle employee reactivation action
async function handleReactivate(id) {
    const employee = allEmployees.find(e => e.id === id);
    const name = employee ? fullName(employee) : 'this employee';

    if (!confirm(`Reactivate ${name}?`)) return;

    try {
        await EmployeeAPI.reactivate(id);
        await loadEmployees();
    } catch (err) {
        alert(friendlyError(err, 'reactivate'));
    }
}

// Download the rows currently shown in the directory as a CSV report (REQ-12)
function exportCsv() {
    const headers = ['ID', 'First Name', 'Last Name', 'Email', 'Phone', 'Department', 'Job Title', 'Status'];
    const rows = currentList.map(e => [
        e.id, e.firstName, e.lastName, e.email, e.phone, e.department, e.jobTitle, getStatus(e)
    ]);

    const csv = [headers, ...rows]
        .map(row => row.map(v => `"${String(v ?? '').replace(/"/g, '""')}"`).join(','))
        .join('\r\n');

    const link = document.createElement('a');
    link.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv' }));
    link.download = `employee_report_${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    URL.revokeObjectURL(link.href);
}


/* ----------------------- View / Edit modal ------------------------------------------- */

// Load the latest copy of the record, then open the modal
async function openEmployee(id, mode) {
    try {
        currentEmployee = await EmployeeAPI.getById(id);
    } catch (err) {
        alert(friendlyError(err, 'load'));
        return;
    }

    lastFocusedElement = document.activeElement;

    if (mode === 'edit') {
        renderEditMode();
    } else {
        renderViewMode();
    }

    document.getElementById('employeeModal').hidden = false;
    document.body.classList.add('modal-open');
    document.getElementById('modalCloseBtn').focus();
}

function closeModal() {
    document.getElementById('employeeModal').hidden = true;
    document.body.classList.remove('modal-open');
    currentEmployee = null;

    if (lastFocusedElement) lastFocusedElement.focus();
}

// Read-only view of the full employee record
function renderViewMode(successMessage = '') {
    const e = currentEmployee;
    const status = getStatus(e);

    const fields = [
        ['Employee ID', e.id],
        ['First name', e.firstName],
        ['Last name', e.lastName],
        ['Email', e.email],
        ['Phone', e.phone],
        ['Department', e.department],
        ['Job title', e.jobTitle]
    ];

    document.getElementById('modalTitle').textContent = fullName(e) || 'Employee details';

    document.getElementById('modalBody').innerHTML = `
        ${successMessage ? `<p id="statusMessage" class="success" role="status">${escapeHtml(successMessage)}</p>` : ''}
        <dl class="detail-list">
            ${fields.map(([label, value]) => `
                <dt>${label}</dt>
                <dd>${escapeHtml(value) || '<span class="muted">Not provided</span>'}</dd>
            `).join('')}
            <dt>Status</dt>
            <dd><span class="${statusClass(status)}">${escapeHtml(formatStatus(status))}</span></dd>
        </dl>
    `;

    document.getElementById('modalFooter').innerHTML = `
        <button type="button" class="btn-secondary" id="modalCancelBtn">Close</button>
        ${hasRole('HR_ADMIN') || hasRole('SYS_ADMIN')
        ? '<button type="button" id="modalEditBtn">Edit</button>'
         : ''}
          `;

    document.getElementById('modalCancelBtn').addEventListener('click', closeModal);
    document.getElementById('modalEditBtn')?.addEventListener('click', () => renderEditMode());
}

// Builds one labeled input with a spot for its error message
function inputField(id, label, value, type = 'text', attrs = '') {
    return `
        <div class="form-field">
            <label for="${id}">${label}</label>
            <input type="${type}" id="${id}" value="${escapeHtml(value)}" ${attrs}>
            <span class="field-error" id="${id}Error"></span>
        </div>
    `;
}

// Editable form for the employee record
function renderEditMode() {
    const e = currentEmployee;
    const status = getStatus(e);

    // Keep an unexpected current status (e.g. ONBOARDING) selectable so it isn't changed by accident
    const statusOptions = EDITABLE_STATUSES.includes(status)
        ? EDITABLE_STATUSES
        : [status, ...EDITABLE_STATUSES];

    const statusField = isInactive(status)
        ? `<p class="field-note">Status: ${escapeHtml(formatStatus(status))}. Use Reactivate in the directory to restore this employee.</p>`
        : `
            <div class="form-field">
                <label for="editStatus">Status</label>
                <select id="editStatus">
                    ${statusOptions.map(s => `
                        <option value="${escapeHtml(s)}" ${s === status ? 'selected' : ''}>${escapeHtml(formatStatus(s))}</option>
                    `).join('')}
                </select>
            </div>
        `;

    document.getElementById('modalTitle').textContent = `Edit ${fullName(e) || 'employee'}`;

    document.getElementById('modalBody').innerHTML = `
        <form id="editEmployeeForm" novalidate>
            <div id="statusMessage" class="error" role="alert" hidden></div>

            ${inputField('editId', 'Employee ID', e.id, 'text', 'readonly')}

            <div class="form-row">
                ${inputField('editFirstName', 'First name', e.firstName, 'text', 'required')}
                ${inputField('editLastName', 'Last name', e.lastName, 'text', 'required')}
            </div>

            ${inputField('editEmail', 'Email', e.email, 'email', 'required')}
            ${inputField('editPhone', 'Phone', e.phone, 'tel', 'required')}

            <div class="form-row">
                ${inputField('editDepartment', 'Department', e.department, 'text', 'required list="departmentOptions"')}
                ${inputField('editJobTitle', 'Job title', e.jobTitle, 'text', 'required')}
            </div>

            <datalist id="departmentOptions">
                ${getDepartments().map(d => `<option value="${escapeHtml(d)}"></option>`).join('')}
            </datalist>

            ${statusField}
        </form>
    `;

    document.getElementById('modalFooter').innerHTML = `
        <button type="button" class="btn-secondary" id="modalCancelBtn">Cancel</button>
        <button type="submit" form="editEmployeeForm" id="modalSaveBtn">Save changes</button>
    `;

    document.getElementById('editEmployeeForm').addEventListener('submit', handleSave);
    document.getElementById('modalCancelBtn').addEventListener('click', () => renderViewMode());
    document.getElementById('editFirstName').focus();
}

// Client-side checks so HR gets a clear message before anything is sent
function validateEmployee(values) {
    const errors = {};

    if (!values.firstName) errors.editFirstName = 'Enter a first name.';
    if (!values.lastName) errors.editLastName = 'Enter a last name.';

    if (!values.email) {
        errors.editEmail = 'Enter an email address.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email)) {
        errors.editEmail = 'Enter a valid email address, such as name@company.com.';
    }

    if (!values.phone) {
        errors.editPhone = 'Enter a phone number.';
    } else {
        const digits = values.phone.replace(/\D/g, '');
        if (!/^[0-9()+\-.\s]+$/.test(values.phone) || digits.length < 7 || digits.length > 15) {
            errors.editPhone = 'Enter a valid phone number, such as 555-0101.';
        }
    }

    if (!values.department) errors.editDepartment = 'Enter a department.';
    if (!values.jobTitle) errors.editJobTitle = 'Enter a job title.';

    return errors;
}

function clearErrors() {
    document.querySelectorAll('#editEmployeeForm .field-error').forEach(el => { el.textContent = ''; });
    document.querySelectorAll('#editEmployeeForm .invalid').forEach(el => {
        el.classList.remove('invalid');
        el.removeAttribute('aria-invalid');
    });
    const formError = document.getElementById('statusMessage');
    formError.hidden = true;
    formError.textContent = '';
}

function showErrors(errors) {
    Object.entries(errors).forEach(([fieldId, message]) => {
        const input = document.getElementById(fieldId);
        input.classList.add('invalid');
        input.setAttribute('aria-invalid', 'true');
        document.getElementById(`${fieldId}Error`).textContent = message;
    });

    showFormError('Fix the highlighted fields, then save again.');
    document.getElementById(Object.keys(errors)[0]).focus();
}

function showFormError(message) {
    const formError = document.getElementById('statusMessage');
    formError.textContent = message;
    formError.hidden = false;
}

async function handleSave(event) {
    event.preventDefault();
    clearErrors();

    const value = (id) => document.getElementById(id).value.trim();

    const values = {
        firstName: value('editFirstName'),
        lastName: value('editLastName'),
        email: value('editEmail'),
        phone: value('editPhone'),
        department: value('editDepartment'),
        jobTitle: value('editJobTitle')
    };

    const errors = validateEmployee(values);
    if (Object.keys(errors).length > 0) {
        showErrors(errors);
        return;
    }

    const statusSelect = document.getElementById('editStatus');

    // Sends the full record so no field is cleared by the update
    const payload = {
        ...currentEmployee,
        ...values,
        employmentStatus: statusSelect ? statusSelect.value : getStatus(currentEmployee)
    };

    const saveBtn = document.getElementById('modalSaveBtn');
    saveBtn.disabled = true;
    saveBtn.textContent = 'Saving...';

    try {
        currentEmployee = await EmployeeAPI.update(currentEmployee.id, payload);
        await loadEmployees();
        renderViewMode('Employee record saved.');
    } catch (err) {
        showFormError(friendlyError(err, 'save'));
        saveBtn.disabled = false;
        saveBtn.textContent = 'Save changes';
    }
}

// apiRequest throws the raw response body; turn it into a readable message
function friendlyError(err, action) {
    let status = null;
    const text = err && err.message ? err.message : '';

    try {
        const data = JSON.parse(text);
        status = data.status || null;
    } catch (_) {
        const match = text.match(/status (\d{3})/);
        if (match) status = Number(match[1]);
    }

    if (status === 401) return 'Your session has expired. Sign in again to continue.';
    if (status === 403) return 'Only HR administrators can make changes to employee records.';
    if (status === 404) return 'This employee record could not be found. Refresh the directory and try again.';
    if (status === 400) return 'Some information was not accepted. Check the email and other fields, then save again.';

    const messages = {
        load: 'This employee record could not be loaded. Refresh the page and try again.',
        save: 'Changes were not saved. Check your connection and try again.',
        deactivate: 'The employee could not be deactivated. Refresh the page and try again.',
        reactivate: 'The employee could not be reactivated. Refresh the page and try again.'
    };

    return messages[action] || 'Something went wrong. Refresh the page and try again.';
}