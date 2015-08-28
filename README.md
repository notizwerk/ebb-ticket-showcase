# vert-x3 eventbus bridge with tickets

This project shows the use of tickets send over the websocker to the vert-x3 eventbus. The Eventbus Bridge in the server instance can use this ticket to identify or authorize the client.

A user logs in via a standard HTTP(S) request (press the __login__ button on the page) and receives a session id. This session id is used in the eventbus as ticket and send to the server (press the __start eventbus__ button then the __send message__ and/or __register handler__ buttons).


## usage

```BASH
git clone https://github.com/sibay/ebb-ticket-showcase.git
cd ebb-ticket-showcase/
./gradlew execute
```

After startup open the URL http://localhost:8080/ and use the buttons on the page. Open the debug console of the browser to see all eventbus messages.


