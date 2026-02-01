package com.lms.search.dto;

import com.lms.search.document.CourseDocument;
import com.lms.search.document.UserDocument;
import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SearchResult {
    private List<CourseDocument> courses;
    private List<UserDocument> users;
    private long totalCourses;
    private long totalUsers;
}
