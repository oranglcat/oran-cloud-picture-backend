package com.oran.oranpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {

    /**
     * 图片 id（用于修改）
     */
    private Long id;

    private String fileurl;

    /*
    * 图片名称
    * */
    private String picname;

    private static final long serialVersionUID = 1L;
}

