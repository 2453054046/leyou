package com.leyou.item.pojo;

import lombok.Data;

import java.util.List;

@Data
public class JsonObject {

    private String group;//组名
    private List<Param> params;//param数组
    private Boolean empty;//未知，原来没加，然后空指针异常，在某条数据上发现的
}