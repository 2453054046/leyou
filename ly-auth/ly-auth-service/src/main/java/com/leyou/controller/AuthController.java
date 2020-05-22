package com.leyou.controller;


import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.util.JwtUtils;
import com.leyou.common.utlis.enums.ExceptionEnum;
import com.leyou.common.utlis.exception.LyException;
import com.leyou.common.utlis.utils.CookieUtils;
import com.leyou.config.JwtProperties;
import com.leyou.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    AuthService authService;

    @Autowired
    JwtProperties prop;

    /**
     * 用户登录授权，生成公私密匙将公钥放到zool网关，生成的密文存入客户端cookie中
     * @param username
     * @param password
     * @param request
     * @param response
     * @return
     */
    @PostMapping("accredit")
    public ResponseEntity<Void> authentication(
            String username, String password, HttpServletRequest request,
            HttpServletResponse response
    ){  //登陆校验
        String token = authService.authentication(username,password);
        if(StringUtils.isBlank(token)){
            throw new LyException(ExceptionEnum.USER_LOGIN_FOUND);
        }
        //将token写入cookie，并指定httpOnly为true，防止通过JS获取和修改
        //CookieUtils.setCookie(request,response,prop.getCookieName(),token,prop.getExpire());
        //CookieUtils.setCookie(request, response, prop.getCookieName(), token, 1800);
        CookieUtils.newBuilder(response).httpOnly().request(request).build(prop.getCookieName(),token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 校验用户登陆状态（是否已经登陆）
     * @param token         cookie的token
     * @param response
     * @param request
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(
            @CookieValue("LY_TOKEN")String token,HttpServletResponse response,HttpServletRequest request){

        try {
            //解析token
            UserInfo infoFromToken = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            //刷新token，重新生成token
            String newToken = JwtUtils.generateToken(infoFromToken, prop.getPrivateKey(), prop.getExpire());
            //写入cookie
            CookieUtils.newBuilder(response).httpOnly().request(request).build(prop.getCookieName(),newToken);
            //已经登录，返回用户信息
            return ResponseEntity.ok(infoFromToken);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }

    }
}
