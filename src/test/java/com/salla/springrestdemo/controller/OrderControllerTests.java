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
import com.salla.springrestdemo.order.OrderInputDto;
import com.salla.springrestdemo.order.OrderOutputDto;
import com.salla.springrestdemo.order.OrderRepository;
import com.salla.springrestdemo.product.Product;
import com.salla.springrestdemo.utils.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

import static com.salla.springrestdemo.controller.ProductControllerTests.DEFAULT_NAME;
import static com.salla.springrestdemo.controller.ProductControllerTests.DEFAULT_PRICE;
import static com.salla.springrestdemo.utils.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTests {

    public static final String ORDER_URL = "/order";
    public static final String DEFAULT_EMAIL = "test@email.com";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private OrderRepository orderRepository;

    private TestHelper testHelper;

    @BeforeEach
    public void setup() {
        this.testHelper = new TestHelper(this.mockMvc);
    }


    @Test
    public void createOrder_ValidParams_Success() {
        Product createdProduct = createProduct();
        OrderOutputDto created = createOrder(createdProduct, DEFAULT_EMAIL);

        assertEquals(DEFAULT_EMAIL, created.getBuyerEmail());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        assertEquals(format.format(new Date()), format.format(created.getOrderDate()));
        assertNotNull(created.getId());
        assertEquals(1, created.getProducts().size());
        assertEquals(createdProduct.getId(),created.getProducts().get(0).getId());
        assertEquals(DEFAULT_NAME, created.getProducts().get(0).getName());
        assertEquals(DEFAULT_PRICE, created.getProducts().get(0).getPrice());
        assertEquals(DEFAULT_PRICE, created.getTotalPrice());
    }

    @Test
    public void createOrder_MultipleProducts_Success() {
        Product createdProduct1 = createProduct();
        Product createdProduct2 = createProduct();

        OrderInputDto order = new OrderInputDto();
        order.setBuyerEmail(DEFAULT_EMAIL);
        order.setItems(Arrays.asList(createdProduct1.getId(), createdProduct2.getId()));

        OrderOutputDto created = this.testHelper.postAndParseResult(ORDER_URL, order, OrderOutputDto.class);

        assertEquals(DEFAULT_EMAIL, created.getBuyerEmail());
        assertNotNull(created.getId());
        assertEquals(2, created.getProducts().size());
        assertEquals(createdProduct1.getId(), created.getProducts().get(0).getId() );
        assertEquals(createdProduct2.getId(),created.getProducts().get(1).getId());
        assertEquals(DEFAULT_PRICE * 2, created.getTotalPrice());
    }

    @Test
    public void createOrder_MultipleProductsAndInterimPriceChange_OrderPriceNotEffected() {
        Product createdProduct1 = createProduct();
        Product createdProduct2 = createProduct();

        OrderInputDto order = new OrderInputDto();
        order.setBuyerEmail(DEFAULT_EMAIL);
        order.setItems(Arrays.asList(createdProduct1.getId(), createdProduct2.getId()));

        OrderOutputDto createdOrder = this.testHelper.postAndParseResult(ORDER_URL, order, OrderOutputDto.class);
        assertEquals(DEFAULT_PRICE * 2, createdOrder.getTotalPrice());

        createdProduct1.setPrice(200D);

        Product updatedProduct = this.testHelper.putAndParseResult(getProductUpdateUrl(createdProduct1),
                createdProduct1, Product.class);
        assertEquals(200D, updatedProduct.getPrice());

        LocalDateTime now = LocalDateTime.now();
        OrderOutputDto[] returnedOrdersAfterPriceChange = this.testHelper.executeFindOrdersWithSuccess(
                now.minusSeconds(5), now.plusSeconds(5));

        OrderOutputDto orderAfterPriceUpdate = findOrderById(createdOrder, returnedOrdersAfterPriceChange);

        assertEquals(DEFAULT_PRICE * 2, orderAfterPriceUpdate.getTotalPrice());
    }


    @Test
    public void createOrder_SameProductMultipleTimes_400IsReturned() {
        Product createdProduct = createProduct();

        OrderInputDto order = new OrderInputDto();
        order.setBuyerEmail(DEFAULT_EMAIL);
        order.setItems(Arrays.asList(createdProduct.getId(), createdProduct.getId()));

        ApiError error = this.testHelper.postAndReturnError(ORDER_URL, order);
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertEquals("Product list can't contain duplicates", error.getMessage());

    }

    @Test
    public void createOrder_InvalidParams_400IsReturned() {
        Product createdProduct = createProduct();
        OrderInputDto order = getOrderInputDto(createdProduct, "invalidEmail");
        ApiError error = this.testHelper.postAndReturnError(ORDER_URL, order);
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertEquals(1, error.getSubErrors().size());
        assertEquals("buyerEmail", error.getSubErrors().get(0).getField());
        assertEquals("must be a well-formed email address", error.getSubErrors().get(0).getMessage());
        assertEquals("Order", error.getSubErrors().get(0).getObject());

        order.setBuyerEmail(DEFAULT_EMAIL);

        order.setItems(null);
        error = this.testHelper.postAndReturnError(ORDER_URL, order);
        assertEquals("items", error.getSubErrors().get(0).getField());
        assertEquals("must not be empty", error.getSubErrors().get(0).getMessage());
        assertEquals("Order", error.getSubErrors().get(0).getObject());

        order.setItems(Arrays.asList(new Long[]{}));
        error = this.testHelper.postAndReturnError(ORDER_URL, order);
        assertEquals("items", error.getSubErrors().get(0).getField());
        assertEquals("must not be empty", error.getSubErrors().get(0).getMessage());
        assertEquals("Order", error.getSubErrors().get(0).getObject());


        order.setItems(Arrays.asList(333L));
        error = this.testHelper.postAndReturnError(ORDER_URL, order);
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertEquals("Product doesn't exist with this id", error.getMessage());
        assertNull(error.getSubErrors());

        order.setBuyerEmail(null);
        order.setItems(Arrays.asList(createdProduct.getId()));
        error = this.testHelper.postAndReturnError(ORDER_URL, order);
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertEquals(1, error.getSubErrors().size());
        assertEquals("buyerEmail", error.getSubErrors().get(0).getField());
        assertEquals("must not be blank", error.getSubErrors().get(0).getMessage());
        assertEquals("Order", error.getSubErrors().get(0).getObject());

    }

    @Test
    public void createOrder_BadHeadersAndNoBody_400IsReturned() throws Exception {


        this.mockMvc.perform(post(ORDER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").doesNotExist());

        this.mockMvc.perform(post(ORDER_URL)).andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").doesNotExist());

    }

    @Test
    public void createOrder_BadHeadersButGoodBody_400IsReturned() throws Exception {

        Product createdProduct = createProduct();
        OrderInputDto order = getOrderInputDto(createdProduct, DEFAULT_EMAIL);

        this.mockMvc.perform(post(ORDER_URL).content(asJsonString(order))).andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    public void findOrders_InvalidParams_400IsReturned() throws Exception {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now();
        end = end.plusDays(2);

        mockMvc.perform(get(ORDER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .param("startDate", start.format(STANDARD_DATE_FORMAT))
                .accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value("Required String parameter 'endDate' is not present"))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));

        mockMvc.perform(get(ORDER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value("Required String parameter 'startDate' is not present"))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));

        mockMvc.perform(get(ORDER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .param("endDate", end.format(STANDARD_DATE_FORMAT))
                .accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value("Required String parameter 'startDate' is not present"))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));

        mockMvc.perform(get(ORDER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .param("startDate", start.format(STANDARD_DATE_FORMAT))
                .param("endDate", "badformat")
                .accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value("date format has to be yyyy-MM-dd'T'HH:mm:ss"))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));

        mockMvc.perform(get(ORDER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .param("startDate", start.format(STANDARD_DATE_FORMAT))
                .param("endDate", "2019-22-01T10:00:00Z")
                .accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value("date format has to be yyyy-MM-dd'T'HH:mm:ss"))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));

        mockMvc.perform(get(ORDER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .param("startDate", start.format(STANDARD_DATE_FORMAT))
                .param("endDate", end.minusYears(2).format(STANDARD_DATE_FORMAT))
                .accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value("endDate has to be after startDate"))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));

        //no content type headers, this is fine for a get
        mockMvc.perform(get(ORDER_URL)
                .param("startDate", start.format(STANDARD_DATE_FORMAT))
                .param("endDate", end.format(STANDARD_DATE_FORMAT)))
                .andDo(print()).andExpect(status().is2xxSuccessful());

    }


    @Test
    public void findOrders_ValidParams_OrdersReturnedInDateRange() throws Exception {
        orderRepository.deleteAll();
        Product product = createProduct();

        String email1 = "email_1@order.com";
        String email2 = "email_2@order.com";
        String email3 = "email_3@order.com";

        LocalDateTime atFirst = LocalDateTime.now();
        createOrder(product, email1);
        Thread.sleep(2000);
        LocalDateTime atSecond = LocalDateTime.now();
        createOrder(product, email2);
        Thread.sleep(2000);
        LocalDateTime atThird = LocalDateTime.now();
        createOrder(product, email3);

        OrderOutputDto[] returnedOrders = this.testHelper.executeFindOrdersWithSuccess(
                atFirst.minusSeconds(2), atThird.plusSeconds(5));
        assertEquals(3, returnedOrders.length);

        returnedOrders = this.testHelper.executeFindOrdersWithSuccess(atFirst.plusSeconds(1), atThird.plusSeconds(5));
        assertEquals(2, returnedOrders.length);
        assertTrue(returnedProductsContainProductsWithEmails(returnedOrders, email2, email3));

        returnedOrders = this.testHelper.executeFindOrdersWithSuccess(atFirst.minusSeconds(1), atThird.minusSeconds(1));
        assertEquals(2, returnedOrders.length);
        assertTrue(returnedProductsContainProductsWithEmails(returnedOrders, email2, email1));

        returnedOrders = this.testHelper.executeFindOrdersWithSuccess(atFirst.plusSeconds(1), atThird.minusSeconds(1));
        assertEquals(1, returnedOrders.length);
        assertTrue(returnedProductsContainProductsWithEmails(returnedOrders, email2));

        returnedOrders = this.testHelper.executeFindOrdersWithSuccess(atThird.plusSeconds(2), atThird.plusSeconds(5));
        assertEquals(0, returnedOrders.length);

    }


    private boolean returnedProductsContainProductsWithEmails(OrderOutputDto[] returnedOrders, String... emails) {
        return Arrays.asList(returnedOrders).stream().map(OrderOutputDto::getBuyerEmail)
                .anyMatch(email -> Arrays.asList(emails).contains(email));
    }


    private Product createProduct() {
        return this.testHelper.createTestProduct();
    }

    private OrderOutputDto createOrder(Product createdProduct, String email) {
        OrderInputDto order = getOrderInputDto(createdProduct, email);
        return this.testHelper.postAndParseResult(ORDER_URL, order, OrderOutputDto.class);
    }

    private OrderOutputDto findOrderById(OrderOutputDto createdOrder, OrderOutputDto[] returnedOrdersAfterPriceChange) {
        return Arrays.asList(returnedOrdersAfterPriceChange)
                .stream().filter(o -> o.getId().equals(createdOrder.getId()))
                .findFirst().get();
    }

}
