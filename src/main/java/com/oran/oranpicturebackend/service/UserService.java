package com.oran.oranpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.oran.oranpicturebackend.model.dto.user.UserQueryRequest;
import com.oran.oranpicturebackend.model.dto.user.UserRegisterRequest;
import com.oran.oranpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oran.oranpicturebackend.model.vo.LoginUserVO;
import com.oran.oranpicturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author oranglcat
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2026-03-30 16:36:24
 */
public interface UserService extends IService<User> {

    /*
     *  用户注册
     * */
    long UserRegister(String userAccount, String userPassword, String checkPassword);

    /*
    * 加密密码
    * */
    String getEncryptPassword(String userPassword);


    /*
     * 用户登录
     * */
    LoginUserVO UserLogin(String userAccount, String userPassword, HttpServletRequest request);

    /*
     * 获取脱敏后的用户信息（管理员）
     * */
    LoginUserVO getLoginUserVO(User user);

    /*
     * 获取脱敏后的用户信息（用户使用）
     * */
    UserVO getUserVO(User user);


    /*
     * 获取脱敏后的用户信息列表（管理员使用）
     * */
    List<UserVO> getUserVOList(List<User> userList);

    /*
     * 获取当前登录用户
     * */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户退出登录
     */
    boolean userLogout(HttpServletRequest request);

    /*
     * 获取查询条件
     * */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);
}
