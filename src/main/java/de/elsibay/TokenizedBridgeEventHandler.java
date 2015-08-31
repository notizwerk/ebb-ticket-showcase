/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.elsibay;

import io.vertx.core.Handler;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import java.util.function.BiConsumer;

/**
 *
 * @author Tarek El-Sibay
 */
public interface TokenizedBridgeEventHandler  extends Handler<BridgeEvent> {

		public static TokenizedBridgeEventHandler create(BiConsumer<BridgeEvent,String> bridgeEventAuthTokenHandler) {
			return new TokenizedBridgeEventHandlerImpl("authorization",bridgeEventAuthTokenHandler);
		}
	
}
