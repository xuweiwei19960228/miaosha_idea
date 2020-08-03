package cn.xww.miaosha.controller;

import cn.xww.miaosha.domain.User;
import cn.xww.miaosha.rabbitmq.MQSender;
import cn.xww.miaosha.redis.RedisService;
import cn.xww.miaosha.redis.UserKey;
import cn.xww.miaosha.result.Result;
import cn.xww.miaosha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;



@Controller
@RequestMapping("/demo")
public class SampleController {

	@Autowired
    UserService userService;
	
	@Autowired
    RedisService redisService;

	@Autowired
    MQSender sender;

//	@RequestMapping("/mq")
//    @ResponseBody
//    public Result<String> mq(){
//	    sender.send("hello,xww,mq");
//	    return  Result.success("Hello,mqRedirect");
//    }
//
//    @RequestMapping("/mq/topic")
//    @ResponseBody
//    public Result<String> topic(){
//        sender.sendTopic("hello,xww,mqTopic");
//        return  Result.success("Hello,mqTopic");
//    }
//
//    @RequestMapping("/mq/fanout")
//    @ResponseBody
//    public Result<String> fanout(){
//        sender.sendFanout("hello,xww,mqFanout");
//        return  Result.success("Hello,maFanout");
//    }
//
//    @RequestMapping("/mq/header")
//    @ResponseBody
//    public Result<String> header(){
//        sender.sendHeader("hello,xww,mqHeader");
//        return  Result.success("Hello,mqHeader");
//    }




    //
//    @RequestMapping("/hello")
//    @ResponseBody
//    public Result<String> home() {
//        return Result.success("Hello，world");
//    }
//
//    @RequestMapping("/error")
//    @ResponseBody
//    public Result<String> error() {
//        return Result.error(CodeMsg.SESSION_ERROR);
//    }
//
//    @RequestMapping("/hello/themaleaf")
//    public String themaleaf(Model model) {
//        model.addAttribute("name", "Joshua");
//        return "hello";
//    }
//
    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> dbGet() {
    	User user = userService.getById(1);
        return Result.success(user);
    }
    
    
    @RequestMapping("/db/tx")
    @ResponseBody
    public Result<Boolean> dbTx() {
    	userService.tx();
        return Result.success(true);
    }

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet() {
    	User  user  = redisService.get(UserKey.getById, ""+1, User.class);
        return Result.success(user);
    }
//
    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet() {
    	User user  = new User();
    	user.setId(4);
    	user.setName("xww123");
    	redisService.set(UserKey.getById, ""+1, user);//UserKey:id1
        return Result.success(true);
    }

    
}
