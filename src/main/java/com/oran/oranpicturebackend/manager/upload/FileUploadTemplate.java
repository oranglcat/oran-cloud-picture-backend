package com.oran.oranpicturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import com.oran.oranpicturebackend.common.ErrorCode;
import com.oran.oranpicturebackend.config.CosClientConfig;
import com.oran.oranpicturebackend.exception.BusinessException;
import com.oran.oranpicturebackend.exception.ThrowUtils;
import com.oran.oranpicturebackend.manager.CosManager;
import com.oran.oranpicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class FileUploadTemplate {


    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    private final List<String> ALLOW_PREFIX = Arrays.asList("jpeg", "jpg", "png", "webp");

    private final long ONE_MB = 1024 * 1024;

    /**
     * 上传图片
     *
     * @param inputSource
     * @param uploadPathPrefix
     * @return
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        //1.校验文件
        validFile(inputSource);
        //2.获取文件路径
        String multiFileName = getOriginalFilename(inputSource);
        String fileSuffix = FileUtil.getSuffix(multiFileName);
        String uuid = RandomUtil.randomString(16);
        String fileName = String.format("%s_%s.%s", DateUtil.formatTime(new Date()),
                uuid, fileSuffix);
        String filePath = String.format("%s/%s", uploadPathPrefix, fileName);
        //3.上传文件
        File file = null;
        try {
            file = File.createTempFile("tmp_", "_" + multiFileName);
            processFile(inputSource, file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(filePath, file);
            //4.获取图片信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //获取处理后的图片信息
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if(CollUtil.isNotEmpty(objectList)){
                //获取压缩后的图片处理结果
                CIObject compressObject = objectList.get(0);
                //默认缩略图等于压缩后的结果
                CIObject thumbnailCiObject = compressObject;
                if(objectList.size() > 1){
                    //如果存在缩略图处理，获取等比缩放的图片结果
                    thumbnailCiObject = objectList.get(1);
                }
                return getUploadPictureResult(fileName,compressObject,thumbnailCiObject);
            }
            return getUploadPictureResult(imageInfo, filePath, fileName, file);
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            //6.释放资源，删除临时文件
            deleteTempFile(file, filePath);
        }
    }

    private UploadPictureResult getUploadPictureResult(String fileName, CIObject compressObject,CIObject thumbnailCiObject) {
        String format = compressObject.getFormat();
        int width = compressObject.getWidth();
        int height = compressObject.getHeight();
        Double picScale = NumberUtil.round((width * 1.0 / height), 2).doubleValue();
        //5.封装返回结果
        UploadPictureResult pictureResult = new UploadPictureResult();
        pictureResult.setUrl(cosClientConfig.getHost() + "/" + compressObject.getKey());
        pictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
        pictureResult.setPicName(fileName);
        pictureResult.setPicSize(compressObject.getSize().longValue());
        pictureResult.setPicWidth(width);
        pictureResult.setPicHeight(height);
        pictureResult.setPicScale(picScale);
        pictureResult.setPicFormat(format);

        return pictureResult;
    }

    protected abstract void processFile(Object inputSource, File file) throws Exception;

    protected abstract String getOriginalFilename(Object inputSource);

    protected abstract void validFile(Object inputSource);

    public static void deleteTempFile(File file, String filePath) {
        if (file != null) {
            boolean deleted = file.delete();
            if (!deleted) {
                log.error("file delete error,filepath: {}", filePath);
            }
        }
    }


    @NotNull
    private UploadPictureResult getUploadPictureResult(ImageInfo imageInfo, String filePath, String fileName, File file) {
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
    }


}
