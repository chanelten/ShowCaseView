package smartdevelop.ir.eram.showcaseviewlib;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Xfermode;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import smartdevelop.ir.eram.showcaseviewlib.utils.BitmapUtil;
import smartdevelop.ir.eram.showcaseviewlib.utils.PersistentData;

/**
 * Created by Mohammad Reza Eram on 20/01/2018.
 */

public class GuideView extends FrameLayout {

    private static final int DEFAULT_RADIUS = 15;
    private static final int DEFAULT_BACKGROUND_COLOR = 0xdd000000;
    private static final float DEFAULT_INDICATOR_HEIGHT = 30;
    private static final int DEFAULT_INDICATOR_MARGIN_START = 15;

    private final float density;
    private final View target;
    private final int radius;
    private final int backgroundColor;
    private final Bitmap indicatorDrawable;
    private final float indicatorHeight;
    private final int indicatorMarginStartRequired;
    private int indicatorMarginStartComputed;
    private final int padding;
    private RectF rect;
    private GuideMessageView mMessageView;
    private boolean isTop;
    private Gravity mGravity;
    private DismissType dismissType;
    private boolean mIsShowing;
    private boolean showOnce;
    private String showId;
    private GuideListener mGuideListener;
    int xMessageView = 0;
    int yMessageView = 0;

    final int ANIMATION_DURATION = 400;
    final Paint emptyPaint = new Paint();
    final Paint paintLine = new Paint();
    final Paint paintCircle = new Paint();
    final Paint paintCircleInner = new Paint();
    final Paint mPaint = new Paint();
    final Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Xfermode XFERMODE_CLEAR = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    public interface GuideListener {
        /**
         * @param view
         * @return boolean true if ShowCaseView is ended, or false if another ShowCaseView will be displayed.
         * This boolean is used to determine if showing a group of ShowCaseView has reached its end for #enableShowOnce.
         */
        boolean onDismiss(View view);
    }

    public enum Gravity {
        auto, center
    }

    public enum DismissType {
        outside, anywhere, targetView
    }

