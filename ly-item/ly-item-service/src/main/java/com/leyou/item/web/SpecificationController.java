package com.leyou.item.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {
    @Autowired
    SpecificationService specificationService;

    /**
     * 根据分类id查询规格组
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupById(@PathVariable("cid") Long cid){
        return ResponseEntity.ok(specificationService.queryGroupById(cid));
    }


    /**
     *  查询参数集合
     * @param gid       组id
     * @param cid       分类id
     * @param searching 是否搜索
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParamById(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value = "searching",required = false) Boolean searching
            ){
        return ResponseEntity.ok(specificationService.queryParamByList(gid,cid,searching));
    }

    /**
     * 根据cid（商品三级分类）查询规格组和规格组的详情字段
     * @param cid
     * @return
     */
    @GetMapping("group")
    ResponseEntity<List<SpecGroup>> queryGroupByCid(@RequestParam("cid") Long cid){
        return ResponseEntity.ok(specificationService.queryGroupByCid(cid));
    }
}
