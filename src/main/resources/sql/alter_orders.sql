-- 添加订单编号字段
ALTER TABLE orders ADD COLUMN order_no VARCHAR(50) NOT NULL AFTER id;
