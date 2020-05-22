package com.leyou.order.dto;

import lombok.Data;

/**
 * 地址信息
 */
@Data
public class AddressDTO {
    private long id;
    private String name;
    private String phone;
    private String state;
    private String city;
    private String district;
    private String address;
    private String zipCode;
    private Boolean isDefault;
}
