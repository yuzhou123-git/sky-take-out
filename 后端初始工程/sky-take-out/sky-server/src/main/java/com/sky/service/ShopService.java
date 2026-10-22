package com.sky.service;

/**
 * 店铺相关业务
 */
public interface ShopService {

    /**
     * 获取店铺营业状态
     *
     * @return 0 打烊，1 营业中
     */
    Integer getShopStatus();

    /**
     * 设置店铺营业状态
     *
     * @param status 0 打烊，1 营业中
     */
    void setShopStatus(Integer status);
}
