package com.leyou.config;


import com.leyou.auth.util.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {
    private String pubKeyPath;          //公钥
    private String cookieName;          //cookie的名册
    private PublicKey publicKey;        //公钥pubKeyPath

    //对象一旦实例化后，就应该读取公钥和私钥,这个注解是在构造方法实例化执行
    @PostConstruct
    public void init() throws Exception {
        //公钥私钥如果不存在
        File pubPath = new File(pubKeyPath);
        //读取公钥和私钥
         this.publicKey = RsaUtils.getPublicKey(pubKeyPath);

    }

}
