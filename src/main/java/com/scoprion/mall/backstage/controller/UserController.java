package com.scoprion.mall.backstage.controller;

import com.scoprion.mall.domain.Member;
import com.scoprion.mall.backstage.service.user.UserService;
import com.scoprion.result.BaseResult;
import com.scoprion.result.PageResult;
import com.scoprion.utils.IPUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Created on 2017/9/26.
 *
 * @author adming
 */
@RestController
@RequestMapping("/backstage/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 后台登录
     *
     * @param mobile   手机号
     * @param password 密码
     * @param request  请求
     * @return
     */
    @ApiOperation(value = "后台登录")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public BaseResult login(String mobile, String password, HttpServletRequest request) throws Exception {
        String ip = IPUtil.getIPAddress(request);
        return userService.login(mobile, password, ip);
    }


    /**
     * 管理后台注册
     *
     * @param member  Member
     * @param request HttpServletRequest
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "管理后台注册")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public BaseResult register(@RequestBody Member member, HttpServletRequest request) throws Exception {
        String ip = IPUtil.getIPAddress(request);
        return userService.register(member, ip);
    }

    /**
     * 修改个人资料
     *
     * @param member
     * @return
     */
    @ApiOperation(value = "修改个人信息")
    @RequestMapping(value = "/modify", method = RequestMethod.POST)
    public BaseResult modifyUserInfo(@RequestBody Member member) {
        return userService.modifyUserInfo(member);
    }

    /**
     * 后台系统退出登录
     *
     * @param mobile 手机号
     * @return BaseResult
     */
    @ApiOperation(value = "后台系统退出登录")
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public BaseResult backstageLogout(String mobile) {
        return userService.backstageLogout(mobile);
    }

    /**
     * 分页查询会员列表
     *
     * @param pageNo
     * @param pageSize
     * @param startDate
     * @param endDate
     * @param searchKey
     * @return
     */
    @ApiOperation(value = "会员列表(运营平台)")
    @RequestMapping(value = "/userList", method = RequestMethod.GET)
    public PageResult findByPage(int pageNo, int pageSize, String startDate,
                                 String endDate, String searchKey) {
        return userService.findByPage(pageNo, pageSize, startDate, endDate, searchKey);
    }


    /**
     * 根据id查询详情
     *
     * @param id 主键
     * @return BaseResult
     */
    @ApiOperation(value = "根据id查询详情")
    @RequestMapping(value = "/findById", method = RequestMethod.GET)
    public BaseResult findById(Long id) {
        return userService.findById(id);
    }
}