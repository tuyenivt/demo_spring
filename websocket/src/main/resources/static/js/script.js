// ========================================
// Constants
// ========================================
const MessageType = {
  BROADCAST: "broadcast",
  PRIVATE: "private",
  NOTIFICATION: "notification",
};

const SystemSender = {
  NAME: "System",
  ICON: "🔔",
};

const Destinations = {
  TOPIC_MESSAGES: "/topic/messages",
  TOPIC_NOTIFICATIONS: "/topic/notifications",
  USER_QUEUE_PRIVATE: "/user/queue/private",
  USER_QUEUE_ERRORS: "/user/queue/errors",
  APP_CHAT_SEND: "/app/chat.send",
  APP_CHAT_PRIVATE: "/app/chat.private",
};

const SystemMessages = {
  CONNECTED: "Connected to WebSocket server",
  DISCONNECTED: "Disconnected from server",
  CONNECTION_FAILED: "Connection failed: ",
  ENTER_USERNAME: "Please enter a username",
};

// ========================================
// State Management
// ========================================
let stompClient = null;
let isConnected = false;
let currentUsername = null;
let selectedUser = null;
let onlineUsers = new Set();

// DOM elements
const statusDot = document.getElementById("statusDot");
const statusText = document.getElementById("statusText");
const sessionInfo = document.getElementById("sessionInfo");
const chatArea = document.getElementById("chatArea");
const userList = document.getElementById("userList");
const usernameInput = document.getElementById("usernameInput");
const messageInput = document.getElementById("messageInput");
const sendButton = document.getElementById("sendButton");
const connectButton = document.getElementById("connectButton");
const disconnectButton = document.getElementById("disconnectButton");
const clearChatButton = document.getElementById("clearChatButton");
const modeIndicator = document.getElementById("modeIndicator");

// ========================================
// WebSocket Connection
// ========================================

/**
 * Connect to WebSocket endpoint
 */
function connect() {
  const username = usernameInput.value.trim();

  if (!username) {
    alert(SystemMessages.ENTER_USERNAME);
    return;
  }

  currentUsername = username;
  const socket = new SockJS("/ws");
  stompClient = StompJs.Stomp.over(socket);

  // Enable debug for troubleshooting
  stompClient.debug = (str) => {
    console.log("[STOMP]", str);
  };

  // Pass username in STOMP CONNECT headers (more secure than query params)
  // In production, this would typically be a JWT token instead of plain username
  const connectHeaders = {
    username: username,
  };

  stompClient.connect(connectHeaders, onConnected, onError);
}

/**
 * Callback when connection is established
 */
function onConnected() {
  console.log("[WebSocket] Connected successfully");
  isConnected = true;
  updateConnectionStatus(true);

  // Subscribe to all destinations
  console.log("[WebSocket] Subscribing to topics...");

  stompClient.subscribe(Destinations.TOPIC_MESSAGES, (message) => {
    console.log("[Received] Broadcast message:", message);
    onMessageReceived(message);
  });

  stompClient.subscribe(Destinations.TOPIC_NOTIFICATIONS, (message) => {
    console.log("[Received] Notification:", message);
    onNotificationReceived(message);
  });

  // CRITICAL: Subscribe to private messages
  // This subscribes to /user/queue/private
  // Server sends via convertAndSendToUser(username, "/queue/private", msg)
  stompClient.subscribe(Destinations.USER_QUEUE_PRIVATE, (message) => {
    console.log("[Received] Private message:", message);
    onPrivateMessageReceived(message);
  });

  stompClient.subscribe(Destinations.USER_QUEUE_ERRORS, (message) => {
    console.log("[Received] Error:", message);
    onErrorReceived(message);
  });

  console.log("[WebSocket] All subscriptions active");
  addSystemMessage(SystemMessages.CONNECTED);
  addDebugInfo(`Subscribed to: ${Destinations.USER_QUEUE_PRIVATE}`);

  // Add self to user list
  addUser(currentUsername, true);
}

/**
 * Callback when connection fails
 */
function onError(error) {
  console.error("[WebSocket] Connection error:", error);
  updateConnectionStatus(false);
  addSystemMessage(SystemMessages.CONNECTION_FAILED + error, true);
}

/**
 * Disconnect from WebSocket
 */
