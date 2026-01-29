// WebSocket client implementation
let stompClient = null;
let isConnected = false;

// DOM elements
const statusDot = document.getElementById("statusDot");
const statusText = document.getElementById("statusText");
const sessionInfo = document.getElementById("sessionInfo");
const chatArea = document.getElementById("chatArea");
const usernameInput = document.getElementById("usernameInput");
const messageInput = document.getElementById("messageInput");
const sendButton = document.getElementById("sendButton");
const connectButton = document.getElementById("connectButton");
const disconnectButton = document.getElementById("disconnectButton");
const sendPrivateButton = document.getElementById("sendPrivateButton");

/**
 * Connect to WebSocket endpoint
 */
function connect() {
  const username = usernameInput.value.trim();

  if (!username) {
    alert("Please enter a username");
    return;
  }

  // Create SockJS connection
  const socket = new SockJS("/ws");

  // Create STOMP client
  stompClient = StompJs.Stomp.over(socket);

  // Optional: Disable console debug messages
  stompClient.debug = (str) => {
    console.log("STOMP: " + str);
  };

  // Connect to WebSocket
  stompClient.connect({}, onConnected, onError);
}

/**
 * Callback when connection is established
 */
function onConnected() {
  isConnected = true;
  updateConnectionStatus(true);

  // Subscribe to broadcast topic
  stompClient.subscribe("/topic/messages", onMessageReceived);

  // Subscribe to notification topic
  stompClient.subscribe("/topic/notifications", onNotificationReceived);

  // Subscribe to user-specific queue for errors
  stompClient.subscribe("/user/queue/errors", onErrorReceived);

  // Subscribe to user-specific queue for private messages
  stompClient.subscribe("/user/queue/reply", onPrivateMessageReceived);

  addSystemMessage("Connected to WebSocket server");
}

/**
 * Callback when connection fails
 */
function onError(error) {
  console.error("WebSocket error:", error);
  updateConnectionStatus(false);
  addSystemMessage("Connection failed: " + error, true);
}

/**
 * Disconnect from WebSocket
 */
function disconnect() {
  if (stompClient !== null) {
    stompClient.disconnect(() => {
      isConnected = false;
      updateConnectionStatus(false);
      addSystemMessage("Disconnected from server");
    });
  }
}

/**
 * Send chat message to /app/chat.send
 */
function sendMessage() {
  const username = usernameInput.value.trim();
  const content = messageInput.value.trim();

  if (!content) {
    return;
  }

  const chatMessage = {
    username: username,
    content: content,
  };

  // Send to /app/chat.send (mapped to @MessageMapping("/chat.send"))
  stompClient.send("/app/chat.send", {}, JSON.stringify(chatMessage));

  messageInput.value = "";
}

/**
 * Send private message to /app/chat.private
 */
function sendPrivateMessage() {
  const username = usernameInput.value.trim();
  const content = messageInput.value.trim();

  if (!content) {
    return;
  }

  const chatMessage = {
    username: username,
    content: content,
  };

  stompClient.send("/app/chat.private", {}, JSON.stringify(chatMessage));

  messageInput.value = "";
}

/**
 * Handle received broadcast message
 */
function onMessageReceived(payload) {
  const message = JSON.parse(payload.body);
  displayMessage(message);
}

/**
 * Handle received private message
 */
function onPrivateMessageReceived(payload) {
  const message = JSON.parse(payload.body);
  displayMessage(message, true);
}

/**
 * Handle received notification
 */
function onNotificationReceived(payload) {
  const notification = JSON.parse(payload.body);
  displayNotification(notification);
}

/**
 * Display notification in chat area
 */
function displayNotification(notification) {
  const messageDiv = document.createElement("div");
  messageDiv.className = "message notification";
  messageDiv.style.background = "#fff3cd";
  messageDiv.style.borderLeft = "3px solid #ffc107";

  const time = new Date(notification.timestamp).toLocaleTimeString();

  messageDiv.innerHTML = `
        <div class="message-header">
            <span class="message-username">${escapeHtml(notification.username)}</span>
            <span class="message-time">${time}</span>
        </div>
        <div class="message-content">${escapeHtml(notification.content)}</div>
    `;

  chatArea.appendChild(messageDiv);
  chatArea.scrollTop = chatArea.scrollHeight;
}

/**
 * Handle error messages
 */
function onErrorReceived(payload) {
  const error = JSON.parse(payload.body);
  addSystemMessage(`Error: ${error.message}`, true);
}

/**
 * Display message in chat area
 */
function displayMessage(message, isPrivate = false) {
  const messageDiv = document.createElement("div");
  messageDiv.className = "message" + (isPrivate ? " private" : "");

  const time = new Date(message.timestamp).toLocaleTimeString();

  messageDiv.innerHTML = `
        <div class="message-header">
            <span class="message-username">${escapeHtml(message.username)}</span>
            <span class="message-time">${time} ${isPrivate ? "(Private)" : ""}</span>
        </div>
        <div class="message-content">${escapeHtml(message.content)}</div>
    `;

  chatArea.appendChild(messageDiv);
  chatArea.scrollTop = chatArea.scrollHeight;
}

/**
 * Add system message
 */
function addSystemMessage(text, isError = false) {
  const messageDiv = document.createElement("div");
  messageDiv.className = "message" + (isError ? " error" : "");
  messageDiv.innerHTML = `
        <div class="message-content"><em>${escapeHtml(text)}</em></div>
    `;
  chatArea.appendChild(messageDiv);
  chatArea.scrollTop = chatArea.scrollHeight;
}

/**
 * Update connection status UI
 */
function updateConnectionStatus(connected) {
  if (connected) {
    statusDot.classList.add("connected");
    statusText.textContent = "Connected";
    usernameInput.disabled = true;
    messageInput.disabled = false;
    sendButton.disabled = false;
    sendPrivateButton.disabled = false;
    connectButton.disabled = true;
    disconnectButton.disabled = false;
  } else {
    statusDot.classList.remove("connected");
    statusText.textContent = "Disconnected";
    usernameInput.disabled = false;
    messageInput.disabled = true;
    sendButton.disabled = true;
    sendPrivateButton.disabled = true;
    connectButton.disabled = false;
    disconnectButton.disabled = true;
  }
}

/**
 * Escape HTML to prevent XSS
 */
function escapeHtml(text) {
  const div = document.createElement("div");
  div.textContent = text;
  return div.innerHTML;
}

// Event listeners
connectButton.addEventListener("click", connect);
disconnectButton.addEventListener("click", disconnect);
sendButton.addEventListener("click", sendMessage);
sendPrivateButton.addEventListener("click", sendPrivateMessage);

messageInput.addEventListener("keypress", (e) => {
  if (e.key === "Enter") {
    sendMessage();
  }
});

usernameInput.addEventListener("keypress", (e) => {
  if (e.key === "Enter") {
    connect();
  }
});
