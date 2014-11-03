package us.axelia.axelia;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by mac on 3/11/14.
 */
public class TextViewPermanentMarquee extends TextView {
    public TextViewPermanentMarquee(Context context) {
        super(context);
    }

    public TextViewPermanentMarquee(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewPermanentMarquee(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (focused) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (hasWindowFocus) {
            super.onWindowFocusChanged(hasWindowFocus);
        }
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
