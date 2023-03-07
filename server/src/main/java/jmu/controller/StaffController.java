package jmu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jmu.common.R;
import jmu.pojo.Staff;
import jmu.service.StaffService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
@Slf4j
@RestController
@RequestMapping("/staff")
public class StaffController {
    @Autowired
    private StaffService staffService;

    /**
     * 工作人员登录
     * @param request
     * @param staff
     * @return
     */
    @PostMapping("/login")
    public R<Staff> login(HttpServletRequest request, @RequestBody Staff staff){
        //1、将页面提交的密码password进行md5加密处理
        String password = staff.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Staff> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(Staff::getUsername,staff.getUsername());
        Staff st = staffService.getOne(queryWrapper);
        //3、如果没有查询到则返回登录失败结果
        if (st == null){
            return R.error("登陆失败，无此用户名");
        }
        //4、密码比对，如果不一致则返回登录失败结果
        if (!st.getPassword().equals(password)){
            return R.error("登录失败，密码错误");
        }
        //5、查看工作人员状态，如果为已禁用状态，则返回工作人员已禁用结果
        if (st.getStatus() == 0){
            return R.error("账号已禁用");
        }
        //6、登录成功，将工作人员id存入Session并返回登录成功结果
        request.getSession().setAttribute("staff",st.getId());
        return R.success(st);

    }

    /**
     * 工作人员退出登录
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("staff");
        return R.success("退出成功");
    }

    /**
     * 新增工作人员信息
     * @param staff
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Staff staff){
        staff.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        staffService.save(staff);
        return R.success("新增工作人员成功");
    }

    /**
     * 工作人员分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        Page pageInfo = new Page(page,pageSize);
        LambdaQueryWrapper<Staff> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name),Staff::getName,name)
                .orderByDesc(Staff::getUpdateTime);
        staffService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据id修改工作人员信息
     * @param staff
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Staff staff){
        staffService.updateById(staff);
        return R.success("修改成功");
    }

    /**
     * 根据id查询工作人员信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Staff> getById(@PathVariable Long id){
        Staff staff = staffService.getById(id);
        if (staff != null) {
            return R.success(staff);
        }else return R.error("查询不到该工作人员信息");

    }
}
