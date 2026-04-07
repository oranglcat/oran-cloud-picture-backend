package com.oran.oranpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oran.oranpicturebackend.common.ErrorCode;
import com.oran.oranpicturebackend.exception.BusinessException;
import com.oran.oranpicturebackend.exception.ThrowUtils;
import com.oran.oranpicturebackend.manager.FileManager;
import com.oran.oranpicturebackend.manager.QwenAiManager;
import com.oran.oranpicturebackend.mapper.PictureMapper;
import com.oran.oranpicturebackend.model.dto.file.UploadPictureResult;
import com.oran.oranpicturebackend.model.dto.picture.PictureQueryRequest;
import com.oran.oranpicturebackend.model.dto.picture.PictureReviewRequest;
import com.oran.oranpicturebackend.model.dto.picture.PictureUploadRequest;
import com.oran.oranpicturebackend.model.entity.Picture;
import com.oran.oranpicturebackend.model.entity.User;
import com.oran.oranpicturebackend.model.enums.PictureReviewStatusEnum;
import com.oran.oranpicturebackend.model.vo.PictureVO;
import com.oran.oranpicturebackend.model.vo.UserVO;
import com.oran.oranpicturebackend.service.PictureService;
import com.oran.oranpicturebackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Resource
    private UserService userService;


    @Resource
    private QwenAiManager qwenAiManager;

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
            Picture oldPicture = getById(pictureId);
            Long id = loginUser.getId();
            if (oldPicture == null || !userService.isAdmin(loginUser) || !oldPicture.getUserId().equals(id)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
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

        this.fillReviewParams(picture, loginUser);

        boolean saveOrUpdate = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!saveOrUpdate,ErrorCode.OPERATION_ERROR,"数据库操作失败，文件未上传");
        //5.封装结果
        return PictureVO.objToVo(picture);
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        if (pictureQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        Long id = pictureQueryRequest.getId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();


        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if(StrUtil.isNotBlank(searchText)){
            queryWrapper.and(q -> q.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        if(CollUtil.isNotEmpty(tags)){
            for (String tag : tags) {
                queryWrapper.like( "tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        if (pictureVO != null) {
            User user = userService.getById(picture.getUserId());
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, User> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.toMap(User::getId, user -> user, (existing, replacement) -> existing));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }



    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    /*
    * 图片审核
    * */
    @Override
    public void doReviewPicture(PictureReviewRequest pictureReviewRequest, User loginUser) {
        //1.参数校验
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        String reviewMessage = pictureReviewRequest.getReviewMessage();

        //ID不能为空，审核状态必须存在，提交要修改的审核状态不能为待审核
        if(id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.校验图片是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //3.检测审核状态是否重复
        if(oldPicture.getReviewStatus().equals(reviewStatus)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片已审核");
        }
        // 4.数据库操作
        Picture updatePicture = new Picture();
        BeanUtil.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setEditTime(new Date());
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());

        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    /*
     * 填充审核参数
     * */
    /*
     * 填充审核参数
     * */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员权限通过");
        } else {
            // 判断是新增还是更新
            boolean isNew = picture.getId() == null;

            if (isNew) {
                // 新增图片：进行AI审核
                try {
                    QwenAiManager.AiReviewResult aiResult = qwenAiManager.reviewImage(picture.getUrl());

                    picture.setAi_review_status(aiResult.getPass() ? 1 : 2);
                    picture.setAi_review_score(aiResult.getScore());
                    picture.setAi_review_result(JSONUtil.toJsonStr(aiResult));
                    picture.setAi_review_time(new Date());

                    if (aiResult.getPass() && aiResult.getScore() >= 70) {
                        picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
                        picture.setReviewMessage("AI审核通过 (评分: " + aiResult.getScore() + ")");
                    } else {
                        picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
                        String violations = aiResult.getViolations().isEmpty() ?
                                "疑似违规" :
                                String.join(", ", aiResult.getViolations());
                        picture.setReviewMessage("AI初审未通过: " + aiResult.getReason() + " [" + violations + "]");
                    }

                } catch (Exception e) {
                    picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
                    picture.setReviewMessage("AI审核异常,转入人工审核: " + e.getMessage());
                }
            } else {
                // 更新图片：保留原有的AI审核结果，不重新审核
                Picture oldPicture = getById(picture.getId());
                if (oldPicture != null) {
                    // 只在不为空时保留旧值，允许手动更新审核状态
                    if (picture.getAi_review_status() == null) {
                        picture.setAi_review_status(oldPicture.getAi_review_status());
                    }
                    if (picture.getAi_review_score() == null) {
                        picture.setAi_review_score(oldPicture.getAi_review_score());
                    }
                    if (picture.getAi_review_result() == null) {
                        picture.setAi_review_result(oldPicture.getAi_review_result());
                    }
                    if (picture.getAi_review_time() == null) {
                        picture.setAi_review_time(oldPicture.getAi_review_time());
                    }

                    // 如果用户没有手动修改审核状态，则保留原状态
                    if (picture.getReviewStatus() == null) {
                        picture.setReviewStatus(oldPicture.getReviewStatus());
                    }
                    if (picture.getReviewMessage() == null) {
                        picture.setReviewMessage(oldPicture.getReviewMessage());
                    }
                    if (picture.getReviewerId() == null) {
                        picture.setReviewerId(oldPicture.getReviewerId());
                    }
                }
            }
        }
    }



}




