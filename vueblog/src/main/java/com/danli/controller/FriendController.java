package com.danli.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.danli.annotation.VisitLogger;
import com.danli.common.lang.Result;
import com.danli.entity.Blog;
import com.danli.entity.Friend;
import com.danli.entity.Visitor;
import com.danli.service.FriendService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author fanfanli
 * @since 2021-04-08
 */
@RestController

public class FriendController {


    @Autowired
    FriendService friendService;
    //查询所有友链
    @RequestMapping("/friend/all")
    public Result getFriendList(){
        List<Friend> list = friendService.lambdaQuery().eq(Friend::getIsPublished, 1).list();

        return Result.succ(list);
    }

    //分页查询友链
    @RequiresAuthentication
    @RequiresPermissions("user:read")
    @GetMapping("/friendList")
    public Result friendList(@RequestParam(defaultValue = "1") Integer currentPage,@RequestParam(defaultValue = "10") Integer pageSize) {

        Page page = new Page(currentPage, pageSize);
        IPage pageData = friendService.page(page, new QueryWrapper<Friend>().orderByDesc("create_time"));
        return Result.succ(pageData);
    }
    //友链浏览次数加一
    @VisitLogger(behavior = "点击友链")
    @RequestMapping("/friend/onclick")
    public Result addView(@RequestParam(name = "")String nickname ){
        if(nickname.equals("")){
            return Result.fail("访问出错");
        }
        Friend friend = friendService.getOne(new QueryWrapper<Friend>().eq("nickname",nickname));
        friend.setViews(friend.getViews()+1);
//        Friend temp = new Friend();
//        BeanUtil.copyProperties(friend, temp);
        friendService.saveOrUpdate(friend);
        return Result.succ(null);
    }


    @RequiresPermissions("user:update")
    @RequestMapping("friend/publish/{id}")
    public Result publish(@PathVariable(name = "id")Long id){
        Friend friend = friendService.getById(id);
        friend.setIsPublished(!friend.getIsPublished());
//        Friend temp = new Friend();
//        BeanUtil.copyProperties(friend, temp);
        friendService.saveOrUpdate(friend);
        return Result.succ(null);

    }


    //增改
    @RequiresPermissions("user:update")
    @RequiresAuthentication
    @PostMapping("/friend/update")
    public Result updateFriend(@Validated @RequestBody Friend friend){
        if(friend==null){
            return Result.fail("不能为空");

        }
        else{
            if (friend.getId()==null){
                friend.setCreateTime(LocalDateTime.now());
            }
            friendService.saveOrUpdate(friend);
        }
        return Result.succ(null);
    }


    @RequiresPermissions("user:create")
    @RequiresAuthentication
    @PostMapping("/friend/create")
    public Result createFriend(@Validated @RequestBody Friend friend){
        if(friend==null){
            return Result.fail("不能为空");
        }
        else{
            if (friend.getId()==null){
                friend.setCreateTime(LocalDateTime.now());
            }
            friendService.saveOrUpdate(friend);
        }
        return Result.succ(null);
    }

    //删除
    @RequiresRoles("role_root")
    @RequiresPermissions("user:delete")
    @RequiresAuthentication
    @GetMapping("/friend/delete/{id}")
    public Result delete(@PathVariable(name = "id") Long id) {

        if (friendService.removeById(id)) {
            return Result.succ(null);
        } else {
            return Result.fail("删除失败");
        }


    }
}
