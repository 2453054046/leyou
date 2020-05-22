package com.leyou.page.client;

import com.leyou.item.Api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("item-service")//调用微服务
public interface CategoryClient extends CategoryApi {

}
