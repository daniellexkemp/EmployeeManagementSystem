// Danielle: Role permission logic (UC-7)

document.addEventListener('DOMContentLoaded', async () => {
    const select = document.getElementById('employeeSelect');
    try {
        const list = await fetchEmployees();
        select.innerHTML = list.map(e => `<option value="${e.id}">${e.firstName} ${e.lastName} (${e.email})</option>`).join('');
    } catch (err) {
        select.innerHTML = '<option>Error loading employees</option>';
    }

    document.getElementById('assignBtn').addEventListener('click', async () => {
        const id = select.value;
        const status = document.getElementById('roleSelect').value;
        try {
            await fetch(`http://localhost:8080/api/employees/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ employmentStatus: status })
            });
            document.getElementById('accessFeedback').innerText = 'Access status updated!';
        } catch (err) {
            document.getElementById('accessFeedback').innerText = 'Update failed.';
        }
    });
});