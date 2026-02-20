package com.example.demo;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/reviews")
public class ReviewApiController {

    private final ReviewService service;
    private final UserRepository userRepository;

    public ReviewApiController(ReviewService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    // 全件取得
    @GetMapping
    public List<Review> list() {
        return service.getAll();
    }

    // 新規作成
    @PostMapping
    public Review create(@RequestBody Review review) {
        service.save(review);
        return review;
    }

    // 1件取得
    @GetMapping("/{id}")
    public Review get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @GetMapping("/universities/suggest")
    public List<String> suggestUniversities(@RequestParam(name = "q", required = false) String query) {
        return service.suggestUniversities(query);
    }

    // 削除
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        User current = getCurrentUser();
        Review review = service.getById(id);
        if (review.getUser() == null || !review.getUser().getId().equals(current.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "自分の投稿だけ削除できます。");
        }
        service.deleteById(id);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "ログインが必要です。"));
    }
}
