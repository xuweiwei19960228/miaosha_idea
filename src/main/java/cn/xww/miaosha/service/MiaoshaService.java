package cn.xww.miaosha.service;

import cn.xww.miaosha.domain.MiaoshaOrder;
import cn.xww.miaosha.domain.MiaoshaUser;
import cn.xww.miaosha.domain.OrderInfo;
import cn.xww.miaosha.redis.MiaoshaKey;
import cn.xww.miaosha.redis.RedisService;
import cn.xww.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class MiaoshaService {
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;

	@Autowired
	RedisService redisService;

	@Transactional
	public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
//		//减库存 下订单
//		goodsService.reduceStock(goods);
//		//写订单，order_info maiosha_order
//		return orderService.createOrder(user, goods);

		//减库存  下订单  写入秒杀订单
		boolean success = goodsService.reduceStock(goods);
		if(success){
			return orderService.createOrder(user, goods);
			//return null;//如果这条减库存的SQL执行失败返回0，那么就直接返回，不要再执行下面的下订单了。
		}else {
			setGoodsOver(goods.getId());
			return null;
		}
		//order_info maiosha_order

	}

	public long getMiaoshaResult(Long userId, long goodsId) {
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
		if(order != null) {//秒杀成功
			return order.getOrderId();
		}else {
			boolean isOver = getGoodsOver(goodsId);
			if(isOver) {//卖完了没抢到
				return -1;
			}else {//没卖完继续轮询
				return 0;
			}
		}
	}

	private void setGoodsOver(Long goodsId) {
		redisService.set(MiaoshaKey.isGoodsOver, ""+goodsId, true);
	}
//有此key卖完了
	private boolean getGoodsOver(long goodsId) {
		return redisService.exists(MiaoshaKey.isGoodsOver, ""+goodsId);
	}


	public void reset(List<GoodsVo> goodsList) {
		goodsService.resetStock(goodsList);
		orderService.deleteOrders();
	}
	
}
