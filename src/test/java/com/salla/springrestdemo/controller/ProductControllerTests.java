/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.salla.springrestdemo.controller;

import com.salla.springrestdemo.error.ApiError;
import com.salla.springrestdemo.error.ApiValidationError;
import com.salla.springrestdemo.product.Product;
import com.salla.springrestdemo.utils.TestHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.web.servlet.MockMvc;

import static com.salla.springrestdemo.utils.TestHelper.getProductUpdateUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTests {
    public static final String PRODUCT_URL = "/product";
    public static final double DEFAULT_PRICE = 10D;
    public static final String DEFAULT_NAME = "testproduct";

    @Autowired
    private MockMvc mockMvc;

    private TestHelper testHelper;

    @BeforeEach
    public void setup() {
        this.testHelper = new TestHelper(this.mockMvc);
    }

    @Test
    public void createProduct_ValidParams_Success() {
        Product res = createProduct();
        assertEquals(DEFAULT_PRICE, res.getPrice());
        assertEquals(DEFAULT_NAME, res.getName());
        assertNotNull(res.getId());
    }

    @Test
    public void createProduct_InvalidParams_400IsReturned() {
        Product product = new Product();
        ApiError apiReturnError = this.testHelper.postAndReturnError(PRODUCT_URL, product);
        assertEquals(HttpStatus.BAD_REQUEST, apiReturnError.getStatus());

        ApiValidationError priceError = getErrorByFieldName(apiReturnError, "price");
        ApiValidationError nameError = getErrorByFieldName(apiReturnError, "name");
        assertEquals("must not be null", priceError.getMessage());
        assertEquals("Product", priceError.getObject());
        assertEquals("must not be blank", nameError.getMessage());
        assertEquals("Product", nameError.getObject());


        product.setPrice(null);
        product.setName("name");
        apiReturnError = this.testHelper.postAndReturnError(PRODUCT_URL, product);
        assertEquals(HttpStatus.BAD_REQUEST, apiReturnError.getStatus());
        assertEquals(1, apiReturnError.getSubErrors().size());
        assertEquals("price", apiReturnError.getSubErrors().get(0).getField());
        assertEquals("must not be null", apiReturnError.getSubErrors().get(0).getMessage());
        assertEquals("Product", apiReturnError.getSubErrors().get(0).getObject());


        product.setName("name");
        product.setPrice(-44D);
        apiReturnError = this.testHelper.postAndReturnError(PRODUCT_URL, product);
        assertEquals(HttpStatus.BAD_REQUEST, apiReturnError.getStatus());
        assertEquals(1, apiReturnError.getSubErrors().size());
        assertEquals("price", apiReturnError.getSubErrors().get(0).getField());
        assertEquals("must be greater than or equal to 0", apiReturnError.getSubErrors().get(0).getMessage());
        assertEquals("Product", apiReturnError.getSubErrors().get(0).getObject());
    }


    @Test
    public void updateProduct_ValidParams_ProductUpdatedAndReturned() {
        Product createdProduct = createProduct();
        assertEquals(DEFAULT_PRICE, createdProduct.getPrice());
        assertEquals(DEFAULT_NAME, createdProduct.getName());

        Product productToUpdateTo = new Product();
        productToUpdateTo.setPrice(444D);
        productToUpdateTo.setName("modified");

        Product updatedProduct = this.testHelper.putAndParseResult(getProductUpdateUrl(createdProduct),
                productToUpdateTo, Product.class);

        assertEquals("modified", updatedProduct.getName());
        assertEquals(444D, updatedProduct.getPrice());
        assertEquals(createdProduct.getId(), updatedProduct.getId());
    }

    @Test
    public void updateProduct_InvalidParams_400IsReturned() {
        Product createdProduct = createProduct();
        Product updatedProduct = new Product();

        ApiError apiReturnError = this.testHelper.putAndReturnError(getProductUpdateUrl(createdProduct), updatedProduct);

        assertEquals(2, apiReturnError.getSubErrors().size());
        assertEquals(HttpStatus.BAD_REQUEST, apiReturnError.getStatus());

        ApiValidationError priceError = getErrorByFieldName(apiReturnError, "price");
        ApiValidationError nameError = getErrorByFieldName(apiReturnError, "name");
        assertEquals("must not be null", priceError.getMessage());
        assertEquals("Product", priceError.getObject());
        assertEquals("must not be blank", nameError.getMessage());
        assertEquals("Product", nameError.getObject());

        updatedProduct.setName("name");
        updatedProduct.setPrice(null);
        apiReturnError = this.testHelper.putAndReturnError(getProductUpdateUrl(createdProduct), updatedProduct);
        assertEquals(HttpStatus.BAD_REQUEST, apiReturnError.getStatus());
        assertEquals(1, apiReturnError.getSubErrors().size());
        assertEquals("price", apiReturnError.getSubErrors().get(0).getField());
        assertEquals("must not be null", apiReturnError.getSubErrors().get(0).getMessage());
        assertEquals("Product", apiReturnError.getSubErrors().get(0).getObject());

        updatedProduct.setName("name");
        updatedProduct.setPrice(-44D);
        apiReturnError = this.testHelper.putAndReturnError(getProductUpdateUrl(createdProduct), updatedProduct);
        assertEquals(HttpStatus.BAD_REQUEST, apiReturnError.getStatus());
        assertEquals(1, apiReturnError.getSubErrors().size());
        assertEquals("price", apiReturnError.getSubErrors().get(0).getField());
        assertEquals("must be greater than or equal to 0", apiReturnError.getSubErrors().get(0).getMessage());
        assertEquals("Product", apiReturnError.getSubErrors().get(0).getObject());
    }


    @Test
    public void findAllProducts_ValidParam_200ProductsReturned() {
        Product createdProduct = createProduct();
        Product[] res = this.testHelper.getAndParseResult(PRODUCT_URL, Product[].class);
        assertEquals(DEFAULT_NAME, res[res.length - 1].getName());
    }


    @Test
    public void updateProduct_MissingHeaders_400IsReturned() throws Exception {

        this.mockMvc.perform(post(PRODUCT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").doesNotExist());

        this.mockMvc.perform(post(PRODUCT_URL)).andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").doesNotExist());

    }

    private ApiValidationError getErrorByFieldName(ApiError error, String price) {
        return error.getSubErrors().stream().filter(err -> err.getField().equals(price)).findFirst().get();
    }

    private Product createProduct() {
        return this.testHelper.createTestProduct();
    }

}
