package jmu.controller;

import jmu.common.R;
import jmu.rabbitmq.publisher.BasicPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 功能描述
 *
 * @author: 林振鑫
 * @date: 2022年12月12日 21:18
 */
@RestController
@RequestMapping("/notice")
@Slf4j
public class NoticeController {

    @Autowired
    private BasicPublisher basicPublisher;

    @GetMapping(value = "/sendMsg")
    public R<String> senMsg(String msg){
        if(null == msg){
            basicPublisher.sendMsg("空的");
        }else{
            basicPublisher.sendMsg(msg);
        }
        return R.success("成功广播公告！");
    }

}
