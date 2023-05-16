package top.rectorlee.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.rectorlee.entity.Product;
import top.rectorlee.mapper.ProductMapper;
import top.rectorlee.service.ProductService;
import top.rectorlee.utils.HttpStatus;
import top.rectorlee.utils.RestResult;

import java.util.List;

/**
 * @author Lee
 */
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductMapper productMapper;

    @Override
    public RestResult selectProductList() {
        List<Product> list = productMapper.selectProductList();
        log.info("商品列表为: {}", list);

        return new RestResult<>(HttpStatus.SUCCESS, "商品列表查询成功", list, list.size());
    }
}
