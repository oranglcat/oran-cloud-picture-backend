package com.oran.oranpicturebackend.controller;

import com.oran.oranpicturebackend.annotation.AuthCheck;
import com.oran.oranpicturebackend.common.BaseResponse;
import com.oran.oranpicturebackend.common.ErrorCode;
import com.oran.oranpicturebackend.common.ResultUtils;
import com.oran.oranpicturebackend.constant.UserConstant;
import com.oran.oranpicturebackend.exception.BusinessException;
import com.oran.oranpicturebackend.manager.CosManager;
import com.oran.oranpicturebackend.model.dto.picture.PictureUploadRequest;
import com.oran.oranpicturebackend.model.entity.User;
import com.oran.oranpicturebackend.model.vo.PictureVO;
import com.oran.oranpicturebackend.service.PictureService;
import com.oran.oranpicturebackend.service.UserService;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {


    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;


    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }


}
