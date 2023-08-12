package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

/**
 * @Author Cheems
 * @Date 2023/8/11 17:37
 * @PackageName:com.sky.service
 * @ClassName: UserService
 * @Description: TODO
 * @Version 1.0
 */
public interface UserService {

    User wxLogin(UserLoginDTO userLoginDTO);
}
