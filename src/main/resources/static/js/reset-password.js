const resetForm = document.getElementById('reset-form');
const resetToken = new URLSearchParams(window.location.search).get('token');
// Remove the secret from the address bar and history once it is read.
window.history.replaceState(null, '', window.location.pathname);
if (!resetToken) {
    showMessage('Open the reset link from your email to continue.');
    resetForm.querySelector('button').disabled = true;
}
resetForm.addEventListener('submit', event => {
    event.preventDefault();
    if (!resetToken) return;
    submitTask(resetForm, async () => {
        const result = await api('/api/auth/reset-password', jsonRequest('POST', {
            email: resetForm.elements.email.value.trim(),
            newPassword: resetForm.elements.newPassword.value,
            token: resetToken
        }));
        sessionStorage.removeItem('token');
        resetForm.hidden = true;
        showMessage(result + ' You can now log in with your new password.', true);
    });
});
