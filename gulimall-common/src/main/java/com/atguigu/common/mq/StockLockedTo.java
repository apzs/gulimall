package com.atguigu.common.mq;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author 无名氏
 * @date 2022/8/19
 * @Description: 每锁一件商品库存就向RabbitMQ发送一条消息
 */
@Data
public class StockLockedTo {
    /**
     * wms_ware_order_task
     * 库存工作单的id
     */
    private Long id;

    /**
     * wms_ware_order_task_detail
     * 工作单详情的id
     */
    private StockDetailTo detail;

    @Data
    public static class StockDetailTo{
        /**
         * id
         */
        @TableId
        private Long id;
        /**
         * sku_id
         */
        private Long skuId;
        /**
         * sku_name
         */
        private String skuName;
        /**
         * 购买个数
         */
        private Integer skuNum;
        /**
         * 工作单id
         */
        private Long taskId;
        /**
         * 仓库id
         */
        private Long wareId;
        /**
         * 1-已锁定  2-已解锁  3-扣减
         */
        private Integer lockStatus;
    }
}
