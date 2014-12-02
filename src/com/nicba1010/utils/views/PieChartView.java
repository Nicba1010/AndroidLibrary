package com.nicba1010.utils.views;

import java.math.BigDecimal;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.nicba1010.utils.views.utils.OnScaleCompleteListener;
import com.nicba1010.utils.views.utils.PieChartSlice;

public class PieChartView extends View implements OnScaleCompleteListener {
	private static final String TAG = "PieChartView";
	private OnSliceSelectedListener onSliceSelectedListener;
	Buffer scaleTaskBuffer = new Buffer();

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

	public interface OnSliceSelectedListener {
		public abstract void onSliceSelected(View v, PieChartSlice e);
	}

	public void setOnSliceSelectedListener(
			OnSliceSelectedListener _onSliceSelectedListener) {
		this.onSliceSelectedListener = _onSliceSelectedListener;
	}

	class Buffer {
		ArrayList<ScalerThread> scaleQueue = new ArrayList<PieChartView.ScalerThread>();
		boolean executing = false;

		public synchronized void insert(ScalerThread s) {
			scaleQueue.add(s);
			if (scaleQueue.size() == 1) {
				dispatch();
			}
		}

		public synchronized void dispatch() {
			scaleQueue.get(0).start();
			executing = true;

		}

		public synchronized void remove() {
			scaleQueue.remove(0);
		}
	}

	@Override
	public void onScaleComplete() {
		scaleTaskBuffer.executing = false;
		scaleTaskBuffer.remove();
		if (scaleTaskBuffer.scaleQueue.size() > 0) {
			scaleTaskBuffer.dispatch();
		}
	}

	private void init() {
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
	ArrayList<PieChartSlice> slices = new ArrayList<PieChartSlice>();
	float totalAmount;
	PieChartSlice selected;
	Paint blackOutlinePaint;
	Paint black;
	Paint white;

	float rads = 0;

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// draw background circle anyway
		int left = (int) (getHeight() * 0.05f);
		int diameter = (int) (getHeight() * 0.9f);
		int top = (int) (getHeight() * 0.05f);
		int last = 0;
		if (selected == null && !scaleTaskBuffer.executing)
			rect.set(left, top, left + diameter, top + diameter);
		rectSelect.set(left, top, left + diameter, top + diameter);
		float from = 0;
		float total = 0;
		boolean found = false;
		for (PieChartSlice e : slices) {
			if (slices.indexOf(e) == (slices.size() - 1)) {
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
				from += !found ? e.getPercentage()* 0.99f : 0f;
				if (selected.equals(e)) {
					found = true;
				}
			}
		}
		for (PieChartSlice e : slices) {
			System.out.println(total);
			if(slices.indexOf(e)==slices.size()-1)
				total =total;
			canvas.drawLine(
					rect.centerX(),
					rect.centerY(),
					(float) (rect.centerX() + (rect.width() / 2)
							* Math.cos(Math.toRadians(total - 90))),
					(float) (rect.centerY() + (rect.width() / 2)
							* Math.sin(Math.toRadians(total - 90))),
					blackOutlinePaint);
			total += e.getPercentage() * 360f * 0.99f;
		}
		from *= 360f ;
		canvas.drawArc(rect, -90 + (selected != null ? from : 0),
				360 - (selected != null ? selected.getPercentage() * 360 : 0),
				true, blackOutlinePaint);
		if (selected != null) {
			canvas.drawArc(rectSelect, -90 + from - selected.getPercentage()
					* 360, selected.getPercentage() * 360, true,
					blackOutlinePaint);
			drawOutlinedCenteredText(
					canvas,
					selected.getName() + " "
							+ round(selected.getPercentage() * 100, 2) + "%",
					rectSelect.centerX(), rectSelect.centerY(), black, white,
					25);
		}
	}

	public void drawOutlinedCenteredText(Canvas canvas, String text, float x,
			float y, Paint paint, Paint outline, int sizeText) {
		drawCenteredText(canvas, text, x, y, outline, sizeText);
		drawCenteredText(canvas, text, x, y, paint, sizeText);
	}

	public void drawCenteredText(Canvas canvas, String text, float x, float y,
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

	public double getPositionOnCircumference(double deltaX, double deltaY) {
		double temp = Math.atan2(deltaY, deltaX) * 180 / Math.PI;
		temp = (temp > 0 ? temp : 360D - Math.abs(temp)) - 90;
		temp = temp > 0 ? temp : 360D + temp;
		return Math.abs(temp - 360);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
			selected = null;
			invalidate();
			return true;
		}
		double deltaX = event.getX() - rect.width() / 2 - rect.left;
		double deltaY = -(event.getY() - rect.height() / 2 - rect.top);
		double fromMid = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
		boolean inCircle = fromMid < (rectSelect.bottom / 2);
		if (inCircle) {
			double angleInDegrees = getPositionOnCircumference(deltaX, deltaY);
			float percentage = (float) (angleInDegrees / 360f);
			float totalPerc = 0;
			int index = -1, i = 0;
			for (PieChartSlice e : slices) {
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
				final PieChartSlice tmp = slices.get(index);
				addScaleTask(rect, 500, 0.9f, new Runnable() {
					@Override
					public void run() {
						selected = tmp;
					}
				});
				selected = slices.get(index);
				invalidate();
				Toast.makeText(getContext(), selected.getName(),
						Toast.LENGTH_SHORT).show();
				onSliceSelectedListener.onSliceSelected(this, selected);
			}
		} else {
			if (selected != null) {
				addScaleTask(rect, 500, 1f, new Runnable() {
					@Override
					public void run() {
						selected = null;
					}
				});
				invalidate();
			}
		}
		return super.onTouchEvent(event);
	}

