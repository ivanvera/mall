package com.scoprion.mall.order.service;

import com.scoprion.result.PageResult;

/**
 * Created on 2017/9/29.
 */
public interface OrderService {

    /**
     * 查询用户的订单列表
     *
     * @param pageNo   当前页
     * @param pageSize 每页条数
     * @param status   订单状态
     * @param userId   用户id
     * @return
     */
    PageResult findByPage(int pageNo, int pageSize, String status, Long userId);
}
