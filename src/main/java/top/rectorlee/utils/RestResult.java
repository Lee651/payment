package top.rectorlee.utils;

import wiremock.org.apache.commons.lang3.builder.ToStringBuilder;
import wiremock.org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * @author Lee
 * @description 统一返回实体类
 * @date 2023-05-04  18:22:05
 */
public class RestResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private int code;

    /**
     * 返回内容
     */
    private String msg;

    /**
     * 数据对象
     */
    private T data;

    /**
     * 分页总数
     */
    private long total;

    /**
     * 初始化一个新创建的 AjaxResult 对象，使其表示一个空消息。
     */
    public RestResult() {

    }

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param code 状态类型
     * @param msg 返回内容
     */
    public RestResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param code 状态类型
     * @param msg 返回内容
     * @param data 数据对象
     */
    public RestResult(int code, String msg, T data) {
        this.code = code;
        this.msg =  msg;
        this.data = data;
    }

    /**
     * 初始化一个新创建的 分页AjaxResult 对象
     *
     * @param code 状态类型
     * @param msg 返回内容
     * @param data 数据对象
     */
    public RestResult(int code, String msg, T data,long total) {
        this.code = code;
        this.msg =  msg;
        this.data = data;
        this.total = total;
    }
    /**
     * 返回成功消息
     *
     * @return 成功消息
     */
    public static <T> RestResult<T> success() {
        return RestResult.success("操作成功");
    }

    /**
     * 返回成功数据
     *
     * @return 成功消息
     */
    public static <T> RestResult<T> success(T data) {
        return RestResult.success("操作成功", data);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @return 成功消息
     */
    public static <T> RestResult<T> success(String msg) {
        return RestResult.success(msg, null);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static <T> RestResult<T> success(String msg, T data) {
        return new RestResult<>(HttpStatus.SUCCESS, msg, data);
    }

    /**
     * 返回成功分页消息
     *
     * @param data 数据对象
     * @return 成功消息
     */
    public static <T> RestResult<T> success(T data,long total) {
        return new RestResult<>(HttpStatus.SUCCESS, "操作成功", data,total);
    }
    /**
     * 返回错误消息
     */
    public static <T> RestResult<T> error() {
        return RestResult.error("操作失败");
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回内容
     * @return 警告消息
     */
    public static <T> RestResult<T> error(String msg) {
        return RestResult.error(msg, null);
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static <T> RestResult<T> error(String msg, T data) {
        return new RestResult(HttpStatus.ERROR, msg, data);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("code", getCode())
                .append("msg", getMsg()).append("data", getData()).toString();
    }
}
