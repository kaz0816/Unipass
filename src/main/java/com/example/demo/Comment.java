package com.example.demo;

import jakarta.persistence.*;

@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 500)
    private String content;

    public Comment() {}

    public Comment(Review review, User user, String content) {
        this.review = review;
        this.user = user;
        this.content = content;
    }

    public Long getId() { return id; }
    public Review getReview() { return review; }
    public User getUser() { return user; }
    public String getContent() { return content; }

    public void setReview(Review review) { this.review = review; }
    public void setUser(User user) { this.user = user; }
    public void setContent(String content) { this.content = content; }
}
