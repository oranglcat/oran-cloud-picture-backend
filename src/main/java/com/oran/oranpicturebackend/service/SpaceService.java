package com.oran.oranpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oran.oranpicturebackend.model.dto.space.SpaceAddRequest;
import com.oran.oranpicturebackend.model.dto.space.SpaceQueryRequest;
import com.oran.oranpicturebackend.model.entity.Space;
import com.oran.oranpicturebackend.model.entity.User;
import com.oran.oranpicturebackend.model.vo.LoginUserVO;
import com.oran.oranpicturebackend.model.vo.SpaceVO;
import com.baomidou.mybatisplus.extension.service.IService;


import javax.servlet.http.HttpServletRequest;

/**
 * @author oranglcat
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2026-04-19 19:56:43
 */
public interface SpaceService extends IService<Space> {

    /*
    * 添加空间
    * */
    Long addSpace(SpaceAddRequest request, User loginUser);


    /*
     * 获取查询条件
     * */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);


    /**
     * 获取单个图片的 VO 对象
     *
     * @param space   space 对象
     * @param request request 请求
     * @return 对应图片的 VO
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);


    /**
     * 分页获取图片 VO 对象
     *
     * @param spacePage page 对象
     * @param request   request 请求·
     * @return 分页的 VO
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);


    /**
     * 校验参数
     *
     * @param space 需要校验的 space 对象
     */
    void validSpace(Space space,boolean add);


    /**
     * 填充空间字段
     * @param space
     */
    void fillSpaceByLevel(Space space);
}
