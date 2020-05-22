package com.leyou.user.controller;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册账号和手机号验证
     * @param data      验证的数据
     * @param type      验证类型（1：账号，2：手机号）
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkData(@PathVariable("data") String data,@PathVariable("type") Integer type){
        return ResponseEntity.ok(userService.checkData(data,type));
    }

    /**
     * 发送短息
     * @param phone  手机号
     * @return
     */
    @PostMapping("code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone") String phone){
        userService.sendCode(phone);
        return  ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 用户注册
     * @param user      用户信息
     * @param code      手机验证码
     * @return
     * @Valid   数据校验
     */
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, @RequestParam("code") String code){
        userService.register(user,code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 用户登陆验证，由登陆授权服务调用
     * @return
     */
    @GetMapping("query")
    public ResponseEntity<User> query(@RequestParam("username")String username,@RequestParam("password") String password){
        return ResponseEntity.ok(userService.query(username,password));
    }

}