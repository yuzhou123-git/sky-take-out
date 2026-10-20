package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类管理业务实现
 * <p>
 * 注意：本项目目前没有 com.sky.aspect.AutoFillAspect 切面，
 * 因此 create_time / update_time / create_user / update_user 这四个公共字段
 * 需要在每个方法里手动赋值，不能依赖 @AutoFill 注解自动填充。
 */
@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增分类
     * <p>
     * 业务规则：
     * 1. 分类名称不能重复（数据库 name 字段有唯一索引，重复时会抛 SQLIntegrityConstraintViolationException）
     * 2. 新增的分类默认是禁用状态（status = 0），需要手动启用
     *
     * @param categoryDTO
     */
    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        // 属性拷贝：把 DTO 中的 type、name、sort 拷到实体对象
        BeanUtils.copyProperties(categoryDTO, category);

        // 新增分类默认禁用
        category.setStatus(StatusConstant.DISABLE);

        // 设置创建时间、修改时间
//        category.setCreateTime(LocalDateTime.now());
//        category.setUpdateTime(LocalDateTime.now());
//
//        // 设置创建人、修改人（当前登录员工的 id）
//        category.setCreateUser(BaseContext.getCurrentId());
//        category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.insert(category);
    }

    /**
     * 分页查询
     * <p>
     * PageHelper 的工作原理：调用 startPage 之后紧跟着的第一个 MyBatis 查询会被自动加上 limit，
     * 并且把总记录数塞进返回的 Page 对象里。
     * 注意 startPage 必须紧挨着查询语句，中间不能插入别的数据库操作，否则分页会错乱。
     *
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());

        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);

        // Page 是 ArrayList 的子类，额外带 total 属性
        long total = page.getTotal();
        List<Category> records = page.getResult();

        return new PageResult(total, records);
    }

    /**
     * 根据 id 删除分类
     * <p>
     * 删除前必须校验关联关系：
     * 1. 该分类下有没有菜品，有则不允许删除
     * 2. 该分类下有没有套餐，有则不允许删除
     *
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        // 查询当前分类是否关联了菜品
        Integer dishCount = dishMapper.countByCategoryId(id);
        if (dishCount != null && dishCount > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }

        // 查询当前分类是否关联了套餐
        Integer setmealCount = setmealMapper.countByCategoryId(id);
        if (setmealCount != null && setmealCount > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        // 校验通过，执行删除
        categoryMapper.deleteById(id);
    }

    /**
     * 修改分类
     * <p>
     * CategoryMapper.xml 中的 update 语句使用了动态 SQL（<set> + <if>），
     * 只会更新传入的非 null 字段，所以这里不需要先查再改。
     *
     * @param categoryDTO
     */
    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);

//        category.setUpdateTime(LocalDateTime.now());
//        category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.update(category);
    }

    /**
     * 启用、禁用分类
     * <p>
     * 只更新 status、update_time、update_user 三个字段，其余字段传 null 由动态 SQL 跳过。
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();

        categoryMapper.update(category);
    }

    /**
     * 根据类型查询分类
     * <p>
     * CategoryMapper.xml 的 list 语句固定过滤 status = 1（只查启用的），
     * 管理端要展示全部状态时不能用这个方法，要改用分页查询。
     *
     * @param type
     * @return
     */
    @Override
    public List<Category> list(Integer type) {
        return categoryMapper.list(type);
    }
}
