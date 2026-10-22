package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 店铺相关接口（用户端）
 * <p>
 * 顾客只能查询营业状态，不能修改，所以这里只有一个 GET 接口。
 * <p>
 * 注意：等 Day07 加上 JwtTokenUserInterceptor 拦截 /user/** 之后，
 * 这个接口必须加入白名单（excludePathPatterns("/user/shop/status")），
 * 否则未登录的顾客查不到营业状态，小程序首页会直接报 401。
 */
@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {

    @Autowired
    private ShopService shopService;

    /**
     * 获取店铺的营业状态
     *
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺的营业状态")
    public Result<Integer> getStatus() {
        Integer status = shopService.getShopStatus();
        log.info("获取到店铺的营业状态为：{}", Integer.valueOf(1).equals(status) ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
