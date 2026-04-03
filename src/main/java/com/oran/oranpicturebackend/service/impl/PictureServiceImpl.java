package com.oran.oranpicturebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oran.oranpicturebackend.common.ErrorCode;
import com.oran.oranpicturebackend.exception.ThrowUtils;
import com.oran.oranpicturebackend.manager.FileManager;
import com.oran.oranpicturebackend.model.dto.file.UploadPictureResult;
import com.oran.oranpicturebackend.model.dto.picture.PictureUploadRequest;
import com.oran.oranpicturebackend.model.entity.Picture;
import com.oran.oranpicturebackend.model.entity.User;
import com.oran.oranpicturebackend.model.vo.PictureVO;
import com.oran.oranpicturebackend.service.PictureService;
import com.oran.oranpicturebackend.mapper.PictureMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;

/**
* @author oranglcat
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2026-04-03 10:38:18
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{


    @Resource
    private FileManager fileManager;

    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest request, User loginUser) {
        //1.校验文件
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR,"上传文件为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //2.判断文件是更新还是保存
        Long pictureId = null;
        if(request != null){
            pictureId = request.getId();
        }
        //3.如果是更新，判断是否数据库中是否存在
        if(pictureId != null){
            boolean exists = this.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .exists();
            ThrowUtils.throwIf(!exists,ErrorCode.NOT_FOUND_ERROR);
        }
        //4.上传文件
        String filePrefix = String.format("public/%s",loginUser.getId());
        UploadPictureResult pictureResult = fileManager.uploadPicture(multipartFile, filePrefix);
        //5.操作数据库
        Picture picture = new Picture();
        picture.setUrl(pictureResult.getUrl());
        picture.setName(pictureResult.getPicName());
        picture.setPicSize(pictureResult.getPicSize());
        picture.setPicWidth(pictureResult.getPicWidth());
        picture.setPicHeight(pictureResult.getPicHeight());
        picture.setPicScale(pictureResult.getPicScale());
        picture.setPicFormat(pictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());

        if(pictureId != null){
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }

        boolean saveOrUpdate = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!saveOrUpdate,ErrorCode.OPERATION_ERROR,"数据库操作失败，文件未上传");
        //5.封装结果
        return PictureVO.objToVo(picture);
    }
}




