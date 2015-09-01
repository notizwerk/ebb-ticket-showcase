/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.elsibay;

import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import java.util.function.BiConsumer;

/**
 *
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
