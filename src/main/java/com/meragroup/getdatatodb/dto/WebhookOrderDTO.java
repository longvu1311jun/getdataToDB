package com.meragroup.getdatatodb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookOrderDTO {
	private Long id;
	private String event_type;
	private Integer status;
	private Long total_price;
	private String bill_full_name;
	private String bill_phone_number;
	private String note;
	private CustomerDTO customer;
	private List<ItemDTO> items;
	private ShippingAddressDTO shipping_address;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class CustomerDTO {
		private String name;
		private List<String> phone_numbers;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ItemDTO {
		private Long id;
		private Integer quantity;
		private Long retail_price;
		private VariationInfoDTO variation_info;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class VariationInfoDTO {
		private String name;
		private String product_display_id;
		private Long retail_price;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ShippingAddressDTO {
		private String full_name;
		private String phone_number;
		private String full_address;
		private String address;
	}

}