function disconnect() {
  if (stompClient !== null) {
    stompClient.disconnect(() => {
      console.log("[WebSocket] Disconnected");
      isConnected = false;
      updateConnectionStatus(false);
      addSystemMessage(SystemMessages.DISCONNECTED);
      clearUserList();
      currentUsername = null;
      selectedUser = null;
      updateModeIndicator();
    });
  }
}

// ========================================
// Message Sending
// ========================================

/**
 * Send message (broadcast or private depending on selected user)
 */
function sendMessage() {
  const content = messageInput.value.trim();

  if (!content) {
    return;
  }

  if (selectedUser && selectedUser !== currentUsername) {
    // Send private message with targetUsername
    const privateMessage = {
      username: currentUsername,
      content: content,
      targetUsername: selectedUser,
    };

    console.log("[Sending] Private message:", privateMessage);
    stompClient.send(
      Destinations.APP_CHAT_PRIVATE,
      {},
      JSON.stringify(privateMessage),
    );

    // Show in own chat that message was sent
    displaySentPrivateMessage(selectedUser, content);
  } else {
    // Send broadcast message
    const broadcastMessage = {
      username: currentUsername,
      content: content,
    };

    console.log("[Sending] Broadcast message:", broadcastMessage);
    stompClient.send(
      Destinations.APP_CHAT_SEND,
      {},
      JSON.stringify(broadcastMessage),
    );
  }

  messageInput.value = "";
}

/**
 * Display sent private message in own chat
 */
function displaySentPrivateMessage(targetUsername, content) {
  const messageDiv = document.createElement("div");
  messageDiv.className = "message private-sent";

  const time = new Date().toLocaleTimeString();

  messageDiv.innerHTML = `
        <div class="message-header">
            <span class="message-username">You → ${escapeHtml(targetUsername)}</span>
            <span class="message-time">${time} (Sent)</span>
        </div>
        <div class="message-content">${escapeHtml(content)}</div>
    `;

  chatArea.appendChild(messageDiv);
  chatArea.scrollTop = chatArea.scrollHeight;
}

// ========================================
// Message Receiving
// ========================================

/**
 * Handle received broadcast message
 */
function onMessageReceived(payload) {
  const message = JSON.parse(payload.body);
  console.log("[Handler] Processing broadcast message:", message);
  displayMessage(message);

  // Track users from broadcast messages
  if (message.username !== SystemSender.NAME) {
    addUser(message.username, message.username === currentUsername);
  }
}

/**
 * Handle received private message
 */
function onPrivateMessageReceived(payload) {
  const message = JSON.parse(payload.body);
  console.log("[Handler] Processing private message:", message);
  displayPrivateMessage(message);
  addDebugInfo(`Private message received from ${message.username}`);
}

/**
 * Handle received notification
 */
function onNotificationReceived(payload) {
  const notification = JSON.parse(payload.body);
  console.log("[Handler] Processing notification:", notification);
  displayNotification(notification);

  // Parse join/leave notifications to update user list
  parseUserNotification(notification.content);
}

/**
 * Handle error messages
 */
function onErrorReceived(payload) {
  const error = JSON.parse(payload.body);
  console.error("[Handler] Processing error:", error);
  addSystemMessage(`Error: ${error.message}`, true);
}

/**
 * Parse notification to extract user join/leave events
 */
function parseUserNotification(content) {
  // Pattern: "User 'username' joined the chat"
  const joinMatch = content.match(/User '(.+?)' joined the chat/);
  if (joinMatch) {
    const username = joinMatch[1];
    addUser(username, username === currentUsername);
    return;
  }

  // Pattern: "User 'username' left the chat"
  const leaveMatch = content.match(/User '(.+?)' left the chat/);
  if (leaveMatch) {
    const username = leaveMatch[1];
    removeUser(username);
    return;
  }
}

// ========================================
// Message Display
// ========================================

/**
 * Display broadcast message in chat area
 */
function displayMessage(message) {
  const messageDiv = document.createElement("div");
  messageDiv.className = "message";

  const time = new Date(message.timestamp).toLocaleTimeString();

  messageDiv.innerHTML = `
        <div class="message-header">
            <span class="message-username">${escapeHtml(message.username)}</span>
            <span class="message-time">${time}</span>
        </div>
        <div class="message-content">${escapeHtml(message.content)}</div>
    `;

  chatArea.appendChild(messageDiv);
  chatArea.scrollTop = chatArea.scrollHeight;
}

/**
 * Display received private message
 */
