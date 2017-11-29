package com.scoprion.mall.backstage.service.good;

import com.alibaba.druid.util.StringUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.scoprion.constant.Constant;
import com.scoprion.enums.CommonEnum;
import com.scoprion.mall.backstage.mapper.FileOperationMapper;
import com.scoprion.mall.backstage.mapper.GoodLogMapper;
import com.scoprion.mall.backstage.service.file.FileOperationServiceImpl;
import com.scoprion.mall.domain.GoodExt;
import com.scoprion.mall.domain.GoodLog;
import com.scoprion.mall.domain.Goods;
import com.scoprion.mall.backstage.mapper.GoodsMapper;
import com.scoprion.mall.domain.MallImage;
import com.scoprion.mall.domain.request.GoodRequestParams;
import com.scoprion.result.BaseResult;
import com.scoprion.result.PageResult;
import com.scoprion.utils.DateParamFormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created on 2017/9/29.
 * 运营后台商品控制器
 *
 * @author adming
 */
@Service
public class GoodsServiceImpl implements GoodsService {
    private final static Logger LOGGER = LoggerFactory.getLogger(FileOperationServiceImpl.class);

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodLogMapper goodLogMapper;

    @Autowired
    private FileOperationMapper fileOperationMapper;


    /**
     * 首页展示4件 限时购买商品
     *
     * @return
     */
    @Override
    public List<Goods> findLimit4ByTimeGoods() {
        return goodsMapper.findLimit4ByTimeGoods();
    }

    /**
     * 查询限时购买商品  分页展示
     *
     * @param pageNo   当前页
     * @param pageSize 每页条数
     * @return
     */
    @Override
    public PageResult findByPageAndLimit(int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        Page<Goods> page = goodsMapper.findByPageAndLimit();
        return new PageResult(page);
    }

    /**
     * 创建商品
     *
     * @param good
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResult add(GoodExt good) {
        int result = goodsMapper.add(good);
        if (result > 0) {
            //更新图片信息
            List<MallImage> imgList = good.getImgList();
            if (imgList != null && imgList.size() > 0) {
                for (MallImage mallImage : imgList) {
                    mallImage.setGoodId(good.getId());
                    fileOperationMapper.add(mallImage);
                }
            }
            return BaseResult.success("添加成功");
        }
        saveGoodLog(good.getGoodName(), "创建商品", good.getId());
        return BaseResult.error("mock_fail", "创建商品失败");
    }

    private void saveGoodLog(String goodName, String action, Long goodId) {
        GoodLog goodLog = new GoodLog();
        goodLog.setGoodName(goodName);
        goodLog.setGoodId(goodId);
        goodLog.setAction(action);
        goodLogMapper.add(goodLog);
    }

    /**
     * 优选商品
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Override
    public PageResult preferenceGiven(int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        Page<Goods> page = goodsMapper.preferenceGivenByPage();
        return new PageResult(page);
    }

    /**
     * 根据id查询商品详情
     *
     * @param goodsId
     * @return
     */
    @Override
    public BaseResult findByGoodId(Long goodsId) {
        GoodExt goods = goodsMapper.findById(goodsId);
        if (null == goods) {
            return BaseResult.notFound();
        }

        //获取图片列表
        List<MallImage> imgList = fileOperationMapper.findByCondition(goods.getId(), 0);
        goods.setImgList(imgList);
        return BaseResult.success(goods);
    }

    /**
     * 根据id修改商品信息
     *
     * @param good 商品信息
     * @return
     */
    @Override
    public BaseResult updateGood(GoodExt good) {
        if (good.getId() == null) {
            return BaseResult.parameterError();
        }
        GoodExt localGood = goodsMapper.findById(good.getId());
        if (localGood == null) {
            return BaseResult.error("unable_update", "未找到商品");
        }
        if (!StringUtils.isEmpty(localGood.getOnSale()) &&
                CommonEnum.ON_SALE.getCode().equals(localGood.getOnSale())) {
            //上架状态，不能修改
            return BaseResult.error("unable_update", "商品为上架状态，不能修改");
        }
        goodsMapper.updateGoods(good);
        List<MallImage> imgList = good.getImgList();
        if (imgList != null && imgList.size() > 0) {
            //清空原来的图片
            for (MallImage mallImage : imgList) {
                fileOperationMapper.deleteById(mallImage.getId());
            }
            //插入图片
            for (MallImage mallImage : imgList) {
                mallImage.setGoodId(good.getId());
                fileOperationMapper.add(mallImage);
            }
        }
        saveGoodLog(good.getGoodName(), "修改商品信息", good.getId());
        return BaseResult.success("修改成功");
    }


    /**
     * 商品上下架
     *
     * @param saleStatus saleStatus 1上架 0下架 默认上架  上下ON_SALE", "上架 OFF_SALE", "下架"),
     * @param goodsId    商品id
     * @return
     */
    @Override
    public BaseResult modifySaleStatus(String saleStatus, Long goodsId) {
        if (StringUtils.isEmpty(saleStatus) || null == goodsId) {
            return BaseResult.parameterError();
        }
        List<Long> idList = new ArrayList<>();
        idList.add(goodsId);
        int result = goodsMapper.batchModifySaleStatus(saleStatus, idList);
        if (result > 0) {
            String resultString = CommonEnum.ON_SALE.getCode().equals(saleStatus) ? "商品上架成功" : "商品下架成功";
            return BaseResult.success(resultString);
        }
        GoodExt good = goodsMapper.findById(goodsId);
        saveGoodLog(good.getGoodName(), CommonEnum.ON_SALE.getCode().equals(saleStatus) ? "商品上架" : "商品下架", goodsId);
        return BaseResult.error("006", CommonEnum.ON_SALE.getCode().equals(saleStatus) ? "商品上架失败" : "商品下架失败");
    }

