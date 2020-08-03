package cn.xww.miaosha.controller;

import cn.xww.miaosha.domain.MiaoshaUser;
import cn.xww.miaosha.redis.GoodsKey;
import cn.xww.miaosha.redis.RedisService;
import cn.xww.miaosha.result.Result;
import cn.xww.miaosha.service.GoodsService;
import cn.xww.miaosha.service.MiaoshaUserService;
import cn.xww.miaosha.vo.GoodsDetailVo;
import cn.xww.miaosha.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@Controller
@RequestMapping("/goods")
public class GoodsController {

	@Autowired
	MiaoshaUserService userService;

	@Autowired
	RedisService redisService;

	@Autowired
	GoodsService goodsService;

	@Autowired
	ThymeleafViewResolver thymeleafViewResolver;

	@Autowired
	ApplicationContext applicationContext;



//	@RequestMapping("/to_list")
//	public String list(Model model,MiaoshaUser user) {
//		model.addAttribute("user", user);
//		//查询商品列表
//		List<GoodsVo> goodsList = goodsService.listGoodsVo();
//		//System.out.println(goodsList.toString());
//		model.addAttribute("goodsList", goodsList);
//		return "goods_list";
//	}
@RequestMapping(value="/to_list", produces="text/html")
@ResponseBody
public String list(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user) {
	model.addAttribute("user", user);
	//取缓存
	String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
	if(!StringUtils.isEmpty(html)) {
		return html;
	}
	List<GoodsVo> goodsList = goodsService.listGoodsVo();
	model.addAttribute("goodsList", goodsList);
//    	 return "goods_list";
	SpringWebContext ctx = new SpringWebContext(request,response,
			request.getServletContext(),request.getLocale(), model.asMap(), applicationContext );
	//缓存取不到，就手动渲染,使用thymeleaf引擎，"goods_list"模板名
	html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
	//保存至缓存，下次就能取到
	if(!StringUtils.isEmpty(html)) {
		redisService.set(GoodsKey.getGoodsList, "", html);
	}
	return html;
}

	@RequestMapping(value="/to_detail2/{goodsId}",produces="text/html")
	@ResponseBody
	public String detail2(HttpServletRequest request, HttpServletResponse response, Model model,MiaoshaUser user,
						  @PathVariable("goodsId")long goodsId) {
		model.addAttribute("user", user);

		//取缓存
		String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
		if(!StringUtils.isEmpty(html)) {
			return html;
		}
		//手动渲染
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		model.addAttribute("goods", goods);

		long startAt = goods.getStartDate().getTime();
		long endAt = goods.getEndDate().getTime();
		long now = System.currentTimeMillis();

		int miaoshaStatus = 0;
		int remainSeconds = 0;
		if(now < startAt ) {//秒杀还没开始，倒计时
			miaoshaStatus = 0;
			remainSeconds = (int)((startAt - now )/1000);
		}else  if(now > endAt){//秒杀已经结束
			miaoshaStatus = 2;
			remainSeconds = -1;
		}else {//秒杀进行中
			miaoshaStatus = 1;
			remainSeconds = 0;
		}
		model.addAttribute("miaoshaStatus", miaoshaStatus);
		model.addAttribute("remainSeconds", remainSeconds);
//        return "goods_detail";

		SpringWebContext ctx = new SpringWebContext(request,response,
				request.getServletContext(),request.getLocale(), model.asMap(), applicationContext );
		html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
		if(!StringUtils.isEmpty(html)) {
			redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html);
		}
		return html;
	}

	@RequestMapping(value="/detail/{goodsId}")
	@ResponseBody
	public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model,MiaoshaUser user,
										@PathVariable("goodsId")long goodsId) {
		model.addAttribute("user", user);
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		model.addAttribute("goods", goods);

		long startAt = goods.getStartDate().getTime();
		long endAt = goods.getEndDate().getTime();
		long now = System.currentTimeMillis();
		int miaoshaStatus = 0;
		int remainSeconds = 0;
		if(now < startAt ) {//秒杀还没开始，倒计时
			miaoshaStatus = 0;
			remainSeconds = (int)((startAt - now )/1000);
		}else  if(now > endAt){//秒杀已经结束
			miaoshaStatus = 2;
			remainSeconds = -1;
		}else {//秒杀进行中
			miaoshaStatus = 1;
			remainSeconds = 0;
		}
		model.addAttribute("status", miaoshaStatus);
		model.addAttribute("remailSeconds", remainSeconds);
		GoodsDetailVo vo = new GoodsDetailVo();
		vo.setGoods(goods);
		vo.setUser(user);
		vo.setRemainSeconds(remainSeconds);
		vo.setMiaoshaStatus(miaoshaStatus);
		return Result.success(vo);
	}


