
const AUDIT_PAGE_SIZE = 50;
const AUDIT_EXPORT_PAGE_SIZE = 500;

let auditPage = 0;
let auditTotalPages = 0;
let auditTotalElements = 0;

document.addEventListener('DOMContentLoaded', async () => {
    const authenticated = await requireAuth();

    if (!authenticated) {
        return;
    }

    if (!hasRole('HR_ADMIN')) {
        document.getElementById('auditMessage').textContent =
            'Access denied. The audit log is available to HR administrators only.';

        document.querySelector('.filters').hidden = true;
        document.querySelector('table').hidden = true;
        document.getElementById('pagingControls').hidden = true;
        return;
    }

    document.getElementById('filterBtn')
        .addEventListener('click', applyFilters);

    document.getElementById('resetBtn')
        .addEventListener('click', resetFilters);

    document.getElementById('exportBtn')
        .addEventListener('click', exportCsv);

    document.getElementById('actorInput')
        .addEventListener('keydown', event => {
            if (event.key === 'Enter') {
                applyFilters();
            }
        });

    ['actionFilter', 'fromDate', 'toDate'].forEach(id => {
        document.getElementById(id)
            .addEventListener('change', applyFilters);
    });

    document.getElementById('prevPageBtn')
        .addEventListener('click', () => {
            if (auditPage > 0) {
                auditPage--;
                loadAuditLog();
            }
        });

    document.getElementById('nextPageBtn')
        .addEventListener('click', () => {
            if (auditPage + 1 < auditTotalPages) {
                auditPage++;
                loadAuditLog();
            }
        });

    await populateActionFilter();
    await loadAuditLog();
});

async function populateActionFilter() {
    const select = document.getElementById('actionFilter');

    try {
        const actions = await apiRequest('/audit-log/actions');

        actions.forEach(action => {
            const option = document.createElement('option');
            option.value = action;
            option.textContent = formatAction(action);
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Unable to load audit actions:', error);
    }
}

function buildAuditQuery(page, size) {
    const params = new URLSearchParams({ page, size });

    const actor = document.getElementById('actorInput').value.trim();
    const action = document.getElementById('actionFilter').value;
    const from = document.getElementById('fromDate').value;
    const to = document.getElementById('toDate').value;

    if (actor) params.set('actor', actor);
    if (action) params.set('action', action);
    if (from) params.set('from', from);
    if (to) params.set('to', to);

    return params.toString();
}

function validDateRange() {
    const from = document.getElementById('fromDate').value;
    const to = document.getElementById('toDate').value;

    if (from && to && to < from) {
        document.getElementById('auditMessage').textContent =
            'The To date cannot be before the From date.';
        return false;
    }

    return true;
}

async function loadAuditLog() {
    const message = document.getElementById('auditMessage');

    if (!validDateRange()) {
        return;
    }

    message.textContent = 'Loading audit log...';

    try {
        const result = await apiRequest(
            '/audit-log?' + buildAuditQuery(auditPage, AUDIT_PAGE_SIZE)
        );

        auditTotalPages = result.totalPages;
        auditTotalElements = result.totalElements;

        renderAuditTable(result.items);

    } catch (error) {
        console.error('Unable to load audit log:', error);

        auditTotalPages = 0;
        auditTotalElements = 0;

        document.getElementById('auditTableBody').replaceChildren();
        updatePaging();

        message.textContent =
            'Unable to load the audit log. Please try again.';
    }
}

function applyFilters() {
    auditPage = 0;
    loadAuditLog();
}

function resetFilters() {
    document.getElementById('actorInput').value = '';
    document.getElementById('actionFilter').value = '';
    document.getElementById('fromDate').value = '';
    document.getElementById('toDate').value = '';

    applyFilters();
}

function auditRowValues(entry) {
    return [
        formatTimestamp(entry.createdAt),
        entry.actorUsername,
        entry.actorRole,
        formatAction(entry.action),
        entry.target,
        entry.detail
    ];
}

function renderAuditTable(entries) {
    const tbody = document.getElementById('auditTableBody');
    const message = document.getElementById('auditMessage');

    tbody.replaceChildren();
    updatePaging();

    if (!entries || entries.length === 0) {
        message.textContent = 'No matching audit entries found.';
        return;
    }

    const first = auditPage * AUDIT_PAGE_SIZE + 1;
    const last = first + entries.length - 1;

    message.textContent =
        `Showing ${first}-${last} of ${auditTotalElements} audit entr${auditTotalElements === 1 ? 'y' : 'ies'}.`;

    entries.forEach(entry => {
        const row = document.createElement('tr');

        auditRowValues(entry).forEach(value => {
            const cell = document.createElement('td');
            cell.textContent = value ?? '';
            row.appendChild(cell);
        });

        tbody.appendChild(row);
    });
}

function updatePaging() {
    document.getElementById('prevPageBtn').disabled = auditPage === 0;
    document.getElementById('nextPageBtn').disabled =
        auditPage + 1 >= auditTotalPages;

    document.getElementById('pageInfo').textContent =
        auditTotalPages === 0
            ? ''
            : `Page ${auditPage + 1} of ${auditTotalPages}`;
}

function formatAction(action) {
    if (!action) {
        return '';
    }

    return action
        .toLowerCase()
        .split('_')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ');
}

function formatTimestamp(value) {
    if (!value) {
        return '';
    }

    return String(value).replace('T', ' ').split('.')[0];
}

async function exportCsv() {
    if (!validDateRange()) {
        return;
    }

    const exportBtn = document.getElementById('exportBtn');
    exportBtn.disabled = true;

    try {
        const entries = [];
        let page = 0;
        let totalPages = 1;

        while (page < totalPages) {
            const result = await apiRequest(
                '/audit-log?' + buildAuditQuery(page, AUDIT_EXPORT_PAGE_SIZE)
            );

            entries.push(...result.items);
            totalPages = result.totalPages;
            page++;
        }

        if (entries.length === 0) {
            alert('There are no audit entries to export.');
            return;
        }

        const headers = [
            'Date / Time',
            'User',
            'Role',
            'Action',
            'Target',
            'Details'
        ];

        const escapeCsv = value => {
            let text = String(value ?? '');

            if (/^\s*[=+\-@]/.test(text)) {
                text = "'" + text;
            }

            return '"' + text.replace(/"/g, '""') + '"';
        };

        const csv = [headers, ...entries.map(auditRowValues)]
            .map(row => row.map(escapeCsv).join(','))
            .join('\r\n');

        const blob = new Blob([csv], {
            type: 'text/csv;charset=utf-8;'
        });

        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');

        link.href = url;
        link.download = 'audit_log.csv';

        document.body.appendChild(link);
        link.click();
        link.remove();

        URL.revokeObjectURL(url);

    } catch (error) {
        console.error('Unable to export audit log:', error);
        alert('Unable to export the audit log. Please try again.');

    } finally {
        exportBtn.disabled = false;
    }
}
