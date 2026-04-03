package com.oran.oranpicturebackend.manager;


import cn.hutool.core.annotation.Link;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.math.MathUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.oran.oranpicturebackend.common.ErrorCode;
import com.oran.oranpicturebackend.common.ResultUtils;
import com.oran.oranpicturebackend.config.CosClientConfig;
import com.oran.oranpicturebackend.exception.BusinessException;
import com.oran.oranpicturebackend.exception.ThrowUtils;
import com.oran.oranpicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;


    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        //1.校验文件
        validFile(multipartFile);
        //2.获取文件路径
        String multiFileName = multipartFile.getOriginalFilename();
        String uuid = RandomUtil.randomString(16);
        String fileName = String.format("%s_%s.%s", DateUtil.formatTime(new Date()),
                uuid, multiFileName);
        String filePath = String.format("%s/%s", uploadPathPrefix, fileName);
        //3.上传文件
        File file = null;
        try {
            file = File.createTempFile("tmp_", "_" + multiFileName);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(filePath, file);
            //4.获取图片信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            String format = imageInfo.getFormat();
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();
            Double picScale = NumberUtil.round((width * 1.0 / height), 2).doubleValue();
            //5.封装返回结果
            UploadPictureResult pictureResult = new UploadPictureResult();
            pictureResult.setUrl(cosClientConfig.getHost() + "/" + filePath);
            pictureResult.setPicName(fileName);
            pictureResult.setPicSize(FileUtil.size(file));
            pictureResult.setPicWidth(width);
            pictureResult.setPicHeight(height);
            pictureResult.setPicScale(picScale);
            pictureResult.setPicFormat(format);

            return pictureResult;
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            //6.释放资源，删除临时文件
            deleteTempFile(file, filePath);
        }


    }

    public static void deleteTempFile(File file, String filePath) {
        if (file != null) {
            boolean deleted = file.delete();
            if (!deleted) {
                log.error("file delete error,filepath: {}", filePath);
            }
        }
    }


    private final List<String> ALLOW_PREFIX = Arrays.asList("jpeg", "jpg", "png", "webp");

    private final long ONE_MB = 1024 * 1024;


    private void validFile(MultipartFile multipartFile) {
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
