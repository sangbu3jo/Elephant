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
import com.sangbu3jo.elephant.notification.entity.Notification;
import com.sangbu3jo.elephant.notification.repository.NotificationRepository;
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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j(topic = "보드 서비스")
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardUserRepository boardUserRepository;
    private final UserRepository userRepository;
    private final JPAQueryFactory jpaQueryFactory;
    private final NotificationService notificationService;
    @Autowired
    private final MongoTemplate mongoTemplate;

    // 보드 생성
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

    public BoardOneResponseDto getOneBoard(User user, Long boardId) {
        log.info("보드 수정");
        Board board = findBoard(boardId);
        BoardUser boardUser = findBoardUser(board, user); // 해당 사용자가 보드의 참여자인지 아닌지 확인

        return new BoardOneResponseDto(board);
    }
    // 보드 조회 (사용자 것만)

    public Page<BoardResponseDto> getBoards(User user, Pageable pageable, Integer pageNo) {
        log.info("보드 전체 조회");
        Sort sort = Sort.by(Sort.Direction.DESC, "expiredAt");
        pageable = PageRequest.of(pageNo, 8, sort);

        Page<Board> boards = boardRepository.findAllByIdIn(boardUserRepository.findAllByUser(user).stream().map(BoardUser::getBoard).map(Board::getId).toList(),
                pageable);
        return boards.map(BoardResponseDto::new);
    }
    // 보드 수정 (참여자/매니저)

    @Transactional
    public BoardResponseDto updateBoard(Long boardId, User user, BoardRequestDto boardRequestDto) {
        log.info("보드 수정");
        Board board = findBoard(boardId);
        BoardUser boardUser = findBoardUser(board, user); // 해당 사용자가 보드의 참여자인지 아닌지 확인

        board.updateBoard(boardRequestDto);

        return new BoardResponseDto(board);
    }

    public void deleteBoard(Long boardId, User user) {
        log.info("보드 삭제");
        Board board = findBoard(boardId);

        BoardUser boardUser = findBoardUser(board, user);

        if (! boardUser.getRole().equals(BoardUserRoleEnum.MANAGER) ) {
            throw new IllegalArgumentException();
        }

        mongoTemplate.getCollection(board.getId().toString()).drop();

        /* Board 엔티티 안에 boarduser set 을 orphanremoval = true 속성을 주었기 때문에,
         *  해당 레포지토리에서 따로 찾아서 삭제해줄 필요 없음
         *  채팅방 또한 1:1 매핑으로 cascade=CascadeType.REMOVAL 을 주었음 */
        boardRepository.delete(board);
    }

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

    // 보드 만료 알림 전송
    @Scheduled(fixedDelay = 21600000) // 6시간 마다 알림
    public void sendExpirationNotifications() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Board> boards = boardRepository.findByExpiredAt(tomorrow);


        for (Board board : boards) {
                String notificationContent = "보드 \"" + board.getTitle() + "\" 가 마감까지 얼마남지 않았습니다. (24시간 미만)";
                List<BoardUser> boardUsers = boardUserRepository.findAllByBoardId(board.getId());
                for (BoardUser boardUser : boardUsers) {
                    notificationService.addUserAndSendNotification(boardUser.getUser().getId(), board.getId(), notificationContent);
                }
            }
        }


    // 프로젝트 유저 리스트
    public List<BoardUserResponseDto> findBoardUsers(Long boardId) {
        Board board = findBoard(boardId);
        return boardUserRepository.findAllByBoard(board).stream().map(BoardUserResponseDto::new).toList();
    }

    // 프로젝트 유저 삭제
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

    // 유저를 검색함
//    public Slice<User> search(String searching) {
//        QUser user = QUser.user;
//        return jpaQueryFactory
//                .select(user)
//                .from(user)
//                .where(
//                        user.username.contains(searching)
//                        .or(user.nickname.contains(searching))
//                )
//                .fetch();
//    }

    public Slice<BoardUserResponseDto> search(String searching) {
        Pageable pageable = PageRequest.of(0, 5);

        QUser user = QUser.user;
        QueryResults<BoardUserResponseDto> queryResults = jpaQueryFactory
                .select(Projections.constructor(BoardUserResponseDto.class, user.username, user.nickname))
                .from(user)
                .where(
                        user.username.contains(searching)
                        .or(user.nickname.contains(searching))
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<BoardUserResponseDto> content = queryResults.getResults();
        long total = queryResults.getTotal();


        return new SliceImpl<>(content, pageable, total != pageable.getOffset() + content.size());
    }


    public Board findBoard(Long boardId) {
        return boardRepository.findById(boardId).orElseThrow(IllegalArgumentException::new);
    }

    public BoardUser findBoardUser(Board board, User user) {
        return boardUserRepository.findByBoardAndUser(board, user).orElseThrow(IllegalArgumentException::new);
    }

    public User findUser(String username) {
        return userRepository.findByUsername(username).orElseThrow(IllegalArgumentException::new);
    }
}
