package com.example.demo;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // 平均値計算などで使う（ソートなし）
    List<Review> findByCourseNameContainingIgnoreCase(String keyword);

    // 授業名・教員名の完全一致検索（詳細絞り込み用）
    List<Review> findByCourseName(String courseName);
    List<Review> findByTeacherName(String teacherName);

    // ▼ 検索＋並び替え用（ソート付き）
    List<Review> findByCourseNameContainingIgnoreCase(String keyword, Sort sort);

    List<Review> findByTeacherNameContainingIgnoreCase(String keyword, Sort sort);

    List<Review> findByCourseNameContainingIgnoreCaseOrTeacherNameContainingIgnoreCase(
            String courseKeyword,
            String teacherKeyword,
            Sort sort
    );

    List<Review> findByUniversityContainingIgnoreCase(String keyword, Sort sort);

    List<Review> findByCourseNameContainingIgnoreCaseOrTeacherNameContainingIgnoreCaseOrUniversityContainingIgnoreCase(
            String courseKeyword,
            String teacherKeyword,
            String universityKeyword,
            Sort sort
    );

    // マイページ用
    List<Review> findByUser(User user);
}
