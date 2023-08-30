package com.sangbu3jo.elephant.chat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration // Bean으로 등록
@EnableWebSocketMessageBroker // STOMP 사용을 위한 에너테이션 (WebSocket 메시지 핸들링과 메시지 브로거 구성을 가능하게 함)
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /* WebSocketConfig에서 엔트포인트로 등록해준 경로들은 Spring Security 설정에서 접근 가능하도록 등록해야 함 */
    private final StompInterceptorHandler stompInterceptorHandler;

    /**
     * 메시지 브로거 구성을 설정하는 메소드
     * enableSimpleBroker() 메서드를 사용해 /queue, /topic 프리픽스를 가진 주제로 메시지를 구독할 수 있게 설정
     * setApplicationDestinationPrefixes() 메서드를 사용해 /pub 프리픽스를 가진 주제로 메시지를 발행할 수 있게 설정
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지를 구독하는 요청 (채팅방 구독) [queue는 1:1, topic은 1:N]
        registry.enableSimpleBroker("/queue", "/topic"); // sub
        // 메시지를 수신하는 요청 (메시지 보냄)
        registry.setApplicationDestinationPrefixes("/app");              // pub
    }

    /**
     * STOMP 프로토콜을 사용해는 WebSocket 엔드포인트를 등록하는 메서드
     * /ws-stomp 경로로 websocket 연결을 가능하게 하고, setAllowedOrigins("*") 로 모든 Origin(출처) 에서의 접근을 허용
     * withSockJS()를 사용해 모든 브라우저에서 WebSocket 기능을 사용 가능
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // sock.js를 통해 STOMP가 동작하지 않는 환경에서도 가능하도록
        // setAllowedOrigins("*")로 모든 오리진(Origin)에서의 접근을 허용
        // 웹이 아니라 앱을 통해서 채팅 기능을 구현하는 경우, withSockJS()를 사용하면 동작하지 않으므로 제외해야 함 (현재는 웹이므로 설정 유지)
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*")/*.setHandshakeHandler(new CustomHandshakeHandler())*/.withSockJS();
                                          // 설정해둔 url의 도메인을 적어주어야 함!
    }

    /**
     * 클라이언트의 인바운드 채널에 대한 설정을 구성하는 메서드
     * interceptors() 메서드를 사용해 stompHandler(커스텀)를 등록해 클라이언트의 WebSocket 연결 이전 처리 작업을 수행 가능하게 함
     * 올바른 권한이 있는 사용자인지 확인하는 로직을 추가하기 위함임 -> Filter에서 처리하니 우선 제외
     */
//    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
//        registration.interceptors(stompInterceptorHandler);
//    }
}
