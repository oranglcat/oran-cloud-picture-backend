package com.oran.oranpicturebackend.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SpaceLevel {

    /*
    * 值
    * */
    private int value;


    /*
    * 中文
    * */
    private String text;


    /*
    * 最大数量
    * */
    private long maxCount;


    /*
    * 最大额度
    * */
    private long maxSize;
}
