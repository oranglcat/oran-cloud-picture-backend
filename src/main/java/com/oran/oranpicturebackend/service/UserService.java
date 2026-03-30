package com.oran.oranpicturebackend.service;

import com.oran.oranpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oran.oranpicturebackend.model.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;

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
     * 用户登录
     * */
    LoginUserVO UserLogin(String userAccount, String userPassword, HttpServletRequest request);


}
