package com.salla.springrestdemo.order;

import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

public class OrderInputDto {
    @ApiModelProperty(notes = "Mandatory valid email", required = true, example = "test@email.com")
    private String buyerEmail;

    @ApiModelProperty(notes = "Array of product ids", required = true, example ="[1,2]" )
    private List<Long> items = new ArrayList<>();

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public List<Long> getItems() {
        return items;
    }

    public void setItems(List<Long> items) {
        this.items = items;
    }
}
