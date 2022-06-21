package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author 无名氏
 * @date 2022/6/15
 * @Description: 采购完成
 *
 * {
 *    "id": 4,
 *    "items": [
 *        {"itemId":3,"status":3,"reason":""},
 *        {"itemId":4,"status":4,"reason":"无货"}
 *     ]
 * }
 */
@Data
public class PurchaseDoneVo {

    /**
     * 采购单id
     */
    @NotNull
    private Long  id;
    private List<PurchaseItemDone> items;

    /**
     * 采购项
     */
    @Data
    public static class PurchaseItemDone{
        /**
         * 采购项id
         */
        private Long itemId;
        /**
         * 采购状态(3:采购完成 ; 4:采购失败)
         */
        private Integer status;
        /**
         * 失败原因
         */
        private String reason;
    }

}
