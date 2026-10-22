package com.sky.constant;

/**
 * Redis 相关的键名常量
 * <p>
 * 统一在这里维护，避免各个类里硬编码字符串导致键名不一致。
 * <p>
 * 命名采用「项目:模块:属性」的冒号分层格式，这是 Redis 社区的通用约定：
 * - 便于用 SCAN/KEYS 按前缀批量查找（SKY:* 或 SKY:SHOP:*）
 * - 多个项目共用同一个 Redis 实例时不会互相覆盖
 * - 后期接入 Redis Cluster 也能保持一致的命名习惯
 */
public class RedisConstant {

    /**
     * 店铺营业状态
     * <p>
     * 值类型：Integer
     * 取值：0 打烊，1 营业中
     * <p>
     * 注意：这里加了 SKY: 前缀。如果直接用 SHOP_STATUS 这种通用名字，
     * 一旦同一个 Redis 上跑了别的项目（比如 hanye-take-out 也用 database 1），
     * 就会出现键名冲突、互相覆盖的问题。
     */
    public static final String SHOP_STATUS_KEY = "SKY:SHOP:STATUS";
}
