package com.us.articulatedesign.db_csv_compare;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Monitor is a model class for comparing database result sets and CSV files.
 */
public class Monitor {
	private String make;
	private String model;
	private String description;
	private BigDecimal price;

	public Monitor() {}

	public Monitor(String[] values) {
		this(values[0], values[1], values[2], new BigDecimal(values[3]));
	}

	public Monitor(String make, String model, String description, BigDecimal price) {
		super();
		this.make = make;
		this.model = model;
		this.description = description;
		this.price = price;
	}

	public String getMake() {
		return make;
	}

	public void setMake(String make) {
		this.make = make;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	@Override
	public int hashCode() {
		return Objects.hash(description, make, model);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Monitor other = (Monitor) obj;
		return Objects.equals(description, other.description) && Objects.equals(make, other.make)
				&& Objects.equals(model, other.model);
	}

	@Override
	public String toString() {
		return "Monitor [make=" + make + ", model=" + model + ", description=" + description + "]";
	}

}
