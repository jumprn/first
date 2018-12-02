package com.example.hejing2.bo.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.example.hejing2.bo.UserService;
import com.example.hejing2.dao.UserDao;
import com.example.hejing2.vo.UserEntity;

@Service
public class UserServiceImpl extends ServiceImpl<UserDao, UserEntity> implements UserService {


}
