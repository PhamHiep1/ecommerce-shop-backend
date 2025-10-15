package com.example.demo.controllers;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import com.example.demo.dtos.CategoryDTO;
import com.example.demo.dtos.ProductDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

//@Validated
@RestController
@RequestMapping("api/v1/products")
public class ProductController {

    @GetMapping("")
    public ResponseEntity<String> getAllProducts(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit
    ){
        return ResponseEntity.ok(String.format("getAllCategories, page = %d, limit = %d", page, limit));
    }


    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> insertProduct(
            @Valid @ModelAttribute ProductDTO productDTO,
            BindingResult result
            //@RequestPart("file") MultipartFile file
            ){
        try {
            if(result.hasErrors()){
                List<String> errorMessage = result.getFieldErrors()
                        .stream().map(FieldError::getDefaultMessage).toList();
                return ResponseEntity.badRequest().body(errorMessage);
            }
           List< MultipartFile> files = productDTO.getFile();
            files = files==null? new ArrayList<MultipartFile>() : files;
            for(MultipartFile file : files){
                if(file.getSize() > 10 * 1024 * 1024){// > 10MB
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("size of file must be less than 10MB");
                }

                String contentType = file.getContentType();
                if(contentType==null || contentType.startsWith("images/")){
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("file must be image");
                }
                String thumbnail = storeFile(file);
            }

            return  ResponseEntity.ok("create product successfully");
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        String fileName= StringUtils.cleanPath(file.getOriginalFilename());

        String uniqueFileName=UUID.randomUUID().toString()+fileName;

        Path uploadDir = Paths.get("uploads");

        if(!Files.exists(uploadDir)){
            Files.createDirectories(uploadDir);
        }

        Path destination = Paths.get(uploadDir.toString(),uniqueFileName);

        Files.copy(file.getInputStream(),destination, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFileName;
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable Long id){
        return ResponseEntity.ok("this is updateCategory = "+id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id){
        return ResponseEntity.ok("deleteCategory with id = "+ id);
    }
}
