(async () => {
    try {
        const expenses = await api('/api/expenses');
        const monthly = expenses.filter(e => monthKey(e.date) === currentMonth());
        const total = type => monthly.filter(e => e.type === type).reduce((sum, e) => sum + Number(e.amount), 0);
        const income = total('INCOME'), spending = total('EXPENSE');
        document.getElementById('total-income').textContent = money(income);
        document.getElementById('total-expense').textContent = money(spending);
        document.getElementById('total-balance').textContent = money(income - spending);
        if (typeof Chart === 'undefined') return;
        const categories = {}, months = {};
        for (const e of expenses.filter(e => e.type === 'EXPENSE')) {
            const key = monthKey(e.date);
            months[key] = (months[key] || 0) + Number(e.amount);
            if (key === currentMonth()) categories[e.category] = (categories[e.category] || 0) + Number(e.amount);
        }
        new Chart(document.getElementById('expenseCategoryChart'), {
            type: 'doughnut', data: { labels: Object.keys(categories), datasets: [{ data: Object.values(categories) }] },
            options: { responsive: true, maintainAspectRatio: false }
        });
        const keys = Object.keys(months).sort();
        new Chart(document.getElementById('monthlyTrendChart'), {
            type: 'line', data: { labels: keys, datasets: [{ label: 'Monthly Spending ($)', data: keys.map(key => months[key]), borderColor: '#0d6efd' }] },
            options: { responsive: true, maintainAspectRatio: false, scales: { y: { beginAtZero: true } } }
        });
    } catch (error) { showMessage(error.message); }
})();
