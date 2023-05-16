package top.rectorlee.mapper;

import org.springframework.stereotype.Repository;
import top.rectorlee.entity.Product;

import java.util.List;

/**
 * @author Lee
 */
@Repository
public interface ProductMapper {
    Product selectByProductId(Long productId);

    List<Product> selectProductList();
}
