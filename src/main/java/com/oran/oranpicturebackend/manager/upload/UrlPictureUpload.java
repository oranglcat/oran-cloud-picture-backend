package com.oran.oranpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.oran.oranpicturebackend.common.ErrorCode;
import com.oran.oranpicturebackend.exception.BusinessException;
import com.oran.oranpicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Service
public class UrlPictureUpload extends FileUploadTemplate {


    private final List<String> ALLOW_PREFIX = Arrays.asList("jpeg", "jpg", "png", "webp");

    private final long ONE_MB = 1024 * 1024;

    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        String fileUrl = (String) inputSource;
        HttpUtil.downloadFile(fileUrl,file);
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        return FileUtil.mainName(fileUrl);
    }

    @Override
    protected void validFile(Object inputSource) {
        String fileUrl = (String) inputSource;
        //1.校验url是否为空
        ThrowUtils.throwIf(fileUrl == null, ErrorCode.PARAMS_ERROR);
        //2.校验url格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"url格式错误");
        }
        //3.检验url协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"),ErrorCode.PARAMS_ERROR,
                "仅支持http或https格式的协议的文件地址");
        //4.发送head请求，验证图片是否存在
        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl)
                    .execute();
            //5.检查状态，如果不是正常，直接返回
            if(httpResponse.getStatus() != HttpStatus.HTTP_OK){
                return;
            }
            //6.文件存在，校验文件类型
            String contentType = httpResponse.header("Content-Type");
            if(StrUtil.isNotBlank(contentType)){
                //7.允许的图片类型
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType),ErrorCode.PARAMS_ERROR,"不支持此文件类型");
            }
            //8.文件存在，校验文件大小
            String contentLength = httpResponse.header("Content-Length");
            if (StrUtil.isNotBlank(contentLength)){
                long content = Long.parseLong(contentLength);
                ThrowUtils.throwIf(content > ONE_MB * 2, ErrorCode.PARAMS_ERROR, "file size cannot over 2MB");
            }
        }finally {
            //9.释放资源
            if(httpResponse != null){
                httpResponse.close();
            }
        }
    }
}
