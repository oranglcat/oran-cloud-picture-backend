package com.oran.oranpicturebackend.model.dto.picture;

import io.swagger.models.auth.In;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureUploadByBatchRequest implements Serializable {

    /*
    * 搜索词
    * */
    private String query;


    /*
    * 搜索条数
    * */
    private Integer count;

    /*
    * 图片名前缀
    * */
    private String namePrefix;

    private static final long serialVersionUID = 1L;
}

