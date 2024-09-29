package com.example.SeniorProject.DTOs;

public class ProductDTO
{
	/*    public Product(int quantity, double price, String type, String name, String description, String location) {
	 */
	private int id;
	private String name;
	private double price;
	private int quantity;
	private String type;
	private String description;
	private String location;

	public ProductDTO()
	{

	}

	public ProductDTO(int id, String name, double price)
	{
		this.id = id;
		this.name = name;
		this.price = price;
	}

	public ProductDTO(int id, String name, double price, int quantity, String type, String description, String location)
	{
		this(id,name,price);
		this.id = id;
		this.name = name;
		this.price = price;
		this.quantity = quantity;
		this.type = type;
		this.description = description;
		this.location = location;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public double getPrice()
	{
		return price;
	}

	public void setPrice(double price)
	{
		this.price = price;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public void setQuantity(int quantity)
	{
		this.quantity = quantity;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getLocation()
	{
		return location;
	}

	public void setLocation(String location)
	{
		this.location = location;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}




}