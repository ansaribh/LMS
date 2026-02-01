package com.lms.search.repository;

import com.lms.search.document.UserDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSearchRepository extends ElasticsearchRepository<UserDocument, String> {
    Page<UserDocument> findByUsernameContainingOrFirstNameContainingOrLastNameContaining(
            String username, String firstName, String lastName, Pageable pageable);
    Page<UserDocument> findByRolesContaining(String role, Pageable pageable);
}