//	@RequestMapping(value="/to_detail/{goodsId}",produces="text/html")
//	@ResponseBody
//	public String detail2(HttpServletRequest request, HttpServletResponse response, Model model,MiaoshaUser user,
//						  @PathVariable("goodsId")long goodsId) {
//		model.addAttribute("user", user);
//
//		//取缓存
//		String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
//		if(!StringUtils.isEmpty(html)) {
//			return html;
//		}
//		//手动渲染
//		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
//		model.addAttribute("goods", goods);
//
//		long startAt = goods.getStartDate().getTime();
//		long endAt = goods.getEndDate().getTime();
//		long now = System.currentTimeMillis();
//
//		int miaoshaStatus = 0;
//		int remainSeconds = 0;
//		if(now < startAt ) {//秒杀还没开始，倒计时
//			miaoshaStatus = 0;
//			remainSeconds = (int)((startAt - now )/1000);
//		}else  if(now > endAt){//秒杀已经结束
//			miaoshaStatus = 2;
//			remainSeconds = -1;
//		}else {//秒杀进行中
//			miaoshaStatus = 1;
//			remainSeconds = 0;
//		}
//		model.addAttribute("miaoshaStatus", miaoshaStatus);
//		model.addAttribute("remainSeconds", remainSeconds);
////        return "goods_detail";
//
//		SpringWebContext ctx = new SpringWebContext(request,response,
//				request.getServletContext(),request.getLocale(), model.asMap(), applicationContext );
//		html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
//		if(!StringUtils.isEmpty(html)) {
//			//不同页面不同的详情
//			redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html);
//		}
//		return html;
//	}

//	@RequestMapping("/to_detail/{goodsId}")
//	public String detail(Model model,MiaoshaUser user,
//						 @PathVariable("goodsId")long goodsId) {
//		model.addAttribute("user", user);
//
//		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
//		model.addAttribute("goods", goods);
//
//		long startAt = goods.getStartDate().getTime();
//		long endAt = goods.getEndDate().getTime();
//		long now = System.currentTimeMillis();
//
//		int miaoshaStatus = 0;
//		int remainSeconds = 0;
//		if(now < startAt ) {//秒杀还没开始，倒计时
//			miaoshaStatus = 0;
//			remainSeconds = (int)((startAt - now )/1000);
//		}else  if(now > endAt){//秒杀已经结束
//			miaoshaStatus = 2;
//			remainSeconds = -1;
//		}else {//秒杀进行中
//			miaoshaStatus = 1;
//			remainSeconds = 0;
//		}
//		model.addAttribute("miaoshaStatus", miaoshaStatus);
//		model.addAttribute("remainSeconds", remainSeconds);
//		return "goods_detail";
//	}

	
//    @RequestMapping("/to_list")
//	//把token成用户
////    public String list(HttpServletResponse response,Model model,
////					   @CookieValue(value = MiaoshaUserService.COOKI_NAME_TOKEN,required = false) String cookieToken,
////					   @RequestParam(value = MiaoshaUserService.COOKI_NAME_TOKEN,required = false) String paramToken) {
////    	if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)){
////    		return "login";
////		}
////		String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
////    	MiaoshaUser user = userService.getByToken(response,token);
////    	model.addAttribute("user", user);
////        return "goods_list";
//	public String list(Model model,MiaoshaUser user) {
//		model.addAttribute("user", user);
//		return "goods_list";
//
//    }
    
}
