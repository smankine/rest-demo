package com.salla.springrestdemo.order;

import com.salla.springrestdemo.product.Product;
import com.salla.springrestdemo.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class OrderService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;


    public Order createOrder(OrderInputDto input) {
        Order order = new Order();
        order.setBuyerEmail(input.getBuyerEmail());

        if (hasDuplicates(input)) {
            throw new IllegalArgumentException("Product list can't contain duplicates");
        }

        order.setItems(convertProductIdsToOrderProducts(input));
        order = orderRepository.save(order);
        return order;
    }

    private boolean hasDuplicates(OrderInputDto input) {
        return input.getItems() != null && (new HashSet<>(input.getItems()).size() != input.getItems().size());
    }

    private List<OrderItem> convertProductIdsToOrderProducts(OrderInputDto input) {
        if (input.getItems() == null) {
            return null;
        }
        return input.getItems().stream().map(this::getOrderItem).collect(Collectors.toList());
    }

    public Iterable<Order> findOrders(Date startDate, Date endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("startDate can't be empty");
        }

        if (endDate == null) {
            throw new IllegalArgumentException("endDate can't be empty");
        }
        return orderRepository.findAllByCreatedDateBetween(startDate, endDate);
    }


    private OrderItem getOrderItem(Long productId) {
        Optional<Product> product = productRepository.findById(productId);
        if (!product.isPresent()) {
            throw new IllegalArgumentException("Product doesn't exist with this id");

        }
        OrderItem orderItem = new OrderItem();
        Product originalProduct = product.get();
        orderItem.setProduct(originalProduct);
        orderItem.setPurchasePrice(originalProduct.getPrice());
        orderItem.setProductName(originalProduct.getName());
        return orderItem;
    }
}