    private GuideView(Context context, View view, int radius, int backgroundColor,
                      Integer drawableIndicator, int indicatorMarginStart,
                      Integer closeButtonPadding, Position closeButtonPosition) {
        super(context);
        setWillNotDraw(false);

        this.target = view;
        this.radius = radius;
        this.backgroundColor = backgroundColor;
        this.indicatorDrawable = drawableIndicator != null ? BitmapFactory.decodeResource(getResources(), drawableIndicator) : null;
        this.indicatorMarginStartRequired = indicatorMarginStart;

        this.density = context.getResources().getDisplayMetrics().density;
        this.indicatorHeight = indicatorDrawable != null ? indicatorDrawable.getHeight() : DEFAULT_INDICATOR_HEIGHT * density;
        this.padding = (int) (5 * density);

        int[] locationTarget = new int[2];
        target.getLocationOnScreen(locationTarget);
        rect = new RectF(locationTarget[0], locationTarget[1],
                locationTarget[0] + target.getWidth(),
                locationTarget[1] + target.getHeight());

        mMessageView = new GuideMessageView(getContext(), closeButtonPosition, closeButtonPadding, new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mMessageView.setPadding(padding, padding, padding, padding);
        mMessageView.setRadius(radius);
        mMessageView.setColor(Color.WHITE);

        addView(mMessageView, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));


        setMessageLocation(resolveMessageViewLocation());

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setMessageLocation(resolveMessageViewLocation());
                int[] locationTarget = new int[2];
                target.getLocationOnScreen(locationTarget);
                rect = new RectF(locationTarget[0], locationTarget[1],
                        locationTarget[0] + target.getWidth(), locationTarget[1] + target.getHeight());
            }
        });
    }

    private int getNavigationBarSize() {
        Resources resources = getContext().getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private boolean isLandscape() {
        int display_mode = getResources().getConfiguration().orientation;
        return display_mode != Configuration.ORIENTATION_PORTRAIT;
    }

    private final Rect srcRect = new Rect();
    private final Rect destRect = new Rect();
    private final Rect clipRect = new Rect();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (target != null) {
            Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas tempCanvas = new Canvas(bitmap);

            // Paint background
            mPaint.setColor(backgroundColor);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setAntiAlias(true);
            canvas.getClipBounds(clipRect);
            tempCanvas.drawRect(clipRect, mPaint);

            // Paint Indicator (Arrow Pointer)
            // Tip of arrow pointer
            final float startY = (isTop ? rect.bottom : rect.top) + indicatorMarginStartComputed;
            // End of arrow pointer
            final float stopY = yMessageView + (isTop ? padding : mMessageView.getHeight() - padding);
            final float x = (rect.left / 2 + rect.right / 2);

            if (indicatorDrawable != null) {
                // Draw Indicator using Drawable
                final int halfWidth = indicatorDrawable.getWidth() / 2;
                final int left = (int) x - halfWidth;
                final int top = isTop ? (int) startY : (int) stopY;
                final int right = (int) x + halfWidth;
                final int bottom = isTop ? (int) stopY : (int) startY;

                srcRect.set(0, 0, indicatorDrawable.getWidth(), indicatorDrawable.getHeight());
                destRect.set(left, top, right, bottom);

                tempCanvas.drawBitmap(isTop ? indicatorDrawable : BitmapUtil.rotate(indicatorDrawable, 180),
                        srcRect, destRect, null);
            } else {
                // Draw Indicator using default arrow
                float lineWidth = 3 * density;
                float strokeCircleWidth = 3 * density;
                float circleSize = 6 * density;
                float circleInnerSize = 5f * density;
                float startYTemp = startY + ((indicatorMarginStartComputed == 0 ? strokeCircleWidth + circleSize : 0) * (isTop ? 1 : -1));

                paintLine.setStyle(Paint.Style.FILL);
                paintLine.setColor(Color.WHITE);
                paintLine.setStrokeWidth(lineWidth);
                paintLine.setAntiAlias(true);

                paintCircle.setStyle(Paint.Style.STROKE);
                paintCircle.setColor(Color.WHITE);
                paintCircle.setStrokeCap(Paint.Cap.ROUND);
                paintCircle.setStrokeWidth(strokeCircleWidth);
                paintCircle.setAntiAlias(true);

                paintCircleInner.setStyle(Paint.Style.FILL);
                paintCircleInner.setColor(0xffcccccc);
                paintCircleInner.setAntiAlias(true);

                tempCanvas.drawLine(x, startYTemp, x, stopY, paintLine);
                tempCanvas.drawCircle(x, startYTemp, circleSize, paintCircle);
                tempCanvas.drawCircle(x, startYTemp, circleInnerSize, paintCircleInner);
            }

            // Paint target
            targetPaint.setXfermode(XFERMODE_CLEAR);
            targetPaint.setAntiAlias(true);
            tempCanvas.drawRoundRect(rect, radius, radius, targetPaint);

            canvas.drawBitmap(bitmap, 0, 0, emptyPaint);
        }
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    public void dismiss() {
        AlphaAnimation startAnimation = new AlphaAnimation(1f, 0f);
        startAnimation.setDuration(ANIMATION_DURATION);
        startAnimation.setFillAfter(true);
        this.startAnimation(startAnimation);
        ((ViewGroup) ((Activity) getContext()).getWindow().getDecorView()).removeView(this);
        mIsShowing = false;
        if (mGuideListener != null) {
            boolean dismissed = mGuideListener.onDismiss(target);
            if (showOnce && dismissed) {
                PersistentData.instance(getContext()).setShowView(showId, false);
            }
        } else if (showOnce) {
            PersistentData.instance(getContext()).setShowView(showId, false);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            switch (dismissType) {

                case outside:
                    if (!isViewContains(mMessageView, x, y)) {
                        dismiss();
                    }
                    break;

                case anywhere:
                    dismiss();
                    break;

                case targetView:
                    if (rect.contains(x, y)) {
                        target.performClick();
                        dismiss();
                    }
                    break;

            }
            return true;
        }
        return false;
    }

    private boolean isViewContains(View view, float rx, float ry) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        int w = view.getWidth();
        int h = view.getHeight();

        return !(rx < x || rx > x + w || ry < y || ry > y + h);
    }

    void setMessageLocation(Point p) {
        mMessageView.setX(p.x);
        mMessageView.setY(p.y);
        requestLayout();
    }


    private Point resolveMessageViewLocation() {

        if (mGravity == Gravity.center) {
            xMessageView = (int) (rect.left - mMessageView.getWidth() / 2 + target.getWidth() / 2);
        } else
            xMessageView = (int) (rect.right) - mMessageView.getWidth();

        if (isLandscape()) {
            xMessageView -= getNavigationBarSize();
        }

        if (xMessageView + mMessageView.getWidth() > getWidth())
            xMessageView = getWidth() - mMessageView.getWidth();
        if (xMessageView < 0)
            xMessageView = 0;

        indicatorMarginStartComputed = (int) (indicatorMarginStartRequired * density);
        //set message view bottom
        if (rect.top + indicatorHeight > getHeight() / 2) {
            isTop = false;
            indicatorMarginStartComputed = indicatorMarginStartComputed * -1;
            yMessageView = (int) (indicatorMarginStartComputed + rect.top - mMessageView.getHeight() - indicatorHeight);
        }
        //set message view top
        else {
            isTop = true;
            indicatorMarginStartComputed = indicatorMarginStartComputed * 1;
            yMessageView = (int) (indicatorMarginStartComputed + rect.top + target.getHeight() + indicatorHeight);
        }

        if (yMessageView < 0)
            yMessageView = 0;


        return new Point(xMessageView, yMessageView);
    }

    public void show() {
        if (!PersistentData.instance(getContext()).showView(showId)) {
            // Do nothing
            return;
        }
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        this.setClickable(false);

        ((ViewGroup) ((Activity) getContext()).getWindow().getDecorView()).addView(this);
        AlphaAnimation startAnimation = new AlphaAnimation(0.0f, 1.0f);
        startAnimation.setDuration(ANIMATION_DURATION);
        startAnimation.setFillAfter(true);
        this.startAnimation(startAnimation);
        mIsShowing = true;
    }

    public void setTitle(CharSequence str) {
        mMessageView.setTitle(str);
    }

    public void setTitleTextColor(@ColorInt int color) {
        mMessageView.setTitleTextColor(color);
    }

    public void setTitleTypeface(Typeface typeface) {
        mMessageView.setTitleTypeface(typeface);
    }

    public void setTitleTextSize(int unit, int size) {
        mMessageView.setTitleTextSize(unit, size);
    }

    public void setTitleGravity(int gravity) {
        mMessageView.setTitleGravity(gravity);
    }

    public void setContentText(CharSequence str) {
        mMessageView.setContentText(str);
    }

    public void setContentTextColor(@ColorInt int color) {
        mMessageView.setContentTextColor(color);
    }

    public void setContentTypeface(Typeface typeface) {
        mMessageView.setContentTypeface(typeface);
    }

    public void setContentTextSize(int unit, int size) {
        mMessageView.setContentTextSize(unit, size);
    }

    public void setContentGravity(int gravity) {
        mMessageView.setContentGravity(gravity);
    }

    public void setBorder(@ColorInt int color, float width) {
        mMessageView.setBorder(color, width);
    }

    public void setCloseButtonBackground(@DrawableRes @ColorRes int resId) {
        mMessageView.setCloseBtnBackground(resId);
    }

    private void showOnce(String showId) {
        this.showOnce = true;
        this.showId = showId;
    }

    private void setShowId(String showId) {
        this.showId = showId;
    }


    public static class Builder {
        private Integer radius;
        private View targetView;
        private Integer backgroundColor;
        private Integer indicatorResId;
        private Integer indicatorMarginStart;
        private CharSequence title, contentText;
        private int titleTextColor;
        private Integer titleTextSizeUnit;
        private int titleTextSize;
        private Integer titleGravity;
        private int contentTextColor;
        private Integer contentTextSizeUnit;
        private int contentTextSize;
        private Integer contentGravity;
        private Gravity gravity;
        private DismissType dismissType;
        private Context context;
        private Typeface titleTypeface, contentTypeface;
        private GuideListener guideListener;
        private Integer borderColor;
        private Float borderWidth;
        private Integer closeButtonPadding;
        private Position closeButtonPosition;
        private int closeButtonBackgroundResource;
        private boolean showOnce;
        private String showId;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setRadius(int radius) {
            this.radius = radius;
            return this;
        }

        public Builder setTargetView(@NonNull View view) {
            this.targetView = view;
            return this;
        }

        public Builder setBackgroundColor(@ColorInt int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder setIndicator(@DrawableRes int indicatorResId) {
            this.indicatorResId = indicatorResId;
            return this;
        }

        public Builder setIndicatorMarginStart(int indicatorMarginStart) {
            this.indicatorMarginStart = indicatorMarginStart;
            return this;
        }

        public Builder setGravity(Gravity gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder setTitle(CharSequence title) {
            this.title = title;
            return this;
        }

        public Builder setTitleTextColor(@ColorInt int color) {
            this.titleTextColor = color;
            return this;
        }

        public Builder setContentText(CharSequence contentText) {
            this.contentText = contentText;
            return this;
        }

        public Builder setContentTextColor(@ColorInt int color) {
            this.contentTextColor = color;
            return this;
        }

        public Builder setContentTypeface(Typeface typeface) {
            this.contentTypeface = typeface;
            return this;
        }

        public Builder setGuideListener(GuideListener guideListener) {
            this.guideListener = guideListener;
            return this;
        }

        public Builder setTitleTypeface(Typeface typeface) {
            this.titleTypeface = typeface;
            return this;
        }

        public Builder setCloseButton(Position position, @DrawableRes int drawableResId) {
            this.closeButtonPosition = position;
            this.closeButtonBackgroundResource = drawableResId;
            return this;
        }

        public Builder setCloseButtonPadding(@Px int padding) {
            this.closeButtonPadding = padding;
            return this;
        }

        /**
         * the defined text size overrides any defined size in the default or provided style
         *
         * @param unit Unit
         * @param size title text by sp unit
         * @return builder
         */
        public Builder setContentTextSize(int unit, int size) {
            this.contentTextSizeUnit = unit;
            this.contentTextSize = size;
            return this;
        }

        /**
         * the defined text size overrides any defined size in the default or provided style
         *
         * @param size title text by sp unit
         * @return builder
         */
        public Builder setTitleTextSize(int unit, int size) {
            this.titleTextSizeUnit = unit;
            this.titleTextSize = size;
            return this;
        }

        public Builder setDismissType(DismissType dismissType) {
            this.dismissType = dismissType;
            return this;
        }

        public Builder setTitleGravity(int gravity) {
            this.titleGravity = gravity;
            return this;
        }

        public Builder setContentGravity(int gravity) {
            this.contentGravity = gravity;
            return this;
        }

        public Builder setBorder(@ColorInt int color, float width) {
            this.borderColor = color;
            this.borderWidth = width;
            return this;
        }

        /**
         * Show the ShowCaseView only once, and persists its state.
         *
         * @return
         */
        public Builder enableShowOnce(String id) {
            this.showOnce = true;
            this.showId = id;
            return this;
        }

        /**
         * Show the ShowCaseView again, and persists its state.
         */
        public Builder disableShowOnce(String id) {
            this.showId = id;
            disableShowOnce(context, id);
            return this;
        }

        /**
         * Show the ShowCaseView again, and persists its state.
         * This function can be called even without instantiating a new Builder object.
         *
         * @param context
         */
        public static void disableShowOnce(Context context, String id) {
            PersistentData.instance(context).setShowView(id, true);
        }

        public GuideView build() {
            GuideView guideView = new GuideView(context, targetView,
                    radius != null ? radius : DEFAULT_RADIUS,
                    backgroundColor != null ? backgroundColor : DEFAULT_BACKGROUND_COLOR,
                    indicatorResId,
                    closeButtonPadding,
                    indicatorMarginStart != null ? indicatorMarginStart : DEFAULT_INDICATOR_MARGIN_START,
                    closeButtonPosition);
            guideView.mGravity = gravity != null ? gravity : Gravity.auto;
            guideView.dismissType = dismissType != null ? dismissType : DismissType.targetView;

            guideView.setTitle(title);
            if (contentText != null)
                guideView.setContentText(contentText);
            if (titleTextSize != 0)
                guideView.setTitleTextSize(titleTextSizeUnit == null ? -1 : titleTextSizeUnit, titleTextSize);
            if (titleTextColor != 0)
                guideView.setTitleTextColor(titleTextColor);
            if (titleTypeface != null)
                guideView.setTitleTypeface(titleTypeface);
            if (titleGravity != null)
                guideView.setTitleGravity(titleGravity);
            if (contentTextSize != 0)
                guideView.setContentTextSize(contentTextSizeUnit == null ? -1 : contentTextSizeUnit, contentTextSize);
            if (contentTextColor != 0)
                guideView.setContentTextColor(contentTextColor);
            if (contentTypeface != null)
                guideView.setContentTypeface(contentTypeface);
            if (contentGravity != null)
                guideView.setContentGravity(contentGravity);
            if (guideListener != null)
                guideView.mGuideListener = guideListener;
            if (borderColor != null && borderWidth != null)
                guideView.setBorder(borderColor, borderWidth);
            if (closeButtonPosition != null && closeButtonBackgroundResource != 0)
                guideView.setCloseButtonBackground(closeButtonBackgroundResource);
            if (showOnce && showId != null) {
                guideView.showOnce(showId);
            } else if (showId != null) {
                guideView.setShowId(showId);
            }

            return guideView;
        }
    }
}

