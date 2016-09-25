/*
 * (C) Copyright 2013 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kurento.jsonrpc.client;


import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.SSLSocketFactory;

public class JsonRpcClientWebSocket extends AbstractJsonRpcClientWebSocket {
    private static final String TAG = "WebSocket";

    /*@WebSocket
    public class WebSocketClientSocket {

        @OnWebSocketClose
        public void onClose(int statusCode, String closeReason) {
            log.debug("Websocket disconnected because '{}' (status code {})", closeReason, statusCode);
            handleReconnectDisconnection(statusCode, closeReason);
        }

        @OnWebSocketConnect
        public void onConnect(Session session) {
        }

        @OnWebSocketMessage
        public void onMessage(String message) {
            receivedTextMessage(message);
        }
    }*/

    protected final SSLSocketFactory mSSLSocketFactory;

    private volatile WebSocketClient mWebSocketClient;

    public JsonRpcClientWebSocket(String url, SSLSocketFactory sslContextFactory) {
        this(url, null, sslContextFactory);
    }

    public JsonRpcClientWebSocket(String url, JsonRpcWSConnectionListener connectionListener,
                                  SSLSocketFactory sslContextFactory) {
        super(url, connectionListener);
        this.mSSLSocketFactory = sslContextFactory;
    }

    @Override
    protected void sendTextMessage(String jsonMessage) throws IOException {

        if (mWebSocketClient == null) {
            throw new IllegalStateException(
                    label + " JsonRpcClient is disconnected from WebSocket server at '" + this.url + "'");
        }

        synchronized (JsonRpcClientWebSocket.class) {
            System.out.println("sendTextMessage: " + jsonMessage);
            mWebSocketClient.send(jsonMessage);
        }
    }

    protected boolean isNativeClientConnected() {
        return mWebSocketClient != null && mWebSocketClient.isOpen();
    }

    protected boolean connectNativeClient() throws InterruptedException {

        if (mWebSocketClient == null || mWebSocketClient.isClosed() || mWebSocketClient.isClosing()) {

          /*  jettyClient = new WebSocketClient(sslContextFactory);
              jettyClient.setConnectTimeout(this.connectionTimeout);
              WebSocketPolicy policy = jettyClient.getPolicy();
              policy.setMaxBinaryMessageBufferSize(MAX_PACKET_SIZE);
              policy.setMaxTextMessageBufferSize(MAX_PACKET_SIZE);
              policy.setMaxBinaryMessageSize(MAX_PACKET_SIZE);
              policy.setMaxTextMessageSize(MAX_PACKET_SIZE);

              jettyClient.start();*/
            try {
                mWebSocketClient = new WebSocketClient(new URI(url)) {

                    @Override
                    public void onOpen(ServerHandshake handshakedata) {
                        System.out.println("Connected");
                    }

                    @Override
                    public void onMessage(String message) {
                        log.debug("got message '{}'" + message);
                        receivedTextMessage(message);
                    }

                    @Override
                    public void onClose(int code, String reason, boolean remote) {
                        log.debug("Websocket disconnected because '{}' (status code {})", reason, code);
                        mWebSocketClient = null;
                        handleReconnectDisconnection(code, reason);
                    }

                    @Override
                    public void onError(Exception ex) {
                        System.out.println("onError");
                        ex.printStackTrace();
                    }

                };
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        try {
            mWebSocketClient.setSocket(mSSLSocketFactory.createSocket());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mWebSocketClient.connectBlocking();
    }

    @Override
    public void closeNativeClient() {
        if (isNativeClientConnected()) {
            log.debug("{} Closing client", label);
            mWebSocketClient.close();
            mWebSocketClient = null;
        }
    }

}
