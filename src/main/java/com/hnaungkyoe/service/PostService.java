package com.hnaungkyoe.service;

import com.hnaungkyoe.entity.Notification;
import com.hnaungkyoe.entity.Post;
import com.hnaungkyoe.entity.User;
import com.hnaungkyoe.repository.PostRepository;
import com.hnaungkyoe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;

    // ✅ ထည့်လိုက်
    @Autowired private NotificationService notificationService;

    public Post createPost(Post post, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        post.setAuthor(author);
        Post saved = postRepository.save(post);

        // ✅ PUBLISHED ဆိုရင်သာ users အားလုံးကို notify
        if (saved.getStatus() == Post.PostStatus.PUBLISHED) {
            notificationService.sendToAllUsers(
                    "📰 သတင်းအသစ်!",
                    "'" + saved.getTitle() + "' — ဖတ်ရှုကြည့်ပါ။",
                    Notification.Type.STATUS_CHANGED,
                    saved.getId(),
                    "POST"
            );
        }

        return saved;
    }

    public Post updatePost(Long id, Post updatedPost) {
        Post existing = getPostById(id);
        Post.PostStatus oldStatus = existing.getStatus();

        existing.setTitle(updatedPost.getTitle());
        existing.setContent(updatedPost.getContent());
        existing.setImageUrl(updatedPost.getImageUrl());
        existing.setStatus(updatedPost.getStatus());

        Post saved = postRepository.save(existing);

        // ✅ DRAFT → PUBLISHED ဖြစ်သွားရင် notify
        if (oldStatus == Post.PostStatus.DRAFT
                && saved.getStatus() == Post.PostStatus.PUBLISHED) {
            notificationService.sendToAllUsers(
                    "📰 သတင်းအသစ် ထုတ်ပြန်ပြီ!",
                    "'" + saved.getTitle() + "' — ဖတ်ရှုကြည့်ပါ။",
                    Notification.Type.STATUS_CHANGED,
                    saved.getId(),
                    "POST"
            );
        }

        return saved;
    }

    // ဒါတွေ မပြောင်းဘူး
    public List<Post> getPublishedPosts() {
        return postRepository.findByStatusOrderByCreatedAtDesc(Post.PostStatus.PUBLISHED);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
}