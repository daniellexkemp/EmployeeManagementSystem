
// HR Time-Off Reporting

let allTimeOffRequests = [];
let filteredTimeOffRequests = [];
let reportLoaded = false;

// Initialize the report page.
document.addEventListener('DOMContentLoaded', async () => {
    const authenticated = await requireAuth();

    if (!authenticated) {
        return;
    }

    const reportContent = document.getElementById('reportContent');
    const reportLink = document.getElementById('timeOffReportLink');
    const message = document.getElementById('reportMessage');

    // Only HR administrators may view the report.
    if (!hasRole('HR_ADMIN')) {
        message.textContent =
            'Access denied. This report is available to HR administrators only.';

        reportContent.hidden = true;
        reportLink.hidden = true;
        return;
    }

    reportContent.hidden = false;
    reportLink.hidden = false;

    document.getElementById('filterBtn')
        .addEventListener('click', applyFilters);

    document.getElementById('resetBtn')
        .addEventListener('click', resetFilters);

    document.getElementById('exportBtn')
        .addEventListener('click', exportCsv);

    // Automatically filter when the employee name changes.
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
    const resultsMessage = document.getElementById('resultsMessage');
    const exportMessage = document.getElementById('exportMessage');

    reportLoaded = false;

    message.textContent = 'Loading time-off report...';
    resultsMessage.textContent = '';
    exportMessage.textContent = '';

    document.getElementById('exportBtn').disabled = true;

    try {
        allTimeOffRequests = await apiRequest('/timeoff/report');

        populateLeaveTypeFilter();

        reportLoaded = true;
        message.textContent = '';

        applyFilters();

    } catch (error) {
        console.error('Unable to load time-off report:', error);

        allTimeOffRequests = [];
        filteredTimeOffRequests = [];

        document.getElementById('reportTableBody')
            .replaceChildren();

        message.textContent =
            'Unable to load the time-off report. Please refresh the page to try again.';

        exportMessage.textContent =
            'Export is unavailable until the report loads successfully.';

        document.getElementById('exportBtn').disabled = true;
    }
}

// Convert stored values into readable display labels.
function formatDisplayLabel(value) {
    if (!value) {
        return '';
    }

    return String(value)
        .toLowerCase()
        .replace(/_/g, ' ')
        .replace(/\b\w/g, character => character.toUpperCase());
}

// Format timestamps for display without fractional seconds.
function formatDisplayTimestamp(value) {
    if (!value) {
        return '';
    }

    return String(value)
        .replace('T', ' ')
        .replace(/\.\d+/, '');
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
        option.textContent = formatDisplayLabel(type);

        select.appendChild(option);
    });
}

// Apply employee name, leave type, and status filters.
function applyFilters() {
    if (!reportLoaded) {
        return;
    }

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
    const message = document.getElementById('resultsMessage');
    const exportButton = document.getElementById('exportBtn');
    const exportMessage = document.getElementById('exportMessage');

    tbody.replaceChildren();

    const count = filteredTimeOffRequests.length;

    exportButton.disabled = count === 0;

    if (count === 0) {
        message.textContent =
            'No matching time-off requests found. Adjust your filters or select Reset.';

        exportMessage.textContent =
            'Export CSV is unavailable because no records match your filters.';

        return;
    }

    const recordLabel = count === 1
        ? 'time-off request'
        : 'time-off requests';

    message.textContent =
        `Showing ${count} of ${allTimeOffRequests.length} ${allTimeOffRequests.length === 1
            ? 'time-off request'
            : 'time-off requests'}.`;

    exportMessage.textContent =
        `${count} matching ${recordLabel} available to export.`;

    filteredTimeOffRequests.forEach(request => {
        const row = document.createElement('tr');

        const values = [
            request.id,
            request.employeeName,
            formatDisplayLabel(request.leaveType),
            request.startDate,
            request.endDate,
            request.daysRequested,
            formatDisplayLabel(request.status),
            formatDisplayTimestamp(request.submittedAt)
        ];

        values.forEach((value, index) => {
            const cell = document.createElement('td');

            cell.textContent = value ?? '';

            // Reuse existing status styles from the shared stylesheet.
            if (index === 6) {
                const status = String(request.status || '')
                    .toLowerCase();

                if (['pending', 'approved', 'denied'].includes(status)) {
                    cell.classList.add(`status-${status}`);
                }
            }

            row.appendChild(cell);
        });

        tbody.appendChild(row);
    });
}

// Export currently displayed requests to a CSV file.
function exportCsv() {
    const exportMessage = document.getElementById('exportMessage');

    if (!reportLoaded || filteredTimeOffRequests.length === 0) {
        exportMessage.textContent =
            'There are no time-off requests to export.';

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

    // Preserve original values and full timestamp precision in CSV.
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

    const count = filteredTimeOffRequests.length;

    exportMessage.textContent =
        `CSV download initiated for ${count} matching ${count === 1
            ? 'time-off request'
            : 'time-off requests'}.`;
}
