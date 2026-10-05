const budgetForm = document.getElementById('budget-form');
budgetForm.elements.period.value = currentMonth();
async function loadBudgets() {
    const budgets = await api('/api/budgets/me');
    const rows = budgets.map(budget => {
        const period = `${budget.year}-${String(budget.month).padStart(2, '0')}`;
        const spent = Number(budget.spentAmount);
        const limit = Number(budget.limitAmount);
        const remaining = Number(budget.remainingAmount);
        const percentage = Number(budget.percentage);
        const row = document.createElement('tr');
        for (const value of [`${budget.category} / ${period}`, money(limit), money(spent), money(remaining)]) cell(row, value);
        const progress = cell(row, '');
        const track = document.createElement('div'); track.className = 'progress';
        const bar = document.createElement('div'); bar.className = `progress-bar bg-${percentage >= 100 ? 'danger' : percentage >= 75 ? 'warning' : 'success'}`;
        bar.style.width = `${Math.min(100, Math.max(0, percentage))}%`;
        bar.setAttribute('role', 'progressbar'); bar.setAttribute('aria-valuenow', String(Math.round(percentage)));
        bar.setAttribute('aria-valuemin', '0'); bar.setAttribute('aria-valuemax', '100');
        track.append(bar);
        const label = document.createElement('span');
        label.className = 'small';
        label.textContent = `${percentage.toFixed(2)}% used`;
        progress.append(track, label);
        cell(row, percentage >= 100 ? 'Limit reached / exceeded' : percentage >= 75 ? 'Nearing limit' : 'On track');
        return row;
    });
    document.getElementById('records').replaceChildren(...rows);
    if (!rows.length) emptyRecords(6, 'No budgets yet. Set one above.');
}
budgetForm.addEventListener('submit', event => {
    event.preventDefault();
    submitTask(budgetForm, async () => {
        const period = budgetForm.elements.period.value;
        if (!/^\d{4}-(0[1-9]|1[0-2])$/.test(period)) {
            throw new Error('Select a valid budget month and year.');
        }
        const [year, month] = period.split('-').map(Number);
        if (year < 1 || year > 9999) {
            throw new Error('Select a year between 1 and 9999.');
        }
        await api('/api/budgets', jsonRequest('POST', { category: budgetForm.elements.category.value, amount: Number(budgetForm.elements.amount.value), month, year }));
        await loadBudgets(); showMessage('Budget saved.', true);
    });
});
loadBudgets().catch(error => { emptyRecords(6, 'Unable to load budgets.'); showMessage(error.message); });

window.addEventListener('focus', () => {
    loadBudgets().catch(error => showMessage(error.message));
});
