package com.example.demo.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.datatransfer.FlavorEvent;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    @NotBlank(message="full name is required")
    @Size(min=3, max = 100, message = "name product must be between 3 to 100")
    @JsonProperty("product_name")
    private String name;

    @Min(value=0, message="price must be greater than 0")
    @Max(value=10000000, message="price must be less than 10000000")
    @NotNull(message="price is required")
    private Float price;

    private String thumnail;

    @JsonProperty("product_description")
    private String description;

    @JsonProperty("category_id")
    private int categotyId;

    private List<MultipartFile> file;
}
