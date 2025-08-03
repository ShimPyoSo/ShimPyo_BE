package com.example.shimpyo.domain.course.repository;

import com.example.shimpyo.domain.course.entity.UserCourseDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCourseDetailRepository extends JpaRepository<UserCourseDetail, Long> {
}
