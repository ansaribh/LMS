package com.lms.search.service;

import com.lms.search.document.CourseDocument;
import com.lms.search.document.UserDocument;
import com.lms.search.dto.SearchResult;
import com.lms.search.repository.CourseSearchRepository;
import com.lms.search.repository.UserSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Map;

@Slf4j @Service @RequiredArgsConstructor
public class SearchService {
    private final CourseSearchRepository courseRepository;
    private final UserSearchRepository userRepository;

    @KafkaListener(topics = "search-index-updates", groupId = "search-service")
    public void handleIndexUpdate(Map<String, Object> event) {
        log.info("Received index update event: {}", event.get("type"));
        // Handle reindexing based on event type
    }

    public SearchResult globalSearch(String query, Pageable pageable) {
        Page<CourseDocument> courses = courseRepository.findByTitleContainingOrDescriptionContaining(query, query, pageable);
        Page<UserDocument> users = userRepository.findByUsernameContainingOrFirstNameContainingOrLastNameContaining(query, query, query, pageable);
        return SearchResult.builder().courses(courses.getContent()).users(users.getContent())
                .totalCourses(courses.getTotalElements()).totalUsers(users.getTotalElements()).build();
    }

    public Page<CourseDocument> searchCourses(String query, Pageable pageable) {
        return courseRepository.findByTitleContainingOrDescriptionContaining(query, query, pageable);
    }

    public Page<UserDocument> searchUsers(String query, Pageable pageable) {
        return userRepository.findByUsernameContainingOrFirstNameContainingOrLastNameContaining(query, query, query, pageable);
    }

    public void indexCourse(CourseDocument course) {
        courseRepository.save(course);
        log.info("Course indexed: {}", course.getId());
    }

    public void indexUser(UserDocument user) {
        userRepository.save(user);
        log.info("User indexed: {}", user.getId());
    }

    public void deleteCourseIndex(String id) {
        courseRepository.deleteById(id);
        log.info("Course index deleted: {}", id);
    }

    public void deleteUserIndex(String id) {
        userRepository.deleteById(id);
        log.info("User index deleted: {}", id);
    }
}
