package com.oran.oranpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.oran.oranpicturebackend.common.ErrorCode;
import com.oran.oranpicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Service
public class FilePictureUpload extends FileUploadTemplate {

    private final List<String> ALLOW_PREFIX = Arrays.asList("jpeg", "jpg", "png", "webp");

    private final long ONE_MB = 1024 * 1024;
    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void validFile(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        //1.检验文件是否为空
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "null file error");
        //2.检查文件后缀是否合规
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!ALLOW_PREFIX.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
        //3.检查文件大小
        long size = multipartFile.getSize();
        ThrowUtils.throwIf(size > ONE_MB * 2, ErrorCode.PARAMS_ERROR, "file size cannot over 2MB");
    }
}
