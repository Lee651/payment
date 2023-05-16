package top.rectorlee.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Lee
 */

@AllArgsConstructor
@Getter
public enum PayType {
    /**
     * 微信
     */
    WXPAY("微信"),


    /**
     * 支付宝
     */
    ALIPAY("支付宝");

    /**
     * 类型
     */
    private final String type;
}
