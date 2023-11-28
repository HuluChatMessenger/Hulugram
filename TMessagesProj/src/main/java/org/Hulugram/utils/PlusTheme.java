package plus.utils;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextPaint;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.RLottieDrawable;


public class PlusTheme {

    //plus
    public static TextPaint walletTitlePaint;
    public static TextPaint walletStatusPaint;
    public static TextPaint walletAmountPaint;
    public static TextPaint walletDataPaint;
    //plus

    public static RLottieDrawable chat_walletButtonDrawable;

    public static TextPaint productTitlePaint;
    public static TextPaint productPricePaint;
    public static TextPaint productShopTitlePaint;
    public static TextPaint productDatePaint;
    private static   Paint backgroundPaint;
    public static TextPaint whitePaint;
    public static Paint redPaint;

    private static boolean dialogDrawableLoaded;
    private static boolean chatDrawableLoaded;

    public static Drawable shopDrawable;
    public static Drawable[] walletAvatarDrawables = new Drawable[7];

    public static void applyCommonTheme(){
        productTitlePaint.setColor(Theme.getColor(Theme.key_dialogTextBlack));
        productPricePaint.setColor(Theme.getColor(Theme.key_dialogTextGray));
        productShopTitlePaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
        productDatePaint.setColor(Theme.getColor(Theme.key_avatar_text));
    }


    public static void setDrawableColorByKey(Drawable drawable, int key) {
        if (key == -1 || drawable == null) {
            return;
        }
        drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(key), PorterDuff.Mode.MULTIPLY));

    }

    public static void loadDialogsDrawables(){
        if(!dialogDrawableLoaded){
            Resources resources = ApplicationLoader.applicationContext.getResources();

            shopDrawable =resources.getDrawable(R.drawable.ic_shop);

            productTitlePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            productTitlePaint.setTypeface(HulugramUtils.getRoboBoldTypeface());
            productTitlePaint.setTextSize(AndroidUtilities.dp(10));
            productTitlePaint.setColor(Theme.getColor(Theme.key_chats_name));

            productPricePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
//            productPricePaint.setTypeface(HulugramUtils.getRobotoMediumTypeface());
            productPricePaint.setTextSize(AndroidUtilities.dp(12));
            productPricePaint.setColor(Theme.getColor(Theme.key_chats_name));

            productShopTitlePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            productShopTitlePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            productShopTitlePaint.setTextSize(AndroidUtilities.dp(10));
            productShopTitlePaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));


            productDatePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            productDatePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            productDatePaint.setTextSize(AndroidUtilities.dp(10));
            productDatePaint.setColor(Theme.getColor(Theme.key_avatar_text));

            backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            backgroundPaint.setColor(Color.BLACK);
            backgroundPaint.setAlpha(66);


            whitePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            whitePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            whitePaint.setTextSize(AndroidUtilities.dp(10));
            whitePaint.setColor(0xffffffff);

            redPaint = new Paint(TextPaint.ANTI_ALIAS_FLAG);
            redPaint.setColor(Color.RED);


            dialogDrawableLoaded = true;

        }
        applyCommonTheme();
    }


    private static boolean rideDrawableLoaded;


    public static void loadRideDrawables(){
        if(!rideDrawableLoaded){

            rideDrawableLoaded = true;

        }
    }


}
