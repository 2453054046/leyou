package com.leyou.user.api;

import com.leyou.user.pojo.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface UserApi {
    /**
     * 用户注册验证
     * @return
     */
    @GetMapping("query")
    public User query(@RequestParam("username")String username,@RequestParam("password") String password);
}
