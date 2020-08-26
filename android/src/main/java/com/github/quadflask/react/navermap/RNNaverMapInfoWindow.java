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
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import androidx.cardview.widget.CardView;

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
    private final Context context;

    public RNNaverMapInfoWindow(EventEmittable emitter, Context context) {
        super(emitter, context);
        this.context = context;
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

    public void setChildWidth(int width) {
        this.width = width;
    }

    public void setChildHeight(int height) {
        this.height = height;
    }

    public void setText(String str) {
        if (str != null) {
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
//            int width = this.width <= 0 ? 100 : this.width;
//            int height = this.height <= 0 ? 100 : this.height;
//
////            LinearLayout LL = new LinearLayout(context);
////            LL.setOrientation(LinearLayout.VERTICAL);
////            LL.setLayoutParams(new LinearLayout.LayoutParams(
////                    width,
////                    height,
////                    0f
////            ));
////
////            LL.addView(view);
//
////            LayoutParams params = view.getLayoutParams();
////            params.width = width;
////            params.height = height;
////            view.setLayoutParams(params);
//
//            int specWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
//            int specHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
//            view.measure(specWidth, specHeight);
//            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
//
//            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            params.width = width;
//            params.height = height;
//            view.setLayoutParams(params);
//
//            feature.setAdapter(new InfoWindow.ViewAdapter() {
//                @NonNull
//                @Override
//                public View getView(@NonNull InfoWindow feature) {
//                    return view;
//                }
//            });

            setImage(view);
        } else {
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

    private LinearLayout createView() {
        LinearLayout LL = new LinearLayout(context);
        LL.setOrientation(LinearLayout.VERTICAL);
        LL.setLayoutParams(new LinearLayout.LayoutParams(
                this.width,
                this.height
        ));
        LL.layout();

        TextView textView1 = new TextView(context);
        textView1.setText("심평원 데이터에요");
        textView1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        textView1.setTextColor(Color.parseColor("#000000"));

        TextView textView2 = new TextView(context);
        textView2.setText("120만원");
        textView2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView2.setTextColor(Color.parseColor("#ffffff"));

        CardView cardView1 = new CardView(context);
        cardView1.setCardBackgroundColor(Color.parseColor("#4c1192"));
        cardView1.setRadius(6);
        cardView1.addView(textView2);

        TextView textView3 = new TextView(context);
        textView3.setText("▼");
        textView3.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView3.setTextColor(Color.parseColor("#4c1192"));

        LL.addView(textView1);
        LL.addView(cardView1);
        LL.addView(textView3);
        return LL;
    }

    public void setImage(View view) {
        OverlayImage overlayImage = getImageView();
//        OverlayImage overlayImage = getViewImage(view);

        feature.setAdapter(new InfoWindow.Adapter() {
            @NonNull
            @Override
            public OverlayImage getImage(@NonNull InfoWindow feature) {
                return overlayImage;
            }
        });
    }

//    private OverlayImage getImage(View view) {
//        int width = this.width <= 0 ? 100 : this.width;
//        int height = this.height <= 0 ? 100 : this.height;
//
//        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
////        if (view.getMeasuredHeight() <= 0) {
////            view.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
////            Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
////            Canvas canvas = new Canvas(bitmap);
////            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
////            view.draw(canvas);
////            return OverlayImage.fromBitmap(bitmap);
////        }
////
////        Bitmap bitmap = Bitmap.createBitmap(view.getLayoutParams().width, view.getLayoutParams().height, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
//        view.draw(canvas);
//
//        return OverlayImage.fromBitmap(bitmap);
//    }

//    private OverlayImage getViewImage(View view) {
//        int width = this.width <= 0 ? 100 : this.width;
//        int height = this.height <= 0 ? 100 : this.height;
//
//        int specWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
//        int specHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
//        view.measure(specWidth, specHeight);
//        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
//
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        params.width = view.getMeasuredWidth();
//        params.height = view.getMeasuredHeight();
//        view.setLayoutParams(params);
//
//        return OverlayImage.fromView(view);
//    }

    private OverlayImage getImageView() {
        LinearLayout view = createView();

        int specWidth = MeasureSpec.makeMeasureSpec(this.width, MeasureSpec.AT_MOST);
        int specHeight = MeasureSpec.makeMeasureSpec(this.height, MeasureSpec.AT_MOST);
        view.measure(specWidth, specHeight);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        // creating a bitmap from an arbitrary view
        Bitmap viewBitmap = createDrawable(view);

//        int width = viewBitmap.getWidth();
//        int height = viewBitmap.getHeight();
//        Bitmap combinedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(viewBitmap);
//        canvas.drawBitmap(viewBitmap, 0, 0, null);
        view.draw(canvas);
        return OverlayImage.fromBitmap(viewBitmap);
    }

    private Bitmap createDrawable(View view) {
        this.width = view.getWidth();
        this.height = view.getHeight();

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

//        Canvas canvas = new Canvas(bitmap);
//        this.draw(canvas);

        return bitmap;
    }
}
