package com.sangbu3jo.elephant.board.entity;

import com.sangbu3jo.elephant.board.dto.BoardRequestDto;
import com.sangbu3jo.elephant.boarduser.entity.BoardUser;
import com.sangbu3jo.elephant.chat.entity.ChatRoom;
import com.sangbu3jo.elephant.columns.entity.Columns;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.*;

// lombok
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

// jpa
@Entity
@Table(name = "board")
public class Board {
    /**
     * 컬럼 - 연관관계 컬럼을 제외한 컬럼을 정의합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "expired_at")
    private LocalDate expiredAt;

    /**
     * 생성자 - 약속된 형태로만 생성가능하도록 합니다.
     */
    @Builder
    public Board(BoardRequestDto boardRequestDto) {
        this.title = boardRequestDto.getTitle();
        this.content = boardRequestDto.getContent();
        this.expiredAt = boardRequestDto.getExpiredAt();
    }

    /**
     * 연관관계 - Foreign Key 값을 따로 컬럼으로 정의하지 않고 연관 관계로 정의합니다.
     */
    @OneToMany(mappedBy = "board", orphanRemoval = true)
    Set<BoardUser> boardUsers = new HashSet<>();

    @OneToMany(mappedBy = "board", orphanRemoval = true)
    List<Columns> columnsList = new LinkedList<>();

    @OneToOne(mappedBy = "board", cascade = CascadeType.REMOVE)
    private ChatRoom chatRoom;

    /**
     * 연관관계 편의 메소드 - 반대쪽에는 연관관계 편의 메소드가 없도록 주의합니다.
     */
    public void updateId(Long id) {
        this.id = id;
    }

    public void addBoardUser(BoardUser boardUser) {
        this.boardUsers.add(boardUser);
    }

    public void addColumns(Columns columns) {
        this.columnsList.add(columns);
    }

    /**
     * 서비스 메소드 - 외부에서 엔티티를 수정할 메소드를 정의합니다. (단일 책임을 가지도록 주의합니다.)
     */
    public void updateBoard(BoardRequestDto boardRequestDto) {
        if (boardRequestDto.getTitle() != null) {
            this.title = boardRequestDto.getTitle();
        }
        if (boardRequestDto.getContent() != null) {
            this.content = boardRequestDto.getContent();
        }
        if (boardRequestDto.getExpiredAt() != null) {
            this.expiredAt = boardRequestDto.getExpiredAt();
        }
    }

    public void removeColumns(Columns columns) {
        this.columnsList.remove(columns);
    }

    public void changeColumns(Columns columns, Integer order) {
        this.columnsList.remove(columns);
        this.columnsList.add(order, columns);
    }

    public void updateChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }


}
