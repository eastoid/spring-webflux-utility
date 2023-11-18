## RSocket `browser` & `server` documentation

JS and Spring Boot implementations

---

### Libraries
Client:
- buffer
- rsocket-websocket-client
- rsocket-core
Buffer must be assigned for use in browser environment
<br>
Server (Webflux):

- org.springframework.boot:spring-boot-starter-rsocket

Can also include RSocket security integration
<br><br>

---

### Creating client connection
```javascript
import { RSocketConnector } from "rsocket-core";
import { WebsocketClientTransport } from "rsocket-websocket-client";
import { Buffer } from "buffer"
```

```javascript
async function setupRSocket() {
    const connector = new RSocketConnector({
        setup: { // RSocket message encoding mime types
            dataMimeType: "text/plain",
            metadataMimeType: "message/x.rsocket.routing.v0"
        },
        transport: new WebsocketClientTransport({
            url: "ws://127.0.0.1:7000/rsocket", // RSocket URI
        }),
    });

    try {
        rsocket = await connector.connect(); // Connect to RSocket - ==> `rsocket` variable contains our connected RSocket
    } catch (error) {
        console.error("Connection failed", error);
        return null;
    }
}

```

---

### Encoding metadata (routes)

In the example setup code chunk, metadata is encoded as `message/x.rsocket.routing.v0`.<br>
For correct functionality, use `Buffer.from(encodeRoutingMetadata("route"))`

```javascript
function encodeRoutingMetadata(route: string): Uint8Array {
    const encodedRoute = new TextEncoder().encode(route);
    const length = encodedRoute.length;
    const buffer = new Uint8Array(1 + length);
    buffer[0] = length;
    buffer.set(encodedRoute, 1);
    return buffer;
    // max route length: 255 char
}
```

---

### Sending Request-Response message from client

```javascript
rsocket.requestResponse(
{
  data: Buffer.from('My message'),
  metadata: Buffer.from 
},
{
}

```


























