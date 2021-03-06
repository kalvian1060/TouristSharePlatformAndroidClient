package zjut.salu.share.animation;


import android.animation.TypeEvaluator;

/**底部弹窗回弹动画
 * Created by Salu on 2016/12/6.
 */

public class KickBackAnimator implements TypeEvaluator<Float> {
    private final float s = 1.70158f;
    float mDuration = 0f;

    public void setDuration(float duration) {
        mDuration = duration;
    }

    public Float evaluate(float fraction, Float startValue, Float endValue) {
        float t = mDuration * fraction;
        float b = startValue.floatValue();
        float c = endValue.floatValue() - startValue.floatValue();
        float d = mDuration;
        float result = calculate(t, b, c, d);
        return result;
    }

    public Float calculate(float t, float b, float c, float d) {
        return c * ((t = t / d - 1) * t * ((s + 1) * t + s) + 1) + b;
    }
}
