package com.leyou.filters;


import com.leyou.auth.util.JwtUtils;
import com.leyou.common.utlis.utils.CookieUtils;
import com.leyou.config.FilterProperties;
import com.leyou.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 网关自定义过滤器
 */
@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class AuthFilter extends ZuulFilter {

    @Autowired
    JwtProperties prop;

    @Autowired
    FilterProperties filterProp;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;  //过滤器类型，前置过滤
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER-1;  //过滤器顺序
    }

    /**
     * 指定不要过滤的请求
     * @return
     */
    @Override
    public boolean shouldFilter() {
        // 获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        // 获取request
        HttpServletRequest req = ctx.getRequest();
        // 获取路径
        String requestURI = req.getRequestURI();
        // 判断白名单
        return !isAllowPath(requestURI);
    }

    /**
     * 遍历白名单查看请求前缀是否对应白名单的内容
     * @param requestURI
     * @return
     */
    private boolean isAllowPath(String requestURI) {
        // 定义一个标记

        // 遍历允许访问的路径
        for (String path : this.filterProp.getAllowPaths()) {
            // 然后判断是否是符合  startsWith：查看字符串是否以对应字符为前缀开头
            if(requestURI.startsWith(path)){
                return true;
            }
        }
        return false;
    }

    /**
     * 查看当前是否登陆
     * @return
     * @throws ZuulException
     */
    //过滤逻辑
    @Override
    public Object run() throws ZuulException {
        System.out.println("[网关自定义过滤器执行]");
        //获得上下文
        RequestContext currentContext = RequestContext.getCurrentContext();
        //获得request域
        HttpServletRequest request = currentContext.getRequest();
        //获得cookie的token
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        //解析token
        try {
            //已经登陆
            JwtUtils.getInfoFromToken(token,prop.getPublicKey());
        } catch (Exception e) {
            e.printStackTrace();
            //没有登陆
            currentContext.setSendZuulResponse(false);
            currentContext.setResponseStatusCode(403);
        }
        return null;//放行，默认true
    }
}
