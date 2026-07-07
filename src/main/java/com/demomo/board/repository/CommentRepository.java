package com.demomo.board.repository;

import com.demomo.board.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 게시글에 달린 댓글만 싹 다 가져오고 싶을 때 사용해
    // JPA가 메서드 이름을 보고 "아, Board의 ID로 찾으라는 거구나!" 하고 쿼리를 자동으로 짜줘.
    List<Comment> findAllByBoardId(Long boardId);

    void deleteAllByBoardId(Long boardId);

    @Query("select c.board.id as boardId, count(c.id) as commentCount from Comment c group by c.board.id")
    List<CommentCount> countAllGroupedByBoard();

    interface CommentCount {
        Long getBoardId();
        long getCommentCount();
    }
}