function displayPrivateMessage(message) {
  const messageDiv = document.createElement("div");
  messageDiv.className = "message private";

  const time = new Date(message.timestamp).toLocaleTimeString();

  messageDiv.innerHTML = `
        <div class="message-header">
            <span class="message-username">🔒 ${escapeHtml(message.username)} (Private)</span>
            <span class="message-time">${time}</span>
        </div>
        <div class="message-content">${escapeHtml(message.content)}</div>
    `;

  chatArea.appendChild(messageDiv);
  chatArea.scrollTop = chatArea.scrollHeight;
}

/**
 * Display notification in chat area
 */
function displayNotification(notification) {
  const messageDiv = document.createElement("div");
  messageDiv.className = "message notification";

  const time = new Date(notification.timestamp).toLocaleTimeString();
  const icon =
    notification.username === SystemSender.NAME ? SystemSender.ICON : "🔔";

  messageDiv.innerHTML = `
        <div class="message-header">
            <span class="message-username">${icon} ${escapeHtml(notification.username)}</span>
            <span class="message-time">${time}</span>
        </div>
        <div class="message-content">${escapeHtml(notification.content)}</div>
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
 * Add debug info message
 */
function addDebugInfo(text) {
  const debugDiv = document.createElement("div");
  debugDiv.className = "debug-info";
  debugDiv.textContent = `[DEBUG] ${text}`;
  chatArea.appendChild(debugDiv);
  chatArea.scrollTop = chatArea.scrollHeight;
}

// ========================================
// User List Management
// ========================================

/**
 * Add user to online users list
 */
function addUser(username, isSelf = false) {
  if (onlineUsers.has(username)) {
    return;
  }

  onlineUsers.add(username);
  renderUserList();
}

/**
 * Remove user from online users list
 */
function removeUser(username) {
  onlineUsers.delete(username);

  if (selectedUser === username) {
    selectedUser = null;
    updateModeIndicator();
  }

  renderUserList();
}

/**
 * Clear user list
 */
function clearUserList() {
  onlineUsers.clear();
  renderUserList();
}

/**
 * Render user list
 */
function renderUserList() {
  userList.innerHTML = "";

  if (onlineUsers.size === 0) {
    userList.innerHTML = '<div class="info-box">No users online</div>';
    return;
  }

  const sortedUsers = Array.from(onlineUsers).sort((a, b) => {
    if (a === currentUsername) return -1;
    if (b === currentUsername) return 1;
    return a.localeCompare(b);
  });

  sortedUsers.forEach((username) => {
    const userDiv = document.createElement("div");
    userDiv.className = "user-item";

    if (username === currentUsername) {
      userDiv.classList.add("self");
    }

    if (username === selectedUser) {
      userDiv.classList.add("selected");
    }

    userDiv.innerHTML = `
            <div class="user-status"></div>
            <span>${escapeHtml(username)}${username === currentUsername ? " (You)" : ""}</span>
        `;

    userDiv.addEventListener("click", () => selectUser(username));
    userList.appendChild(userDiv);
  });
}

/**
 * Select user for private messaging
 */
function selectUser(username) {
  if (username === currentUsername) {
    selectedUser = null;
  } else {
    selectedUser = username;
  }

  updateModeIndicator();
  renderUserList();
}

/**
 * Update mode indicator
 */
function updateModeIndicator() {
  if (selectedUser && selectedUser !== currentUsername) {
    modeIndicator.innerHTML = `Mode: <strong>Private</strong> → <span class="target">${escapeHtml(selectedUser)}</span>`;
  } else {
    modeIndicator.innerHTML = `Mode: <strong>Broadcast</strong> (messages go to all users)`;
  }
}

// ========================================
// UI Utilities
// ========================================

/**
 * Clear chat area
 */
function clearChat() {
  chatArea.innerHTML = "";
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
    connectButton.disabled = true;
    disconnectButton.disabled = false;
  } else {
    statusDot.classList.remove("connected");
    statusText.textContent = "Disconnected";
    usernameInput.disabled = false;
    messageInput.disabled = true;
    sendButton.disabled = true;
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

// ========================================
// Event Listeners
// ========================================

connectButton.addEventListener("click", connect);
disconnectButton.addEventListener("click", disconnect);
sendButton.addEventListener("click", sendMessage);
clearChatButton.addEventListener("click", clearChat);

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

console.log("[App] WebSocket chat application loaded");