	class ScalerThread extends Thread {
		PieChartView chartView;
		OnScaleCompleteListener mListener;
		long timeinmillis;
		private RectF rect;
		double scalePerMilli;
		float from, to;
		int defaultleft = (int) (getHeight() * 0.05f);
		int defaultdiameter = (int) (getHeight() * 0.9f);
		int defaulttop = (int) (getHeight() * 0.05f);
		ArrayList<Runnable> tasks = new ArrayList<Runnable>();

		public ScalerThread(PieChartView chartView, long timeinmillis,
				RectF rect, float to, Runnable... tasks) {
			super();
			this.chartView = chartView;
			this.timeinmillis = timeinmillis;
			this.rect = rect;
			mListener = this.chartView;
			this.to = to;
			for (Runnable runnable : tasks) {
				this.tasks.add(runnable);
			}
		}

		long timePassed;

		public void scaleRectF(float factor) {
			rect.set(defaultleft, defaulttop, defaultleft + defaultdiameter,
					defaulttop + defaultdiameter);
			float diffHorizontal = (rect.right - rect.left) * (factor - 1f);
			float diffVertical = (rect.bottom - rect.top) * (factor - 1f);

			rect.top -= diffVertical / 2f;
			rect.bottom += diffVertical / 2f;

			rect.left -= diffHorizontal / 2f;
			rect.right += diffHorizontal / 2f;
			invalidate();
		}

		public void invalidate() {
			chartView.post(new Runnable() {
				@Override
				public void run() {
					chartView.invalidate();
				}
			});
		}

		@Override
		public void run() {
			this.from = rect.width() / defaultdiameter;
			this.scalePerMilli = (from - to) / timeinmillis;
			if (from != to)
				while (true) {
					long t1 = System.currentTimeMillis();
					if (timePassed > timeinmillis) {
						scaleRectF(to);
						for (Runnable r : tasks) {
							r.run();
						}
						invalidate();
						mListener.onScaleComplete();
						return;
					} else {
						scaleRectF((float) (from - scalePerMilli * timePassed));
					}
					try {
						Thread.currentThread();
						Thread.sleep(50);
					} catch (Exception ex) {
					}
					timePassed += System.currentTimeMillis() - t1;
				}
			else {
				for (Runnable r : tasks) {
					r.run();
				}
				mListener.onScaleComplete();
			}
		}
	}

	public void addScaleTask(RectF rect, long timeinmillis, float to,
			Runnable... tasks) {
		scaleTaskBuffer.insert(new ScalerThread(this, timeinmillis, rect, to,
				tasks));
	}

	public int getAmountOfElements() {
		return slices.size();
	}

	public void updateValues() {
		updateTotalAmount();
		updatePercentages();
		invalidate();
	}

	public void updatePercentages() {
		for (PieChartSlice e : slices) {
			e.updatePercentage(totalAmount);
		}
	}

	public void updateTotalAmount() {
		totalAmount = 0;
		for (PieChartSlice e : slices) {
			totalAmount += e.getAmount();
		}
	}

	public void reset() {
		slices.clear();
		updateValues();
	}

	public void addSlice(PieChartSlice el) {
		for (PieChartSlice e : slices) {
			if (e.getName().equalsIgnoreCase(el.getName())) {
				Log.e(TAG,
						"There can not be 2 pie chart slices with the same name!");
				return;
			}
		}
		slices.add(el);
		updateValues();
	}

	public void removeElement(String name) {
		for (PieChartSlice e : slices) {
			if (e.getName().equalsIgnoreCase(name)) {
				selected = null;
				slices.remove(e);
				break;
			}
		}
		updateValues();
	}

	public void removeElement(int i) {
		selected = null;
		slices.remove(i);
		updateValues();
	}

	public void setAmount(int i, float a) {
		slices.get(i).setAmount(a);
		updateValues();
	}

	public void setAmount(String name, float a) {
		for (PieChartSlice e : slices) {
			if (e.getName().equalsIgnoreCase(name)) {
				e.setAmount(a);
				break;
			}
		}
		updateValues();
	}

	public void select(int i) {
		selected = slices.get(i);
		invalidate();
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