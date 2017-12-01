package com.scoprion.mall.backstage.mapper;

import com.github.pagehelper.Page;
import com.scoprion.mall.domain.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author ycj
 * @version V1.0 <>
 * @date 2017-11-21 17:05
 */
@Mapper
public interface MenuMapper {

    /**
     * 新增
     *
     * @param sysMenu
     * @return
     */
    Integer add(SysMenu sysMenu);

    /**
     * 条件查询
     *
     * @param searchKey
     * @return
     */
    Page<SysMenu> findByCondition(@Param("searchKey") String searchKey);

    /**
     * 修改
     *
     * @param sysMenu
     * @return
     */
    Integer modify(SysMenu sysMenu);

    /**
     * 登录之后初始化
     *
     * @param userId
     * @return
     */
    Page<SysMenu> init(@Param("userId") String userId);

    /**
     * 根据id查询详情
     *
     * @param id
     * @return
     */
    SysMenu findById(@Param("id") Long id);

    /**
     * 停用/删除
     *
     * @param id
     * @return
     */
    Integer deleteById(@Param("id") Long id);

    /**
     * 名称-url校验
     *
     * @param name
     * @param url
     * @return
     */
    Integer validByNameAndUrl(@Param("name") String name,
                              @Param("url") String url);

    /**
     * 名称-id-url校验-id
     *
     * @param id
     * @param name
     * @param url
     * @return
     */
    Integer validByIdAndNameAndUrl(@Param("id") Long id,
                                   @Param("name") String name,
                                   @Param("url") String url);

    /**
     * 校验是不是管理员
     *
     * @param userId
     * @return
     */
    Integer validAdmin(@Param("userId") String userId);

    /**
     * 查询根节点
     *
     * @return
     */
    List<SysMenu> findRootMenuList();


    /**
     * 根据userid-url查询列表
     *
     * @param url
     * @param userId
     * @return
     */
    List<SysMenu> findByUrlAndUserId(@Param("url") String url, @Param("userId") String userId);

    /**
     * 根据角色id查询菜单列表
     *
     * @param roleId
     * @param parentId
     * @param type     type=0  一级菜单 type=1  二级菜单
     * @return
     */
    List<SysMenu> findMenuListByRoleId(@Param("roleId") Long roleId,
                                       @Param("parentId") Long parentId,
                                       @Param("type") Integer type);
}
