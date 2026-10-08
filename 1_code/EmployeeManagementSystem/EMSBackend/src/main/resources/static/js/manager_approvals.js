// Stephen: Manager review and decision logic (UC-4)
// manager_approvals.js - Manager Approvals screen.



var MOCK_REQUESTS = [
  { id: 1, employeeId: 10, employeeName: 'Alice Johnson', leaveType: 'VACATION',
    startDate: '2026-10-05', endDate: '2026-10-09', daysRequested: 5,
    reason: 'Family vacation', status: 'PENDING',
    submittedAt: '2026-09-18T09:00:00', decidedBy: null, decidedAt: null, decisionNote: null },
  { id: 2, employeeId: 11, employeeName: 'Bob Smith', leaveType: 'SICK',
    startDate: '2026-09-24', endDate: '2026-09-25', daysRequested: 2,
    reason: 'Flu', status: 'PENDING',
    submittedAt: '2026-09-19T14:30:00', decidedBy: null, decidedAt: null, decisionNote: null },
  { id: 3, employeeId: 12, employeeName: 'Carol White', leaveType: 'PERSONAL',
    startDate: '2026-10-01', endDate: '2026-10-01', daysRequested: 1,
    reason: 'Medical appointment', status: 'PENDING',
    submittedAt: '2026-09-20T10:15:00', decidedBy: null, decidedAt: null, decisionNote: null },
  { id: 4, employeeId: 13, employeeName: 'Dave Brown', leaveType: 'UNPAID',
    startDate: '2026-09-10', endDate: '2026-09-12', daysRequested: 3,
    reason: 'Moving house', status: 'APPROVED',
    submittedAt: '2026-09-01T11:00:00', decidedBy: 1, decidedAt: '2026-09-02T09:00:00',
    decisionNote: 'Approved, coverage arranged.' },
  { id: 5, employeeId: 14, employeeName: 'Eve Davis', leaveType: 'VACATION',
    startDate: '2026-09-15', endDate: '2026-09-19', daysRequested: 5,
    reason: 'Holiday', status: 'DENIED',
    submittedAt: '2026-09-02T16:00:00', decidedBy: 1, decidedAt: '2026-09-03T08:30:00',
    decisionNote: 'Two others already off that week.' }
];

var selectedRequestId = null;



function getAllRequests() {
  if (USE_MOCK) {
    return Promise.resolve(MOCK_REQUESTS);
  }
  return apiRequest('/approvals');
}

function getRequest(id) {
  if (USE_MOCK) {
    return Promise.resolve(findMockRequest(id));
  }
  return apiRequest('/approvals/' + id);
}

function approveRequest(id, note) {
  return decide(id, note, 'APPROVED', '/approve');
}

function denyRequest(id, note) {
  return decide(id, note, 'DENIED', '/deny');
}

function decide(id, note, newStatus, path) {
  if (USE_MOCK) {
    var request = findMockRequest(id);
    if (!request) {
      return Promise.resolve(null);
    }
    request.status = newStatus;
    request.decidedBy = CURRENT_MANAGER_ID;
    request.decidedAt = new Date().toISOString().slice(0, 19);
    request.decisionNote = note;
    return Promise.resolve(request);
  }
  // The back end records the audit-log entry for this decision.
  return apiRequest('/approvals/' + id + path, 'PUT',
    { decidedBy: CURRENT_MANAGER_ID, decisionNote: note });
}

function findMockRequest(id) {
  for (var i = 0; i < MOCK_REQUESTS.length; i++) {
    if (MOCK_REQUESTS[i].id === id) {
      return MOCK_REQUESTS[i];
    }
  }
  return null;
}



function showMessage(text, isError) {
  var box = document.getElementById('statusMessage');
  box.textContent = text;
  box.className = isError ? 'error' : 'success';
  box.style.display = 'block';
}

function loadRequests() {
  getAllRequests().then(function (requests) {
    renderTable(requests);
  }).catch(function (error) {
    showMessage('Could not load requests: ' + error.message, true);
  });
}

