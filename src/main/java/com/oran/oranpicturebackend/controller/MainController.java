package com.oran.oranpicturebackend.controller;

import com.oran.oranpicturebackend.common.BaseResponse;
import com.oran.oranpicturebackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class MainController {


    @GetMapping("/health")
    public BaseResponse<String> health(){
        return ResultUtils.success("ok");
    }


}
