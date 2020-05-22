package com.leyou.user.service;

import com.leyou.common.utlis.enums.ExceptionEnum;
import com.leyou.common.utlis.exception.LyException;
import com.leyou.common.utlis.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.util.CodecUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    AmqpTemplate amqpTemplate;

    //redis注解：继承RedisTemplate，功能：存取为字符不会乱码
    @Autowired
    StringRedisTemplate redisTemplate;

    //存入redis的前缀
    private static final String KEY_PREFIX = "user:verify:phone";

    /**
     * 用户注册账号和手机号验证
     * @param data      验证的数据
     * @param type      验证类型（1：账号，2：手机号）
     * @return
     */
    public Boolean checkData(String data, Integer type) {
        //查询数据是否存在该数据
        User user = new User();
        switch (type){      //类型确认
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
             default:
                 throw new LyException(ExceptionEnum.INVALID_USER_DATA_TYPE);
        }
        //userMapper.selectCount :查找数据是否存在该映射类的数据返回1或0
        int i = userMapper.selectCount(user);
        System.out.println(i);
        return userMapper.selectCount(user) == 0 ;
    }

    /**
     * 发送短息
     * @param phone  手机号
     * @return
     */
    public void sendCode(String phone) {
        //生成存入redis的key
        String key = KEY_PREFIX + phone;
        //生成随机数  自定义类生成1-10的6位数
        String code = NumberUtils.generateCode(6);
        //准备向短信服务发送的数据格式
        Map<String,String> map = new HashMap<>();
        map.put("phone",phone);
        map.put("name",code);
        //发送验证码  amqpTemplate.convertAndSend("交换机","消息标记",消息数据);
        amqpTemplate.convertAndSend("ly.sms.exchange","sms.verify.code",map);
        //将验证码存入redis，设置过期时间5分钟
        redisTemplate.opsForValue().set(key,code,5, TimeUnit.MINUTES);
    }

    /**
     * 用户注册
     * @param user      用户信息
     * @param code      手机验证码
     * @return
     */
    public void register(User user, String code) {
        //从redis中获得验证码
        String redisCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        //判断验证码是否正确
        if(!StringUtils.equals(code,redisCode)){
            return;
        }
        //生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        //加盐加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));
        //新增用户
        user.setId(null);
        user.setCreated(new Date());
        userMapper.insertSelective(user);
    }

    /**
     * 用户登陆验证
     * @param username
     * @param password
     * @return
     */
    public User query(String username, String password) {
        //查询数据库
        User record = new User();
        record.setUsername(username);
        User user = userMapper.selectOne(record);
        //校验用户
        if(user ==null && !user.getPassword().equals(CodecUtils.md5Hex(password, user.getSalt()))){
            throw new LyException(ExceptionEnum.USER_LOGIN_FOUND);
        }
        return user;
    }
}