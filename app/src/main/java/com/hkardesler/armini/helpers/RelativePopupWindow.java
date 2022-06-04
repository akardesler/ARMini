package com.hkardesler.armini.helpers;

import android.content.Context;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.core.widget.PopupWindowCompat;

public class RelativePopupWindow extends PopupWindow {

    public RelativePopupWindow(Context context) {
    }

    public void showOnAnchor(@NonNull View anchor, int vertPos, int horizPos) {
        showOnAnchor(anchor, vertPos, horizPos, 0, 0);
    }

    public void showOnAnchor(@NonNull View anchor, int vertPos, int horizPos, int x, int y) {
        showOnAnchor(anchor, vertPos, horizPos, x, y, true);
    }

    public void showOnAnchor(@NonNull View anchor, int vertPos, int horizPos, int x, int y, boolean fitInScreen) {
        setClippingEnabled(fitInScreen);
        View contentView = getContentView();
        contentView.measure(makeDropDownMeasureSpec(getWidth()), makeDropDownMeasureSpec(getHeight()));
        int measuredW = contentView.getMeasuredWidth();
        int measuredH = contentView.getMeasuredHeight();
        if (!fitInScreen) {
            int[] anchorLocation = new int[2];
            anchor.getLocationInWindow(anchorLocation);
            x += anchorLocation[0];
            y += anchorLocation[1] + anchor.getHeight();
        }
        switch (vertPos) {
            case 0:
                y -= (anchor.getHeight() / 2) + (measuredH / 2);
                break;
            case 1:
                y -= anchor.getHeight() + measuredH;
                break;
            case 3:
                y -= anchor.getHeight();
                break;
            case 4:
                y -= measuredH;
                break;
        }
        switch (horizPos) {
            case 0:
                x += (anchor.getWidth() / 2) - (measuredW / 2);
                break;
            case 1:
                x -= measuredW;
                break;
            case 2:
                x += anchor.getWidth();
                break;
            case 4:
                x -= measuredW - anchor.getWidth();
                break;
        }
        if (fitInScreen) {
            PopupWindowCompat.showAsDropDown(this, anchor, x, y, 0);
        } else {
            showAtLocation(anchor, 0, x, y);
        }
    }

    private static int makeDropDownMeasureSpec(int measureSpec) {
        return MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(measureSpec), getDropDownMeasureSpecMode(measureSpec));
    }

    private static int getDropDownMeasureSpecMode(int measureSpec) {
        switch (measureSpec) {
            case -2:
                return 0;
            default:
                return 0;
        }
    }
}