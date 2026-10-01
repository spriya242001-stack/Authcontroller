/* global Chart */
// Chart.js configuration for dashboard graphs and visual analytics

document.addEventListener('DOMContentLoaded', function () {
    // 1. Expenses by Category (Doughnut Chart)
    const categoryCtx = document.getElementById('expenseCategoryChart');
    if (categoryCtx) {
        new Chart(categoryCtx, {
            type: 'doughnut',
            data: {
                labels: ['Food', 'Utilities', 'Entertainment', 'Transport', 'Shopping'],
                datasets: [{
                    data: [450, 200, 150, 300, 650], // Sample default data or data passed from backend
                    backgroundColor: [
                        '#0d6efd',
                        '#6c757d',
                        '#ffc107',
                        '#198754',
                        '#dc3545'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
    }

    // 2. Monthly Spending Trend (Line Chart)
    const trendCtx = document.getElementById('monthlyTrendChart');
    if (trendCtx) {
        new Chart(trendCtx, {
            type: 'line',
            data: {
                labels: ['May', 'Jun', 'Jul', 'Aug', 'Sep'],
                datasets: [{
                    label: 'Monthly Spending ($)',
                    data: [1200, 1350, 1100, 1600, 1750],
                    borderColor: '#0d6efd',
                    backgroundColor: 'rgba(13, 110, 253, 0.1)',
                    fill: true,
                    tension: 0.3
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }
});