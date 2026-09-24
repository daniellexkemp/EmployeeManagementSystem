// Nathaniel: Leave request submission logic (UC-3)

document.addEventListener("DOMContentLoaded", function () {

    const submitButton = document.getElementById("submitRequestBtn");

    submitButton.addEventListener("click", function () {

        const leaveType = document.getElementById("leaveType").value;
        const startDate = document.getElementById("startDate").value;
        const endDate = document.getElementById("endDate").value;

        if (!leaveType || !startDate || !endDate) {
            showMessage("Please complete all required fields.", "error");
            return;
        }

        if (endDate < startDate) {
            showMessage("End date cannot be before start date.", "error");
            return;
        }

        showMessage("Time-off request is ready to be submitted.", "success");
    });

});

function showMessage(message, type) {
    const statusMessage = document.getElementById("statusMessage");

    statusMessage.textContent = message;
    statusMessage.className = type;
    statusMessage.style.display = "block";
}
