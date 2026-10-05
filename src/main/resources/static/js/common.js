const money = value => '$' + Number(value || 0).toFixed(2);
const monthKey = date => date.slice(0, 7);
const currentMonth = () => {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
};
function showMessage(text, success = false) {
    const message = document.getElementById('page-message');
    message.textContent = text;
    message.className = `alert alert-${success ? 'success' : 'danger'}`;
    message.hidden = false;
}
async function api(url, options = {}) {
    const headers = new Headers(options.headers);
    const token = sessionStorage.getItem('token');
    if (token && !headers.has('Authorization') && new URL(url, window.location.origin).origin === window.location.origin) {
        headers.set('Authorization', `Bearer ${token}`);
    }
    const response = await fetch(url, { credentials: 'same-origin', ...options, headers });
    // Logout redirects to the backend login page; keep navigation on the frontend.
    if (response.type === 'opaqueredirect' && options.redirect === 'manual'
            && options.method === 'POST' && new URL(url, window.location.origin).pathname === '/logout') {
        return null;
    }
    if (response.status === 401 || response.status === 403) {
        sessionStorage.removeItem('token');
        window.location.assign('/login');
        throw new Error('Please log in again.');
    }
    if (!response.ok) {
        const text = await response.text();
        let message = 'Request failed. Please try again.';
        const contentType = response.headers.get('content-type') || '';
        if (contentType.includes('application/json')) {
            try {
                const detail = JSON.parse(text);
                message = detail.error || detail.message || message;
            } catch { /* Use the fallback message for an invalid JSON response. */ }
        } else if (contentType.includes('text/plain') && text) {
            message = text;
        }
        throw new Error(message);
    }
    if (response.status === 204) return null;
    return response.headers.get('content-type')?.includes('application/json')
        ? response.json() : response.text();
}
function jsonRequest(method, body) {
    return { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) };
}
function cell(row, value) {
    const td = document.createElement('td');
    td.textContent = value ?? '';
    row.append(td);
    return td;
}
function emptyRecords(columns, text) {
    const row = document.createElement('tr');
    cell(row, text).colSpan = columns;
    document.getElementById('records').replaceChildren(row);
}
async function submitTask(form, task) {
    const button = form.querySelector('button[type="submit"]');
    button.disabled = true;
    try { await task(); } catch (error) { showMessage(error.message); }
    finally { button.disabled = false; }
}
document.getElementById('logout-button')?.addEventListener('click', async () => {
    try {
        await api('/logout', { method: 'POST', redirect: 'manual' });
        sessionStorage.removeItem('token');
        window.location.assign('/login?logout');
    } catch (error) { showMessage(error.message); }
});
