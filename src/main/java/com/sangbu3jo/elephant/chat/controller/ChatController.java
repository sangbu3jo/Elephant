package com.sangbu3jo.elephant.chat.controller;


import com.sangbu3jo.elephant.board.dto.BoardResponseDto;
import com.sangbu3jo.elephant.board.service.BoardService;
import com.sangbu3jo.elephant.chat.dto.*;
import com.sangbu3jo.elephant.chat.service.ChatRoomService;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
        log.info("채팅 페이지");
        BoardResponseDto boardResponseDto = new BoardResponseDto(boardService.findBoard(board_id));
        chatRoomService.findChatRoom(boardService.findBoard(board_id));
        model.addAttribute("board", boardResponseDto);
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
    public List<ChatMessageResponseDto> getChatMessages(@PathVariable Long board_id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("메세지 리스트 요청 넘어옴");
        // 이 사람이 채팅방에 존재하는 지 여부를 파악하고 나서, 채팅방에 들어온 시간을 기준으로 그 이후의 데이터만 갖고와서 보여주어야 함
        List<ChatMessageResponseDto> messages = chatRoomService.getMessages(board_id, userDetails.getUser().getUsername());
        return messages;
    }

    /**
     * SimpMessageHeaderAccessor (Simp -> Simple Messaging Protocol)
     * 1) 메시지 헤더 읽기 및 쓰기
     * 메시지 헤더에서 값을 읽거나 값을 설정 -> 이를 통해 메시지에 필요한 메타데이터를 추가 or 수정
     * 2) 세션 관리
     * WebSocket 세션과 관련된 정보를 읽거나 설정 (ex. 연결된 WebSocket 세션의 ID를 얻기)
     * 3) 사용자 관리
     * 메시지의 보낸 사용자와 수신 사용자를 처리하는 데 도움 -> 사용자 관리에 대한 다양한 메서드와 속성을 제공
     * 4) 기타 기능
     * 메시지의 목적지, 메시지 유형 등과 같은 다양한 메타데이터를 다루는 데 사용
     */


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
        Boolean user = chatRoomService.findUsersInChatRoom(chatMessageRequestDto.getUsername(), chatMessageRequestDto.getChatRoomId(), chatMessageRequestDto.getSendTime());
        if (user) {
            chatMessageRequestDto.setMessage(chatMessageRequestDto.getNickname() + "님이 입장하셨습니다 :D");
            chatRoomService.saveChatMessage(chatMessageRequestDto);
            messagingTemplate.convertAndSend("/topic/" + chatMessageRequestDto.getChatRoomId(), chatMessageRequestDto);
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
        chatRoomService.saveChatMessage(chatMessageRequestDto);
        messagingTemplate.convertAndSend("/topic/" + chatMessageRequestDto.getChatRoomId(), chatMessageRequestDto);
    }

    /**
     * 개인 채팅방 리스트
     * @param userDetails: 로그인한 사용자인지 확인하기 위함 & 채팅할 사용자 확인 (JWT로 검증)
     * @param model: Model에 프론트로 전송할 데이터를 담아서 보냄
     * @return: 반환할 HTML 페이지
     */
    @GetMapping("/api/chatRooms")
    public String getPrivateChatRooms(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                      Model model) {
        // model에 List혹은 Slice 형태로 담아서 보내기 (프론트에서 list 받아서 출력해주기)
        log.info("채팅방 리스트 반환 (개인 & 단체)");
        List<PrivateChatRoomResponseDto> chatRoomResponseDtos = chatRoomService.findAllPrivateChatRooms(userDetails.getUser()); // username으로 찾기
        model.addAttribute("chatrooms", chatRoomResponseDtos);
        checkAdmin(model, userDetails);
        return "chatRooms";
    }

    /**
     * 개인 채팅 메세지 데이터 반환
     * @param chatRoom_id: URL에 매핑되어 있는 채팅방의 ID 값
     * @return: 채팅 메세지 내역 반환
     */
    @ResponseBody
    @GetMapping("/api/chat/privatemessages/{chatRoom_id}")
    public List<PrivateChatMessageResponseDto> getChatMessages(@PathVariable String chatRoom_id) {
        log.info("메세지 리스트 요청 넘어옴");
        List<PrivateChatMessageResponseDto> messages = chatRoomService.getPrivateMessages(chatRoom_id);
        return messages;
    }

    /**
     * 개인 채팅방 (단체 & 개인) 생성 or 연결
     * @param userDetails: 로그인한 사용자인지 확인하기 위함 & 채팅할 사용자 확인 (JWT로 검증)
     * @param chatUserRequestDto: 채팅에 참여할 사용자의 이름(username)을 받아옴
     * @return: 채팅 페이지 URL 반환
     */
    @PostMapping("/api/chatRooms")
    @ResponseBody
    public String createPrivateChatRooms(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @RequestBody ChatUserRequestDto chatUserRequestDto) {
        log.info("개인 채팅방 생성 요청 넘어옴");
        return chatRoomService.findPrivateChatRoom(userDetails.getUser(), chatUserRequestDto.getUsername());
    }

    /**
     * 개인 채팅방 페이지
     * @param userDetails: 로그인한 사용자인지 확인하기 위함 & 채팅할 사용자 확인 (JWT로 검증)
     * @param model: Model에 프론트로 전송할 데이터를 담아서 보냄
     * @param chatRoom_id: URL에 매핑된 채팅방의 ID 값
     * @return: 반환할 채팅 페이지 HTML
     */
    @GetMapping("/api/chatRooms/{chatRoom_id}")
    public String getPersonalChatRoom(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model,
                                      @PathVariable String chatRoom_id) {
        Boolean groupOrPrivate = chatRoomService.findGroupOrPrivate(chatRoom_id);
        model.addAttribute("group", groupOrPrivate);
        model.addAttribute("username", userDetails.getUser().getUsername());
        model.addAttribute("nickname", userDetails.getUser().getNickname());
        return "privateChat";
    }

    /**
     * 개인 채팅 (단체만) 에서 유저 떠나기
     * @param userDetails: 떠날 유저의 정보
     * @param chatRoom_id: 떠날 채팅방
     * @return: 상태코드와 메세지 반환
     */
    @PutMapping("/api/chatRooms/{chatRoom_id}")
    public ResponseEntity<String> leavePrivateChatRoom(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                       @PathVariable String chatRoom_id) {
        log.info(chatRoom_id);
        try {
            chatRoomService.leavePrivateChatRoom(userDetails.getUser(), chatRoom_id);
            return ResponseEntity.ok().body("성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("실패");
        }
    }


    /**
     * 개인 채팅방 구독 요청
     * @param chatMessageRequestDto: 메세지 타입과, 보내는 채팅방의 ID, 유저 정보, 메세지 내용, 보내는 시간을 받아옴
     * @param headerAccessor: WebSocket 및 메세지 정보를 담음
     */
    @MessageMapping("/chat/enteruser")
    public void enterPrivateUser(@Payload PrivateChatMessageRequestDto chatMessageRequestDto,
                                 SimpMessageHeaderAccessor headerAccessor) {
        log.info("서버로 요청 넘어옴을 확인");
        log.info(chatMessageRequestDto.getTitle().toString());
        // 연결하면서 put 해주어야 함
        headerAccessor.getSessionAttributes().put("chatRoomTitle", chatMessageRequestDto.getTitle());
    }

    /**
     * 개인 채팅 메세지 전송
     * @param chatMessageRequestDto: 메세지 타입과, 보내는 채팅방의 ID, 유저 정보, 메세지 내용, 보내는 시간을 받아옴
     * @param headerAccessor: WebSocket 및 메세지 정보를 담음
     */
    @MessageMapping("/chat/send")
    public void sendPrivateMessage(@Payload PrivateChatMessageRequestDto chatMessageRequestDto,
                                   SimpMessageHeaderAccessor headerAccessor) {
        log.info("메시지 전송 요청이 넘어온 것을 확인");
        headerAccessor.getSessionAttributes().put("chatRoomTitle", chatMessageRequestDto.getTitle());
        chatMessageRequestDto.setSendTime(LocalDateTime.now());
        chatRoomService.savePrivateChatMessage(chatMessageRequestDto);
        messagingTemplate.convertAndSend("/queue/" + chatMessageRequestDto.getTitle(), chatMessageRequestDto);
    }

    /**
     * 관리자인지 아닌지 확인해서 Model에 Boolean 값을 담아 반환하는 메서드
     * @param model: Model에 프론트로 전송할 데이터를 담아서 보냄
     * @param userDetails: 로그인한 사용자인지 확인하기 위함 (JWT로 검증)
     */
    private void checkAdmin(Model model, UserDetailsImpl userDetails) {
        Boolean admin = false;
        if (userDetails.getUser().getRole().equals(UserRoleEnum.ADMIN)) {
            admin = true;
        }
        model.addAttribute("admin", admin);
    }

}
