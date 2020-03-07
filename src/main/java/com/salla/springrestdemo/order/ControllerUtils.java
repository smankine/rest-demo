package com.salla.springrestdemo.order;

import com.salla.springrestdemo.product.Product;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ControllerUtils {

    public static String pattern = "yyyy-MM-dd'T'HH:mm:ss";
    public static final DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);

    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }


    public static LocalDateTime parseWithFormat(String date) {
        return LocalDateTime.parse(date, format);
    }

    public static List<OrderOutputDto> convertOrdersForOutput(Iterable<Order> all) {
        return StreamSupport.stream(all.spliterator(), false).map(ControllerUtils::convertToOutputDto)
                .collect(Collectors.toList());
    }

    public static OrderOutputDto convertToOutputDto(Order order) {
        OrderOutputDto result = new OrderOutputDto();
        result.setBuyerEmail(order.getBuyerEmail());
        result.setOrderDate(order.getCreatedDate());
        result.setId(order.getId());
        result.setProducts(convertToOutputProducts(order.getItems()));
        result.setTotalPrice(order.getTotalPrice());
        return result;
    }

    public static List<Product> convertToOutputProducts(List<OrderItem> products) {
        return products.stream().map(ControllerUtils::convertToOutputProduct).collect(Collectors.toList());
    }

    public static Product convertToOutputProduct(OrderItem orderItem) {
        Product result = new Product();
        result.setName(orderItem.getProductName());
        result.setPrice(orderItem.getPurchasePrice());
        result.setId(orderItem.getProduct().getId());
        return result;
    }
}
