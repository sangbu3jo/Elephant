package com.sangbu3jo.elephant.chat.entity;

import com.sangbu3jo.elephant.users.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name="group_chat_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupChatUser {

    /**
     * 컬럼 - 연관관계 컬럼을 제외한 컬럼을 정의합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private LocalDateTime enterTime;

    @ManyToOne
    private GroupChatRoom groupChatRoom;

    /**
     * 생성자 - 약속된 형태로만 생성가능하도록 합니다.
     */
    public GroupChatUser (User user, LocalDateTime time, GroupChatRoom groupChatRoom) {
        this.user = user;
        this.enterTime = time;
        this.groupChatRoom = groupChatRoom;
    }


    /**
     * 연관관계 - Foreign Key 값을 따로 컬럼으로 정의하지 않고 연관 관계로 정의합니다.
     */


    /**
     * 연관관계 편의 메소드 - 반대쪽에는 연관관계 편의 메소드가 없도록 주의합니다.
     */


    /**
     * 서비스 메소드 - 외부에서 엔티티를 수정할 메소드를 정의합니다. (단일 책임을 가지도록 주의합니다.)
     */
    public void updatePrivateChatRoom(GroupChatRoom groupChatRoom) {
        this.groupChatRoom = groupChatRoom;
    }
}
