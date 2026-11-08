// Danielle: New hire setup logic (UC-5)

document.addEventListener('DOMContentLoaded', async () => {
    if (!(await requireAuth())) return;
    displayCurrentUser();
    
    try {
        const employees = await apiRequest('/employees/supervisors');
        const supervisorDropdown = document.getElementById('supervisorId');

        employees.forEach(employee => {
            const option = document.createElement('option');
            option.value = employee.id;
            option.textContent = `${employee.firstName} ${employee.lastName}`;
            supervisorDropdown.appendChild(option);
        });
    } catch (err) {
        console.error('Unable to load supervisors:', err);
    }

});

document.getElementById('onboardingForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
        firstName: document.getElementById('firstName').value,
        lastName: document.getElementById('lastName').value,
        email: document.getElementById('email').value,
        phone: document.getElementById('phone').value,
        department: document.getElementById('department').value,
        jobTitle: document.getElementById('jobTitle').value,
        
        employmentStatus: 'ACTIVE',
        supervisorId: document.getElementById('supervisorId').value || null

    };
    try {
        await apiRequest(
        `/employees${payload.supervisorId ? '?supervisorId=' + encodeURIComponent(payload.supervisorId) : ''}`,
        'POST',
        payload
);
        document.getElementById('feedback').innerText = 'Employee onboarded successfully!';
        document.getElementById('onboardingForm').reset();
    } catch (err) {
        document.getElementById('feedback').innerText = `Error: ${err.message}`;
    }
});