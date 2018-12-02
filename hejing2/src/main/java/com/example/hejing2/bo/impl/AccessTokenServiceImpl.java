package com.example.hejing2.bo.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.example.hejing2.bo.AccessTokenService;
import com.example.hejing2.dao.AccessTokenDao;
import com.example.hejing2.vo.AccessTokenEntity;


/**
 * @author 雷加伟
 *
 */
@Service
public class AccessTokenServiceImpl extends ServiceImpl<AccessTokenDao,AccessTokenEntity> implements AccessTokenService {

}
