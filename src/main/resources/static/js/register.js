const registerForm = document.getElementById('register-form');
const registerMessage = document.getElementById('register-message');

registerForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    const submitButton = registerForm.querySelector('button[type="submit"]');
    submitButton.disabled = true;
    registerMessage.hidden = true;

    try {
        const response = await fetch(registerForm.action, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: registerForm.elements.email.value.trim(),
                password: registerForm.elements.password.value
            })
        });

        if (!response.ok) {
            const detail = await response.text();
            throw new Error(response.headers.get('content-type')?.includes('text/plain') && detail
                ? detail : 'Registration failed. Please try again.');
        }

        registerMessage.className = 'alert alert-success text-center py-2';
        registerMessage.textContent = 'Account created successfully.';
        registerForm.reset();
    } catch (error) {
        registerMessage.className = 'alert alert-danger text-center py-2';
        registerMessage.textContent = error instanceof TypeError
            ? 'Unable to reach the server. Please try again.' : error.message;
    } finally {
        registerMessage.hidden = false;
        submitButton.disabled = false;
    }
});
