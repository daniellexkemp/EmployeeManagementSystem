document.addEventListener('DOMContentLoaded', () => {
    loadTable();
    document.getElementById('searchBtn').addEventListener('click', () => {
        loadTable(document.getElementById('searchInput').value);
    });
});

async function loadTable(query = '') {
    const tbody = document.getElementById('employeeTableBody');
    tbody.innerHTML = '<tr><td colspan="6">Loading...</td></tr>';
    try {
        const list = await fetchEmployees(query);
        tbody.innerHTML = list.map(e => `
            <tr>
                <td>${e.id}</td>
                <td>${e.firstName || ''} ${e.lastName || ''}</td>
                <td>${e.email || ''}</td>
                <td>${e.department || ''}</td>
                <td>${e.employmentStatus || 'ACTIVE'}</td>
                <td>
                    <button onclick="handleDeactivate(${e.id})">Deactivate</button>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6">Error loading records: ${err.message}</td></tr>`;
    }
}

async function handleDeactivate(id) {
    if (!confirm('Are you sure you want to deactivate this employee?')) return;
    try {
        await deactivateEmployeeRecord(id);
        loadTable(document.getElementById('searchInput').value);
    } catch (err) {
        alert(err.message);
    }
}