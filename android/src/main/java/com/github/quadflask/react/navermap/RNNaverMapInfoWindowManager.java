package com.github.quadflask.react.navermap;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.LayoutShadowNode;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.naver.maps.map.overlay.Align;

import com.airbnb.android.react.maps.SizeReportingShadowNode2;
import static com.github.quadflask.react.navermap.ReactUtil.parseAlign;
import static com.github.quadflask.react.navermap.ReactUtil.parseColorString;
import static com.github.quadflask.react.navermap.ReactUtil.toNaverLatLng;

import java.util.HashMap;

public class RNNaverMapInfoWindowManager extends EventEmittableViewGroupManager<RNNaverMapInfoWindow> {
    private static final Align DEFAULT_CAPTION_ALIGN = Align.Bottom;

    private final DisplayMetrics metrics;

    public RNNaverMapInfoWindowManager(ReactApplicationContext reactContext) {
        super(reactContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            metrics = new DisplayMetrics();
            ((WindowManager) reactContext.getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay()
                    .getRealMetrics(metrics);
        } else {
            metrics = reactContext.getResources().getDisplayMetrics();
        }
    }

    @Override
    String[] getEventNames() {
        return new String[]{
                "onClick"
        };
    }

    @NonNull
    @Override
    public String getName() {
        return "RNNaverMapInfoWindow";
    }

    @NonNull
    @Override
    protected RNNaverMapInfoWindow createViewInstance(@NonNull ThemedReactContext reactContext) {
        return new RNNaverMapInfoWindow(this, reactContext);
    }

    @ReactProp(name = "coordinate")
    public void setCoordinate(RNNaverMapInfoWindow view, ReadableMap map) {
        view.setCoordinate(toNaverLatLng(map));
    }

    @ReactProp(name = "anchor")
    public void setAnchor(RNNaverMapInfoWindow view, ReadableMap map) {
        // should default to (0.5, 1) (bottom middle)
        float x = map != null && map.hasKey("x") ? (float) map.getDouble("x") : 0.5f;
        float y = map != null && map.hasKey("y") ? (float) map.getDouble("y") : 1.0f;
        view.setAnchor(x, y);
    }

    @ReactProp(name = "rotation", defaultFloat = 0.0f)
    public void setMarkerRotation(RNNaverMapInfoWindow view, float rotation) {
        view.setRotation(rotation);
    }

    @ReactProp(name = "animated", defaultBoolean = false)
    public void setAnimated(RNNaverMapInfoWindow view, boolean animated) {
        view.setAnimated(animated);
    }

    @ReactProp(name = "easing", defaultInt = -1)
    public void setEasing(RNNaverMapInfoWindow view, int easingFunction) {
        view.setEasing(easingFunction);
    }

    @ReactProp(name = "duration", defaultInt = 500)
    public void setDuration(RNNaverMapInfoWindow view, int duration) {
        view.setDuration(duration);
    }

    @ReactProp(name = "alpha", defaultFloat = 1f)
    public void setAlpha(RNNaverMapInfoWindow view, float alpha) {
        view.setAlpha(alpha);
    }

    @ReactProp(name = "text")
    public void setText(RNNaverMapInfoWindow view, String str) {
        view.setText(str);
    }

    @ReactProp(name = "childWidth")
    public void setChildWidth(RNNaverMapInfoWindow view, int width) {
        view.setChildWidth(width);
    }

    @ReactProp(name = "childHeight")
    public void setChildHeight(RNNaverMapInfoWindow view, int height) {
        view.setChildHeight(height);
    }

    @Override
    public void addView(RNNaverMapInfoWindow parent, View child, int index) {
        super.addView(parent, child, index);
        parent.setCustomView(child);
    }

    @Override
    public void removeViewAt(RNNaverMapInfoWindow parent, int index) {
        super.removeViewAt(parent, index);
        parent.setCustomView(null);
    }

    @Override
    public boolean needsCustomLayoutForChildren() {
        return true;
    }

    @Override
    public LayoutShadowNode createShadowNodeInstance() {
        // we use a custom shadow node that emits the width/height of the view
        // after layout with the updateExtraData method. Without this, we can't generate
        // a bitmap of the appropriate wqidth/height of the rendered view.
        return new SizeReportingShadowNode2();
    }

    @Override
    public void updateExtraData(RNNaverMapInfoWindow view, Object extraData) {
        // This method is called from the shadow node with the width/height of the rendered
        // marker view.
        HashMap<String, Float> data = (HashMap<String, Float>) extraData;
        float width = data.get("width");
        float height = data.get("height");
        view.update((int) width, (int) height, view);
    }
}
