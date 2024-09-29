package com.example.SeniorProject.DTOs;

import com.example.SeniorProject.DTOs.ProductDTO;

public class OrderProductDTO
{
	private int quantity;
	private ProductDTO product;

	public OrderProductDTO(int quantity, ProductDTO product)
	{
		this.quantity = quantity;
		this.product = product;
	}

	public int getQuantity() {
	    return quantity;
	}

	public void setQuantity(int quantity) {
	    this.quantity = quantity;
	}

	public ProductDTO getProduct() {
	    return product;
	}

	public void setProduct(ProductDTO product) {
	    this.product = product;
	}
}