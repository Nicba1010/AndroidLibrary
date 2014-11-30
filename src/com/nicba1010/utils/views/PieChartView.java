package com.nicba1010.utils.views;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.nicba1010.utils.viewutils.PieChartElement;

public class PieChartView extends View {
	private static final String TAG = "PieChartView";

	public PieChartView(Context context) {
		super(context);
		init();
	}

	public PieChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PieChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		elements.add(new PieChartElement("Female", 330, Color.RED));
		elements.add(new PieChartElement("Male", 550, Color.BLUE));
		elements.add(new PieChartElement("Sheep", 440, Color.LTGRAY));
		bgpaint = new Paint();
		bgpaint.setColor(Color.WHITE);
		bgpaint.setAntiAlias(true);
		bgpaint.setStyle(Paint.Style.FILL);
		rect = new RectF();
		rectSelect = new RectF();
		updateValues();
	}

	Paint bgpaint;
	RectF rect;
	RectF rectSelect;
	ArrayList<PieChartElement> elements = new ArrayList<PieChartElement>();
	float totalAmount;
	PieChartElement selected;

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// draw background circle anyway
		int left = 0;
		int width = getWidth();
		int top = 0;
		int last = 0;
		if (selected == null)
			rect.set(left, top, left + width, top + width);
		rectSelect.set(left, top, left + width, top + width);
		// canvas.drawArc(rect, -90, 360, true, bgpaint);
		for (PieChartElement e : elements) {
			if (elements.indexOf(e) == (elements.size() - 1)) {
				canvas.drawArc(
						e.equals(selected) ? rectSelect : rect,
						-90 + last,
						360f - last,
						true,
						selected != null && e.equals(selected) == false ? darken(e
								.getColor()) : e.getColor());
			} else {
				canvas.drawArc(
						e.equals(selected) ? rectSelect : rect,
						-90 + last,
						(360 * e.getPercentage()),
						true,
						selected != null && e.equals(selected) == false ? darken(e
								.getColor()) : e.getColor());
				last += 360 * e.getPercentage();
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		double deltaX = event.getX() - this.getWidth() / 2;
		double deltaY = -(event.getY() - this.getHeight() / 2);
		double fromMid = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
		boolean inCircle = fromMid < (rectSelect.bottom / 2);
		if (inCircle) {
			double temp = Math.atan2(deltaY, deltaX) * 180 / Math.PI;
			temp = (temp > 0 ? temp : 360D - Math.abs(temp)) - 90;
			temp = temp > 0 ? temp : 360D + temp;
			double angleInDegrees = Math.abs(temp - 360);
			float percentage = (float) (angleInDegrees / 360f);
			float totalPerc = 0;
			int index = -1, i = 0;
			for (PieChartElement e : elements) {
				if (percentage > totalPerc) {
					index = i;
				} else if (percentage < totalPerc) {
					break;
				}
				totalPerc += e.getPercentage();
				i++;
			}
			if (index == -1) {
				Toast.makeText(getContext(), "ERROR", Toast.LENGTH_LONG).show();
			} else {
				if (selected == null) {
					scaleRectF(rect, 0.9f);
				}
				selected = elements.get(index);
				invalidate();

				Toast.makeText(getContext(), selected.getName(),
						Toast.LENGTH_SHORT).show();
				// Log.d(TAG, /* quadrant + " : " + */deltaX + " : "
				// + deltaY + " : " + fromMid + " : " + inCircle + " : "
				// + angleInDegrees + " : " + percentage + " : " + index);
			}
		} else {
			selected = null;
			invalidate();
		}
		return super.onTouchEvent(event);
	}

	public void scaleRectF(RectF rect, float factor) {
		float diffHorizontal = (rect.right - rect.left) * (factor - 1f);
		float diffVertical = (rect.bottom - rect.top) * (factor - 1f);

		rect.top -= diffVertical / 2f;
		rect.bottom += diffVertical / 2f;

		rect.left -= diffHorizontal / 2f;
		rect.right += diffHorizontal / 2f;
	}

	public int getAmountOfElements() {
		return elements.size();
	}

	public void updateValues() {
		updateTotalAmount();
		updatePercentages();
		invalidate();
	}

	public void updatePercentages() {
		for (PieChartElement e : elements) {
			e.updatePercentage(totalAmount);
		}
	}

	public void updateTotalAmount() {
		totalAmount = 0;
		for (PieChartElement e : elements) {
			totalAmount += e.getAmount();
		}
	}

	public void reset() {
		elements.clear();
		updateValues();
	}

	public void addElement(PieChartElement el) {
		for (PieChartElement e : elements) {
			if (e.getName().equalsIgnoreCase(el.getName())) {
				Log.e(TAG,
						"There can not be 2 pie chart elements with the sam name!");
				return;
			}
		}
		elements.add(el);
		updateValues();
	}

	public void removeElement(String name) {
		for (PieChartElement e : elements) {
			if (e.getName().equalsIgnoreCase(name)) {
				selected = null;
				elements.remove(e);
				break;
			}
		}
		updateValues();
	}

	public void removeElement(int i) {
		selected = null;
		elements.remove(i);
		updateValues();
	}

	public void setAmount(int i, float a) {
		elements.get(i).setAmount(a);
		updateValues();
	}

	public void setAmount(String name, float a) {
		for (PieChartElement e : elements) {
			if (e.getName().equalsIgnoreCase(name)) {
				e.setAmount(a);
				break;
			}
		}
		updateValues();
	}

	public static Paint darken(Paint _color) {
		int color = _color.getColor();
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] /= 2;
		hsv[1] /= 2;
		Paint newPaint = new Paint();
		newPaint.setColor(Color.HSVToColor(hsv));
		newPaint.setAntiAlias(true);
		newPaint.setStyle(Paint.Style.FILL);
		return newPaint;
	}
}