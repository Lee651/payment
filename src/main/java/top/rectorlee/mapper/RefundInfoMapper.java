package top.rectorlee.mapper;

import org.springframework.stereotype.Repository;
import top.rectorlee.entity.RefundInfo;

/**
 * @author Lee
 */
@Repository
public interface RefundInfoMapper {
    void insertRefundInfo(RefundInfo refundInfo);

    RefundInfo selectByRefundNo(String refundNo);

    void updateRefundInfo(RefundInfo refundInfo);
}
