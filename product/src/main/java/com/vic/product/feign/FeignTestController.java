package com.vic.product.feign;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 罗利华
 * date: 2021/1/12 14:04
 */
@RestController
@RequestMapping("feignTest")
public class FeignTestController {

    @GetMapping("test1")
    public String test1() {
        return "product test1";
    }


}
