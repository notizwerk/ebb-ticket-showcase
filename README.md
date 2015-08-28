# vert-x3 eventbus bridge with tickets

This project shows the use of tickets send over the websocker to the vert-x3 eventbus. The Eventbus Bridge in the server instance can use this ticket to identify or authorize the client.

Here  a user logs in via a standard HTTP(S) request and receives a session id. This session id is used in the eventbus as ticket and send to the server.


## usage

```BASH
git clone https://github.com/sibay/ebb-ticket-showcase.git
./gradlew execute
```

After startup open the URL http://localhost:8080/ and use the buttons on the page.