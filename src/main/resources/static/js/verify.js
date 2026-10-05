(async () => {
    const code = new URLSearchParams(window.location.search).get('code');
    window.history.replaceState(null, '', window.location.pathname);
    try {
        if (!code) throw new Error('Open the verification link from your email to continue.');
        const result = await api('/api/auth/verify?code=' + encodeURIComponent(code));
        showMessage(result, true);
    } catch (error) {
        showMessage(error.message);
    } finally {
        document.getElementById('verification-progress').hidden = true;
    }
})();
