package com.example.bookbuddyproject.web;

import com.example.bookbuddyproject.domain.BookCondition;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookForm {

    private Long id;  // 수정 시 사용

    @NotEmpty(message = "도서명은 필수입니다")
    private String title;

    @NotEmpty(message = "저자는 필수입니다")
    private String author;

    private String publisher;

    @NotNull(message = "가격은 필수입니다")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다")
    private Integer price;

    private String category;

    @NotNull(message = "도서 상태는 필수입니다")
    private BookCondition bookCondition;

    private String description;
}
