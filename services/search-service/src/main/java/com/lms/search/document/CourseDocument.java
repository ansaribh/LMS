package com.lms.search.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.math.BigDecimal;
import java.util.List;

@Document(indexName = "courses")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CourseDocument {
    @Id private String id;
    @Field(type = FieldType.Text, analyzer = "standard") private String title;
    @Field(type = FieldType.Text, analyzer = "standard") private String description;
    @Field(type = FieldType.Text) private String shortDescription;
    @Field(type = FieldType.Keyword) private String instructorId;
    @Field(type = FieldType.Text) private String instructorName;
    @Field(type = FieldType.Keyword) private String status;
    @Field(type = FieldType.Keyword) private String difficulty;
    @Field(type = FieldType.Keyword) private List<String> tags;
    @Field(type = FieldType.Double) private BigDecimal price;
    @Field(type = FieldType.Integer) private Integer enrollmentCount;
    @Field(type = FieldType.Double) private Double averageRating;
    @Field(type = FieldType.Text) private String thumbnailUrl;
}
