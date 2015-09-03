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

import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import java.util.function.BiConsumer;

/**
 * @author Tarek El-Sibay
 */
public class TokenizedBridgeEventHandlerImpl implements TokenizedBridgeEventHandler {
		
		private final BiConsumer<BridgeEvent,String> bridgeEventAuthTokenHandler;
		private final String tokenName;
		
		public TokenizedBridgeEventHandlerImpl(String tokenName,BiConsumer<BridgeEvent,String> bridgeEventAuthTokenHandler) {
			this.tokenName = tokenName;
			this.bridgeEventAuthTokenHandler = bridgeEventAuthTokenHandler;
		}
		
		@Override
		public void handle(BridgeEvent event) {
			String token = null;
			if ( event.rawMessage() != null) {
				token = event.rawMessage().getString(tokenName);
			}
			bridgeEventAuthTokenHandler.accept(event, token);
		}
		

	
}
