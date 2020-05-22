package com.leyou.item.service;

import com.leyou.common.utlis.enums.ExceptionEnum;
import com.leyou.common.utlis.exception.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationService {


    @Autowired
    SpecGroupMapper specGroupMapper;

    @Autowired
    SpecParamMapper specParamMapper;

    /**
     * 根据商品分类id查询对应的规格组
     * @return
     */
    public List<SpecGroup> queryGroupById(Long id){
        SpecGroup group = new SpecGroup();
        group.setCid(id);
        List<SpecGroup> select = specGroupMapper.select(group);
        if(CollectionUtils.isEmpty(select)){
            throw new LyException(ExceptionEnum.SPEC_GROUP_FOND);
        }
        return select;
    }

    /**
     * 根据组id查询对应规格数据
     * @return
     */
    public List<SpecParam> queryParamByList(Long gid, Long cid, Boolean searching){
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
        List<SpecParam> select = specParamMapper.select(specParam);
        if(CollectionUtils.isEmpty(select)){
            throw new LyException(ExceptionEnum.SPEC_GROUP_FOND);
        }
        return select;
    }

    /**
     * 根据cid（商品三级分类）查询规格组和规格组的详情字段
     * @param cid
     * @return
     */
    public List<SpecGroup> queryGroupByCid(Long cid) {
        //查询规格组
        List<SpecGroup> specGroups = queryGroupById(cid);
        //查询规格组的字段（规格参数）
        List<SpecParam> specParams = queryParamByList(null, cid, null);
        //先把规格参数变成map  key:规格组的id  value：规格参数
        Map<Long,List<SpecParam>> map = new HashMap<>();
        //遍历规格参数放入map
        for (SpecParam param : specParams) {
            //如果map中没有当前的规格参数，添加一个新的list数组准备存放该规格参数
            if(!map.containsKey(param.getGroupId())){
                map.put(param.getGroupId(),new ArrayList<>());
            }
            //将当前规格参数放入map
            map.get(param.getGroupId()).add(param);
        }
        //将规格参数map放入规格组中
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(map.get(specGroup.getId()));
        }
        return specGroups;
    }
}
