package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.result.Result;
import com.sky.vo.OrderVO;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param order
     */
    void insert(Orders order);

    /**
     * 分页条件查询并按下单时间排序
     * @param ordersPageQueryDTO
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);



    /**
     * 根据id查询订单
     * @param id
     */
    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    /**
     * 根据订单号 + 用户id 查询订单
     * <p>
     * 支付时必须带上 userId：否则用户 A 拿到用户 B 的订单号就能替别人"付款"，
     * 或者把别人的订单状态改掉（越权）。
     * <p>
     * 注意：多参数必须用 @Param 命名，否则 XML/注解里只能写 #{param1} #{param2}，顺序一乱就错位。
     */
    @Select("select * from orders where number = #{number} and user_id = #{userId}")
    Orders getByNumberAndUserId(@Param("number") String number, @Param("userId") Long userId);

    /**
     * 动态更新订单
     * @param orders 只设置需要修改的字段，其余为 null 的字段不会被更新
     */
    void update(Orders orders);

    /**
     * 根据状态统计订单数量
     * @param status
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);




}
