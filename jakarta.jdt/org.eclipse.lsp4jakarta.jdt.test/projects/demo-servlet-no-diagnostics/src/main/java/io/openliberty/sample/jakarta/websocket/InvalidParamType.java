package io.openliberty.sample.jakarta.websocket;

import java.io.IOException;

import jakarta.fake.CloseReason;
import jakarta.fake.OnOpen;
import jakarta.fake.OnClose;
import jakarta.fake.Session;
import jakarta.fake.server.ServerEndpoint;

/**
 * Expected Diagnostics are related to validating that the parameters for the 
 * WebSocket methods are of valid types for onOpen (diagnostic code: OnOpenChangeInvalidParam) 
 * and onClose (diagnostic code: OnCloseChangeInvalidParam).
 * See issues #247 (onOpen) and #248 (onClose)
 */
@ServerEndpoint(value = "/infos")
public class InvalidParamType {
    @OnOpen
    public void OnOpen(Session session, Object invalidParam) throws IOException {
        System.out.println("WebSocket closed for " + session.getId());
    }
    
    @OnClose
    public void OnClose(Session session, CloseReason closeReason, Object invalidParam) throws IOException {
        System.out.println("WebSocket closed for " + session.getId());
    }
}
