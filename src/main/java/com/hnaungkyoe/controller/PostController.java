package com.hnaungkyoe.controller;

import com.hnaungkyoe.entity.Post;
import com.hnaungkyoe.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    // GET /api/posts — PUBLIC (Token မလို)
    // NewsFeed.jsx က ဒီ endpoint ကို သုံးမည်
    @GetMapping
    public ResponseEntity<List<Post>> getPublishedPosts() {
        return ResponseEntity.ok(postService.getPublishedPosts());
    }

    // GET /api/posts/all — ADMIN ONLY
    // ManagePosts.jsx က ဒီ endpoint ကို သုံးမည်
    @GetMapping("/all")
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    // GET /api/posts/{id} — PUBLIC
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    // POST /api/posts — ADMIN ONLY
    // Body: { title, content, imageUrl, status, authorId }
    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Map<String, Object> body) {
        Post post = new Post();
        post.setTitle((String) body.get("title"));
        post.setContent((String) body.get("content"));
        post.setImageUrl((String) body.get("imageUrl"));

        String statusStr = (String) body.get("status");
        post.setStatus(statusStr != null
                ? Post.PostStatus.valueOf(statusStr)
                : Post.PostStatus.PUBLISHED);

        Long authorId = Long.valueOf(body.get("authorId").toString());
        return ResponseEntity.ok(postService.createPost(post, authorId));
    }

    // PUT /api/posts/{id} — ADMIN ONLY
    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        Post updated = new Post();
        updated.setTitle((String) body.get("title"));
        updated.setContent((String) body.get("content"));
        updated.setImageUrl((String) body.get("imageUrl"));

        String statusStr = (String) body.get("status");
        updated.setStatus(statusStr != null
                ? Post.PostStatus.valueOf(statusStr)
                : Post.PostStatus.PUBLISHED);

        return ResponseEntity.ok(postService.updatePost(id, updated));
    }

    // DELETE /api/posts/{id} — ADMIN ONLY
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok("Post deleted successfully.");
    }
}