const expenseForm = document.getElementById('expense-form');
const filterForm = document.getElementById('filter-form');
let editingId = null;
let loadVersion = 0;
function prepareExpense(expense = null) {
    expenseForm.reset();
    editingId = expense?.id ?? null;
    document.getElementById('expenseModalLabel').textContent = expense ? 'Edit Transaction' : 'Add Transaction';
    if (expense) {
        for (const name of ['title', 'amount', 'category', 'type', 'date', 'description']) {
            expenseForm.elements[name].value = expense[name] ?? '';
        }
    }
}
async function loadExpenses() {
    const version = ++loadVersion;
    const query = new URLSearchParams();
    for (const name of ['category', 'startDate', 'endDate']) {
        const value = filterForm.elements[name].value;
        if (value) query.set(name, value);
    }
    const expenses = await api('/api/expenses/filter?' + query);
    if (version !== loadVersion) return;
    const rows = expenses.map(expense => {
        const row = document.createElement('tr');
        for (const value of [expense.date, expense.title, expense.category, expense.type, money(expense.amount), expense.description]) cell(row, value);
        const actions = cell(row, '');
        const edit = document.createElement('button');
        edit.textContent = 'Edit'; edit.className = 'btn btn-sm btn-outline-secondary me-1';
        edit.addEventListener('click', () => {
            prepareExpense(expense);
            bootstrap.Modal.getOrCreateInstance(document.getElementById('expenseModal')).show();
        });
        const remove = document.createElement('button');
        remove.textContent = 'Delete'; remove.className = 'btn btn-sm btn-outline-danger';
        remove.addEventListener('click', async () => {
            if (!confirm('Delete this transaction?')) return;
            remove.disabled = true;
            try {
                await api(`/api/expenses/${expense.id}`, { method: 'DELETE' });
                await loadExpenses(); showMessage('Transaction deleted.', true);
            } catch (error) { showMessage(error.message); }
            finally { remove.disabled = false; }
        });
        actions.append(edit, remove);
        return row;
    });
    document.getElementById('records').replaceChildren(...rows);
    if (!rows.length) emptyRecords(7, 'No transactions found. Add one above.');
}
document.getElementById('add-expense').addEventListener('click', () => prepareExpense());
filterForm.addEventListener('submit', event => {
    event.preventDefault();
    if (filterForm.elements.startDate.value && filterForm.elements.endDate.value && filterForm.elements.startDate.value > filterForm.elements.endDate.value) {
        showMessage('Start date must be before the end date.'); return;
    }
    submitTask(filterForm, loadExpenses);
});
expenseForm.addEventListener('submit', event => {
    event.preventDefault();
    submitTask(expenseForm, async () => {
        const body = Object.fromEntries(new FormData(expenseForm));
        body.amount = Number(body.amount);
        await api(editingId === null ? '/api/expenses' : `/api/expenses/${editingId}`, jsonRequest(editingId === null ? 'POST' : 'PUT', body));
        bootstrap.Modal.getOrCreateInstance(document.getElementById('expenseModal')).hide();
        prepareExpense();
        await loadExpenses(); showMessage('Transaction saved.', true);
    });
});
loadExpenses().catch(error => { emptyRecords(7, 'Unable to load transactions.'); showMessage(error.message); });
