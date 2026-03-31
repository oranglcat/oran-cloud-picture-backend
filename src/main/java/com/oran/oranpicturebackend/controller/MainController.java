package com.oran.oranpicturebackend.controller;

import com.oran.oranpicturebackend.annotation.AuthCheck;
import com.oran.oranpicturebackend.common.BaseResponse;
import com.oran.oranpicturebackend.common.ResultUtils;
import com.oran.oranpicturebackend.constant.UserConstant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class MainController {

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/health")
    public BaseResponse<String> health(){
        return ResultUtils.success("ok");
    }


}
