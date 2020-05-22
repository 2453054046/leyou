package com.leyou.item.Api;

import com.leyou.common.utlis.vo.PageResult;
import com.leyou.item.pojo.Brand;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("brand")
public interface BrandApi {

    /**
     *
     * @param page      当前页
     * @param rows      每页显示多少
     * @param sortBy
     * @param desc      排序
     * @param key       搜索的值
     * @return
     */
    @GetMapping("page")
    PageResult<Brand> queryBrandByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy",required = false) String sortBy,
            @RequestParam(value = "desc", defaultValue = "false") Boolean desc,
            @RequestParam(value = "key", required = false) String key

    );

    @PostMapping
    Void saveBrand(Brand brand, @RequestParam("cids") List<Long> cids);

    /**
     * 根据id查询商品品牌
     * @param cid
     * @return
     */
    @GetMapping("/cid/{cid}")
    List<Brand> queryBrandByCid(@PathVariable("cid") Long cid);

    @GetMapping("{id}")
    Brand queryById(@PathVariable("id")Long id);

    @GetMapping("list")
    List<Brand> queryByIds(@RequestParam("ids") List<Long> ids);
}

