package com.leyou.common.utlis.enums;

public enum  ExceptionEnum {
    GOODS_SAVE_ERROR(500,"商品添加失败"),
    GOODS_NOT_FOUND(502,"商品不存在"),
    CATEGORY_NOT_FOND(404,"商品分类不存在"),
    GOODS_SKU_NOT_FOUND(502,"商品没有sku"),
    GOODS_STOCK_NOT_FOUND(502,"商品没有库存"),
    GOODS_DETAIL_NOT_FOND(502,"商品详情不存在"),
    GOODS_UPDATE_ERROR(502,"商品修改错误"),
    PRICE_CANNOT_BE_NULL(400,"价格不对"),
    CATEGORY_NOT_FUND(404,"新增商品失败"),
    INVALID_FILE_TYPE(400,"无效的问价类型"),
    SPEC_GROUP_FOND(502,"对应规格组不存在"),
    SPEC_PARAM_FOND(502,"对应规格数据不存在"),
    INVALID_USER_DATA_TYPE(400,"注册数据类型不正确"),
    USER_LOGIN_FOUND(400,"用户名或密码错误"),
    UNAUTHORIZED(403,"用户登陆超时"),
    USER_CART_LIST_FOUND(404,"该用户没有购物车数据"),
    CREATE_ORDER_ERROR(500,"订单生成失败"),
    STOCK_NOT_ENOUGH(500,"库存不足"),
    ORDER_NOT_FOUND(404,"订单不存在"),
    ORDER_DETAIL_NOT_FOUND(404,"订单详情不存在"),
    ORDER_STATUS_NOT_FOUND(404,"订单状态不存在"),
    WX_PAY_ORDER_FAIL(500,"下单失败"),
    ORDER_STATUS_ERROR(400,"订单状态异常"),
    INVALID_SIGN_ERROR(400,"无效的签名"),
    INVALID_ORDER_PARAM(400,"订单参数异常"),
    UPDATE_ORDER_STATUS_ERROR(500,"更新订单状态失败"),
    ;
    private int code;
    private String msg;

    ExceptionEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
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
}
