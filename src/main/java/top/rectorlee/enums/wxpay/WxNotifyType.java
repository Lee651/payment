package top.rectorlee.enums.wxpay;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Lee
 */

@AllArgsConstructor
@Getter
public enum WxNotifyType {

	/**
	 * 支付通知
	 */
	NATIVE_NOTIFY("/api/wx-native-pay/callBack"),

	/**
	 * 支付通知
	 */
	NATIVE_NOTIFY_V2("/api/wx-pay-v2/native/notify"),


	/**
	 * 退款结果通知
	 */
	REFUND_NOTIFY("/api/wx-native-pay/refundCallBack");

	/**
	 * 类型
	 */
	private final String type;
}
