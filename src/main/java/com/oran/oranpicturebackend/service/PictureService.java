package com.oran.oranpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oran.oranpicturebackend.model.dto.picture.PictureQueryRequest;
import com.oran.oranpicturebackend.model.dto.picture.PictureReviewRequest;
import com.oran.oranpicturebackend.model.dto.picture.PictureUploadRequest;
import com.oran.oranpicturebackend.model.dto.user.UserQueryRequest;
import com.oran.oranpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oran.oranpicturebackend.model.entity.User;
import com.oran.oranpicturebackend.model.vo.LoginUserVO;
import com.oran.oranpicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author oranglcat
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2026-04-03 10:38:18
*/
public interface PictureService extends IService<Picture> {


     /*
      * 上传图片
      * */
     PictureVO uploadPicture(Object multipartFile, PictureUploadRequest request, User loginUser);

     /*
      * 获取查询条件
      * */
     QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);


     /**
      * 获取单个图片的 VO 对象
      * @param picture picture 对象
      * @param request request 请求
      * @return 对应图片的 VO
      */
     PictureVO getPictureVO(Picture picture, HttpServletRequest request);


     /**
      * 分页获取图片 VO 对象
      * @param picturePage  page 对象
      * @param request request 请求
      * @return 分页的 VO
      */
     Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);


     /**
      * 校验参数
      * @param picture 需要校验的 picture 对象
      */
     void validPicture(Picture picture);

     /**
      * 审核图片
      *
      * @param pictureReviewRequest
      * @param loginUser
      */
     void doReviewPicture(PictureReviewRequest pictureReviewRequest, User loginUser);

    void fillReviewParams(Picture picture, User loginUser);
}
