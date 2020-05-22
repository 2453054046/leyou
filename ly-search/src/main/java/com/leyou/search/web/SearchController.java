package com.leyou.search.web;

import com.leyou.common.utlis.vo.PageResult;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.annotation.RegisterMapper;

@RestController
@RequestMapping()
public class SearchController {

    @Autowired
    SearchService searchService;

    @PostMapping("page")
    public ResponseEntity<PageResult<Goods>> search(@RequestBody SearchRequest searchRequest){
        return ResponseEntity.ok(searchService.search(searchRequest));
    }

    @RequestMapping("{id}")
    public String s(@PathVariable("id") int id){
        return "你好";
    }
}
