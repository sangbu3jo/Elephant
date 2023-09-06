package com.sangbu3jo.elephant.users.custom;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sangbu3jo.elephant.boarduser.dto.BoardUserResponseDto;
import com.sangbu3jo.elephant.users.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

import static com.sangbu3jo.elephant.users.entity.QUser.user;

public class UserRepositorySupportImpl extends QuerydslRepositorySupport implements UserRepositorySupport {

    private final JPAQueryFactory jpaQueryFactory;

    public UserRepositorySupportImpl(JPAQueryFactory jpaQueryFactory) {
        super(User.class);
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public Slice<BoardUserResponseDto> findByUsernameOrNickname(String search, Pageable pageable) {
        QueryResults<BoardUserResponseDto> list = jpaQueryFactory
                .select(Projections.constructor(BoardUserResponseDto.class, user.username, user.nickname))
                .from(user)
                .where(
                        user.username.contains(search)
                                .or(user.nickname.contains(search))
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<BoardUserResponseDto> content = list.getResults();
        long total = list.getTotal();

        return new SliceImpl<>(content, pageable, total != pageable.getOffset() + content.size());

    }
}
