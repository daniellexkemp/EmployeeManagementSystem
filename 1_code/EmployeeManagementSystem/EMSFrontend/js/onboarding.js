// Danielle: New hire setup logic (UC-5)

document.getElementById('onboardingForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
        firstName: document.getElementById('firstName').value,
        lastName: document.getElementById('lastName').value,
        email: document.getElementById('email').value,
        phone: document.getElementById('phone').value,
        department: document.getElementById('department').value,
        jobTitle: document.getElementById('jobTitle').value,
        employmentStatus: 'ACTIVE'
    };
    try {
        await createEmployeeRecord(payload);
        document.getElementById('feedback').innerText = 'Employee onboarded successfully!';
        document.getElementById('onboardingForm').reset();
    } catch (err) {
        document.getElementById('feedback').innerText = `Error: ${err.message}`;
    }
});