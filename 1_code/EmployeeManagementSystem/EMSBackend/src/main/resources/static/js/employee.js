// Danielle: Employee Directory Controller logic

let allEmployees = [];

document.addEventListener('DOMContentLoaded', async () => {
    await loadEmployees();

    // Attach event listeners for filtering
    document.getElementById('searchBtn').addEventListener('click', filterTable);
    document.getElementById('searchInput').addEventListener('keyup', (e) => {
        if (e.key === 'Enter') filterTable();
    });
    document.getElementById('departmentFilter').addEventListener('change', filterTable);
    document.getElementById('statusFilter').addEventListener('change', filterTable);
});

// Fetch all employees from the Spring Boot backend / MySQL database
async function loadEmployees() {
    const tbody = document.getElementById('employeeTableBody');
    tbody.innerHTML = '<tr><td colspan="6">Loading records from database...</td></tr>';

    try {
        allEmployees = await EmployeeAPI.getAll();
        populateDepartmentDropdown(allEmployees);
        renderTable(allEmployees);
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6">Error loading records: ${err.message}</td></tr>`;
    }
}

// Populate department dropdown dynamically based on existing data
function populateDepartmentDropdown(employees) {
    const deptSelect = document.getElementById('departmentFilter');
    const currentVal = deptSelect.value;

    // Extract unique departments
    const departments = [...new Set(employees.map(e => e.department).filter(Boolean))].sort();

    deptSelect.innerHTML = '<option value="">All Departments</option>' +
        departments.map(dept => `<option value="${dept}">${dept}</option>`).join('');

    deptSelect.value = currentVal;
}

// Filter employees based on search input, department, and status selection
function filterTable() {
    const nameQuery = document.getElementById('searchInput').value.toLowerCase();
    const deptQuery = document.getElementById('departmentFilter').value;
    const statusQuery = document.getElementById('statusFilter').value;

    const filtered = allEmployees.keyBy ? allEmployees : allEmployees.filter(e => {
        const fullName = `${e.firstName || ''} ${e.lastName || ''} ${e.email || ''}`.toLowerCase();
        const matchesName = fullName.includes(nameQuery);
        const matchesDept = !deptQuery || e.department === deptQuery;
        const matchesStatus = !statusQuery || (e.employmentStatus || 'ACTIVE') === statusQuery;
        return matchesName && matchesDept && matchesStatus;
    });

    renderTable(filtered);
}

// Render the employee rows into the HTML table
function renderTable(list) {
    const tbody = document.getElementById('employeeTableBody');

    if (!list || list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6">No matching employee records found.</td></tr>';
        return;
    }

    tbody.innerHTML = list.map(e => `
        <tr>
            <td>${e.id}</td>
            <td>${e.firstName || ''} ${e.lastName || ''}</td>
            <td>${e.email || ''}</td>
            <td>${e.department || ''}</td>
            <td><span class="${(e.employmentStatus || 'ACTIVE') === 'ACTIVE' ? 'status-approved' : 'status-denied'}">${e.employmentStatus || 'ACTIVE'}</span></td>
            <td>
                <button onclick="handleDeactivate(${e.id})">Deactivate</button>
            </td>
        </tr>
    `).join('');
}

// Handle employee deactivation action
async function handleDeactivate(id) {
    if (!confirm('Are you sure you want to deactivate this employee?')) return;
    try {
        await EmployeeAPI.deactivate(id);
        await loadEmployees(); // Reload table data from backend
    } catch (err) {
        alert(err.message);
    }
}