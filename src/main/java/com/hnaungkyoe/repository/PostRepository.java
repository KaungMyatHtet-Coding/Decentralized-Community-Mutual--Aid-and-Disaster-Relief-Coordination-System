package com.hnaungkyoe.repository;

import com.hnaungkyoe.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // PUBLISHED post တွေသာ newest first ဆွဲမည်
    List<Post> findByStatusOrderByCreatedAtDesc(Post.PostStatus status);

    // Admin အတွက် — post အကုန် newest first
    List<Post> findAllByOrderByCreatedAtDesc();

    // Township အလိုက် (နှင့် Global) post အကုန်
    List<Post> findByTownshipInOrderByCreatedAtDesc(List<String> townships);
}