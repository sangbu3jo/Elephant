package com.sangbu3jo.elephant.board.service;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sangbu3jo.elephant.board.dto.BoardOneResponseDto;
import com.sangbu3jo.elephant.board.dto.BoardRequestDto;
import com.sangbu3jo.elephant.board.dto.BoardResponseDto;
import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.board.repository.BoardRepository;
import com.sangbu3jo.elephant.boarduser.dto.BoardUserResponseDto;
import com.sangbu3jo.elephant.boarduser.entity.BoardUser;
import com.sangbu3jo.elephant.boarduser.entity.BoardUserRoleEnum;
import com.sangbu3jo.elephant.boarduser.repository.BoardUserRepository;
import com.sangbu3jo.elephant.chat.entity.ChatRoom;
import com.sangbu3jo.elephant.chat.repository.ChatRoomRepository;
import com.sangbu3jo.elephant.chat.repository.ChatUserRepository;
import com.sangbu3jo.elephant.notification.service.NotificationService;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import com.sangbu3jo.elephant.users.entity.QUser;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j(topic = "보드 서비스")
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardUserRepository boardUserRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatUserRepository chatUserRepository;
    private final UserRepository userRepository;
    private final JPAQueryFactory jpaQueryFactory;
    private final NotificationService notificationService;
    @Autowired
    private final MongoTemplate mongoTemplate;

    /**
     * 프로젝트(보드) 생성
     * @param user: 사용자 정보
     * @param boardRequestDto: 보드 제목, 보드 내용, 보드 마감일을 받아옴
     * @return: 생성된 보드에 대한 내용(BoardResponseDto)을 반환
     */
    @Transactional
    public BoardResponseDto createBoard(User user, BoardRequestDto boardRequestDto) {
        log.info("보드 생성");
        Board board = new Board(boardRequestDto);
        boardRepository.save(board);
        BoardUser boardUser = new BoardUser(board, user, BoardUserRoleEnum.MANAGER);
        boardUserRepository.save(boardUser);

        board.addBoardUser(boardUser);
        return new BoardResponseDto(board);
    }

    /**
     * 프로젝트(보드) 전체 조회
     * @param user: 사용자 정보
     * @param pageable: 페이징을 위한 정보를 담은 인터페이스
     * @param pageNo: 페이징 될 페이지 번호
     * @return: 사용자가 참여하고 있는 보드에 대한 정보(BoardResonseDto)를 Page에 담아서 반환
     */
    public Page<BoardResponseDto> getBoards(User user, Pageable pageable, Integer pageNo) {
        log.info("보드 전체 조회");
        Sort sort = Sort.by(Sort.Direction.DESC, "expiredAt");
        pageable = PageRequest.of(pageNo, 8, sort);

        Page<Board> boards = boardRepository.findAllByIdIn(boardUserRepository.findAllByUser(user).stream().map(BoardUser::getBoard).map(Board::getId).toList(),
                pageable);
        return boards.map(BoardResponseDto::new);
    }

    /**
     * 프로젝트(보드) 단건 조회
     * @param user: 사용자 정보
     * @param boardId: URL에 매핑되어 있는 프로젝트(보드)의 ID 값
     * @return: 보드에 대한 정보(BoardResponseDto)를 반환
     */
    public BoardOneResponseDto getOneBoard(User user, Long boardId) {
        log.info("보드 수정");
        Board board = findBoard(boardId);
        // 해당 사용자가 보드의 참여자인지 아닌지 확인
        findBoardUser(board, user);
        return new BoardOneResponseDto(board);
    }

    /**
     * 프로젝트(보드) 수정 [보드의 매니저, 참여자 모두 가능]
     * @param boardId: URL에 매핑되어 있는 프로젝트(보드)의 ID 값
     * @param user: 사용자 정보
     * @param boardRequestDto: 수정할 보드 제목, 보드 내용, 보드 마감일을 받아옴
     * @return: 수정한 보드에 대한 내용(BoardResponseDto)을 반환
     */
    @Transactional
    public BoardResponseDto updateBoard(Long boardId, User user, BoardRequestDto boardRequestDto) {
        log.info("보드 수정");
        Board board = findBoard(boardId);
        // 해당 사용자가 보드의 참여자인지 아닌지 확인
        findBoardUser(board, user);

        board.updateBoard(boardRequestDto);
        return new BoardResponseDto(board);
    }

    /**
     * 프로젝트(보드) 삭제 [보드의 매니저만 가능]
     * @param boardId: URL에 매핑되어 있는 프로젝트(보드)의 ID 값
     * @param user: 사용자 정보
     */
    public void deleteBoard(Long boardId, User user) {
        log.info("보드 삭제");
        Board board = findBoard(boardId);

        BoardUser boardUser = findBoardUser(board, user);

        if (! boardUser.getRole().equals(BoardUserRoleEnum.MANAGER) ) {
            throw new IllegalArgumentException();
        }

        // MongoDB에서 해당 채팅방의 메세지 내역 삭제
        if (mongoTemplate.getCollection(boardId.toString()) != null) {
            mongoTemplate.getCollection(boardId.toString()).drop();
        }

        if (board.getChatRoom() != null) {
            ChatRoom chatRoom = chatRoomRepository.findById(board.getChatRoom().getId()).orElseThrow();
            chatUserRepository.deleteAll(chatUserRepository.findAllByChatroom(chatRoom));
            chatRoomRepository.delete(chatRoom);
        }

        /* Board 엔티티 안에 Set<BoardUser>를 orphanremoval = true 속성을 주었기 때문에, 해당 레포지토리에서 따로 찾아서 삭제해줄 필요 없음 */
        boardRepository.delete(board);
    }

    /**
     * 프로젝트(보드)에 유저 초대
     * @param userDetails: 사용자가 로그인 했는지 여부를 판단하기 위해서 가져옴
     * @param boardId: 유저를 초대할 보드의 ID
     * @param username: 초대할 유저의 username
     * @return: 로그인 페이지(로그인 X) 혹은 해당 보드 페이지로 반환(로그인 O)
     */
    public String inviteUser(UserDetailsImpl userDetails, Long boardId, String username) {
        User user = findUser(username);
        Board board = findBoard(boardId);
        Optional<BoardUser> boardUser = boardUserRepository.findByBoardAndUser(board, user);
        if (boardUser.isEmpty()) {
            BoardUser boardUsermember = new BoardUser(board, user, BoardUserRoleEnum.MEMBER);
            boardUserRepository.save(boardUsermember);
        }

        // 유저가 초대받은 링크를 클릭하여 프로젝트에 참여할때 보드에 존재하는 유저들에게 알림 전송
        String notificationContent = user.getNickname() + "님이 \"" + board.getTitle() + "\" 프로젝트에 참여하셨습니다.";

        List<BoardUser> boardUsers = boardUserRepository.findAllByBoardId(boardId);
        for(BoardUser boardUsers2 : boardUsers){
            User members = boardUsers2.getUser();
            if(!members.getId().equals(user.getId())){
                notificationService.addUserAndSendNotification(members.getId(), boardId, notificationContent);
            }
        }

        if (userDetails != null) {
            if (userDetails.getUser().getUsername().equals(username)) {
                // 해당 보드 페이지로 넘어가도록
                return "redirect:/api/boards/" + boardId;
            }
        }

        // 로그인 되어있지 않다면 로그인 페이지로 넘어가도록 설정 (html)
        return "login-page";
    }

    /**
     * 프로젝트(보드) 만료 알림 전송
     */
    @Scheduled(fixedDelay = 21600000) // 6시간 마다 알림
    public void sendExpirationNotifications() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Board> boards = boardRepository.findByExpiredAt(tomorrow);

        for (Board board : boards) {
            String notificationContent = "보드 \"" + board.getTitle() + "\" 가 마감까지 얼마남지 않았습니다. (24시간 미만)";
            List<BoardUser> boardUsers = boardUserRepository.findAllByBoardId(board.getId());
            for (BoardUser boardUser : boardUsers) {
                notificationService.boarddDeadlineNotification(boardUser.getUser().getId(), board.getId(), notificationContent);
            }
        }
    }

    /**
     * 프로젝트(보드) 유저 리스트
     * @param boardId: 유저 목록을 찾을 보드의 ID
     * @return: 보드에 참여하고 있는 유저 리스트
     */
    public List<BoardUserResponseDto> findBoardUsers(Long boardId) {
        Board board = findBoard(boardId);
        return boardUserRepository.findAllByBoard(board).stream().map(BoardUserResponseDto::new).toList();
    }

    /**
     * 프로젝트(보드) 떠나기
     * @param user: 떠나는 user
     * @param boardId: 떠날 프로젝트(보드)의 ID
     * @return: 메세지와 상태코드 반환
     */
    public ResponseEntity<String> leaveBoard(User user, Long boardId) {
        Board board = findBoard(boardId);
        BoardUser boardUser = findBoardUser(board, user);

        if (boardUser.getRole().equals(BoardUserRoleEnum.MANAGER)) {
            return ResponseEntity.badRequest().body("매니저는 프로젝트를 떠날 수 없습니다");
        }

        // 해당 유저 삭제
        boardUserRepository.delete(boardUser);
        return ResponseEntity.ok().body("프로젝트 떠나기 완료");
    }

    /**
     * 프로젝트(보드)에 초대할 유저(username 혹은 nickname) 검색
     * @param searching: 검색어 (검색할 username 혹은 nickname)
     * @return: 결과를 Slice에 담아서 반환
     */
    public Slice<BoardUserResponseDto> search(String searching, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findByUsernameOrNickname(searching, pageable);
    }

    /**
     * 프로젝트(보드) ID로 프로젝트(보드)를 찾음
     * @param boardId: 찾을 프로젝트(보드)의 ID
     * @return: Board를 반환
     */
    public Board findBoard(Long boardId) {
        return boardRepository.findById(boardId).orElseThrow(IllegalArgumentException::new);
    }

    /**
     * 프로젝트(보드)와 사용자로 BoardUser를 찾음
     * @param board: 찾을 프로젝트(보드)
     * @param user: 찾을 사용자
     * @return: BoardUser를 반환
     */
    public BoardUser findBoardUser(Board board, User user) {
        return boardUserRepository.findByBoardAndUser(board, user).orElseThrow(IllegalArgumentException::new);
    }

    /**
     * 사용자를 찾음
     * @param username: 사용자 username으로 사용자를 찾음
     * @return: User를 반환
     */
    public User findUser(String username) {
        return userRepository.findByUsername(username).orElseThrow(IllegalArgumentException::new);
    }
}
