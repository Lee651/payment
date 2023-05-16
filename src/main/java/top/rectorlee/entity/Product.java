package top.rectorlee.entity;

import lombok.*;

import java.util.Date;

/**
 * @author Lee
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {
    private Long id;

    // 商品名称
    private String title;

    //价格(分)
    private Integer price;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;
}