function renderTable(requests) {
  var body = document.getElementById('requestsBody');
  var filter = document.getElementById('statusFilter').value;
  body.innerHTML = '';

  for (var i = 0; i < requests.length; i++) {
    var request = requests[i];
    if (filter !== 'ALL' && request.status !== filter) {
      continue;
    }
    body.appendChild(buildRow(request));
  }

  var hasRows = body.children.length > 0;
  document.getElementById('requestsTable').style.display = hasRows ? '' : 'none';
  document.getElementById('emptyMessage').style.display = hasRows ? 'none' : 'block';
}

function buildRow(request) {
  var row = document.createElement('tr');
  addCell(row, request.id);
  addCell(row, request.employeeName);
  addCell(row, request.leaveType);
  addCell(row, request.startDate);
  addCell(row, request.endDate);
  addCell(row, request.daysRequested);

  var statusCell = addCell(row, request.status);
  statusCell.className = 'status-' + request.status.toLowerCase();

  var actionCell = document.createElement('td');
  var viewButton = document.createElement('button');
  viewButton.textContent = 'View';
  viewButton.setAttribute('data-id', request.id);
  actionCell.appendChild(viewButton);
  row.appendChild(actionCell);

  return row;
}

function addCell(row, text) {
  var cell = document.createElement('td');
  cell.textContent = text;
  row.appendChild(cell);
  return cell;
}



function showDetail(id) {
  getRequest(id).then(function (request) {
    if (!request) {
      showMessage('Request ' + id + ' was not found.', true);
      return;
    }
    selectedRequestId = request.id;
    document.getElementById('detailId').textContent = request.id;
    document.getElementById('detailEmployee').textContent = request.employeeName;
    document.getElementById('detailLeaveType').textContent = request.leaveType;
    document.getElementById('detailDates').textContent = request.startDate + ' to ' + request.endDate;
    document.getElementById('detailDays').textContent = request.daysRequested;
    document.getElementById('detailReason').textContent = request.reason;
    document.getElementById('detailStatus').textContent = request.status;
    document.getElementById('decisionNote').value = '';

  
    var stillPending = request.status === 'PENDING';
    document.getElementById('approveBtn').disabled = !stillPending;
    document.getElementById('denyBtn').disabled = !stillPending;

    document.getElementById('detailPanel').style.display = 'block';
  }).catch(function (error) {
    showMessage('Could not load request: ' + error.message, true);
  });
}

function closeDetail() {
  document.getElementById('detailPanel').style.display = 'none';
  selectedRequestId = null;
}

function handleDecision(decideFunction, verb) {
  if (selectedRequestId === null) {
    return;
  }
  var id = selectedRequestId;
  var note = document.getElementById('decisionNote').value;

  decideFunction(id, note).then(function (request) {
    if (!request) {
      showMessage('Request ' + id + ' was not found.', true);
      return;
    }
    showMessage('Request ' + id + ' ' + verb + '.', false);
    closeDetail();
    loadRequests();
  }).catch(function (error) {
    showMessage('Could not ' + verb.slice(0, -1) + ' request ' + id + ': ' + error.message, true);
  });
}



function init() {
  document.getElementById('currentUser').textContent = CURRENT_MANAGER_NAME;

  document.getElementById('refreshBtn').addEventListener('click', loadRequests);
  document.getElementById('statusFilter').addEventListener('change', loadRequests);
  document.getElementById('closeDetailBtn').addEventListener('click', closeDetail);

  document.getElementById('approveBtn').addEventListener('click', function () {
    handleDecision(approveRequest, 'approved');
  });
  document.getElementById('denyBtn').addEventListener('click', function () {
    handleDecision(denyRequest, 'denied');
  });

  
  document.getElementById('requestsBody').addEventListener('click', function (event) {
    var id = event.target.getAttribute('data-id');
    if (id) {
      showDetail(Number(id));
    }
  });

  loadRequests();
}

window.addEventListener('DOMContentLoaded', init);
