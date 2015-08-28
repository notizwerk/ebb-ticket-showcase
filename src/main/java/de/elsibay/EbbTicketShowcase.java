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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import java.util.Date;

/**
 *
 * @author Tarek El-Sibay
 */
public class EbbTicketShowcase extends AbstractVerticle{
	
	public static void main(String[] args) {
		VertxOptions vo = new VertxOptions();
		vo.setBlockedThreadCheckInterval(1000*60*2);
		Vertx vertx = Vertx.vertx(vo);
		vertx.deployVerticle(EbbTicketShowcase.class.getName());
	}

	public static final int WEBSERVER_PORT = 8080;
	public static final String WEBSERVER_HOST = "localhost";
	public static final int BACKENDSERVER_PORT = 8090;
	public static final String BACKENDSERVER_HOST = "localhost";
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		
		SessionStore sessionStore = LocalSessionStore.create(vertx);
		Router backendRouter = Router.router(vertx);
		backendRouter.route().handler(LoggerHandler.create(LoggerHandler.DEFAULT_FORMAT));
		CookieHandler cookieHandler = CookieHandler.create();
		SessionHandler sessionHandler = SessionHandler.create(sessionStore);
		// the CORS OPTION request must not set cookies
		backendRouter.get().handler(cookieHandler);
		backendRouter.get().handler(sessionHandler);
		backendRouter.post().handler(cookieHandler);
		backendRouter.post().handler(sessionHandler);

		// setup CORS
		CorsHandler corsHandler = CorsHandler.create("http(s)?://" +WEBSERVER_HOST + ":"+WEBSERVER_PORT)
				.allowCredentials(true)
				.allowedHeader(HttpHeaders.ACCEPT.toString())
				.allowedHeader(HttpHeaders.ORIGIN.toString())
				.allowedHeader(HttpHeaders.AUTHORIZATION.toString())
				.allowedHeader(HttpHeaders.CONTENT_TYPE.toString())
				.allowedHeader(HttpHeaders.COOKIE.toString())
				.exposedHeader(HttpHeaders.SET_COOKIE.toString())
				.allowedMethod(HttpMethod.POST)
				.allowedMethod(HttpMethod.PUT)
				.allowedMethod(HttpMethod.GET)
				.allowedMethod(HttpMethod.DELETE);
		
		// setup event bus bridge
		TicketEventbusBridge sebb = new TicketEventbusBridge(sessionStore);		 
		backendRouter.mountSubRouter("/eventbus", sebb.route(vertx));
		
		// dummy eventbus services
		vertx.eventBus().consumer("ping",(Message<JsonObject> msg) -> {
			msg.reply(new JsonObject().put("answer","pong "+ msg.body().getString("text","")));
		});
		
		vertx.setPeriodic(5000, id -> {
			vertx.eventBus().send("topic",new JsonObject().put("timestamp",new Date().getTime()));
		});

		
		// session manager for login
		backendRouter.route("/api/*").handler(corsHandler);
		backendRouter.route("/api/*")
			.method(HttpMethod.POST)
			.method(HttpMethod.PUT)
			.handler(BodyHandler.create());
		
		backendRouter.route("/api/session").handler((RoutingContext rc)->{
			JsonObject user = rc.getBodyAsJson();
			String sessionId = rc.session().id();
			rc.session().put("user", user);
			rc.response().end(user.copy().put("sessionId", sessionId).encodePrettily());
		});
		
		// dummy ping REST service
		backendRouter.route("/api/ping").handler((RoutingContext rc)->{
			JsonObject replyMsg = new JsonObject();
			replyMsg.put("timestamp", new Date().getTime());
			Cookie sessionCookie = rc.getCookie(SessionHandler.DEFAULT_SESSION_COOKIE_NAME);
			if ( sessionCookie != null ) {
				replyMsg.put("sessionId",sessionCookie.getValue());
			}
			rc.response().end(replyMsg.encode());
		});
		
		
		// start backend on one port
		vertx.createHttpServer().requestHandler(backendRouter::accept).listen(BACKENDSERVER_PORT,BACKENDSERVER_HOST,(AsyncResult<HttpServer> async)->{
			System.out.println(async.succeeded() ? "Backend Server started" : "Backend Server start FAILED");
		});
		
		
		// static files on other port
		Router staticFilesRouter = Router.router(vertx);
		staticFilesRouter.route("/*").handler(StaticHandler.create("src/main/www").setCachingEnabled(false));
		vertx.createHttpServer().requestHandler(staticFilesRouter::accept).listen(WEBSERVER_PORT,WEBSERVER_HOST,(AsyncResult<HttpServer> async)->{
			System.out.println(async.succeeded() ? "Web Server started\ngoto http://"+WEBSERVER_HOST+":"+WEBSERVER_PORT+"/" : "Web Server start FAILED");
		});
	}
	
}
