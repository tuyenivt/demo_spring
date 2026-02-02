# Demo WebSocket

## Run project

```bash
./gradlew websocket:clean websocket:bootRun
```

## Access the application

- Open browser to: `http://localhost:8080`
- Enter a username
- Click "Connect"
- Open multiple browser tabs/windows to test broadcasting

## Test functionality

- Broadcast: Type message and click "Send" → All connected clients receive it
- Private: Type message and click "Send Private" → Only you receive the reply
- Validation: Try sending empty messages or very long messages to see error handling
