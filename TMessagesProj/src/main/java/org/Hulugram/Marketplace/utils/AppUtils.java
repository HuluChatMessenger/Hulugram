/*

 This is the source code of exteraGram for Android.

 We do not and cannot prevent the use of our code,
 but be respectful and credit the original author.

 Copyright @immat0x1, 2023

*/

package plus.utils;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.NotificationCenter.didUserBecomeOnline;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Base64;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;

import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CombinedDrawable;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.Random;

import plus.database.DataStorage;
import plus.database.TableModels;
import plus.notifications.NotificationDataManager;

public class AppUtils {


    public static int getNotificationIconColor() {
        return BuildVars.isBetaApp() ? 0xff747f9f : 0xfff54142;
    }

    public static int[] getDrawerIconPack() {
        switch (org.telegram.ui.ActionBar.Theme.getEventType()) {
            case 0:
                return new int[]{
                        R.drawable.msg_groups_ny,
                        R.drawable.msg_secret_ny,
                        R.drawable.msg_channel_ny,
                        R.drawable.msg_contacts_ny,
                        R.drawable.msg_calls_ny,
                        R.drawable.msg_saved_ny,
                        R.drawable.msg_nearby_ny
                };
            case 1:
                return new int[]{
                        R.drawable.msg_groups_14,
                        R.drawable.msg_secret_14,
                        R.drawable.msg_channel_14,
                        R.drawable.msg_contacts_14,
                        R.drawable.msg_calls_14,
                        R.drawable.msg_saved_14,
                        R.drawable.msg_nearby_14
                };
            case 2:
                return new int[]{
                        R.drawable.msg_groups_hw,
                        R.drawable.msg_secret_hw,
                        R.drawable.msg_channel_hw,
                        R.drawable.msg_contacts_hw,
                        R.drawable.msg_calls_hw,
                        R.drawable.msg_saved_hw,
                        R.drawable.msg_nearby_hw
                };
            default:
                return new int[]{
                        R.drawable.msg_groups,
                        R.drawable.msg_secret,
                        R.drawable.msg_channel,
                        R.drawable.msg_contacts,
                        R.drawable.msg_calls,
                        R.drawable.msg_saved,
                        R.drawable.msg_nearby
                };
        }
    }

    public static boolean isWinter() {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        return currentMonth == Calendar.DECEMBER || currentMonth == Calendar.JANUARY || currentMonth == Calendar.FEBRUARY;
    }

    // do not change or remove this part of the code if you're making public fork
    private static final String EXPECTED_SIGNATURE = "tcaLgrODWBN9GQvrHPfGzA==";
    private static final String EXPECTED_PACKAGE_NAME = "plus.ride.huluchat";

    public static boolean isAppModified() {
        try {
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo packageInfo = ApplicationLoader.applicationContext.getPackageManager()
                    .getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), PackageManager.GET_SIGNATURES);

            String currentPackageName = packageInfo.packageName;

