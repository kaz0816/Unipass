package com.example.demo;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewApiController {

    private final ReviewService service;

    public ReviewApiController(ReviewService service) {
        this.service = service;
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
        service.deleteById(id);
    }
}
