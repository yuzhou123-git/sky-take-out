package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 店铺管理（管理端）
 * <p>
 * Controller 只负责：接收请求 → 参数校验 → 调用 Service → 返回结果。
 * Redis 的读写全部下沉到 ShopServiceImpl，这样以后把存储换成
 * 「MySQL + 缓存」时，这个类一行都不用改。
 * <p>
 * 注意：类名和用户端的 ShopController 重名，必须用 @RestController("adminShopController")
 * 指定 bean 名称，否则启动时会因 bean 名冲突报 ConflictingBeanDefinitionException。
 */
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺操作接口")
@Slf4j
public class ShopController {

    @Autowired
    private ShopService shopService;

    /**
     * 设置店铺的营业状态
     *
     * @param status 0 打烊，1 营业中
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺的营业状态")
    public Result setStatus(@PathVariable Integer status) {
        // 参数校验：本项目没有引入 spring-boot-starter-validation，
        // 用不了 @Range 注解，所以在方法里手动校验
        if (status == null || (status != 0 && status != 1)) {
            log.warn("非法的营业状态值：{}", status);
            return Result.error("营业状态只能是 0（打烊）或 1（营业）");
        }

        log.info("设置店铺的营业状态为：{}", status == 1 ? "营业中" : "打烊中");
        shopService.setShopStatus(status);
        return Result.success();
    }

    /**
     * 获取店铺的营业状态
     *
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺的营业状态")
    public Result<Integer> getStatus() {
        Integer status = shopService.getShopStatus();
        // 用 Integer.valueOf(1).equals(status) 而不是 status == 1，
        // 避免 status 为 null 时自动拆箱抛 NullPointerException
        log.info("获取到店铺的营业状态为：{}", Integer.valueOf(1).equals(status) ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