    /**
     * 根据商品id删除商品
     *
     * @param id 商品id
     * @return
     */
    @Override
    public BaseResult deleteGoodsById(Long id) {
        Goods goods = goodsMapper.findById(id);
        if (CommonEnum.ON_SALE.getCode().equals(goods.getOnSale())) {
            return BaseResult.error("del_error", "删除失败，商品未下架，不能删除");
        }
        List<Long> idList = new ArrayList<>();
        idList.add(id);
        int result = goodsMapper.batchDeleteGood(idList);
        if (result > 0) {
            return BaseResult.success("删除商品成功");
        }
        saveGoodLog(goods.getGoodName(), "删除商品", goods.getId());
        return BaseResult.error("sysError", "删除商品失败");
    }

    /**
     * 批量删除商品
     *
     * @param idList 商品id集合
     * @return
     */
    @Override
    public BaseResult batchDeleteGood(List<Long> idList) {
        if (idList == null || idList.size() == 0) {
            return BaseResult.parameterError();
        }
        int result = goodsMapper.batchDeleteGood(idList);
        if (result == 0) {
            return BaseResult.error("delete_error", "商品未下架，不能删除");
        }
        if (idList.size() > result) {
            return BaseResult.success("部分商品未下架，不能删除，其余的已经删除成功");
        }
        idList.forEach(goodId -> {
            saveGoodLog("", "批量删除商品", goodId);
        });
        return BaseResult.success("删除成功");
    }

    @Override
    public BaseResult modifyGoodsDeduction(Long id, Integer count) {
        int result = goodsMapper.modifyGoodsDeduction(id, count);
        if (result > 0) {
            GoodExt good = goodsMapper.findById(id);
            saveGoodLog(good.getGoodName(), "修改商品库存", good.getId());
            return BaseResult.success("修改成功");
        }
        return BaseResult.error("modify-error", "修改失败");
    }

    /**
     * 批量商品上下架
     *
     * @param saleStatus  1上架 0下架 默认上架 上下ON_SALE", "上架 OFF_SALE", "下架"),
     * @param goodsIdList 商品id集合
     * @return
     */
    @Override
    public BaseResult batchModifySaleStatus(String saleStatus, List<Long> goodsIdList) {
        if (goodsIdList == null || goodsIdList.size() == 0) {
            return BaseResult.parameterError();
        }
        if (!CommonEnum.ON_SALE.getCode().equals(saleStatus) && !CommonEnum.OFF_SALE.getCode().equals(saleStatus)) {
            return BaseResult.parameterError();
        }
        goodsMapper.batchModifySaleStatus(saleStatus, goodsIdList);
        String action = CommonEnum.ON_SALE.getCode().equals(saleStatus) ? "商品批量上架" : "商品批量下架";
        goodsIdList.forEach(goodId -> saveGoodLog("", action, goodId));
        return BaseResult.success(action + "成功");
    }

    /**
     * 选择绑定活动的商品列表
     *
     * @param pageNo
     * @param pageSize
     * @param searchKey 模糊信息
     * @return
     */
    @Override
    public PageResult findForActivity(Integer pageNo, Integer pageSize, String searchKey,
                                      String goodNo, Long brandId, Long categoryId) {
        PageHelper.startPage(pageNo, pageSize);
        if (StringUtils.isEmpty(searchKey)) {
            searchKey = null;
        }
        if (!StringUtils.isEmpty(searchKey)) {
            searchKey = "%" + searchKey + "%";
        }
        List<GoodExt> result = goodsMapper.findForActivity(searchKey, goodNo, brandId, categoryId);
        return new PageResult(result);
    }

    @Override
    public PageResult findByCondition(GoodRequestParams goodRequestParams) {
        PageHelper.startPage(goodRequestParams.getPageNo(), goodRequestParams.getPageSize());
        if (StringUtils.isEmpty(goodRequestParams.getSearchKey())) {
            goodRequestParams.setSearchKey(null);
        }
        if (!StringUtils.isEmpty(goodRequestParams.getSearchKey())) {
            goodRequestParams.setSearchKey("%" + goodRequestParams.getSearchKey() + "%");
        }
        goodRequestParams.setStartDate(DateParamFormatUtil.formatDate(goodRequestParams.getStartDate()));
        goodRequestParams.setEndDate(DateParamFormatUtil.formatDate(goodRequestParams.getEndDate()));
        Page<GoodExt> page = goodsMapper.findByCondition(goodRequestParams);
        if (page == null) {
            return new PageResult();
        }
        return new PageResult(page);
    }

    /**
     * 选择绑定活动的商品列表
     */
    @Override
    public PageResult findByActivityId(GoodRequestParams requestParams) {
        PageHelper.startPage(requestParams.getPageNo(), requestParams.getPageSize());
        if (StringUtils.isEmpty(requestParams.getSearchKey())) {
            requestParams.setSearchKey(null);
        }
        if (!StringUtils.isEmpty(requestParams.getSearchKey())) {
            requestParams.setSearchKey("%" + requestParams.getSearchKey() + "%");
        }
        requestParams.setStartDate(DateParamFormatUtil.formatDate(requestParams.getStartDate()));
        requestParams.setEndDate(DateParamFormatUtil.formatDate(requestParams.getEndDate()));
        List<GoodExt> result = goodsMapper.findByActivityId(requestParams);
        return new PageResult(result);
    }

}
