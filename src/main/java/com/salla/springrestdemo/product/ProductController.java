package com.salla.springrestdemo.product;

import com.salla.springrestdemo.error.ApiError;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;


@RestController
@RequestMapping("/product")
@Api(value = "Product API", description = "Product API")
public class ProductController {


    @Autowired
    private ProductRepository repository;

    @PostMapping
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Invalid Request", response = ApiError.class)})
    public ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.ok(repository.save(product));
    }


    @PutMapping("/{id}")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 400, message = "Invalid Request", response = ApiError.class)})
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product product) {
        if (!repository.findById(id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        product.setId(id);
        return ResponseEntity.ok(repository.save(product));
    }

    @GetMapping
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Invalid Request", response = ApiError.class)})
    @ApiOperation(value = "get all products", response = Product[].class)
    public ResponseEntity<Iterable<Product>> findAll() {
        return ResponseEntity.ok(repository.findAll());

    }


}
