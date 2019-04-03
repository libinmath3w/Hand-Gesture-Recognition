package tech.aksharam.handgesture.RecognitionObjectsTensorFlow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

/** A {@link TextureView} that can be adjusted to a specified aspect ratio. */
public class AutoFitTextureView2 extends TextureView {

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    public AutoFitTextureView2(Context context) {
        this(context, null);
    }

    public AutoFitTextureView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitTextureView2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Set the aspect ratio of this view. The size of the view will be measured based on the proportion
     *       * calculated from the parameters. Keep in mind that the actual size of the parameters does not matter, that is,
     *       * call setAspectRatio (2, 3) and setAspectRatio (4, 6) make the same result.
     *       *
     *       * @param width Relative horizontal size
     *       * @param height Relative vertical size
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("The size can not be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }
}