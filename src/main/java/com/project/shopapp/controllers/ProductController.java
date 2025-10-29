package com.project.shopapp.controllers;

import com.github.javafaker.Faker;
import com.project.shopapp.dtos.*;
import com.project.shopapp.exceptions.InvalidParamException;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.responses.ProductListResponse;
import com.project.shopapp.responses.ProductResponse;
import com.project.shopapp.services.IProductService;
import com.project.shopapp.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private final IProductService productService;
    @PostMapping("")
    //POST http://localhost:8088/v1/api/products
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductDTO productDTO,
            BindingResult result
    ) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }
            Product newProduct = productService.createProduct(productDTO);
            return ResponseEntity.ok(ProductResponse.fromProduct(newProduct));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping(value = "uploads/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //POST http://localhost:8088/v1/api/products
    public ResponseEntity<?> uploadImages(
            @PathVariable("id") Long productId,
            @ModelAttribute("files") List<MultipartFile> files
    ){
        try {
            Product existingProduct = productService.getProductById(productId);

            files = files == null ? new ArrayList<MultipartFile>() : files;
            List<ProductImage> productImages = new ArrayList<>();
            if(files.size() > ProductImage.MAXIMUM_IMAGE_PER_PRODUCT){
                throw new InvalidParamException("Number of images must be <= "+ ProductImage.MAXIMUM_IMAGE_PER_PRODUCT);
            }
            for (MultipartFile file : files) {
                if(file.getSize() == 0) {
                    continue;
                }


                // check file size and file format
                if(file.getSize() > 10 * 1024 * 1024) { // Kích thước > 10MB
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                            .body("File is too large! Maximum size is 10MB");
                }
                String contentType = file.getContentType();
                if(contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                            .body("File must be an image");
                }
                // Process file and thumbnail data in DTO
                String filename = storeFile(file);
                //Process product model data in DB
                ProductImage productImage = productService.createProductImage(
                        existingProduct.getId(),
                        ProductImageDTO.builder()
                                .imageUrl(filename)
                                .build()
                );
                productImages.add(productImage);
            }
            return ResponseEntity.ok().body(productImages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    private String storeFile(MultipartFile file) throws IOException {
        if(!isImage(file) || file.getOriginalFilename()==null){
            throw  new IOException("Image inavalid format");
        }

        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        // prepend UUID to file name to ensure file name is unique
        String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
        // The path to folder where you want to store file
        java.nio.file.Path uploadDir = Paths.get("uploads");
        // check and create folder if it does not exist
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        // Full path to file
        java.nio.file.Path destination = Paths.get(uploadDir.toString(), uniqueFilename);
        // copy file to destination folder
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;
    }

    public boolean isImage(MultipartFile multipartFile){
        String contentType = multipartFile.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    @GetMapping("")
    public ResponseEntity<?> getProducts(
            @RequestParam("page")     int page,
            @RequestParam("limit")    int limit
    ) {
        PageRequest pageRequest = PageRequest.
                of(page,limit, Sort.by("createdAt")
                                        .descending());

        Page<ProductResponse> productPage = productService.getAllProducts(pageRequest);
        int totalPage = productPage.getTotalPages();
        List<ProductResponse> products = productPage.getContent();

        return ResponseEntity.ok(ProductListResponse
                .builder()
                        .products(products)
                        .totalPage(totalPage)
                .build());
    }

    //http://localhost:8080/api/v1/products/6
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(
            @PathVariable("id") Long productId
    ) {
        try{
            Product product = productService.getProductById(productId);
            return ResponseEntity.ok(ProductResponse.fromProduct(product));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("cannot find product with id = "+ productId);
        }

    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProductById(
            @PathVariable("id") Long productId,
           @RequestBody ProductDTO productDTO

    ) {
        try {

            productService.updateProduct(productId,productDTO);
            return ResponseEntity.ok(String.format("update product with id = %d successfully ",productId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable long id) {
        try{
            productService.deleteProduct(id);
            return ResponseEntity.ok(String.format("Delete product with id = %d successfully",id));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(String.format("Delete product with id = %d successfully",id));
        }

    }

    @PostMapping("/generateFakeProducts")
    public ResponseEntity<?> generateFakeProducts(){
        Faker faker = new Faker();
        for(int i = 0; i<5000; i++){
            String productName = faker.commerce().productName();

            if(productService.existsByName(productName)){
                continue;
            }

            ProductDTO productDTO = ProductDTO.builder()
                    .name(productName)
                    .price((float)faker.number().numberBetween(10,50_000_000))
                    .thumbnail("")
                    .categoryId((long) faker.number().numberBetween(1,4))
                    .description(faker.lorem().sentence())

                    .build();
            try{
                productService.createProduct(productDTO);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        return ResponseEntity.ok().body("Generate product succesfully");
    }
}
