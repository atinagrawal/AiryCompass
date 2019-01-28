/*
 * Copyright 2019 Dmitriy Ponomarenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain prevDegree copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dimowner.airycompass.app.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.dimowner.airycompass.R;
import com.dimowner.airycompass.util.AndroidUtils;

import timber.log.Timber;

public class MagneticFieldView extends View {

	public static final float OUTER_RADIUS = 0.43f;
	public static final float MAGNETIC_VIEW_RADIUS = 0.407f;
	public static final float MAGNETIC_NAME_RADIUS = 0.414f;
	public static final float MAGNETIC_VAL_RADIUS = 0.4f;

	private String mT;
	private String magField;

	private Paint outerCirclePaint;
	private Paint magneticBackgroundPaint;
	private Paint magneticTextPaint;
	private Paint magneticFieldPaint;
	private Paint northMarkStaticPaint;

	private Point CENTER;
	private float WIDTH;

	private Path magneticPath = null;
	private Path magneticBackgroundPath = null;
	private Path staticNorthMarkPath = null;

	private float magneticField;

	private boolean showCircle = false;

	public MagneticFieldView(Context context) {
		super(context);
		init(context, null);
	}

	public MagneticFieldView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public MagneticFieldView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		Typeface typeface = Typeface.create("sans-serif-light", Typeface.NORMAL);

		Resources res = context.getResources();
		mT = res.getString(R.string.mt_val);
		magField = res.getString(R.string.mag_field);

		int outerCircleColor= res.getColor(R.color.outer_circle_color);
		int magneticBackgroundColor = res.getColor(R.color.magnetic_background);
		int magneticTextColor = res.getColor(R.color.magnetic_text_color);
		int magneticFieldColor = res.getColor(R.color.magnetic_field_color);
		int northMarkColor = res.getColor(R.color.north_mark_color);

		if (attrs != null) {
			TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MagneticFieldView);
			if (ta != null) {
				//Read View custom attributes
				magneticBackgroundColor  = ta.getColor(R.styleable.MagneticFieldView_magneticBackground, res.getColor(R.color.magnetic_background));
				magneticTextColor = ta.getColor(R.styleable.MagneticFieldView_magneticText, res.getColor(R.color.magnetic_text_color));
				magneticFieldColor = ta.getColor(R.styleable.MagneticFieldView_magneticIndicator,  res.getColor(R.color.magnetic_field_color));
				ta.recycle();
			} else {
				//If failed to read View attributes, then read app theme attributes for for view colors.
				TypedValue typedValue = new TypedValue();
				Resources.Theme theme = context.getTheme();
				theme.resolveAttribute(R.attr.magneticBackground, typedValue, true);
				magneticBackgroundColor = typedValue.data;
				theme.resolveAttribute(R.attr.magneticText, typedValue, true);
				magneticTextColor = typedValue.data;
				theme.resolveAttribute(R.attr.magneticIndicator, typedValue, true);
				magneticFieldColor = typedValue.data;
			}
		}

		magneticPath = new Path();
		CENTER = new Point(0, 0);

		//Background circles
		outerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		outerCirclePaint.setColor(outerCircleColor);
		outerCirclePaint.setStyle(Paint.Style.STROKE);
		outerCirclePaint.setStrokeWidth(AndroidUtils.dpToPx(1));

		//Magnetic view
		magneticBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		magneticBackgroundPaint.setStyle(Paint.Style.STROKE);
		magneticBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);
		magneticBackgroundPaint.setStrokeWidth(AndroidUtils.dpToPx(6));
		magneticBackgroundPaint.setColor(magneticBackgroundColor);

		magneticTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		magneticTextPaint.setColor(magneticTextColor);
		magneticTextPaint.setTextSize(AndroidUtils.dpToPx(10));
		magneticTextPaint.setTypeface(typeface);
		magneticTextPaint.setTextAlign(Paint.Align.CENTER);

		magneticFieldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		magneticFieldPaint.setColor(magneticFieldColor);
		magneticFieldPaint.setStrokeWidth(AndroidUtils.dpToPx(6));
		magneticFieldPaint.setStyle(Paint.Style.STROKE);
		magneticFieldPaint.setStrokeCap(Paint.Cap.ROUND);

		//NorthMark
		northMarkStaticPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		northMarkStaticPaint.setStyle(Paint.Style.FILL);
		northMarkStaticPaint.setColor(northMarkColor);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int w = MeasureSpec.getSize(widthMeasureSpec);
		int h = MeasureSpec.getSize(heightMeasureSpec);

		if (w > h) {
			w = h;
		} else {
			h = w;
		}

		setMeasuredDimension(
				resolveSize(w, widthMeasureSpec),
				resolveSize(h, heightMeasureSpec));
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		initConstants();

		if (showCircle) {
			layoutStaticNorthMark();
		}
		layoutMagnetic();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Timber.v("onDraw");

		drawMagnetic(canvas);

		if (showCircle) {
			//Draw outer circle
			canvas.drawCircle(CENTER.x, CENTER.y, WIDTH*OUTER_RADIUS, outerCirclePaint);
			//Draw static north mark (triangle)
			canvas.drawPath(staticNorthMarkPath, northMarkStaticPaint);
		}
	}

	private void initConstants() {
		WIDTH = getWidth();
		CENTER.set((int)WIDTH/2, getHeight()/2);
	}

	private void layoutStaticNorthMark() {
		if (staticNorthMarkPath == null) {
			float x = CENTER.x;
			float length = AndroidUtils.dpToPx(12);
			float y = (CENTER.y - WIDTH*OUTER_RADIUS + length - AndroidUtils.dpToPx(2));
			staticNorthMarkPath = new Path();
			staticNorthMarkPath.moveTo(x - length/2.0f, y - length);
			staticNorthMarkPath.lineTo(x + length/2.0f, y - length);
			staticNorthMarkPath.lineTo(x, y);
			staticNorthMarkPath.lineTo(x - length/2.0f, y - length);
		}
	}

	private void layoutMagnetic() {
		if (magneticBackgroundPath == null) {
			float r = WIDTH* MAGNETIC_VIEW_RADIUS;
			RectF bound = new RectF(CENTER.x - r, CENTER.y - r, CENTER.x + r, CENTER.y + r);
			magneticBackgroundPath = new Path();
			magneticBackgroundPath.addArc(bound, 315, 85);
		}
	}

	private void drawMagnetic(Canvas canvas) {
		int angle = 85;
		float r = WIDTH* MAGNETIC_VIEW_RADIUS;
		RectF bound = new RectF(CENTER.x - r, CENTER.y - r, CENTER.x + r, CENTER.y + r);

		//Draw magnetic background
		canvas.drawPath(magneticBackgroundPath, magneticBackgroundPaint);

		int max = 160;
		float percent = Math.min(1, magneticField / max);
		percent = percent * angle;

		//Draw magnetic field value
		magneticPath.reset();
		magneticPath.addArc(bound, 315 + angle - percent, percent);
		canvas.drawPath(magneticPath, magneticFieldPaint);

		drawText(canvas, 307, (int)magneticField+mT, WIDTH*MAGNETIC_VAL_RADIUS, magneticTextPaint);
		drawTextInverted(canvas, 53, magField, WIDTH*MAGNETIC_NAME_RADIUS, magneticTextPaint);
	}

	private void drawText(Canvas canvas, float degree, String text, float radius, Paint paint) {
		canvas.save();
		canvas.translate(
				((float) Math.cos(Math.toRadians(degree)) * radius) + CENTER.x,
				((float) Math.sin(Math.toRadians(degree)) * radius) + CENTER.y
		);
		canvas.rotate(90.0f + degree);
		canvas.drawText(text, 0, 0, paint);
		canvas.restore();
	}

	private void drawTextInverted(Canvas canvas, float degree, String text, float radius, Paint paint) {
		canvas.save();
		canvas.translate(
				((float) Math.cos(Math.toRadians(degree)) * radius) + CENTER.x,
				((float) Math.sin(Math.toRadians(degree)) * radius) + CENTER.y
		);
		canvas.rotate(270f + degree);
		canvas.drawText(text, 0, 0, paint);
		canvas.restore();
	}

	public void updateMagneticField(float field) {
//		Timber.v("updateMagneticField omMF = " + magneticField + "newMF = " + field);
		if ((int)magneticField != (int)field) {
			magneticField = field;
			invalidate();
		}
	}

	public void showCircle() {
		this.showCircle = true;
	}

	public void hideCircle() {
		this.showCircle = false;
	}
}
