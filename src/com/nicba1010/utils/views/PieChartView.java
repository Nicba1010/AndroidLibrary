package com.nicba1010.utils.views;

import java.math.BigDecimal;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
		rect = new RectF();
		rectSelect = new RectF();
		blackOutlinePaint = new Paint();
		blackOutlinePaint.setColor(Color.BLACK);
		blackOutlinePaint.setAntiAlias(true);
		blackOutlinePaint.setStyle(Paint.Style.STROKE);
		blackOutlinePaint.setStrokeWidth(2);
		black = new Paint();
		black.setColor(Color.BLACK);
		black.setAntiAlias(true);
		white = new Paint();
		white.setColor(Color.WHITE);
		white.setAntiAlias(true);
		white.setStyle(Paint.Style.STROKE);
		white.setStrokeWidth(3);
		blackOutlinePaint.setStrokeWidth(2);
		updateValues();
	}

	RectF rect;
	RectF rectSelect;
	ArrayList<PieChartElement> elements = new ArrayList<PieChartElement>();
	float totalAmount;
	PieChartElement selected;
	Paint blackOutlinePaint;
	Paint black;
	Paint white;

	float rads = 0;

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// draw background circle anyway
		int left = (int) (getHeight() * 0.05f);
		int radius = (int) (getHeight() * 0.9f);
		int top = (int) (getHeight() * 0.05f);
		int last = 0;
		if (selected == null)
			rect.set(left, top, left + radius, top + radius);
		rectSelect.set(left, top, left + radius, top + radius);
		float from = 0;
		float total = 0;
		boolean found = false;
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
			if (selected != null) {
				from += !found ? e.getPercentage() : 0f;
				if (selected.equals(e)) {
					found = true;
				}
			}
		}
		for (PieChartElement e : elements) {
			canvas.drawLine(
					rect.centerX(),
					rect.centerY(),
					(float) (rect.centerX() + (rect.width() / 2)
							* Math.cos(Math.toRadians(total - 90))),
					(float) (rect.centerY() + (rect.width() / 2)
							* Math.sin(Math.toRadians(total - 90))),
					blackOutlinePaint);
			total += e.getPercentage() * 360f;
		}
		from *= 360f;
		canvas.drawArc(rect, -90 + (selected != null ? from : 0),
				360 - (selected != null ? selected.getPercentage() * 360 : 0),
				true, blackOutlinePaint);
		if (selected != null) {
			canvas.drawArc(rectSelect, -90 + from - selected.getPercentage()
					* 360, selected.getPercentage() * 360, true,
					blackOutlinePaint);
			drawCeneteredText(
					canvas,
					selected.getName() + " "
							+ round(selected.getPercentage() * 100, 2) + "%",
					rectSelect.centerX(), rectSelect.centerY(), white, 25);
			drawCeneteredText(
					canvas,
					selected.getName() + " "
							+ round(selected.getPercentage() * 100, 2) + "%",
					rectSelect.centerX(), rectSelect.centerY(), black, 25);
		}
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction,
			Rect previouslyFocusedRect) {
		System.out.println(gainFocus);
		if (!gainFocus) {
			selected = null;
			invalidate();
		}
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
	}

	public void drawCeneteredText(Canvas canvas, String text, float x, float y,
			Paint paint, int sizeText) {
		Paint textPaint = new Paint(paint);
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setTextSize(sizeText
				* (rectSelect.width() > 297f ? rectSelect.width() / 297f : 1f));
		int xPos = (int) (rectSelect.right - (rectSelect.width() / 2));
		int yPos = (int) (rectSelect.bottom - (rectSelect.height() / 2) - ((textPaint
				.descent() + textPaint.ascent()) / 2));

		canvas.drawText(text, xPos, yPos, textPaint);
	}

	public static float round(float d, int decimalPlace) {
		BigDecimal bd = new BigDecimal(Float.toString(d));
		bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
		return bd.floatValue();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
			selected = null;
			invalidate();
			return true;
		}
		double deltaX = event.getX() - rect.width() / 2;
		double deltaY = -(event.getY() - rect.height() / 2);
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
					scaleRectF(rect, 0.9f, 500);
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
			if (selected != null) {
				selected = null;
				invalidate();
			}
		}
		return super.onTouchEvent(event);
	}

	class ScalerRunnable extends Thread {
		PieChartView chartView;
		float factor;
		long timeinmillis;
		private RectF rect;

		public ScalerRunnable(PieChartView chartView, float factor,
				long timeinmillis, RectF rect) {
			super();
			this.chartView = chartView;
			this.factor = factor;
			this.timeinmillis = timeinmillis;
			this.rect = rect;
		}

		long timePassed;

		@Override
		public void run() {
			while (true) {
				long t1 = System.currentTimeMillis();
				if (timePassed > timeinmillis) {
					int left = (int) (getHeight() * 0.05f);
					int radius = (int) (getHeight() * 0.9f);
					int top = (int) (getHeight() * 0.05f);
					rect.set(left, top, left + radius, top + radius);
					float diffHorizontal = (rect.right - rect.left)
							* (factor - 1f);
					float diffVertical = (rect.bottom - rect.top)
							* (factor - 1f);

					rect.top -= diffVertical / 2f;
					rect.bottom += diffVertical / 2f;

					rect.left -= diffHorizontal / 2f;
					rect.right += diffHorizontal / 2f;
					chartView.post(new Runnable() {
						@Override
						public void run() {
							chartView.invalidate();
						}
					});
					return;
				} else {
					int left = (int) (getHeight() * 0.05f);
					int radius = (int) (getHeight() * 0.9f);
					int top = (int) (getHeight() * 0.05f);
					rect.set(left, top, left + radius, top + radius);
					float fac = -(timePassed / (float) timeinmillis)
							* (1f - factor);
					float diffHorizontal = (rect.right - rect.left) * fac;
					float diffVertical = (rect.bottom - rect.top) * fac;

					rect.top -= diffVertical / 2f;
					rect.bottom += diffVertical / 2f;

					rect.left -= diffHorizontal / 2f;
					rect.right += diffHorizontal / 2f;
				}
				chartView.post(new Runnable() {
					@Override
					public void run() {
						chartView.invalidate();
					}
				});
				try {
					Thread.currentThread().sleep(50);
				} catch (Exception ex) {
				}

				timePassed += System.currentTimeMillis() - t1;
			}
		}
	}

	public void scaleRectF(RectF rect, float factor, long timeinmillis) {
		new ScalerRunnable(this, factor, timeinmillis, rect).start();
		// float diffHorizontal = (rect.right - rect.left) * (factor - 1f);
		// float diffVertical = (rect.bottom - rect.top) * (factor - 1f);
		//
		// rect.top -= diffVertical / 2f;
		// rect.bottom += diffVertical / 2f;
		//
		// rect.left -= diffHorizontal / 2f;
		// rect.right += diffHorizontal / 2f;
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