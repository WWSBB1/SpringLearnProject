package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @Author Cheems
 * @Date 2023/8/12 18:40
 * @PackageName:com.sky.mapper
 * @ClassName: UserMapper
 * @Description: TODO
 * @Version 1.0
 */



@Mapper
public interface UserMapper {

    @Select("select * from user where openid=#{openid}")
    User getByOpenid(String openid);

    void insert(User user);
}
