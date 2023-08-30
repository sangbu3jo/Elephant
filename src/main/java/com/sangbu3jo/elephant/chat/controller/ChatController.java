package com.sangbu3jo.elephant.chat.controller;


import com.sangbu3jo.elephant.board.dto.BoardResponseDto;
import com.sangbu3jo.elephant.board.service.BoardService;
import com.sangbu3jo.elephant.chat.dto.*;
import com.sangbu3jo.elephant.chat.service.ChatRoomService;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final BoardService boardService;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/api/minigame/{board_id}")
    public String getMiniGame(@PathVariable Long board_id) {
        return "miniGame";
    }

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
    public List<ChatMessageResponseDto> getChatMessages(@PathVariable Long board_id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("메세지 리스트 요청 넘어옴");
        // 이 사람이 채팅방에 존재하는 지 여부를 파악하고 나서, 채팅방에 들어온 시간을 기준으로 그 이후의 데이터만 갖고와서 보여주어야 함
        List<ChatMessageResponseDto> messages = chatRoomService.getMessages(board_id, userDetails.getUser().getUsername());
        return messages;
    }

    /* 단체 채팅 요청 */
    @MessageMapping("/chat/adduser")
    public void enterUser(@Payload ChatMessageRequestDto chatMessageRequestDto, SimpMessageHeaderAccessor headerAccessor) {
        log.info("서버로 요청 넘어옴을 확인");
        log.info(chatMessageRequestDto.getChatRoomId().toString());
        // 연결하면서 put 해주어야 함
        headerAccessor.getSessionAttributes().put("chatRoomId", chatMessageRequestDto.getChatRoomId());
        headerAccessor.getSessionAttributes().put("nickname", chatMessageRequestDto.getNickname());
        headerAccessor.getSessionAttributes().put("username", chatMessageRequestDto.getUsername());

        // 두 개의 case를 분리해야 함 ->
        Boolean user = chatRoomService.findUsersInChatRoom(chatMessageRequestDto.getUsername(), chatMessageRequestDto.getChatRoomId(), chatMessageRequestDto.getSendTime());
        if (user) {
            chatMessageRequestDto.setMessage(chatMessageRequestDto.getNickname() + "님이 입장하셨습니다 :D");
            chatRoomService.saveChatMessage(chatMessageRequestDto);
            messagingTemplate.convertAndSend("/topic/" + chatMessageRequestDto.getChatRoomId(), chatMessageRequestDto);
        }
    }

    /* 단체 채팅 메세지 요청 */
    @MessageMapping("/chat/sending")
    public void sendMessage(@Payload ChatMessageRequestDto chatMessageRequestDto, SimpMessageHeaderAccessor headerAccessor) {
        log.info("메시지 전송 요청이 넘어온 것을 확인");
        headerAccessor.getSessionAttributes().put("chatRoomId", chatMessageRequestDto.getChatRoomId());
        chatRoomService.saveChatMessage(chatMessageRequestDto);
        messagingTemplate.convertAndSend("/topic/" + chatMessageRequestDto.getChatRoomId(), chatMessageRequestDto);
    }

    /* 개인 채팅의 경우 (채팅방 리스트부터 출력함) */
    @GetMapping("/api/chatRooms")
    public String getPrivateChatRooms(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                      Model model) {
        // model에 List혹은 Slice 형태로 담아서 보내기 (프론트에서 list 받아서 출력해주기)
        List<PrivateChatRoomResponseDto> chatRoomResponseDtos = chatRoomService.findAllPrivateChatRooms(userDetails.getUser().getUsername()); // username으로 찾기
        model.addAttribute("chatrooms", chatRoomResponseDtos);
        return "chatRooms";
    }

    @ResponseBody
    @GetMapping("/api/chat/privatemessages/{chatRoom_id}")
    public List<PrivateChatMessageResponseDto> getChatMessages(@PathVariable String chatRoom_id) {
        log.info("메세지 리스트 요청 넘어옴");
        List<PrivateChatMessageResponseDto> messages = chatRoomService.getPrivateMessages(chatRoom_id);
        return messages;
    }

    // 개인 채팅방 생성 혹은 연결
    @PostMapping("/api/chatRooms")
    @ResponseBody
    public String createPrivateChatRooms(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @RequestBody ChatUserRequestDto chatUserRequestDto) {
//        return new RedirectView(chatRoomService.findPrivateChatRoom(userDetails.getUser().getUsername(), chatUserRequestDto.getUsername()));
        return chatRoomService.findPrivateChatRoom(userDetails.getUser().getUsername(), chatUserRequestDto.getUsername());
    }

    // 개인 채팅방 페이지 반환
    @GetMapping("/api/chatRooms/{chatRoom_id}")
    public String getPersonalChatRoom(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model,
                                      @PathVariable String chatRoom_id) {
        // 일단 UUID로 구분하는 것으로 결정
        // 개인 채팅방 페이지 return 하기 (저 채팅을 .. 어째구분하지..)
        model.addAttribute("username", userDetails.getUser().getUsername());
        model.addAttribute("nickname", userDetails.getUser().getNickname());
        return "privateChat";
    }

    /* 개인 채팅 구독 */
    @MessageMapping("/chat/enteruser")
    public void enterPrivateUser(@Payload PrivateChatMessageRequestDto chatMessageRequestDto,
                                 SimpMessageHeaderAccessor headerAccessor) {
        log.info("서버로 요청 넘어옴을 확인");
        log.info(chatMessageRequestDto.getTitle().toString());
        // 연결하면서 put 해주어야 함
        headerAccessor.getSessionAttributes().put("chatRoomTitle", chatMessageRequestDto.getTitle());
    }

    /* 개인 채팅 메세지 요청 */
    @MessageMapping("/chat/send")
    public void sendPrivateMessage(@Payload PrivateChatMessageRequestDto chatMessageRequestDto,
                                   SimpMessageHeaderAccessor headerAccessor) {
        log.info("메시지 전송 요청이 넘어온 것을 확인");
        headerAccessor.getSessionAttributes().put("chatRoomTitle", chatMessageRequestDto.getTitle());
        chatMessageRequestDto.setSendTime(LocalDateTime.now());
        chatRoomService.savePrivateChatMessage(chatMessageRequestDto);
        messagingTemplate.convertAndSend("/queue/" + chatMessageRequestDto.getTitle(), chatMessageRequestDto);
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
