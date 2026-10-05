const forgotForm = document.getElementById('forgot-form');
forgotForm.addEventListener('submit', event => {
    event.preventDefault();
    submitTask(forgotForm, async () => {
        const body = new URLSearchParams({ email: forgotForm.elements.email.value.trim() });
        const result = await api(forgotForm.action, { method: 'POST', body });
        showMessage(result, true);
    });
});
