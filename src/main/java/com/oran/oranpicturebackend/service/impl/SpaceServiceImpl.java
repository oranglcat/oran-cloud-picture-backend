package com.oran.oranpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oran.oranpicturebackend.common.ErrorCode;
import com.oran.oranpicturebackend.exception.BusinessException;
import com.oran.oranpicturebackend.exception.ThrowUtils;
import com.oran.oranpicturebackend.model.dto.space.SpaceAddRequest;
import com.oran.oranpicturebackend.model.dto.space.SpaceQueryRequest;
import com.oran.oranpicturebackend.model.entity.Picture;
import com.oran.oranpicturebackend.model.entity.Space;
import com.oran.oranpicturebackend.model.entity.Space;
import com.oran.oranpicturebackend.model.entity.User;
import com.oran.oranpicturebackend.model.enums.SpaceLevelEnum;
import com.oran.oranpicturebackend.model.vo.PictureVO;
import com.oran.oranpicturebackend.model.vo.SpaceVO;
import com.oran.oranpicturebackend.model.vo.UserVO;
import com.oran.oranpicturebackend.service.SpaceService;
import com.oran.oranpicturebackend.mapper.SpaceMapper;
import com.oran.oranpicturebackend.service.UserService;
import kotlin.jvm.Synchronized;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author oranglcat
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2026-04-19 19:56:43
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public Long addSpace(SpaceAddRequest request, User loginUser) {
        // 1. 填充参数默认值
        // 转换实体类和 DTO
        Space space = new Space();
        BeanUtils.copyProperties(request, space);
        if (StrUtil.isBlank(space.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        if (space.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        // 填充容量和大小
        this.fillSpaceByLevel(space);
        // 2. 校验参数
        this.validSpace(space, true);

        if(StrUtil.isBlank(space.getSpaceName())){
            space.setSpaceName("默认空间");
        }
        if(space.getSpaceLevel() == null){
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        if(space.getSpaceLevel() != SpaceLevelEnum.COMMON.getValue() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建");
        }
        //创建
        //字符串上加锁
        String lock = String.valueOf(space.getId()).intern();
        synchronized (lock){
            Long newSpaceId = transactionTemplate.execute(
                    status -> {
                        //判断空间是否已经存在
                        Long userId = loginUser.getId();
                        space.setUserId(userId);
                        boolean exists = this.lambdaQuery()
                                .eq(Space::getUserId, userId)
                                .exists();
                        //已存在则不创建
                        if (exists) {
                            throw new BusinessException(ErrorCode.OPERATION_ERROR, "每个用户只能创建一个私有空间");
                        }
                        //创建
                        boolean save = this.save(space);
                        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "数据库操作失败");
                        return space.getId();
                    }
            );
            return newSpaceId;
        }
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        if (spaceQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();


        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        if (spaceVO != null) {
            User user = userService.getById(space.getUserId());
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> pictureList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> pictureVOList = pictureList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = pictureList.stream().map(Space::getUserId).collect(Collectors.toSet());
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
        spaceVOPage.setRecords(pictureVOList);
        return spaceVOPage;
    }

    @Override
    public void validSpace(Space space,boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);

        // 添加空间
        if(add){
            if(StrUtil.isBlank(spaceName)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间名称不能为空");
            }
            if(spaceLevel == null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间级别不能为空");
            }
        }
        if(StrUtil.isNotBlank(spaceName) && spaceName.length() > 30){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间名称过长");
        }
        if(spaceLevel != null && spaceLevelEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间级别不存在");
        }
    }

    @Override
    public void fillSpaceByLevel(Space space) {
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);

        long maxCount = spaceLevelEnum.getMaxCount();
        long maxSize = spaceLevelEnum.getMaxSize();

        if(space.getMaxSize() == null){
            space.setMaxSize(maxSize);
        }

        if (space.getMaxCount() == null){
            space.setMaxCount(maxCount);
        }
    }


}




