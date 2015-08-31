/*
 *   Copyright (c) 2011-2015 The original author or authors
 *   ------------------------------------------------------
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *
 *       The Eclipse Public License is available at
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *       The Apache License v2.0 is available at
 *       http://www.opensource.org/licenses/apache2.0.php
 *
 *   You may elect to redistribute this code under either of these licenses.
 */
package de.elsibay;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import io.vertx.ext.web.sstore.SessionStore;

/**
 *
 * @author Tarek El-Sibay
 */
public class TicketEventbusBridge {

	private final SessionStore sessionStore;
	
	public TicketEventbusBridge(SessionStore sessionStore) {
		this.sessionStore = sessionStore;
	}

	public Router route(Vertx vertx) {
		Router router = Router.router(vertx);
		SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
		PermittedOptions sendPermission = new PermittedOptions().setAddress("ping");
		PermittedOptions registerPermission = new PermittedOptions().setAddress("topic");
		BridgeOptions options = new BridgeOptions()
				.addInboundPermitted(sendPermission)
				.addInboundPermitted(registerPermission)
				.addOutboundPermitted(registerPermission);

		sockJSHandler.bridge(options, TokenizedBridgeEventHandler.create((BridgeEvent event,String authToken) -> {
			SockJSSocket socket = event.socket();
			// Using the session id would rely on Cookies (in some cases the session id is transported in a Cookie)
			// what opens some security issues. see https://github.com/sockjs/sockjs-node
			/* 
				Session session = socket.webSession();
			*/
			// Use a ticket/sessionId/token send on the socket instead.
			switch (event.type()) {
				case SOCKET_CREATED:
					event.complete(true);
					break;
				case REGISTER:
				case SEND:
				case PUBLISH:
					System.out.println("bridge event of type "+event.type()+" with auth token "+authToken);
					checkToken(authToken, event,(Session session)->{
						System.out.println("found user in session:"+session.get("user").toString());
						event.complete(true);
					});
					break;
				case UNREGISTER:
					// thats ok, even if we have no ticket
				case RECEIVE:
					// the client will receive a message from the server. we have no ticket here.
					event.complete(true);
					break;
				default:
					event.complete(false);
			}
		}));
		router.route("/*").handler(sockJSHandler);		
		return router;
	}
	
	private void checkToken(String authToken, BridgeEvent event, Handler<Session> successHandler) {
		if( authToken == null || "".equals(authToken)) {
			event.complete(false);
		}
		// use the authToken as key to the user session
		this.sessionStore.get(authToken, (AsyncResult<Session> asyncSession) -> {
			if (asyncSession.failed()) {
				System.out.println("cannot get session with id " + authToken + " from sessionStore: " + asyncSession.cause().getMessage());
				event.complete(false);
			} else {
				Session session = asyncSession.result();
				if ( session.isDestroyed() ) {
					System.out.println("session with id " + authToken + " is destroyed");
					event.complete(false);
				} else {
					JsonObject userData = session.get("user");
					if ( userData == null ) {
						System.out.println("session with id " + authToken + " contains no user data ");
						event.complete(false);
					} else {
						successHandler.handle(session);
					}
				}
			}
		});
	};
}
