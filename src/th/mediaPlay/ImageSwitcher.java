package th.mediaPlay;

import th.pd.R;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ImageSwitcher extends View {

    private static ValueAnimator getAnimator(Context context, boolean asNext,
            float interpolated) {
        final int DURATION = 500;

        ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
        a.setDuration(DURATION);
        a.setInterpolator(interpolator);

        final int playedTime = (int) (SquareInterpolator
                .getInversed(interpolated) * DURATION);
        a.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                if (animator instanceof ValueAnimator) {
                    ValueAnimator a = (ValueAnimator) animator;
                    a.setCurrentPlayTime(playedTime);
                }
            }
        });

        return a;
    }

    private static int getFlagsForAnim(boolean isEnter, boolean isForward) {
        if (isEnter) {
            return isForward
                    ? ANIM_FLAGS_ENTER_SCALE_FROM_CENTER
                    : ANIM_FLAGS_ENTER_TRANS_FROM_LEFT;
        } else {
            return isForward
                    ? ANIM_FLAGS_LEAVE_TRANS_TO_LEFT
                    : ANIM_FLAGS_LEAVE_SCALE_TO_CENTER;
        }
    }

    private static SquareInterpolator interpolator = new SquareInterpolator();

    private static final int FLAG_ENTER = 0x1;
    private static final int FLAG_SCALE = 0x10;
    private static final int FLAG_ALPHA = 0x20;
    private static final int FLAG_TRANS = 0x40;

    private static final int FLAG_TRANS_TO_LEFT = 0x400;
    private static final int FLAG_TRANS_TO_RIGHT = 0x600;
    private static final int FLAG_TRANS_TO_TOP = 0x800;
    private static final int FLAG_TRANS_TO_BOTTOM = 0x200;

    private static final int ANIM_FLAGS_ENTER_SCALE_FROM_CENTER =
            FLAG_ENTER | FLAG_SCALE | FLAG_ALPHA;
    private static final int ANIM_FLAGS_LEAVE_SCALE_TO_CENTER =
            FLAG_SCALE | FLAG_ALPHA;

    private static final int ANIM_FLAGS_ENTER_TRANS_FROM_LEFT =
            FLAG_ENTER | FLAG_TRANS | FLAG_TRANS_TO_RIGHT;
    private static final int ANIM_FLAGS_LEAVE_TRANS_TO_LEFT =
            FLAG_TRANS | FLAG_TRANS_TO_LEFT;

    private AnimatorSet mAnimatorSet = null;

    private ImageStatus mImage;
    private ImageStatus mComingImage;
    private boolean mComingAsNext = true;

    private Paint mPaint;

    public ImageSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        mImage = new ImageStatus();
        mComingImage = new ImageStatus();
        mPaint = new Paint();
    }

    public void doneSwitching() {
        if (isSwitching()) {
            mAnimatorSet.end();
        }
    }

    public void fallbackSwitching(float currentFraction) {
        if (isSwitching()) {
            return;
        }

        // swap bitmap and do animate
        Bitmap bitmap = mImage.bitmap;
        mImage.initialize(mComingImage.bitmap);
        setComingImage(bitmap, !mComingAsNext);
        switchTo(1f - currentFraction);
    }

    public boolean isSwitching() {
        return mAnimatorSet != null && mAnimatorSet.isRunning();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(getResources().getColor(R.color.dev_gray9));
        if (mComingAsNext) {
            onDrawImage(canvas, mPaint, mComingImage);
            onDrawImage(canvas, mPaint, mImage);
        } else {
            onDrawImage(canvas, mPaint, mImage);
            onDrawImage(canvas, mPaint, mComingImage);
        }
        super.onDraw(canvas);
    }

    private void onDrawImage(Canvas canvas, Paint paint, ImageStatus image) {
        if (image.isValid()) {
            paint.setAlpha(image.alpha);
            canvas.drawBitmap(image.bitmap, null, image.rect, paint);
        }
    }

    public void reset() {
        mImage.restore();
        mComingImage.clear();
        mComingAsNext = true;
        invalidate();
    }

    /**
     * Will trigger half way switch animation<br/>
     * Note this is totally different from View.scrollTo()<br/>
     */
    public void scrollTo(float animatedFraction) {
        if (isSwitching()) {
            return;
        }

        updateImageStatus(mImage,
                getFlagsForAnim(false, mComingAsNext), animatedFraction);
        updateImageStatus(mComingImage,
                getFlagsForAnim(true, mComingAsNext), animatedFraction);
        invalidate();
    }

    public void setComingImage(Bitmap bitmap, boolean asNext) {
        if (isSwitching()) {
            return;
        }

        mComingImage.initialize(bitmap);
        mComingAsNext = asNext;
    }

    /**
     * switch to given image
     *
     * @param asNext
     *            <code>true</code> if the next image is required
     */
    public void switchTo(float animatedFraction) {
        if (isSwitching()) {
            return;
        }

        ValueAnimator enterAnimator = getAnimator(
                getContext(), true, animatedFraction);
        enterAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                float animatedFraction = animator.getAnimatedFraction();
                updateImageStatus(mComingImage,
                        getFlagsForAnim(true, mComingAsNext),
                        animatedFraction);
                invalidate();
            }
        });

        ValueAnimator leaveAnimator = getAnimator(
                getContext(), false, animatedFraction);
        leaveAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                float animatedFraction = animator.getAnimatedFraction();
                updateImageStatus(mImage,
                        getFlagsForAnim(false, mComingAsNext),
                        animatedFraction);
                // we has called invalidate() in enterAnimator
            }
        });

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                mImage.initialize(mComingImage.bitmap);
                mComingImage.clear();
                invalidate();
            }
        });

        mAnimatorSet.playTogether(enterAnimator, leaveAnimator);
        mAnimatorSet.start();
    }

    private void updateImageStatus(ImageStatus imageStatus, int flags,
            float fraction) {

        if (!imageStatus.isValid()) {
            return;
        }

        boolean isEnter = (flags & FLAG_ENTER) != 0;

        if ((flags & FLAG_ALPHA) != 0) {
            final float ALPHA_START = 0.4f;
            final float ALPHA_FINAL = 1.0f;
            imageStatus.alpha(isEnter
                    ? (ALPHA_FINAL - ALPHA_START) * fraction + ALPHA_START
                    : (ALPHA_START - ALPHA_FINAL) * fraction + ALPHA_FINAL);
        } else {
            imageStatus.alpha(1.0f);
        }

        if ((flags & FLAG_TRANS) != 0) {
            int offsetX = 0;
            int offsetY = 0;
            switch (flags & 0xF00) {
                case FLAG_TRANS_TO_LEFT: {
                    int wBitmap = imageStatus.bitmap.getWidth();
                    if (isEnter) {
                        offsetX = (int) (wBitmap * (1f - fraction));
                    } else {
                        offsetX = (int) (wBitmap * -fraction);
                    }
                    break;
                }
                case FLAG_TRANS_TO_RIGHT: {
                    int wTotal = imageStatus.bitmap.getWidth();
                    if (isEnter) {
                        offsetX = (int) (wTotal * (fraction - 1f));
                    } else {
                        offsetX = (int) (wTotal * fraction);
                    }
                    break;
                }
                case FLAG_TRANS_TO_TOP:
                    offsetY = (int) (imageStatus.bitmap.getHeight() * fraction);
                    break;
                case FLAG_TRANS_TO_BOTTOM:
                    offsetY = -(int) (imageStatus.bitmap.getHeight() * fraction);
                    break;
            }
            imageStatus.offset(offsetX, offsetY);
        }

        if ((flags & FLAG_SCALE) != 0) {
            final float SCALE_START = 0.4f;
            final float SCALE_FINAL = 1.0f;
            imageStatus.scale(isEnter
                    ? (SCALE_FINAL - SCALE_START) * fraction + SCALE_START
                    : (SCALE_START - SCALE_FINAL) * fraction + SCALE_FINAL);

            int wView = getWidth();
            int hView = getHeight();
            int w = imageStatus.rect.width();
            int h = imageStatus.rect.height();
            imageStatus.offset((wView - w) / 2, (hView - h) / 2);
        }
    }
}
