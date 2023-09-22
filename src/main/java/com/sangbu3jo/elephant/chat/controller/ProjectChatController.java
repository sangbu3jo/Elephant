package com.sangbu3jo.elephant.chat.controller;

import com.sangbu3jo.elephant.board.dto.BoardResponseDto;
import com.sangbu3jo.elephant.board.service.BoardService;
import com.sangbu3jo.elephant.chat.dto.ChatMessageRequestDto;
import com.sangbu3jo.elephant.chat.dto.ChatMessageResponseDto;
import com.sangbu3jo.elephant.chat.service.ProjectChatService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProjectChatController {

    private final BoardService boardService;
    private final ProjectChatService projectChatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 미니게임 페이지를 반환
     * @param board_id: URL에 매핑되어 있는 프로젝트(보드)의 ID 값
     * @return: 반환할 HTML 페이지
     */
    @GetMapping("/api/minigame/{board_id}")
    public String getMiniGame(@PathVariable Long board_id) {
        return "miniGame";
    }

    /**
     * 단체 채팅방 페이지 반환
     * @param model: Model에 프론트로 전송할 데이터를 담아서 보냄
     * @param board_id: URL에 매핑되어 있는 프로젝트(보드)의 ID 값
     * @param userDetails: 로그인한 사용자인지 확인하기 위함 & 채팅할 사용자 확인 (JWT로 검증)
     * @return: 반환할 HTML 페이지
     */
    @GetMapping("/api/chat/{board_id}")
    public String getChatRoom(Model model, @PathVariable Long board_id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String title = projectChatService.findChatRoom(boardService.findBoard(board_id));
        model.addAttribute("title", title);
        model.addAttribute("username", userDetails.getUser().getUsername());
        model.addAttribute("nickname", userDetails.getUser().getNickname());
        return "chat";
    }

    /**
     * 단체 채팅방 메세지 리스트 반환
     * @param board_id: URL에 매핑되어 있는 프로젝트(보드)의 ID 값
     * @param userDetails: 로그인한 사용자인지 확인하기 위함 & 채팅할 사용자 확인 (JWT로 검증)
     * @return: 채팅방의 메세지 내역 반환
     */
    @ResponseBody
    @GetMapping("/api/chat/messages/{board_id}")
    public List<ChatMessageResponseDto> getChatMessages(@PathVariable Long board_id, @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                        @RequestParam(required = false, defaultValue = "0") Integer pageNo) {
        log.info("메세지 리스트 요청 넘어옴");
        // 이 사람이 채팅방에 존재하는 지 여부를 파악하고 나서, 채팅방에 들어온 시간을 기준으로 그 이후의 데이터만 갖고와서 보여주어야 함
        try {
            List<ChatMessageResponseDto> messages = projectChatService.getMessages(board_id, userDetails.getUser().getUsername(), pageNo);
            return messages;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 단체 채팅방 구독 요청
     * @param chatMessageRequestDto: 메세지 타입과, 보내는 채팅방의 ID, 유저 정보, 메세지 내용, 보내는 시간을 받아옴
     * @param headerAccessor: WebSocket 및 메세지 정보를 담음
     */
    @MessageMapping("/chat/adduser")
    public void enterUser(@Payload ChatMessageRequestDto chatMessageRequestDto, SimpMessageHeaderAccessor headerAccessor) {
        log.info("서버로 요청 넘어옴을 확인");
        log.info(chatMessageRequestDto.getChatRoomId().toString());
        // 연결하면서 put 해주어야 함
        headerAccessor.getSessionAttributes().put("chatRoomId", chatMessageRequestDto.getChatRoomId());
        headerAccessor.getSessionAttributes().put("nickname", chatMessageRequestDto.getNickname());
        headerAccessor.getSessionAttributes().put("username", chatMessageRequestDto.getUsername());

        // 채팅방에 처음 들어온 사용자인지 구분
        Boolean user = projectChatService.findUsersInChatRoom(chatMessageRequestDto.getUsername(), chatMessageRequestDto.getChatRoomId(), chatMessageRequestDto.getSendTime());
        if (user) {
            log.info("처음 들어온 유저");
            chatMessageRequestDto.setMessage(chatMessageRequestDto.getNickname() + "님이 입장하셨습니다 :D");
            ChatMessageResponseDto chatMessage = projectChatService.saveChatMessage(chatMessageRequestDto);
            messagingTemplate.convertAndSend("/topic/" + chatMessageRequestDto.getChatRoomId(), chatMessage);
        }
    }

    /**
     * 단체 채팅 메세지 전송
     * @param chatMessageRequestDto: 메세지 타입과, 보내는 채팅방의 ID, 유저 정보, 메세지 내용, 보내는 시간을 받아옴
     * @param headerAccessor: WebSocket 및 메세지 정보를 담음
     */
    @MessageMapping("/chat/sending")
    public void sendMessage(@Payload ChatMessageRequestDto chatMessageRequestDto, SimpMessageHeaderAccessor headerAccessor) {
        log.info("메시지 전송 요청이 넘어온 것을 확인");
        headerAccessor.getSessionAttributes().put("chatRoomId", chatMessageRequestDto.getChatRoomId());
        ChatMessageResponseDto message = projectChatService.saveChatMessage(chatMessageRequestDto);
        log.info(message.getType());
        messagingTemplate.convertAndSend("/topic/" + chatMessageRequestDto.getChatRoomId(), message);
    }














}
