package com.sky.service.impl;

import com.sky.constant.RedisConstant;
import com.sky.constant.StatusConstant;
import com.sky.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 店铺业务实现
 * <p>
 * 营业状态存在 Redis 里（而不是数据库），原因：
 * 1. 它是全局单值，建表只会有「一行一列」，不符合关系型表为多行设计的初衷
 * 2. 读多写极少：小程序每次打开都要查，管理员可能一天只改一次
 * 3. 无事务、无关联，不需要关系型数据库的能力
 * <p>
 * 但要知道代价：Redis 默认不开启 AOF 持久化，重启可能丢数据。
 * 生产环境更稳妥的做法是「MySQL 存权威值 + Redis 做缓存」，
 * 这里按课程设计先用 Redis 单点存储。
 */
@Service
@Slf4j
public class ShopServiceImpl implements ShopService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取店铺营业状态
     * <p>
     * 关键点：Redis 中不存在该键时 get() 返回 null，
     * 这里兜底返回「打烊」，避免上层出现 null 拆箱导致的 NullPointerException。
     *
     * @return 0 打烊，1 营业中
     */
    @Override
    public Integer getShopStatus() {
        Object value = redisTemplate.opsForValue().get(RedisConstant.SHOP_STATUS_KEY);

        if (value == null) {
            // 首次访问（或 Redis 重启后数据丢失）时，默认按打烊处理
            log.info("Redis 中未找到营业状态，默认返回：打烊");
            return StatusConstant.DISABLE;
        }

        // 正常情况存的是 Integer；做一次兼容处理，防止将来被改成字符串存
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return Integer.valueOf(value.toString());
    }

    /**
     * 设置店铺营业状态
     *
     * @param status 0 打烊，1 营业中
     */
    @Override
    public void setShopStatus(Integer status) {
        redisTemplate.opsForValue().set(RedisConstant.SHOP_STATUS_KEY, status);
        log.info("店铺营业状态已更新为：{}", Integer.valueOf(1).equals(status) ? "营业中" : "打烊中");
    }
}
