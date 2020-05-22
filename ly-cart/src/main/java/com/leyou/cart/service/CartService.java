package com.leyou.cart.service;


import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.utlis.enums.ExceptionEnum;
import com.leyou.common.utlis.exception.LyException;
import com.leyou.common.utlis.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX="cart:uid";
    /**
     * 新增购物车
     * @param cart
     */
    public void addCart(Cart cart) {
        //获得登陆用户
        UserInfo user = UserInterceptor.getUser();
        //存入redis的key
        String key = KEY_PREFIX + user.getId();
        //redis的hkey
        String hkey = cart.getSkuId().toString();
        //记录当前cart的数量
        Integer num = cart.getNum();
        //获得当前用户的购物车
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        //判断当前用户的redis购物车是否存在该商品
        if(operations.hasKey(hkey)){
            //存在修改数量
            String s = operations.get(hkey).toString();
            cart = JsonUtils.parse(s, Cart.class);
            cart.setNum(cart.getNum()+num);
        }
        //写回redis
        operations.put(hkey,JsonUtils.toString(cart));
    }
    /**
     * 获得redis中用户的购物车所有数据
     * @return
     */
    public List<Cart> queryCartList() {
        //获得登陆用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key = KEY_PREFIX + user.getId();

        if(!redisTemplate.hasKey(key)){
            //不存在购物车返回404
            throw new LyException(ExceptionEnum.USER_CART_LIST_FOUND);
        }
        //获得登陆用户的购物车
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);

        //遍历购物车数据转换cart
        List<Cart> carts = operations.values().stream().map(o -> JsonUtils.parse(o.toString(), Cart.class)).collect(Collectors.toList());
        return carts;
    }

    /**
     * 修改redis商品数量
     * @param skuId
     * @param num
     * @return
     */
    public void updateNum(Long skuId, Integer num) {
        // 获取登录用户
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();
        BoundHashOperations<String, Object, Object> operations = this.redisTemplate.boundHashOps(key);
        // 获取购物车
        Cart cart = JsonUtils.parse(operations.get(skuId.toString()).toString(),Cart.class);
        cart.setNum(num);
        // 写入购物车
        operations.put(skuId.toString(), JsonUtils.toString(cart));
    }

    /**
     * 删除redis中的商品
     * @param skuId
     * @return
     */
    public void deleteCart(String skuId) {
        // 获取登录用户
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();
        //删除
        redisTemplate.opsForHash().delete(key,skuId.toString());
    }
}
