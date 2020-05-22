package com.leyou.item.Api;

import com.leyou.item.pojo.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 查询商品分类
 */
@RestController
@RequestMapping("category")
public interface CategoryApi {


    /**
     * 根据父节点查询商品类目
     * @param pid
     * @return
     */
    @GetMapping("list")
    List<Category> queryListByParent(@RequestParam(value = "pid", defaultValue = "0") Long pid);

    /**
     * 根据多个id查询商品类目
     * @param ids
     * @return
     */
    @GetMapping("list/ids")
    List<Category> queryCategoryByIds(@RequestParam("ids") List<Long> ids);
}
