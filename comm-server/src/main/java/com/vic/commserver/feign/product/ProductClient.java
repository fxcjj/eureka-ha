package com.vic.commserver.feign.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

/**
 */
@FeignClient(name = "product", fallback = ProductClientHystrix.class)
public interface ProductClient {

    @GetMapping("feignTest/test1")
    String test1();

}

/**
 * access to the cause that made the fallback trigger
 */
@Component
class ProductClientHystrix implements ProductClient {

    @Override
    public String test1() {
        return "test1, 服务降级";
    }

}