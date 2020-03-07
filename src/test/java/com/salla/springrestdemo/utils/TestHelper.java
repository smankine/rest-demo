package com.salla.springrestdemo.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salla.springrestdemo.controller.OrderControllerTests;
import com.salla.springrestdemo.controller.ProductControllerTests;
import com.salla.springrestdemo.error.ApiError;
import com.salla.springrestdemo.order.OrderInputDto;
import com.salla.springrestdemo.order.OrderOutputDto;
import com.salla.springrestdemo.product.Product;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static com.salla.springrestdemo.controller.ProductControllerTests.PRODUCT_URL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestHelper {

    private MockMvc mockMvc;
    public static String STANDARD_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    public static final DateTimeFormatter STANDARD_DATE_FORMAT = DateTimeFormatter.ofPattern(STANDARD_DATE_PATTERN);
    private static ObjectMapper objectMapper = new ObjectMapper();


    public TestHelper(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public static String getProductUpdateUrl(Product createdProduct) {
        return PRODUCT_URL + "/" + createdProduct.getId();
    }


    public static OrderInputDto getOrderInputDto(Product product, String email1) {
        OrderInputDto order = new OrderInputDto();
        order.setBuyerEmail(email1);
        order.setItems(Arrays.asList(product.getId()));
        return order;
    }
    
    public  Product createTestProduct() {
        Product product = new Product();
        product.setPrice(ProductControllerTests.DEFAULT_PRICE);
        product.setName(ProductControllerTests.DEFAULT_NAME);
        return postAndParseResult( PRODUCT_URL, product, Product.class);
    }

    public  OrderOutputDto[] executeFindOrdersWithSuccess( LocalDateTime startDate, LocalDateTime endDate) {

        try {
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(OrderControllerTests.ORDER_URL)
                    .param("startDate", startDate.format(STANDARD_DATE_FORMAT))
                    .param("endDate", endDate.format(STANDARD_DATE_FORMAT))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andReturn();

            return objectMapper.readValue(mvcResult.getResponse().getContentAsString(), OrderOutputDto[].class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public  <T> T putAndParseResult( String url, Object body, Class<T> valueType) {
        try {
            MvcResult mvcResult = performPut( url, body).andExpect(status().isOk()).andReturn();
            return objectMapper.readValue(mvcResult.getResponse().getContentAsString(), valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private  ResultActions performPut( String url, Object body) throws Exception {
        return mockMvc.perform(put(url)
                .content(asJsonString(body))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
    }

    public  <T> T getAndParseResult( String url, Class<T> valueType) {
        try {
            MvcResult mvcResult = performGet( url).andExpect(status().isOk()).andReturn();
            return objectMapper.readValue(mvcResult.getResponse().getContentAsString(), valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public  ApiError getAndReturnError( String url) {
        try {
            MvcResult mvcResult = performGet( url).andDo(print()).andExpect(status().is4xxClientError()).andReturn();
            return objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private  ResultActions performGet( String url) throws Exception {
        return mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
    }

    public  ApiError putAndReturnError( String url, Object body) {
        try {
            MvcResult mvcResult = performPut( url, body).andDo(print()).andExpect(status().is4xxClientError()).andReturn();
            return objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public  ApiError postAndReturnError( String url, Object body) {
        try {
            MvcResult mvcResult = performPost( url, body).andDo(print()).andExpect(status().is4xxClientError()).andReturn();
            return objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private  ResultActions performPost( String url, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .content(asJsonString(body))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
    }

    public  <T> T postAndParseResult( String url, Object body, Class<T> valueType) {
        try {
            MvcResult mvcResult = performPost( url, body).andExpect(status().isOk()).andReturn();
            return objectMapper.readValue(mvcResult.getResponse().getContentAsString(), valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static  String asJsonString(Object obj) {
        try {
            final String jsonContent = objectMapper.writeValueAsString(obj);
            return jsonContent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
