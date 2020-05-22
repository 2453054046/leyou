package com.leyou.search.client;

import com.leyou.item.Api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("item-service")//调用微服务
public interface BrandClient extends BrandApi {

}
