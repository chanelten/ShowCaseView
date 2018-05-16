package smartdevelop.ir.eram.showcaseviewlib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Spannable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Mohammad Reza Eram  on 20/01/2018.
 */

class GuideMessageView extends LinearLayout {

    private final Context context;
    private int radius = -1;

    private Paint mPaintBackground;
    private Paint mPaintBorder;
    private RectF mRect;

    private TextView mTitleTextView;
    private TextView mContentTextView;
    private ImageButton mCloseButton;
    float density;

    GuideMessageView(Context context) {
        this(context, null, null);
    }

    GuideMessageView(Context context, Position closeButtonPosition, OnClickListener closeButtonListener) {
        super(context);
        this.context = context;

        density = context.getResources().getDisplayMetrics().density;
        setWillNotDraw(false);

        mRect = new RectF();

        mPaintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBackground.setStrokeCap(Paint.Cap.ROUND);
//        float radius = density * 3.0f;
//        float dy = density * 2f;
//        mPaintBackground.setShadowLayer(radius, 0, dy, 0xFF3D3D3D);

        setLayerType(LAYER_TYPE_SOFTWARE, null);
        setGravity(Gravity.CENTER);

        final int padding = (int) (10 * density);
        final int paddingBetween = (int) (6 * density);

        setPadding(padding, padding, padding, padding);

        mTitleTextView = new TextView(context);
        mTitleTextView.setPadding(padding, padding, padding, paddingBetween);
        mTitleTextView.setGravity(Gravity.CENTER);
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        mTitleTextView.setTextColor(Color.BLACK);
        mTitleTextView.setSingleLine(false);

        mContentTextView = new TextView(context);
        mContentTextView.setTextColor(Color.BLACK);
        mContentTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
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

    public void setTitle(String title) {
        if (title == null) {
            removeView(mTitleTextView);
            return;
        }
        mTitleTextView.setText(title);
    }

    public void setTitleTextColor(int color) {
        mTitleTextView.setTextColor(color);
    }

    public void setContentText(String content) {
        mContentTextView.setText(content);
        if (mContentTextView.getVisibility() != VISIBLE) {
            mContentTextView.setVisibility(View.VISIBLE);
        }
    }

    public void setContentSpan(Spannable content) {
        mContentTextView.setText(content);
        if (mContentTextView.getVisibility() != VISIBLE) {
            mContentTextView.setVisibility(View.VISIBLE);
        }
    }

    public void setContentTypeFace(Typeface typeFace) {
        mContentTextView.setTypeface(typeFace);
    }

    public void setTitleTypeFace(Typeface typeFace) {
        mTitleTextView.setTypeface(typeFace);
    }

    public void setTitleTextSize(int size) {
        mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setTitleGravity(int gravity) {
        mTitleTextView.setGravity(gravity);
    }

    public void setContentTextSize(int size) {
        mContentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setContentTextColor(int color) {
        mContentTextView.setTextColor(color);
    }

    public void setContentGravity(int gravity) {
        mContentTextView.setGravity(gravity);

    }

    public void setColor(int color) {
        mPaintBackground.setAlpha(255);
        mPaintBackground.setColor(color);

        invalidate();
    }

    public void setBorder(int color, float width) {
        if (mPaintBorder == null) {
            mPaintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaintBorder.setStyle(Paint.Style.STROKE);
        }
        mPaintBorder.setColor(color);
        mPaintBorder.setStrokeWidth(width);
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setCloseBtnBackground(int drawableResId) {
        mCloseButton.setBackgroundColor(Color.TRANSPARENT);
        mCloseButton.setImageResource(drawableResId);
    }

    int location[] = new int[2];

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.getLocationOnScreen(location);

        mRect.set(getPaddingLeft(),
                getPaddingTop(),
                canvas.getWidth() - getPaddingRight(),
                canvas.getHeight() - getPaddingBottom());
        canvas.drawRoundRect(mRect, radius, radius, mPaintBackground);
        if (mPaintBorder != null) {
            canvas.drawRoundRect(mRect, radius, radius, mPaintBorder);
        }
    }
}
