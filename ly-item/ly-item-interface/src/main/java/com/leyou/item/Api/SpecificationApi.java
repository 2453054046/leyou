package com.leyou.item.Api;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public interface SpecificationApi {

    /**
     * 根据分类id查询规格组
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    List<SpecGroup> queryGroupById(@PathVariable("cid") Long cid);

    @GetMapping("params")
    List<SpecParam> queryParamById(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value = "searching",required = false) Boolean searching
    );

    /**
     * 根据cid（商品三级分类）查询规格组和规格组的详情字段
     * @param cid
     * @return
     */
    @GetMapping("group")
    List<SpecGroup> queryGroupByCid(@RequestParam("cid") Long cid);
}
