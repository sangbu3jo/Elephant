package com.sangbu3jo.elephant.chat.config;

import com.sangbu3jo.elephant.chat.entity.StompPrincipal;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    
    // 웹 소캣 세션이 연결이 되는 경우 실행 가능한 고유한 UUID를 생성해주는 핸들러
    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler webSocketHandler,
                                      Map<String, Object> attributes) {
        return new StompPrincipal(UUID.randomUUID().toString());
    }

}
