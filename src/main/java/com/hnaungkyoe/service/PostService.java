package com.hnaungkyoe.service;

import com.hnaungkyoe.entity.Post;
import com.hnaungkyoe.entity.User;
import com.hnaungkyoe.repository.PostRepository;
import com.hnaungkyoe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    // === PUBLIC — PUBLISHED post တွေသာ ===
    public List<Post> getPublishedPosts() {
        return postRepository.findByStatusOrderByCreatedAtDesc(Post.PostStatus.PUBLISHED);
    }

    // === ADMIN — Post အကုန် ===
    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    // === ADMIN — Post တစ်ခု detail ===
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    // === ADMIN — Post အသစ် ဆောက် ===
    public Post createPost(Post post, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        post.setAuthor(author);
        return postRepository.save(post);
    }

    // === ADMIN — Post ပြင် ===
    public Post updatePost(Long id, Post updatedPost) {
        Post existing = getPostById(id);
        existing.setTitle(updatedPost.getTitle());
        existing.setContent(updatedPost.getContent());
        existing.setImageUrl(updatedPost.getImageUrl());
        existing.setStatus(updatedPost.getStatus());
        return postRepository.save(existing);
    }

    // === ADMIN — Post ဖျက် ===
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
}