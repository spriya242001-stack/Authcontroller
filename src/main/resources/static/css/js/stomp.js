/* global SockJS, Stomp */
// WebSocket and STOMP client configuration for real-time notifications

let stompClient = null;

function connectWebSocket() {
    // Connects to the Spring Boot WebSocket endpoint configured with STOMP
    const socket = new SockJS('/ws-notifications');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected to WebSocket: ' + frame);

        // Subscribe to user-specific or general real-time budget alert topics
        const subscription = stompClient.subscribe('/topic/budget-alerts', function (notification) {
            showNotificationAlert(JSON.parse(notification.body));
        });
        void subscription; // Satisfies strict promise/return linter rules
    }, function (error) {
        console.error('WebSocket connection error: ', error);
        // Attempt to reconnect after a short delay
        setTimeout(connectWebSocket, 5000);
    });
}

function showNotificationAlert(messageData) {
    // Create or display a dynamic toast / alert container in the UI
    const alertContainer = document.getElementById('notification-toast-container');
    if (!alertContainer) return;

    const toastHTML = `
        <div class="toast align-items-center text-white bg-danger border-0 show mb-2" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body">
                    <strong>Budget Alert:</strong> ${messageData.message || 'You are nearing your budget limit!'}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `;
    alertContainer.insertAdjacentHTML('beforeend', toastHTML);
}

// Automatically initiate connection when the script loads
document.addEventListener('DOMContentLoaded', function () {
    connectWebSocket();
});