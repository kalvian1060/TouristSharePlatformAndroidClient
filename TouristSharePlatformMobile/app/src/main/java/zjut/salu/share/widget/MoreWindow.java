package zjut.salu.share.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import zjut.salu.share.R;
import zjut.salu.share.animation.KickBackAnimator;
import zjut.salu.share.utils.FastBlur;

/**底部弹出框
 * Created by Salu on 2016/12/6.
 */

public class MoreWindow extends PopupWindow{
    private String TAG = MoreWindow.class.getSimpleName();
    Activity mContext;
    private int mWidth;
    private int mHeight;
    private int statusBarHeight ;
    private Bitmap mBitmap= null;
    private Bitmap overlay = null;

    private Handler mHandler = new Handler();
    private OnClickListener listener;
    public MoreWindow(Activity context,OnClickListener listener) {
        mContext = context;
        this.listener=listener;
    }

    public void init() {
        Rect frame = new Rect();
        mContext.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        statusBarHeight = frame.top;
        DisplayMetrics metrics = new DisplayMetrics();
        mContext.getWindowManager().getDefaultDisplay()
                .getMetrics(metrics);
        mWidth = metrics.widthPixels;
        mHeight = metrics.heightPixels;

        setWidth(mWidth);
        setHeight(mHeight);
    }

    private Bitmap blur() {
        if (null != overlay) {
            return overlay;
        }
        long startMs = System.currentTimeMillis();

        View view = mContext.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        mBitmap = view.getDrawingCache();

        float scaleFactor = 8;//图片锟斤拷锟脚憋拷锟斤拷
        float radius = 10;//模锟斤拷潭锟�
        int width = mBitmap.getWidth();
        int height =  mBitmap.getHeight();

        overlay = Bitmap.createBitmap((int) (width / scaleFactor),(int) (height / scaleFactor),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(mBitmap, 0, 0, paint);

        overlay = FastBlur.doBlur(overlay, (int) radius, true);
        Log.i(TAG, "blur time is:"+(System.currentTimeMillis() - startMs));
        return overlay;
    }


    public void showMoreWindow(View anchor,int bottomMargin) {
        final RelativeLayout layout = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.layout_home_pop_more_window, null);
        setContentView(layout);

        ImageView close= (ImageView)layout.findViewById(R.id.center_music_window_close);
        RelativeLayout.LayoutParams params =new RelativeLayout.LayoutParams(60, 60);
        params.bottomMargin = bottomMargin;
        params.addRule(RelativeLayout.BELOW, R.id.more_window_online);
        params.addRule(RelativeLayout.RIGHT_OF, R.id.more_window_local);
        params.topMargin = 200;
        params.leftMargin = 60;

        close.setLayoutParams(params);

        close.setOnClickListener(v -> {
            if (isShowing()) {
                closeAnimation(layout);
            }
        });

        showAnimation(layout);
        setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), blur()));
        setOutsideTouchable(true);
        setFocusable(true);
        showAtLocation(anchor, Gravity.BOTTOM, 0, statusBarHeight);
    }

    private void showAnimation(ViewGroup layout){
        for(int i=0;i<layout.getChildCount();i++){
            final View child = layout.getChildAt(i);
            if(child.getId() == R.id.center_music_window_close){
                continue;
            }
            child.setOnClickListener(listener);//已修改
            child.setVisibility(View.INVISIBLE);
            mHandler.postDelayed(() -> {
                child.setVisibility(View.VISIBLE);
                ValueAnimator fadeAnim = ObjectAnimator.ofFloat(child, "translationY", 600, 0);
                fadeAnim.setDuration(300);
                KickBackAnimator kickAnimator = new KickBackAnimator();
                kickAnimator.setDuration(150);
                fadeAnim.setEvaluator(kickAnimator);
                fadeAnim.start();
            }, i * 50);
        }

    }

    private void closeAnimation(ViewGroup layout){
        for(int i=0;i<layout.getChildCount();i++){
            final View child = layout.getChildAt(i);
            if(child.getId() == R.id.center_music_window_close){
                continue;
            }
            child.setOnClickListener(listener);
            mHandler.postDelayed(() -> {
                child.setVisibility(View.VISIBLE);
                ValueAnimator fadeAnim = ObjectAnimator.ofFloat(child, "translationY", 0, 600);
                fadeAnim.setDuration(200);
                KickBackAnimator kickAnimator = new KickBackAnimator();
                kickAnimator.setDuration(100);
                fadeAnim.setEvaluator(kickAnimator);
                fadeAnim.start();
                fadeAnim.addListener(new Animator.AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        child.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        // TODO Auto-generated method stub

                    }
                });
            }, (layout.getChildCount()-i-1) * 30);

            if(child.getId() == R.id.more_window_local){
                mHandler.postDelayed(() -> dismiss(), (layout.getChildCount()-i) * 30 + 80);
            }
        }

    }

//    @Override
//    public void onClick(View v) {//底部菜单点击事件
//        switch (v.getId()) {
//            case R.id.more_window_local:
//                break;
//            case R.id.more_window_online:
//                break;
//            case R.id.more_window_delete:
//                break;
//
//            default:
//                break;
//        }
//    }

    public void destroy() {
        if (null != overlay) {
            overlay.recycle();
            overlay = null;
            System.gc();
        }
        if (null != mBitmap) {
            mBitmap.recycle();
            mBitmap = null;
            System.gc();
        }
    }
}
