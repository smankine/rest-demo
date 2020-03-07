package com.salla.springrestdemo.order;

import com.salla.springrestdemo.error.ApiError;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;

import static com.salla.springrestdemo.order.ControllerUtils.*;

@RestController
@RequestMapping("/order")
@Api(value = "Order API", description = "Order API")
public class OrderController {

    @Autowired
    private OrderService service;

    @PostMapping
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Invalid Request", response = ApiError.class)})
    public ResponseEntity<OrderOutputDto> create(@RequestBody OrderInputDto order) {
        return ResponseEntity.ok(convertToOutputDto(service.createOrder(order)));
    }

    @GetMapping
    @ApiOperation(value = "Find orders within range")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Invalid Request", response = ApiError.class)})
    public ResponseEntity<Collection<OrderOutputDto>> findOrders(
            @ApiParam(value = "orders created range", example = "2019-09-22T10:00:00", format = "yyyy-MM-dd'T'HH:mm:ss", required = true)
            @RequestParam String startDate,
            @ApiParam(value = "orders created range", example = "2019-09-22T10:00:00", format = "yyyy-MM-dd'T'HH:mm:ss", required = true)
            @RequestParam String endDate) {

        try {
            LocalDateTime startDateParsed = parseWithFormat(startDate);
            LocalDateTime endDateParsed = parseWithFormat(endDate);
            if (endDateParsed.isBefore(startDateParsed)) {
                throw new IllegalArgumentException("endDate has to be after startDate");
            }
            Iterable<Order> orders = service.findOrders(asDate(startDateParsed), asDate(endDateParsed));
            return ResponseEntity.ok(convertOrdersForOutput(orders));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("date format has to be " + pattern);
        }

    }


}
