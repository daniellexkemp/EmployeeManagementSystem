//HR Time-Off Reporting

let allTimeOffRequests = [];
let filteredTimeOffRequests = [];

// Initialize the report page.
document.addEventListener('DOMContentLoaded', async () => {
    const authenticated = await requireAuth();

    if (!authenticated) {
        return;
    }

    // Allow all authenticated users to log out.
    document.getElementById('logoutBtn')
        .addEventListener('click', logout);

    // Only HR administrators may view the report.
    if (!hasRole('HR_ADMIN')) {
        document.getElementById('reportMessage').textContent =
            'Access denied. This report is available to HR administrators only.';

        document.querySelector('.filters').hidden = true;
        document.querySelector('table').hidden = true;
        return;
    }

    document.getElementById('filterBtn')
        .addEventListener('click', applyFilters);

    document.getElementById('resetBtn')
        .addEventListener('click', resetFilters);

    document.getElementById('exportBtn')
        .addEventListener('click', exportCsv);

    document.getElementById('searchInput')
        .addEventListener('input', applyFilters);

    // Automatically filter when a dropdown selection changes.
    document.getElementById('leaveTypeFilter')
        .addEventListener('change', applyFilters);

    document.getElementById('statusFilter')
        .addEventListener('change', applyFilters);

    await loadTimeOffReport();
});

// Retrieve all time-off requests from the backend.
async function loadTimeOffReport() {
    const message = document.getElementById('reportMessage');

    message.textContent = 'Loading time-off report...';

    try {
        allTimeOffRequests = await apiRequest('/timeoff/report');

        populateLeaveTypeFilter();
        applyFilters();

    } catch (error) {
        console.error('Unable to load time-off report:', error);

        allTimeOffRequests = [];
        filteredTimeOffRequests = [];

        document.getElementById('reportTableBody')
            .replaceChildren();

        message.textContent =
            'Unable to load the time-off report. Please try again.';
    }
}

// Populate leave type options using actual database records.
function populateLeaveTypeFilter() {
    const select = document.getElementById('leaveTypeFilter');

    const leaveTypes = [...new Set(
        allTimeOffRequests
            .map(request => request.leaveType)
            .filter(Boolean)
    )].sort();

    select.innerHTML = '<option value="">All Leave Types</option>';

    leaveTypes.forEach(type => {
        const option = document.createElement('option');
        option.value = type;
        option.textContent = type;
        select.appendChild(option);
    });
}

// Apply employee name, leave type, and status filters.
function applyFilters() {
    const search = document.getElementById('searchInput')
        .value.trim().toLowerCase();

    const leaveType = document.getElementById('leaveTypeFilter').value;
    const status = document.getElementById('statusFilter').value;

    filteredTimeOffRequests = allTimeOffRequests.filter(request => {
        const employeeName = (request.employeeName || '')
            .toLowerCase();

        const matchesName = employeeName.includes(search);
        const matchesLeaveType =
            !leaveType || request.leaveType === leaveType;
        const matchesStatus =
            !status || request.status === status;

        return matchesName && matchesLeaveType && matchesStatus;
    });

    renderReportTable();
}

// Reset filters and display all requests.
function resetFilters() {
    document.getElementById('searchInput').value = '';
    document.getElementById('leaveTypeFilter').value = '';
    document.getElementById('statusFilter').value = '';

    applyFilters();
}

// Display time-off requests in the report table.
function renderReportTable() {
    const tbody = document.getElementById('reportTableBody');
    const message = document.getElementById('reportMessage');

    tbody.replaceChildren();

    if (filteredTimeOffRequests.length === 0) {
        message.textContent = 'No matching time-off requests found.';
        return;
    }

    message.textContent =
        `Showing ${filteredTimeOffRequests.length} time-off request(s).`;

    filteredTimeOffRequests.forEach(request => {
        const row = document.createElement('tr');

        const values = [
            request.id,
            request.employeeName,
            request.leaveType,
            request.startDate,
            request.endDate,
            request.daysRequested,
            request.status,
            request.submittedAt
                ? request.submittedAt.replace('T', ' ')
                : ''
        ];

        values.forEach(value => {
            const cell = document.createElement('td');
            cell.textContent = value ?? '';
            row.appendChild(cell);
        });

        tbody.appendChild(row);
    });
}

// Export currently displayed requests to a CSV file.
function exportCsv() {
    if (filteredTimeOffRequests.length === 0) {
        alert('There are no time-off requests to export.');
        return;
    }

    const headers = [
        'Request ID',
        'Employee',
        'Leave Type',
        'Start Date',
        'End Date',
        'Days Requested',
        'Status',
        'Submitted'
    ];

    const rows = filteredTimeOffRequests.map(request => [
        request.id,
        request.employeeName,
        request.leaveType,
        request.startDate,
        request.endDate,
        request.daysRequested,
        request.status,
        request.submittedAt
            ? request.submittedAt.replace('T', ' ')
            : ''
    ]);

    // Escape CSV values, including spreadsheet formula prefixes.
    const escapeCsv = value => {
        let text = String(value ?? '');

        if (/^\s*[=+\-@]/.test(text)) {
            text = "'" + text;
        }

        return '"' + text.replace(/"/g, '""') + '"';
    };

    const csv = [headers, ...rows]
        .map(row => row.map(escapeCsv).join(','))
        .join('\r\n');

    const blob = new Blob([csv], {
        type: 'text/csv;charset=utf-8;'
    });

    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');

    link.href = url;
    link.download = 'timeoff_report.csv';

    document.body.appendChild(link);
    link.click();
    link.remove();

    URL.revokeObjectURL(url);
}
