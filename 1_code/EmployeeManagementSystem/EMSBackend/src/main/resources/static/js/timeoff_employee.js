// Nathaniel: Leave request submission logic (UC-3)

// Nathaniel: Leave request submission and tracking logic (UC-3)

const CURRENT_EMPLOYEE_ID = 1;

document.addEventListener("DOMContentLoaded", function () {

    document.getElementById("currentUser").textContent = "Employee Test";

    document.getElementById("submitRequestBtn")
        .addEventListener("click", submitRequest);

    document.getElementById("refreshBtn")
        .addEventListener("click", loadRequests);

    loadRequests();
});

async function submitRequest() {

    const leaveType = document.getElementById("leaveType").value;
    const startDate = document.getElementById("startDate").value;
    const endDate = document.getElementById("endDate").value;
    const reason = document.getElementById("reason").value.trim();

    if (!leaveType || !startDate || !endDate) {
        showMessage("Please complete all required fields.", "error");
        return;
    }

    if (endDate < startDate) {
        showMessage("End date cannot be before start date.", "error");
        return;
    }

    const request = {
        employeeId: CURRENT_EMPLOYEE_ID,
        leaveType: leaveType,
        startDate: startDate,
        endDate: endDate,
        reason: reason
    };

    try {
        await apiRequest("/timeoff", "POST", request);

        showMessage("Time-off request submitted successfully.", "success");

        document.getElementById("leaveType").value = "";
        document.getElementById("startDate").value = "";
        document.getElementById("endDate").value = "";
        document.getElementById("reason").value = "";

        await loadRequests();

    } catch (error) {
        showMessage(
            "Could not submit time-off request: " + error.message,
            "error"
        );
    }
}

async function loadRequests() {

    const body = document.getElementById("requestsBody");

    try {
        const requests = await apiRequest(
            "/timeoff/employee/" + CURRENT_EMPLOYEE_ID
        );

        body.innerHTML = "";

        if (!requests || requests.length === 0) {
            document.getElementById("requestsTable").style.display = "none";
            document.getElementById("emptyMessage").style.display = "block";
            return;
        }

        document.getElementById("requestsTable").style.display = "";
        document.getElementById("emptyMessage").style.display = "none";

        requests.forEach(function (request) {

            const row = document.createElement("tr");

            addCell(row, request.id);
            addCell(row, request.leaveType);
            addCell(row, request.startDate);
            addCell(row, request.endDate);
            addCell(row, request.daysRequested);
            addCell(row, request.status);

            body.appendChild(row);
        });

    } catch (error) {
        body.innerHTML = "";
        showMessage(
            "Could not load time-off requests: " + error.message,
            "error"
        );
    }
}

function addCell(row, value) {
    const cell = document.createElement("td");
    cell.textContent = value ?? "";
    row.appendChild(cell);
}

function showMessage(message, type) {
    const statusMessage = document.getElementById("statusMessage");

    statusMessage.textContent = message;
    statusMessage.className = type;
    statusMessage.style.display = "block";
}
