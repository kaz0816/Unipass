package com.example.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private static final int SUGGEST_LIMIT = 8;

    private final ReviewRepository repository;
    private final CommentRepository commentRepository;
    private final List<String> passnaviSchoolNames;

    public ReviewService(ReviewRepository repository,
                         CommentRepository commentRepository,
                         ObjectMapper objectMapper) {
        this.repository = repository;
        this.commentRepository = commentRepository;
        this.passnaviSchoolNames = loadPassnaviSchoolNames(objectMapper);
    }

    // =========================
    // 検索 ＋ 並び替え
    // =========================
    public List<Review> search(String keyword, String target, String sortKey) {
        Sort sort = createSort(sortKey);

        // キーワード無し → 全件
        if (keyword == null || keyword.isBlank()) {
            return repository.findAll(sort);
        }

        // 対象によって分岐
        switch (target) {
            case "course":
                return repository.findByCourseNameContainingIgnoreCase(keyword, sort);
            case "teacher":
                return repository.findByTeacherNameContainingIgnoreCase(keyword, sort);
            case "university":
                return repository.findByUser_UniversityContainingIgnoreCase(keyword, sort);
            default: // all
                return repository
                        .findByCourseNameContainingIgnoreCaseOrTeacherNameContainingIgnoreCaseOrUser_UniversityContainingIgnoreCase(
                                keyword, keyword, keyword, sort);
        }
    }

    // 並び替え条件
    private Sort createSort(String sortKey) {
        return switch (sortKey) {
            case "rating" -> Sort.by(Sort.Direction.DESC, "rating"); // 評価順
            case "likes"  -> Sort.by(Sort.Direction.DESC, "likes");  // いいね順
            default       -> Sort.by(Sort.Direction.DESC, "id");     // 新しい順
        };
    }

    // =========================
    // 基本的な CRUD
    // =========================
    public void addReview(Review review) {
        repository.save(review);
    }

    public void save(Review review) {
        repository.save(review);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public Review getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found: " + id));
    }

    // =========================
    // いいね
    // =========================
    public void like(Integer id) {
        Review r = getById(id);
        r.incrementLikes();
        repository.save(r);
    }

    // =========================
    // 平均値計算
    // =========================
    public double getAverage(String courseName) {
        List<Review> list = repository.findByCourseNameContainingIgnoreCase(courseName);
        if (list.isEmpty()) return 0;

        double sum = 0;
        for (Review r : list) {
            sum += r.getRating();
        }
        return sum / list.size();
    }

    public RatingSummary buildRatingSummary(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return new RatingSummary(0, 0, List.of());
        }

        Map<Double, Long> counter = new LinkedHashMap<>();
        for (double score = 5.0; score >= 0.5; score -= 0.5) {
            counter.put(score, 0L);
        }

        double sum = 0;
        for (Review review : reviews) {
            double normalized = normalizeRating(review.getRating());
            sum += normalized;
            counter.computeIfPresent(normalized, (k, v) -> v + 1);
        }

        long total = reviews.size();
        List<RatingBreakdown> breakdowns = new ArrayList<>();
        for (Map.Entry<Double, Long> entry : counter.entrySet()) {
            long count = entry.getValue();
            double percentage = total == 0 ? 0 : (count * 100.0) / total;
            breakdowns.add(new RatingBreakdown(entry.getKey(), count, percentage));
        }

        double average = sum / total;
        return new RatingSummary(average, total, breakdowns);
    }

    private double normalizeRating(double rating) {
        double clamped = Math.max(0.5, Math.min(5.0, rating));
        return Math.round(clamped * 2.0) / 2.0;
    }

    public static class RatingSummary {
        private final double average;
        private final long totalCount;
        private final List<RatingBreakdown> breakdowns;

        public RatingSummary(double average, long totalCount, List<RatingBreakdown> breakdowns) {
            this.average = average;
            this.totalCount = totalCount;
            this.breakdowns = breakdowns;
        }

        public double getAverage() {
            return average;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public List<RatingBreakdown> getBreakdowns() {
            return breakdowns;
        }
    }

    public static class RatingBreakdown {
        private final double rating;
        private final long count;
        private final double percentage;

        public RatingBreakdown(double rating, long count, double percentage) {
            this.rating = rating;
            this.count = count;
            this.percentage = percentage;
        }

        public double getRating() {
            return rating;
        }

        public long getCount() {
            return count;
        }

        public double getPercentage() {
            return percentage;
        }
    }

    // =========================
    // コメント関連
    // =========================
    public List<Comment> getComments(Review review) {
        return commentRepository.findByReviewAndParentCommentIsNullOrderByIdAsc(review);
    }

    public void addComment(Review review, User user, String content, String university, String faculty, String department) {
        Comment c = new Comment(review, user, content, university, faculty, department);
        commentRepository.save(c);
    }

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + id));
    }

    public void addReply(Review review, Comment parent, User user, String content, String university, String faculty, String department) {
        Comment c = new Comment(review, user, content, university, faculty, department);
        c.setParentComment(parent);
        commentRepository.save(c);
    }

    // =========================
    // 絞り込み用（授業名・教員名・ユーザー）
    // =========================
    public List<Review> getByCourseName(String courseName) {
        return repository.findByCourseName(courseName);
    }

    public List<Review> getByTeacherName(String teacherName) {
        return repository.findByTeacherName(teacherName);
    }

    public List<Review> getByUser(User user) {
        return repository.findByUser(user);
    }
    public List<Review> getAll() {
        return repository.findAll();
    }

    public List<String> suggestUniversities(String query) {
        if (query == null) {
            return List.of();
        }

        String normalized = query.trim();
        if (normalized.isEmpty()) {
            return List.of();
        }

        String lowerQuery = normalized.toLowerCase(Locale.ROOT);
        List<String> prefixMatches = passnaviSchoolNames.stream()
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(lowerQuery))
                .limit(SUGGEST_LIMIT)
                .toList();

        if (!prefixMatches.isEmpty()) {
            return prefixMatches;
        }

        return passnaviSchoolNames.stream()
                .filter(name -> name.toLowerCase(Locale.ROOT).contains(lowerQuery))
                .limit(SUGGEST_LIMIT)
                .toList();
    }

    private List<String> loadPassnaviSchoolNames(ObjectMapper objectMapper) {
        try {
            ClassPathResource resource = new ClassPathResource("passnavi_schools.json");
            return objectMapper.readValue(resource.getInputStream(), new TypeReference<List<String>>() {});
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load passnavi_schools.json", e);
        }
    }

}
