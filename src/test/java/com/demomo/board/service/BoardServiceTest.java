package com.demomo.board.service;

import com.demomo.board.domain.Board;
import com.demomo.board.repository.BoardRepository;
import com.demomo.board.repository.CommentRepository;
import com.demomo.global.exception.ApiException;
import com.demomo.member.domain.Member;
import com.demomo.member.domain.Role;
import com.demomo.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {
    @Mock BoardRepository boardRepository;
    @Mock MemberRepository memberRepository;
    @Mock CommentRepository commentRepository;
    @InjectMocks BoardService boardService;

    @Test
    void deletesCommentsBeforeBoard() {
        Member author = member("author", Role.USER);
        Board board = Board.builder().title("title").content("content").member(author).build();
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(memberRepository.findByUsername("author")).thenReturn(Optional.of(author));

        boardService.delete(1L, "author");

        var order = inOrder(commentRepository, boardRepository);
        order.verify(commentRepository).deleteAllByBoardId(1L);
        order.verify(boardRepository).delete(board);
    }

    @Test
    void rejectsDeleteByAnotherUser() {
        Member author = member("author", Role.USER);
        Member stranger = member("stranger", Role.USER);
        Board board = Board.builder().title("title").content("content").member(author).build();
        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(memberRepository.findByUsername("stranger")).thenReturn(Optional.of(stranger));

        assertThatThrownBy(() -> boardService.delete(1L, "stranger"))
                .isInstanceOf(ApiException.class)
                .hasMessage("본인 또는 관리자만 삭제할 수 있습니다.");
        verifyNoInteractions(commentRepository);
    }

    private Member member(String username, Role role) {
        return Member.builder().username(username).password("encoded").role(role).build();
    }
}
