package com.example.likecomment.Service;

import com.example.likecomment.Client.UserClient;
import com.example.likecomment.Dto.UserDto;
import com.example.likecomment.Entity.CommentEntity;
import com.example.likecomment.Repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private CommentRepository commentRepository;

    private Map<String, UserDto> userCache = new HashMap<>();

    public CommentEntity createComment(CommentEntity commentEntity) {
        return commentRepository.save(commentEntity);
    }

    public Optional<CommentEntity> getCommentById(Long commentId) {
        return commentRepository.findById(commentId);
    }

    public Optional<CommentEntity> updateComment(Long commentId, CommentEntity updatedComment) {
        return commentRepository.findById(commentId).map(comment -> {
            comment.setPostingId(updatedComment.getPostingId());
            comment.setUserId(updatedComment.getUserId());
            comment.setRate(updatedComment.getRate());
            comment.setDiff(updatedComment.getDiff());
            comment.setComment(updatedComment.getComment());
            comment.setCommentimg(updatedComment.getCommentimg());
            return commentRepository.save(comment);
        });
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    public List<CommentEntity> getCommentsByUserId(String userId) {
        return commentRepository.findByUserId(userId);
    }

    private void loadAllUsers() {
        if (userCache.isEmpty()) {
            List<UserDto> users = userClient.getAllUsers();
            userCache = users.stream()
                    .collect(Collectors.toMap(UserDto::getUserId, user -> user));
        }
    }

    public UserDto getUserInfo(String userId) {
        loadAllUsers();
        return userCache.getOrDefault(userId, null);
    }

    public List<Map<String, Object>> getCoomentsByPostingId(String postingId) {
        List<CommentEntity> comments = commentRepository.findCommentsByPostingId(postingId);
        if (comments == null || comments.isEmpty()) {
            return null;
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (CommentEntity comment : comments) {
            UserDto user = getUserInfo(comment.getUserId());
            Map<String, Object> commentWithUserDetails = Map.of(
                    "comment", comment,
                    "userName", user != null ? user.getName() : "Unknown User",
                    "userProfile", user != null ? user.getPhoto() : "/assets/cha.png"
            );
            result.add(commentWithUserDetails);
        }
        return result;
    }
}
