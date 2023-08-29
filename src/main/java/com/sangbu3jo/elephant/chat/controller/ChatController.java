package com.sangbu3jo.elephant.chat.controller;


import com.sangbu3jo.elephant.board.dto.BoardResponseDto;
import com.sangbu3jo.elephant.board.service.BoardService;
import com.sangbu3jo.elephant.chat.dto.ChatMessageRequestDto;
import com.sangbu3jo.elephant.chat.dto.ChatMessageResponseDto;
import com.sangbu3jo.elephant.chat.service.ChatRoomService;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final BoardService boardService;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    /* 단체 채팅의 경우 */
    @GetMapping("/api/chat/{board_id}")
    public String getChatRoom(Model model, @PathVariable Long board_id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("채팅 페이지");
        BoardResponseDto boardResponseDto = new BoardResponseDto(boardService.findBoard(board_id));
        chatRoomService.findChatRoom(boardService.findBoard(board_id));
        model.addAttribute("board", boardResponseDto);
        model.addAttribute("username", userDetails.getUser().getUsername());
        model.addAttribute("nickname", userDetails.getUser().getNickname());
        return "chat";
    }

    @ResponseBody
    @GetMapping("/api/chat/messages/{board_id}")
    public List<ChatMessageResponseDto> getChatMessages(@PathVariable Long board_id) {
        log.info("메세지 리스트 요청 넘어옴");
        List<ChatMessageResponseDto> messages = chatRoomService.getMessages(board_id);
        return messages;
    }

    @MessageMapping("/chat/adduser")
    public void enterUser(@Payload ChatMessageRequestDto chatMessageRequestDto, SimpMessageHeaderAccessor headerAccessor) {
        log.info("서버로 요청 넘어옴을 확인");
        log.info(chatMessageRequestDto.getChatRoomId().toString());
        // 연결하면서 put 해주어야 함
        headerAccessor.getSessionAttributes().put("chatRoomId", chatMessageRequestDto.getChatRoomId());
        headerAccessor.getSessionAttributes().put("nickname", chatMessageRequestDto.getNickname());
        headerAccessor.getSessionAttributes().put("username", chatMessageRequestDto.getUsername());

        // 두 개의 case를 분리해야 함 ->
        Boolean user = chatRoomService.findUsersInChatRoom(chatMessageRequestDto.getUsername(), chatMessageRequestDto.getChatRoomId());
        if (user) {
            chatMessageRequestDto.setMessage(chatMessageRequestDto.getNickname() + "님이 입장하셨습니다 :D");
            messagingTemplate.convertAndSend("/topic/" + chatMessageRequestDto.getChatRoomId(), chatMessageRequestDto);
        } /*else {
            String sessionId = headerAccessor.getSessionId();
            log.info(sessionId);
            List<ChatMessageResponseDto> messages = chatRoomService.getMessages(chatMessageRequestDto.getChatRoomId());
            messagingTemplate.convertAndSendToUser(sessionId, "/topic/" + chatMessageRequestDto.getChatRoomId(), messages);
//            messagingTemplate.convertAndSend("/topic/" + chatMessageRequestDto.getChatRoomId(), messages);
        }*/
    }

    @MessageMapping("/chat/sending")
    public void sendMessage(@Payload ChatMessageRequestDto chatMessageRequestDto, SimpMessageHeaderAccessor headerAccessor) {
        log.info("메시지 전송 요청이 넘어온 것을 확인");
        headerAccessor.getSessionAttributes().put("chatRoomId", chatMessageRequestDto.getChatRoomId());
        chatRoomService.saveChatMessage(chatMessageRequestDto);
        messagingTemplate.convertAndSend("/topic/" + chatMessageRequestDto.getChatRoomId(), chatMessageRequestDto);
    }

//    @EventListener
//    public void webSoekctDesconnectListener(SessionDisconnectEvent event) {
//        log.info("서버와의 연결 끊어짐");
//
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
//        // 세션에서 chatRoomId 추출
//        Long chatRoomId = (Long) accessor.getSessionAttributes().get("chatRoomId");
//        String username = (String) accessor.getSessionAttributes().get("username");
//        String nickname = (String) accessor.getSessionAttributes().get("nickname");
//
//        ChatMessageRequestDto chatMessageRequestDto = new ChatMessageRequestDto(
//                username,
//                nickname,
//                ChatMessageRequestDto.MessageType.LEAVE,
//                chatRoomId
//        );
//
//        chatMessageRequestDto.setMessage(nickname + "님이 퇴장하셨습니다 :D");
//        messagingTemplate.convertAndSend("/topic/" + chatMessageRequestDto.getChatRoomId(), chatMessageRequestDto);
//    }



}
