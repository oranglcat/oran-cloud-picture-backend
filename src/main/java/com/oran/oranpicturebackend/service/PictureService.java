package com.oran.oranpicturebackend.service;

import com.oran.oranpicturebackend.model.dto.picture.PictureUploadRequest;
import com.oran.oranpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oran.oranpicturebackend.model.entity.User;
import com.oran.oranpicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

/**
* @author oranglcat
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2026-04-03 10:38:18
*/
public interface PictureService extends IService<Picture> {


     PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest request, User loginUser);

}
