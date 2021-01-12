package com.vic.user.controller;

import com.netflix.discovery.converters.Auto;
import com.vic.commserver.feign.product.ProductClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 罗利华
 * date: 2021/1/12 14:02
 */
@RestController
@RequestMapping("test")
public class TestController {

    @Autowired
    ProductClient productClient;

    @GetMapping("test1")
    public String test1() {
        return productClient.test1();
    }

}