            Signature signature = packageInfo.signatures[0];

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] signatureBytes = signature.toByteArray();
            byte[] md5Bytes = md.digest(signatureBytes);
            String currentSignature = Base64.encodeToString(md5Bytes, Base64.DEFAULT).trim();

            return !EXPECTED_PACKAGE_NAME.equals(currentPackageName)
                    || !EXPECTED_SIGNATURE.equals(currentSignature);
        } catch (Exception e) {
            FileLog.e(e);
        }
        return true;
    }
    public static int getTransparentColor(int color, float opacity){
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        // Set alpha based on your logic, here I'm making it 25% of it's initial value.
        alpha *= opacity;
        return Color.argb(alpha, red, green, blue);
    }

    public static String capitalize(String name){
        String capitalizeString = "";
        if(!name.trim().equals("")){
            capitalizeString = name.substring(0,1).toUpperCase() + name.substring(1);
        }
        return capitalizeString;
    }

    public static Spanned fromHtml(@NonNull String source) {
        return HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_LEGACY);
    }

    public static void selectionSort(ArrayList<CharSequence> x, ArrayList<String> y) {
        for (int i = 0; i < x.size() - 1; i++) {
            for (int j = i + 1; j < x.size(); j++) {
                if (x.get(i).toString().compareTo(x.get(j).toString()) > 0) {
                    CharSequence temp = x.get(i);
                    x.set(i, x.get(j));
                    x.set(j, temp);

                    String tempStr = y.get(i);
                    y.set(i, y.get(j));
                    y.set(j, tempStr);
                }
            }
        }
    }


    public static SharedConfig.ProxyInfo parseProxy(String proxy) {
        if (proxy == null) {
            return null;
        }
        try {
            Uri data = Uri.parse(proxy);
            if (data != null) {
                String user = null;
                String password = null;
                String port = null;
                String address = null;
                String secret = null;
                String scheme = data.getScheme();
                if (scheme != null) {
                    if ((scheme.equals("http") || scheme.equals("https"))) {
                        String host = data.getHost().toLowerCase();
                        if (host.equals("telegram.me") || host.equals("t.me") || host.equals("telegram.dog")) {
                            String path = data.getPath();
                            if (path != null) {
                                if (path.startsWith("/socks") || path.startsWith("/proxy")) {
                                    address = data.getQueryParameter("server");
                                    port = data.getQueryParameter("port");
                                    user = data.getQueryParameter("user");
                                    password = data.getQueryParameter("pass");
                                    secret = data.getQueryParameter("secret");
                                }
                            }
                        }
                    } else if (scheme.equals("tg")) {
                        String url = data.toString();
                        if (url.startsWith("tg:proxy") || url.startsWith("tg://proxy") || url.startsWith("tg:socks") || url.startsWith("tg://socks")) {
                            url = url.replace("tg:proxy", "tg://telegram.org").replace("tg://proxy", "tg://telegram.org").replace("tg://socks", "tg://telegram.org").replace("tg:socks", "tg://telegram.org");
                            data = Uri.parse(url);
                            address = data.getQueryParameter("server");
                            port = data.getQueryParameter("port");
                            user = data.getQueryParameter("user");
                            password = data.getQueryParameter("pass");
                            secret = data.getQueryParameter("secret");
                        }
                    }
                }
                if (!TextUtils.isEmpty(address) && !TextUtils.isEmpty(port)) {
                    if (user == null) {
                        user = "";
                    }
                    if (password == null) {
                        password = "";
                    }
                    if (secret == null) {
                        secret = "";
                    }
                    return new SharedConfig.ProxyInfo(address,Integer.parseInt(port),user,password,secret,true);
                }
            }
        } catch (Exception ignore) {

        }
        return null;
    }


    public static boolean isEmpty(Object object){
        if(object instanceof String){
            String str = (String) object;
            return str.length() == 0;
        }
        return true;
    }

    public static ImageView createImageButtonForLocations(Context context, int res, int size, View.OnClickListener onClickListener){
        ImageView imageView  = new ImageView(context);
        Drawable drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(size), Theme.getColor(Theme.key_location_actionBackground), Theme.getColor(Theme.key_location_actionPressedBackground));
        if (Build.VERSION.SDK_INT < 21) {
            Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow_profile).mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.dp(size), AndroidUtilities.dp(size));
            drawable = combinedDrawable;
        } else {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(imageView, View.TRANSLATION_Z, AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(imageView, View.TRANSLATION_Z, AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            imageView.setStateListAnimator(animator);
            imageView.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(size), AndroidUtilities.dp(size));
                }
            });
        }

        imageView.setBackgroundDrawable(drawable);
        imageView.setImageResource(res);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_location_actionActiveIcon), PorterDuff.Mode.MULTIPLY));
        imageView.setTag(Theme.key_location_actionActiveIcon);
        imageView.setOnClickListener(onClickListener);
        return imageView;
    }

    public static ImageView createImageButtonForLocations(Context context, int res, View.OnClickListener onClickListener){
        ImageView imageView  = new ImageView(context);
        Drawable drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(40), Theme.getColor(Theme.key_location_actionBackground), Theme.getColor(Theme.key_location_actionPressedBackground));
        if (Build.VERSION.SDK_INT < 21) {
            Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow_profile).mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.dp(40), AndroidUtilities.dp(40));
            drawable = combinedDrawable;
        } else {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(imageView, View.TRANSLATION_Z, AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(imageView, View.TRANSLATION_Z, AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            imageView.setStateListAnimator(animator);
            imageView.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(40), AndroidUtilities.dp(40));
                }
            });
        }

        imageView.setBackgroundDrawable(drawable);
        imageView.setImageResource(res);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_location_actionActiveIcon), PorterDuff.Mode.MULTIPLY));
        imageView.setTag(Theme.key_location_actionActiveIcon);
        imageView.setOnClickListener(onClickListener);
        return imageView;
    }

    public  static void sendMessage(SendMessagesHelper sendMessagesHelper,String message, long dialog_id){
        sendMessagesHelper.sendMessage(SendMessagesHelper.SendMessageParams.of(message, dialog_id, null, null, null, false, null, null, null, false, 0, null, false));

    }

    public static  void checkForUpdate(int account, TLRPC.User user, TLRPC.Update baseUpdate,int updateTime) {
        if(baseUpdate instanceof TLRPC.TL_updateUser){
            TLRPC.TL_updateUser update = (TLRPC.TL_updateUser) baseUpdate;
            if(user != null && user.photo != null){
                TableModels.Feed feed = new TableModels.Feed();
                feed.date =  updateTime > 0?updateTime:(int)(System.currentTimeMillis()/1000);
                feed.user_id = update.user_id;
                DataStorage.getInstance(account).getStorageQueue().postRunnable(() -> DataStorage.getInstance(account).getDatabase().feedDao().insert(feed));
            }

        }
    }




    public static CharSequence miniAppBtnSpan(Context context) {
        SpannableString cameraStr = new SpannableString("c");
        Drawable cameraDrawable = context.getResources().getDrawable(R.drawable.ic_shop).mutate();
        final int sz = AndroidUtilities.dp(35);
        cameraDrawable.setBounds(-sz / 4, -sz, sz / 4 * 3, 0);
        cameraStr.setSpan(new ImageSpan(cameraDrawable) {
            @Override
            public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
                return super.getSize(paint, text, start, end, fm) / 3 * 2;
            }

            @Override
            public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
                canvas.save();
                canvas.translate(0, (bottom - top) / 2 + dp(1));
                cameraDrawable.setAlpha(paint.getAlpha());
                super.draw(canvas, text, start, end, x, top, y, bottom, paint);
                canvas.restore();
            }
        }, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return cameraStr;
    }

    public static String getRandomPackageName(){
        Random random = new Random();
        String characters = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder package_name = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            package_name.append(characters.charAt(random.nextInt(characters.length())));
        }
        return package_name.toString();
    }



}
