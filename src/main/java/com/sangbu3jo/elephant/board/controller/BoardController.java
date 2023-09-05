package com.sangbu3jo.elephant.board.controller;

import com.sangbu3jo.elephant.board.dto.BoardOneResponseDto;
import com.sangbu3jo.elephant.board.dto.BoardRequestDto;
import com.sangbu3jo.elephant.board.dto.BoardResponseDto;
import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.board.service.BoardService;
import com.sangbu3jo.elephant.boarduser.dto.BoardUserResponseDto;
import com.sangbu3jo.elephant.columns.dto.ColumnsResponseDto;
import com.sangbu3jo.elephant.columns.service.ColumnsService;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j(topic = "프로젝트(보드) 컨트롤러")
@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final ColumnsService columnsService;

    /**
     * Model: JSP에 컨트롤러에서 생성된 데이터를 담아 전달하는 역할입니다.
     *        이를 이용해 JSP와 같은 뷰(View)로 전달해야 하는 데이터를 담아서 보낼 수 있습니다.
     *        메서드의 파라미터에 Model 타입이 지정된 경우라면, Spring은 특별히 Model 타입의 객체를 만들어 메서드에 주입합니다.
     */

    /**
     * 프로젝트(보드) 생성
     * @param userDetails: 로그인한 사용자인지 확인하기 위함 (JWT로 검증)
     * @param boardRequestDto: 보드 제목, 보드 내용, 보드 마감일을 받아옴
     * @return: 생성된 보드에 대한 내용(BoardResponseDto)과 상태 코드 전달
     */
    @ResponseBody
    @PostMapping("/boards")
    public ResponseEntity<BoardResponseDto> createBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                        @RequestBody BoardRequestDto boardRequestDto) {
        log.info("보드 생성 시도");
        BoardResponseDto boardResponseDto = boardService.createBoard(userDetails.getUser(), boardRequestDto);
        return ResponseEntity.ok().body(boardResponseDto);
    }

    /**
     * 프로젝트(보드)
     * @param userDetails: 로그인한 사용자인지 확인하기 위함 (JWT로 검증)
     * @param pageNo: 페이징 될 페이지 번호
     * @param pageable: 페이징을 위한 정보를 담은 인터페이스
     * @param model: Model에 프론트로 전송할 데이터를 담아서 보냄
     * @return: 반환할 HTML 페이지
     */
    @GetMapping("/boards")
    public String getBoardList(@AuthenticationPrincipal UserDetailsImpl userDetails,
                               @RequestParam(required = false, defaultValue = "0", value = "page") Integer pageNo,
                               Pageable pageable, Model model) {
        log.info("보드 전체 조회 시도");
        pageNo = (pageNo == 0) ? 0 : (pageNo - 1);
        log.info(pageNo.toString());
        Page<BoardResponseDto> boardList = boardService.getBoards(userDetails.getUser(), pageable, pageNo);
        model.addAttribute("boards", boardList);
        checkAdmin(model,userDetails);
        return "boards";
    }

    /**
     * 프로젝트(보드) 단건 조회
     * @param userDetails: 로그인한 사용자인지 확인하기 위함 (JWT로 검증)
     * @param board_id: URL에 매핑되어 있는 프로젝트(보드)의 ID 값
     * @param model: Model에 프론트로 전송할 데이터를 담아서 보냄
     * @return: 반환할 HTML 페이지
     */
    @GetMapping("/boards/{board_id}")
    public String getOneBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                              @PathVariable Long board_id, Model model) {
        log.info("보드 단건 조회 시도");
        BoardOneResponseDto boardOneResponseDto = boardService.getOneBoard(userDetails.getUser(), board_id);
        model.addAttribute("board", boardOneResponseDto);
        List<ColumnsResponseDto> columns = columnsService.findColumnsList(board_id);
        model.addAttribute("columns", columns);
        List<BoardUserResponseDto> boardUsers = boardService.findBoardUsers(board_id);
        model.addAttribute("boardUsers", boardUsers);
        checkAdmin(model,userDetails);
        return "board";

    }

    /**
     * 프로젝트(보드) 수정
     * @param userDetails: 프로젝트(보드)를 만든 사용자인지 확인하기 위함 (JWT로 검증)
     * @param board_id: URL에 매핑되어 있는 프로젝트(보드)의 ID 값
     * @param boardRequestDto: 수정할 보드 제목, 보드 내용, 보드 마감일을 받아옴
     * @return: 수정된 보드에 대한 내용(BoardResponseDto)과 상태 코드 전달
     */
    @ResponseBody
    @PutMapping("/boards/{board_id}")
    public ResponseEntity<BoardResponseDto> updateBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                        @PathVariable Long board_id,
                                                        @RequestBody BoardRequestDto boardRequestDto) {
        log.info("보드 수정 시도");

        BoardResponseDto boardResponseDto = boardService.updateBoard(board_id, userDetails.getUser(), boardRequestDto);
        return ResponseEntity.ok().body(boardResponseDto);
    }

    /**
     * 프로젝트(보드) 삭제
     * @param userDetails: 프로젝트(보드)를 만든 사용자인지 확인하기 위함 (JWT로 검증)
     * @param board_id: URL에 매핑되어 있는 프로젝트(보드)의 ID 값
     * @return: 삭제된 이후의 상태 코드 전달
     */
    @ResponseBody
    @DeleteMapping("/boards/{board_id}")
    public ResponseEntity<String> deleteBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @PathVariable Long board_id) {
        log.info("보드 삭제 시도");
        try {
            boardService.deleteBoard(board_id, userDetails.getUser());
            return ResponseEntity.ok().body("보드 삭제 완료");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("보드 매니저만 삭제 가능합니다");
        }
    }

    /**
     * 프로젝트(보드)에 유저 초대
     * @param userDetails: 로그인한 사용자인지 확인하기 위함 (JWT로 검증)
     * @param board_id: URL에 매핑되어 있는 프로젝트(보드)의 ID 값
     * @param member: 초대할 member의 이름 (username)
     * @return: 이후에 연결할 HTML 파일 혹은 redirect 링크
     */
    @GetMapping("/boards/{board_id}/member")
    public String inviteUser(@AuthenticationPrincipal UserDetailsImpl userDetails,
                             @PathVariable Long board_id, @RequestParam String member) {
        log.info("유저 초대 시도");
        return boardService.inviteUser(userDetails, board_id, member);
    }

    /**
     * 프로젝트(보드) 떠나기
     * @param userDetails: 로그인한 사용자인지 확인하기 위함 (JWT로 검증)
     * @param board_id: URL에 매핑되어 있는 프로젝트(보드)의 ID 값
     * @return: 프로젝트(보드)를 떠난 이후의 상태 코드 전달
     */
    @DeleteMapping("/boards/{board_id}/member")
    public ResponseEntity<String> leaveBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
                             @PathVariable Long board_id) {
        log.info("유저 초대 시도");
        return boardService.leaveBoard(userDetails.getUser(), board_id);
    }

    /**
     * 프로젝트(보드)에 초대할 유저(username 혹은 nickname) 검색
     * @param searching: 검색어 (검색할 username 혹은 nickname => 3자 이상만 넘어옴)
     * @return: 검색어에 해당하는 user 정보를 Slice에 담아 상태코드와 전달
     */
    @GetMapping("/boards/search/{searching}")
    public ResponseEntity<Slice<BoardUserResponseDto>> findUsersToInvite(@PathVariable String searching,
                                                                         @RequestParam(value = "page", defaultValue = "0") int page,
                                                                         @RequestParam(value = "size", defaultValue = "5") int size) {

        Slice<BoardUserResponseDto> users = boardService.search(searching, page, size);
        return ResponseEntity.ok().body(users);
    }

    /**
     * 프로젝트(보드)에 존재하는 카드를 달력에 표시 (마감일 기준)
     * @param board_id: URL에 매핑되어 있는 프로젝트(보드)의 ID 값
     * @param model: Model에 프론트로 전송할 데이터를 담아서 보냄
     * @return: 반환할 HTML 페이지
     */
    @GetMapping("/boards/calendar/{board_id}")
    public String showCalendar(@PathVariable Long board_id, Model model) {
        Board board = boardService.findBoard(board_id);
        BoardResponseDto boardResponseDto = new BoardResponseDto(board);
        model.addAttribute("board", boardResponseDto);
        return "cardCalendar";
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
