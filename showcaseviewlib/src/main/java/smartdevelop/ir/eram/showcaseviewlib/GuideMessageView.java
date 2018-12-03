package smartdevelop.ir.eram.showcaseviewlib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

/**
 * Created by Mohammad Reza Eram on 20/01/2018.
 */
class GuideMessageView extends LinearLayout {

    private int radius = -1;

    private Paint mPaintBackground;
    private Paint mPaintBorder;

    private TextView mTitleTextView;
    private TextView mContentTextView;
    private ImageButton mCloseButton;

    private final RectF mRect = new RectF();
    private final int[] location = new int[2];

    GuideMessageView(@NonNull Context context,
                     @Nullable Position closeButtonPosition,
                     @Nullable Integer padding,
                     @Nullable OnClickListener closeButtonListener) {
        super(context);

        float density = context.getResources().getDisplayMetrics().density;
        setWillNotDraw(false);

        mPaintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBackground.setStrokeCap(Paint.Cap.ROUND);

        setLayerType(LAYER_TYPE_SOFTWARE, null);
        setGravity(Gravity.CENTER);
        final int paddingBetween = (int) (6 * density);

        if (padding == null) {
            padding = (int) (10 * density);
        }

        setPadding(padding, padding, padding, padding);

        mTitleTextView = new TextView(context);
        mTitleTextView.setPadding(padding, padding, padding, paddingBetween);
        mTitleTextView.setGravity(Gravity.CENTER);
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mTitleTextView.setTextColor(Color.BLACK);
        mTitleTextView.setSingleLine(false);

        mContentTextView = new TextView(context);
        mContentTextView.setTextColor(Color.BLACK);
        mContentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mContentTextView.setPadding(padding, 0, padding, padding);
        mContentTextView.setGravity(Gravity.CENTER);
        mContentTextView.setVisibility(View.GONE);
        mContentTextView.setSingleLine(false);

        LinearLayout.LayoutParams textViewParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout innerLayout = new LinearLayout(context);
        innerLayout.setOrientation(VERTICAL);
        innerLayout.addView(mTitleTextView, textViewParams);
        innerLayout.addView(mContentTextView, textViewParams);

        if (closeButtonPosition == null) {
            addView(innerLayout);
        } else {
            mCloseButton = new ImageButton(context);
            mCloseButton.setOnClickListener(closeButtonListener);
            LinearLayout.LayoutParams wrapWithMargin = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            switch (closeButtonPosition) {
                case Left:
                    setOrientation(HORIZONTAL);
                    wrapWithMargin.setMargins(padding, padding, 0, padding);
                    addView(mCloseButton, wrapWithMargin);
                    addView(innerLayout, textViewParams);
                    break;
                case Right:
                    setOrientation(HORIZONTAL);
                    wrapWithMargin.setMargins(0, padding, padding, padding);
                    addView(innerLayout, textViewParams);
                    addView(mCloseButton, wrapWithMargin);
                    break;
                case Top:
                    setOrientation(VERTICAL);
                    wrapWithMargin.setMargins(padding, padding, padding, 0);
                    addView(mCloseButton, wrapWithMargin);
                    addView(innerLayout, textViewParams);
                    break;
                case Bottom:
                    setOrientation(VERTICAL);
                    wrapWithMargin.setMargins(padding, 0, padding, padding);
                    addView(innerLayout, textViewParams);
                    addView(mCloseButton, wrapWithMargin);
                    break;
            }
        }
    }

    public void setTitle(CharSequence title) {
        if (title == null) {
            removeView(mTitleTextView);
            return;
        }
        mTitleTextView.setText(title);
    }

    public void setTitleTextColor(@ColorInt int color) {
        mTitleTextView.setTextColor(color);
    }

    public void setContentText(CharSequence content) {
        mContentTextView.setText(content);
        mContentTextView.setVisibility(View.VISIBLE);
    }

    public void setContentTypeface(Typeface typeface) {
        mContentTextView.setTypeface(typeface);
    }

    public void setTitleTypeface(Typeface typeface) {
        mTitleTextView.setTypeface(typeface);
    }

    public void setTitleTextSize(int unit, float size) {
        if (unit < 0) {
            mTitleTextView.setTextSize(size);
        } else {
            mTitleTextView.setTextSize(unit, size);
        }
    }

    public void setTitleGravity(int gravity) {
        mTitleTextView.setGravity(gravity);
    }

    public void setContentTextSize(int unit, float size) {
        if (unit < 0) {
            mContentTextView.setTextSize(size);
        } else {
            mContentTextView.setTextSize(unit, size);
        }
    }

    public void setContentTextColor(@ColorInt int color) {
        mContentTextView.setTextColor(color);
    }

    public void setContentGravity(int gravity) {
        mContentTextView.setGravity(gravity);

    }

    public void setColor(@ColorInt int color) {
        mPaintBackground.setAlpha(255);
        mPaintBackground.setColor(color);

        invalidate();
    }

    public void setBorder(@ColorInt int color, float width) {
        if (mPaintBorder == null) {
            mPaintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaintBorder.setStyle(Paint.Style.STROKE);
        }
        mPaintBorder.setColor(color);
        mPaintBorder.setStrokeWidth(width);
    }

    public void setRadius(@Px int radius) {
        this.radius = radius;
    }

    public void setCloseBtnBackground(@DrawableRes @ColorRes int drawableResId) {
        mCloseButton.setBackgroundColor(Color.TRANSPARENT);
        mCloseButton.setImageResource(drawableResId);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.getLocationOnScreen(location);

        mRect.set(getPaddingLeft(),
                getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom());
        canvas.drawRoundRect(mRect, radius, radius, mPaintBackground);
        if (mPaintBorder != null) {
            canvas.drawRoundRect(mRect, radius, radius, mPaintBorder);
        }
    }
}
