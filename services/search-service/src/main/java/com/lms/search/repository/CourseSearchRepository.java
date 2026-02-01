package com.lms.search.repository;

import com.lms.search.document.CourseDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseSearchRepository extends ElasticsearchRepository<CourseDocument, String> {
    Page<CourseDocument> findByTitleContainingOrDescriptionContaining(String title, String description, Pageable pageable);
    Page<CourseDocument> findByStatus(String status, Pageable pageable);
    Page<CourseDocument> findByTagsContaining(String tag, Pageable pageable);
    Page<CourseDocument> findByDifficulty(String difficulty, Pageable pageable);
    List<CourseDocument> findByInstructorId(String instructorId);
}
