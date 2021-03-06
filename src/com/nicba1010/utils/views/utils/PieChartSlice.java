package com.nicba1010.utils.views.utils;

import android.graphics.Color;
import android.graphics.Paint;

public class PieChartSlice {
	String name;
	float amount;
	float percentage;

	public float getPercentage() {
		return percentage;
	}

	Paint color = new Paint();

	public PieChartSlice(String name, float amount, Paint color) {
		super();
		this.name = name;
		this.amount = amount;
		setColor(color);
	}

	public PieChartSlice(String name, float amount, int color) {
		super();
		this.name = name;
		this.amount = amount;
		setColor(color);
	}

	public PieChartSlice(String name, float amount, int red, int green,
			int blue) {
		super();
		this.name = name;
		this.amount = amount;
		setColor(red, green, blue);
	}

	public String getName() {
		return name;
	}

	public void updatePercentage(float total) {
		percentage = amount / total;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public Paint getColor() {
		return color;
	}

	public void setColor(int r, int g, int b) {
		color.setColor(Color.rgb(r, g, b));
		color.setAntiAlias(true);
		color.setStyle(Paint.Style.FILL);
		color.setStrokeWidth(0);
	}

	public void setColor(Paint _color) {
		color = _color;
		color.setAntiAlias(true);
		color.setStyle(Paint.Style.FILL);
		color.setStrokeWidth(0);
	}

	public void setColor(int _color) {
		color.setColor(_color);
		color.setAntiAlias(true);
		color.setStyle(Paint.Style.FILL);
		color.setStrokeWidth(0);
	}
}

