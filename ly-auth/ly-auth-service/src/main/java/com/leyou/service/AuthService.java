package com.leyou.service;


import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.util.JwtUtils;
import com.leyou.client.UserClient;
import com.leyou.config.JwtProperties;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {
    //用户表
    @Autowired
    UserClient userClient;

    @Autowired
    JwtProperties jwtProp;
    /**
     * 用户登录授权，生成公私密匙将公钥放到zool网关，生成的密文存入客户端cookie中
     * @param username
     * @param password
     * @return
     */
    public String authentication(String username, String password)  {
        try {
            //调用user服务的登陆校验
            User user = userClient.query(username, password);
            //生成token
            String token = JwtUtils.generateToken(
                    new UserInfo(user.getId(), user.getUsername()),
                    jwtProp.getPrivateKey(), jwtProp.getExpire());
            return token;
        }catch (Exception e){
            return null;
        }
    }
}
