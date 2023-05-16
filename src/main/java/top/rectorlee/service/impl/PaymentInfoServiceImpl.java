package top.rectorlee.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.rectorlee.entity.PaymentInfo;
import top.rectorlee.mapper.PaymentInfoMapper;
import top.rectorlee.service.PaymentInfoService;
import top.rectorlee.utils.HttpStatus;
import top.rectorlee.utils.RestResult;

/**
 * @author Lee
 */
@Service
@Slf4j
public class PaymentInfoServiceImpl implements PaymentInfoService {
    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Transactional
    @Override
    public RestResult insertPaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertPaymentInfo(paymentInfo);

        log.info("支付记录新增成功");

        return new RestResult(HttpStatus.SUCCESS, "支付交易记录添加成功");
    }
}
