package cn.xww.miaosha.service;

import java.util.List;

import cn.xww.miaosha.dao.GoodsDao;
import cn.xww.miaosha.domain.MiaoshaGoods;
import cn.xww.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class GoodsService {
	
	@Autowired
	GoodsDao goodsDao;
	
	public List<GoodsVo> listGoodsVo(){
		return goodsDao.listGoodsVo();
	}

	public GoodsVo getGoodsVoByGoodsId(long goodsId) {
		return goodsDao.getGoodsVoByGoodsId(goodsId);
	}
//
//	public int reduceStock(GoodsVo goods) {
////		MiaoshaGoods g = new MiaoshaGoods();
////		g.setGoodsId(goods.getId());
////		goodsDao.reduceStock(g);
//		MiaoshaGoods g = new MiaoshaGoods();
//		g.setGoodsId(goods.getId());
//		//flag标记这条SQL是否执行成功。0不成功，1成功。
//		int flag=goodsDao.reduceStock(g);
//		return flag;
//	}
public boolean reduceStock(GoodsVo goods) {
	MiaoshaGoods g = new MiaoshaGoods();
	g.setGoodsId(goods.getId());
	int ret = goodsDao.reduceStock(g);
	return ret > 0;
}

	public void resetStock(List<GoodsVo> goodsList) {
		for(GoodsVo goods : goodsList ) {
			MiaoshaGoods g = new MiaoshaGoods();
			g.setGoodsId(goods.getId());
			g.setStockCount(goods.getStockCount());
			goodsDao.resetStock(g);
		}
	}




}
