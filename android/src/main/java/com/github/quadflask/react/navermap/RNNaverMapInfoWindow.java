package com.github.quadflask.react.navermap;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.util.Property;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;


import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeHolder;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.OverlayImage;
//import com.naver.maps.map.overlay.
// import com.naver.maps.map.overlay.OverlayImage;
// import com.naver.maps.map.overlay.Align;

public class RNNaverMapInfoWindow extends ClickableRNNaverMapFeature<InfoWindow> {
    private final DraweeHolder<GenericDraweeHierarchy> imageHolder;
    private boolean animated = false;
    private int duration = 500;
    private TimeInterpolator easingFunction;
    private int width;
    private int height;
    private Bitmap mLastBitmapCreated = null;

    public RNNaverMapInfoWindow(EventEmittable emitter, Context context) {
        super(emitter, context);
        feature = new InfoWindow();
        imageHolder = DraweeHolder.create(createDraweeHierarchy(), context);
        imageHolder.onAttach();
    }

    private GenericDraweeHierarchy createDraweeHierarchy() {
        return new GenericDraweeHierarchyBuilder(getResources())
                .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                .setFadeDuration(0)
                .build();
    }

    public void setCoordinate(LatLng latLng) {
        if (animated && this.duration > 0) setCoordinateAnimated(latLng, this.duration);
        else feature.setPosition(latLng);
    }

    public void setCoordinateAnimated(LatLng finalPosition, int duration) {
        if (Double.isNaN(feature.getPosition().latitude)) {
            feature.setPosition(finalPosition);
        } else {
            Property<InfoWindow, LatLng> property = Property.of(InfoWindow.class, LatLng.class, "position");
            ObjectAnimator animator = ObjectAnimator.ofObject(
                    feature,
                    property,
                    ReactUtil::interpolate,
                    finalPosition);
            animator.setDuration(duration);
            if (easingFunction != null)
                animator.setInterpolator(easingFunction);
            animator.start();
        }
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    public void setEasing(Integer easing) {
        switch (easing) {
            case 0:
                easingFunction = new LinearInterpolator();
                break;
            case 1:
                easingFunction = new AccelerateDecelerateInterpolator();
                break;
            case 2:
                easingFunction = new AccelerateInterpolator();
                break;
            case 3:
                easingFunction = new DecelerateInterpolator();
                break;
            case 4:
                easingFunction = new BounceInterpolator();
                break;
            default:
                easingFunction = null;
        }
    }

    public void setDuration(Integer duration) {
        if (duration != null && duration >= 0) {
            this.duration = duration;
        }
    }

    public void setAlpha(float alpha) {
        feature.setAlpha(alpha);
    }

    public void setAnchor(float x, float y) {
        feature.setAnchor(new PointF(x, y));
    }

    public void setText(String str) {
        if (str != null) {
            Context context = getContext();

            feature.setAdapter(new InfoWindow.DefaultTextAdapter(context) {
                @NonNull
                @Override
                public CharSequence getText(@NonNull InfoWindow feature) {
                    return str;
                }
            });
        }
    }

    public void setCustomView(View view) {
        if (view != null) {
            int width = this.width <= 0 ? 100 : this.width;
            int height = this.height <= 0 ? 100 : this.height;

            feature.setAdapter(new InfoWindow.ViewAdapter() {
                @NonNull
                @Override
                public View getView(@NonNull InfoWindow feature) {
                    view.setLayoutParams(new LayoutParams(width, height));

                    return view;
                }
            });
//            setImage(view);
        } else {
            Context context = getContext();

            feature.setAdapter(new InfoWindow.DefaultTextAdapter(context) {
                @NonNull
                @Override
                public CharSequence getText(@NonNull InfoWindow feature) {
                    return "";
                }
            });
        }
    }

    public void update(int width, int height, RNNaverMapInfoWindow view) {
        this.width = width;
        this.height = height;

        setCustomView(view);
    }

    @Override
    public void addView(View child, int index) {
        super.addView(child, index);
        setCustomView(child);
    }

    public void setImage(View view) {
        OverlayImage overlayImage = getImage(view);

        feature.setAdapter(new InfoWindow.Adapter() {
            @NonNull
            @Override
            public OverlayImage getImage(@NonNull InfoWindow feature) {
                return overlayImage;
            }
        });
    }

    private OverlayImage getImage(View view) {
        int width = view.getWidth() <= 0 ? 100 : view.getWidth();
        int height = view.getHeight() <= 0 ? 100 : view.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        view.draw(canvas);

        return OverlayImage.fromBitmap(bitmap);
    }

//    private OverlayImage getImage() {
//        // creating a bitmap from an arbitrary view
//        Bitmap viewBitmap = createDrawable();
//        int width = viewBitmap.getWidth();
//        int height = viewBitmap.getHeight();
//        Bitmap combinedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(combinedBitmap);
//        canvas.drawBitmap(viewBitmap, 0, 0, null);
//        return OverlayImage.fromBitmap(combinedBitmap);
//    }

    private Bitmap createDrawable() {
        int width = this.width <= 0 ? 100 : this.width;
        int height = this.height <= 0 ? 100 : this.height;
        this.buildDrawingCache();

        // Do not create the doublebuffer-bitmap each time. reuse it to save memory.
        Bitmap bitmap = mLastBitmapCreated;

        if (bitmap == null || bitmap.isRecycled() || bitmap.getWidth() != width || bitmap.getHeight() != height) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mLastBitmapCreated = bitmap;
        } else {
            bitmap.eraseColor(Color.TRANSPARENT);
        }

        Canvas canvas = new Canvas(bitmap);
        this.draw(canvas);

        return bitmap;
    }
}
