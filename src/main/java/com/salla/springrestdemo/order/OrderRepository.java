package com.salla.springrestdemo.order;

import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface OrderRepository extends CrudRepository<Order, Long> {

    List<Order> findAllByCreatedDateBetween(Date createdDateStart, Date createdDateEnd);
}