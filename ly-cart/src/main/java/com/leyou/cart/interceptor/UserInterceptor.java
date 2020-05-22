package com.leyou.cart.interceptor;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.util.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.common.utlis.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截器，查看用户是否登录，如果登陆将cookie中的token取出解析放入线程域中供后面的线程服务使用
 */
@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    private JwtProperties prop;

    //定义线程域,存放登录用户
    private static final ThreadLocal<UserInfo> thread = new ThreadLocal<>();

    //定义构造方法，将拦截器放入MVC扩展类后在扩展类中注入JwtProperties类传入本类中
    public UserInterceptor(JwtProperties jwtProperties) {
        this.prop = jwtProperties;
    }

    //前置拦截
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        //获得cookie
        String cookie = CookieUtils.getCookieValue(request, prop.getCookieName());
        try {
            //解析cookie
            UserInfo user = JwtUtils.getInfoFromToken(cookie,prop.getPublicKey());
            //将用户信息放入线程
            thread.set(user);
            return true;//放行
        } catch (Exception e) {
            log.error("[购物车服务]：解析用户信息失败-->"+e);
            return false;
        }
    }
    //后置拦截
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //删除线程中的数据
        thread.remove();
    }

    /**
     * 取出线程中的数据
     * @return
     */
    public static UserInfo getUser(){
        return thread.get();
    }
}
