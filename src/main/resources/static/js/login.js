const loginForm = document.getElementById('login-form');
const loginMessage = document.getElementById('login-message');

loginForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    const submitButton = loginForm.querySelector('button[type="submit"]');
    submitButton.disabled = true;
    loginMessage.hidden = true;

    try {
        const response = await fetch(loginForm.action, {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: loginForm.elements.email.value.trim(),
                password: loginForm.elements.password.value
            })
        });

        if (!response.ok) {
            const detail = await response.text();
            throw new Error(response.headers.get('content-type')?.includes('text/plain') && detail
                ? detail : 'Login failed. Please try again.');
        }

        const auth = await response.json();
        if (!auth.token) {
            throw new Error('Login failed: the server did not return an authentication token.');
        }
        sessionStorage.setItem('token', auth.token);
        loginMessage.className = 'alert alert-success text-center py-2';
        loginMessage.textContent = 'Logged in successfully.';
        loginForm.reset();
        window.location.assign("/dashboard");
    } catch (error) {
        loginMessage.className = 'alert alert-danger text-center py-2';
        loginMessage.textContent = error instanceof TypeError
            ? 'Unable to reach the server. Please try again.' : error.message;
    } finally {
        loginMessage.hidden = false;
        submitButton.disabled = false;
    }
});
