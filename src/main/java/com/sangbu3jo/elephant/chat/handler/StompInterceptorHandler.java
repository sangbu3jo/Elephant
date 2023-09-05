package com.sangbu3jo.elephant.chat.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99) // 스프링의 빈 순서를 지정하는 에너테이션 (해당 빈의 우선순위를 설정 - 가장 높은 우선순위로 설정)
public class StompInterceptorHandler implements ChannelInterceptor {

    /**
     * ChannelInterceptior 인터페이스의 메서드
     * WebSocket을 통해서 들어온 요청이 처리되기 전 실행됨
     * 수신된 메시지를 가로채고 처리하기 전에 사전 처리 작업 수행 가능
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // StompHeaderAccessor: 메시지의 Stomp 헤더에 쉽게 접근하기 위한 유틸리티 클래스
        // 웹 소캣을 통해 들어온 요청의 STOMP 헤더를 가로채 JWT의 유효성을 검증하는 역할을 수행 - 은 아니고 여기서는 매핑된 url이 잘못된 경우 예외 처리를 해줌 (JWT 검증은 Filter에서 처리함)
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        log.info(message.toString());
        // Web Socket 연결 시 헤더의 JWT Token 유효성 검증 (이걸 filter에서 하지 않나 ?)

        if(StompCommand.SEND.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();

            if (destination == null || !destination.startsWith("/topic")) {
                throw  new IllegalArgumentException("잘못된 접근입니다.");
            }
        }
        return message;
    }


}
