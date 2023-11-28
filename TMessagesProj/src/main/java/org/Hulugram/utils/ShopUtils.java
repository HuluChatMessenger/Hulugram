//package plus.utils;
//
//import static org.telegram.ui.ActionIntroActivity.CAMERA_PERMISSION_REQUEST_CODE;
//
//import android.Manifest;
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.content.ActivityNotFoundException;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.content.res.ColorStateList;
//import android.graphics.Bitmap;
//import android.graphics.Color;
//import android.graphics.Rect;
//import android.graphics.RectF;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
//import android.graphics.drawable.GradientDrawable;
//import android.graphics.drawable.RippleDrawable;
//import android.graphics.drawable.ShapeDrawable;
//import android.graphics.drawable.StateListDrawable;
//import android.graphics.drawable.shapes.RoundRectShape;
//import android.location.LocationManager;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Vibrator;
//import android.provider.Settings;
//import android.text.SpannableStringBuilder;
//import android.text.Spanned;
//import android.text.TextUtils;
//import android.text.style.ForegroundColorSpan;
//import android.util.Log;
//import android.util.StateSet;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.view.View;
//import android.widget.EdgeEffect;
//import android.widget.FrameLayout;
//import android.widget.HorizontalScrollView;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.core.content.FileProvider;
//import androidx.core.widget.NestedScrollView;
//import androidx.palette.graphics.Palette;
//
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.EncodeHintType;
//import com.google.zxing.qrcode.QRCodeWriter;
//import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
//
//
//import org.telegram.messenger.AccountInstance;
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.ApplicationLoader;
//import org.telegram.messenger.BuildConfig;
//import org.telegram.messenger.BuildVars;
//import org.telegram.messenger.FileLog;
//import org.telegram.messenger.LocaleController;
//import org.telegram.messenger.MediaController;
//import org.telegram.messenger.MediaDataController;
//import org.telegram.messenger.MessagesController;
//import org.telegram.messenger.R;
//import org.telegram.messenger.SendMessagesHelper;
//import org.telegram.messenger.SharedConfig;
//import org.telegram.messenger.UserConfig;
//import org.telegram.messenger.Utilities;
//import org.telegram.tgnet.ConnectionsManager;
//import org.telegram.tgnet.TLRPC;
//import org.telegram.ui.ActionBar.AlertDialog;
//import org.telegram.ui.ActionBar.BaseFragment;
//import org.telegram.ui.ActionBar.BottomSheet;
//import org.telegram.ui.ActionBar.Theme;
//import org.telegram.ui.BasePermissionsActivity;
//import org.telegram.ui.CameraScanActivity;
//import org.telegram.ui.ChatActivity;
//import org.telegram.ui.Components.AlertsCreator;
//import org.telegram.ui.Components.BulletSpan;
//import org.telegram.ui.Components.BulletinFactory;
//import org.telegram.ui.Components.CombinedDrawable;
//import org.telegram.ui.Components.EmptyTextProgressView;
//import org.telegram.ui.Components.LayoutHelper;
//import org.telegram.ui.Components.StickerEmptyView;
//import org.telegram.ui.Components.TypefaceSpan;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.lang.reflect.Field;
//import java.text.DecimalFormat;
//import java.text.SimpleDateFormat;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Objects;
//import java.util.TimeZone;
//
//public class ShopUtils {
//    public static String formatFullDate(String date_str){
//
//        try{
//            String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
//            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//            Date serverDate = simpleDateFormat.parse(date_str);
//            return  LocaleController.getInstance().formatterStats.format(serverDate.toInstant().toEpochMilli());
//        }catch (Exception ignore){
//
//        }
//        return "";
//
//    }
//
//    public static void prepareViewForScaleAnimation(View view, float startScale){
//        if(view == null){
//            return;
//        }
//        view.setVisibility(View.GONE);
//        view.setScaleY(startScale);
//        view.setScaleX(startScale);
//    }
//
//    public static void openProductDetailUi(BaseFragment parentFragment,long chat_id,int product_id){
//        if(BuildConfig.DEBUG && false){
//            Bundle args = new Bundle();
//            args.putLong("chat_id", ShopUtils.toClientChannelId(chat_id));
//            args.putInt("item_id", product_id);
//            ProductDetailActivity productDetailFragment = new ProductDetailActivity(args);
//            parentFragment.showAsSheet(productDetailFragment,true,true,false);
//        }else{
//            Bundle args = new Bundle();
//            args.putLong("chat_id", ShopUtils.toClientChannelId(chat_id));
//            args.putInt("item_id", product_id);
//            ProductDetailActivity productDetailFragment = new ProductDetailActivity(args);
//            parentFragment.presentFragment(productDetailFragment);
//        }
//    }
//
//
//    public static CombinedDrawable createDetailCircleDrawable(int res){
//        CombinedDrawable menuButton = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(32), res);
//        menuButton.setIconSize(AndroidUtilities.dp(16), AndroidUtilities.dp(16));
//        Theme.setCombinedDrawableColor(menuButton, Theme.getColor(Theme.key_windowBackgroundGray), false);
//        Theme.setCombinedDrawableColor(menuButton, Theme.getColor(Theme.key_dialogTextBlack), true);
//        return  menuButton;
//    }
//
//
//    public static String formatMessageOpenLink(long user_id,long message_id){
//        String link = "tg://openmessage?user_id={uid}&message_id={mid}";
//        return link.replace("{uid}",user_id + "").replace("{mid}",message_id + "");
//    }
//
//    public static void setScrollViewEdgeEffectColor(NestedScrollView scrollView, int color) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            return;
//        }
//        try {
//            Field field = NestedScrollView.class.getDeclaredField("mEdgeGlowTop");
//            field.setAccessible(true);
//            EdgeEffect mEdgeGlowTop = (EdgeEffect) field.get(scrollView);
//            if (mEdgeGlowTop != null) {
//                mEdgeGlowTop.setColor(color);
//            }
//
//            field = HorizontalScrollView.class.getDeclaredField("mEdgeGlowBottom");
//            field.setAccessible(true);
//            EdgeEffect mEdgeGlowBottom = (EdgeEffect) field.get(scrollView);
//            if (mEdgeGlowBottom != null) {
//                mEdgeGlowBottom.setColor(color);
//            }
//        } catch (Exception e) {
//            FileLog.e(e);
//        }
//    }
//
//
//    public static class userData{
//        public int user_id;
//        public String first_name;
//        public String phoneNumber;
//        public String username;
//
//
//        @Override
//        public String toString() {
//            return "userData{" +
//                    "user_id=" + user_id +
//                    ", first_name='" + first_name + '\'' +
//                    ", phoneNumber='" + phoneNumber + '\'' +
//                    ", username='" + username + '\'' +
//                    '}';
//        }
//    }
//
//
//    public static String capitalize(String string){
//        if(string == null || string.length() <=0){
//            return string;
//        }
//       return string.substring(0,1).toUpperCase() + string.substring(1).toLowerCase();
//
//    }
//
//
//
//    public static final String ui_picture = "ui_picture";
//    public static final String ui_header_body = "ui_header_body";
//    public static final String ui_table = "ui_table";
//    public static final String ui_location = "ui_location";
//    public static final String ui_input_price = "ui_input_price";
//    public static final String ui_input_title = "ui_input_title";
//    public static final String ui_input_str = "ui_input_str";
//    public static final String ui_input_stock = "ui_input_stock";
//    public static final String ui_input_old_price = "ui_input_old_price";
//    public static final String ui_input_deliverable = "ui_input_deliverable";
//
//    public static CombinedDrawable createDetailMenuDrawableFinal(int res){
//        CombinedDrawable menuButton = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(32), res);
//        menuButton.setIconSize(AndroidUtilities.dp(16), AndroidUtilities.dp(16));
//        Theme.setCombinedDrawableColor(menuButton, Theme.getColor(Theme.key_actionBarDefault), false);
//        Theme.setCombinedDrawableColor(menuButton, Theme.getColor(Theme.key_dialogTextBlack), true);
//
//        return  menuButton;
//    }
//
//    public static int NETWORK_EXCEPTION = 1;
//
//    public static void rateApp(Activity activity)
//    {
//        if(activity == null){
//            return;
//        }
//        try
//        {
//            Intent rateIntent = rateIntentForUrl("market://details",activity);
//            activity.startActivity(rateIntent);
//        }
//        catch (ActivityNotFoundException e)
//        {
//            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details",activity);
//            activity.startActivity(rateIntent);
//        }
//    }
//
//    private static Intent rateIntentForUrl(String url,Activity activity)
//    {
//        String packageName = activity.getPackageName();
//        packageName = packageName.replace(".beta","");
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, packageName)));
//        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
//        if (Build.VERSION.SDK_INT >= 21)
//        {
//            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
//        }
//        else
//        {
//            //noinspection deprecation
//            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
//        }
//        intent.addFlags(flags);
//        return intent;
//    }
//
//    @SuppressLint("SetTextI18n")
//    public static void showRatingAlert2(BaseFragment baseFragment){
//        if(baseFragment == null || baseFragment.getParentActivity() == null){
//            return;
//        }
//        Context context = baseFragment.getParentActivity();
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        Runnable runnable1 = builder.getDismissRunnable();
//        FrameLayout frameLayout = new FrameLayout(context){
//            @Override
//            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//                super.onMeasure(widthMeasureSpec, MeasureSpec.getSize(AndroidUtilities.dp(240 + 80)));
//            }
//        };
//        builder.setView(frameLayout);
//
//        LinearLayout linearLayout = new LinearLayout(context);
//        linearLayout.setOrientation(LinearLayout.VERTICAL);
//        frameLayout.addView(linearLayout,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT));
//
//
//        AnimatedInfoView animatedInfoView = new AnimatedInfoView(context,null,AnimatedInfoView.STICKER_TYPE_STAR);
//        animatedInfoView.title.setText(LocaleController.getString("RateUs",R.string.RateUs));
//        animatedInfoView.subtitle.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
//        animatedInfoView.subtitle.setText(LocaleController.getString("RateAppDesc",R.string.RateAppDesc));
//        linearLayout.addView(animatedInfoView,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.CENTER_HORIZONTAL));
//
//        TextView  nameTextView = new TextView(context);
//        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
//        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//        nameTextView.setLines(1);
//        nameTextView.setMaxLines(1);
//        nameTextView.setSingleLine(true);
//        nameTextView.setText("⭐⭐⭐⭐⭐");
//        nameTextView.setGravity(Gravity.CENTER);
//        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
//        linearLayout.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 16));
//
//        TextView textView = new TextView(context);
//        textView.setTextColor(Theme.getColor(Theme.key_passport_authorizeText));
//        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
//        textView.setGravity(Gravity.CENTER);
//        textView.setMaxLines(1);
//        textView.setText("Rate & Send Feedback");
//        textView.setSingleLine();
//        textView.setBackgroundDrawable(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(32),Theme.getColor(Theme.key_passport_authorizeBackground), Theme.getColor(Theme.key_passport_authorizeBackgroundSelected)));
//        textView.setPadding(AndroidUtilities.dp(0),AndroidUtilities.dp(16),AndroidUtilities.dp(0),AndroidUtilities.dp(16));
//        textView.setEllipsize(TextUtils.TruncateAt.END);
//        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER,8,8,8,8));
//        textView.setOnClickListener(v -> {
//            rateApp(baseFragment.getParentActivity());
//            runnable1.run();
//            SharedConfig.askToRateCount = 3;
//            SharedConfig.saveConfig();
//        });
//
//        SharedConfig.askToRateCount++;
//        SharedConfig.saveConfig();
//
//        AlertDialog alertDialog = builder.create();
//        baseFragment.showDialog(alertDialog);
//    }
//
//    public static void showChannelCreateAlert(BaseFragment baseFragment,String title,String[] desc,String button,int sticker,Runnable runnable){
//        if(baseFragment == null || baseFragment.getParentActivity() == null){
//            return;
//        }
//        Context context = baseFragment.getParentActivity();
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        Runnable runnable1 = builder.getDismissRunnable();
//        FrameLayout frameLayout = new FrameLayout(context){
//            @Override
//            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//                super.onMeasure(widthMeasureSpec, MeasureSpec.getSize(AndroidUtilities.dp(240 + 80)));
//            }
//        };
//        builder.setView(frameLayout);
//
//        LinearLayout linearLayout = new LinearLayout(context);
//        linearLayout.setOrientation(LinearLayout.VERTICAL);
//        //linearLayout.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(8),Theme.getColor(Theme.key_dialogBackground)));
//        frameLayout.addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT, Gravity.TOP|Gravity.CENTER_HORIZONTAL,8,8,8,8));
//
//        StickerEmptyView animatedEmptyView = new StickerEmptyView(context,null,StickerEmptyView.STICKER_TYPE_NO_CONTACTS);
//        animatedEmptyView.title.setText(title);
//
//        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
//        for (String s : desc) {
//            int start = spannableStringBuilder.length();
//            int end = s.length() + start;
//            spannableStringBuilder.append(s).append("\n\n");
//            final BulletSpan span = new BulletSpan(AndroidUtilities.dp(10f), 0xff16a842, AndroidUtilities.dp(4f));
//            spannableStringBuilder.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        }
//
//        animatedEmptyView.subtitle.setGravity(Gravity.LEFT);
//        animatedEmptyView.subtitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
//        animatedEmptyView.subtitle.setText(spannableStringBuilder);
//        linearLayout.addView(animatedEmptyView,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,8,0,8,8));
//
//        TextView textView = new TextView(context);
//        textView.setTextColor(Theme.getColor(Theme.key_passport_authorizeText));
//        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
//        textView.setGravity(Gravity.CENTER);
//        textView.setMaxLines(1);
//        textView.setText(button);
//        textView.setSingleLine();
//        textView.setBackgroundDrawable(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(16),Theme.getColor(Theme.key_passport_authorizeBackground), Theme.getColor(Theme.key_passport_authorizeBackgroundSelected)));
//        textView.setPadding(AndroidUtilities.dp(0),AndroidUtilities.dp(16),AndroidUtilities.dp(0),AndroidUtilities.dp(16));
//        textView.setEllipsize(TextUtils.TruncateAt.END);
//        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER,8,8,8,8));
//        textView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(runnable != null){
//                    runnable.run();
//                    runnable1.run();
//                }
//            }
//        });
//
//        AlertDialog alertDialog = builder.create();
//        baseFragment.showDialog(alertDialog);
//    }
//
//    public static void showAnimatedInfo(BaseFragment baseFragment,String title,String desc,String button,boolean success,Runnable runnable){
//        if(baseFragment == null || baseFragment.getParentActivity() == null){
//            return;
//        }
//        Context context = baseFragment.getParentActivity();
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        Runnable runnable1 = builder.getDismissRunnable();
//        FrameLayout frameLayout = new FrameLayout(context){
//            @Override
//            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//                super.onMeasure(widthMeasureSpec, MeasureSpec.getSize(AndroidUtilities.dp(240 + 80)));
//            }
//        };
//        builder.setView(frameLayout);
//
//        LinearLayout linearLayout = new LinearLayout(context);
//        linearLayout.setOrientation(LinearLayout.VERTICAL);
//        linearLayout.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(8),Theme.getColor(Theme.key_dialogBackground)));
//        frameLayout.addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT, Gravity.TOP|Gravity.CENTER_HORIZONTAL,8,8,8,8));
//
//        int stickerType = success?AnimatedInfoView.STICKER_TYPE_LOVE:AnimatedInfoView.STICKER_TYPE_NOT_FOUND;
//        AnimatedInfoView animatedEmptyView = new AnimatedInfoView(context,null,stickerType);
//        animatedEmptyView.title.setText(title);
//
//        SpannableStringBuilder spannableStringBuilder = AndroidUtilities.replaceTags(desc);
//
//        animatedEmptyView.subtitle.setText(spannableStringBuilder);
//        linearLayout.addView(animatedEmptyView,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,8,0,8,8));
//
//        TextView textView = new TextView(context);
//        textView.setTextColor(Theme.getColor(Theme.key_passport_authorizeText));
//        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
//        textView.setGravity(Gravity.CENTER);
//        textView.setMaxLines(1);
//        textView.setText(button);
//        textView.setSingleLine();
//        textView.setBackgroundDrawable(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(16),Theme.getColor(Theme.key_passport_authorizeBackground), Theme.getColor(Theme.key_passport_authorizeBackgroundSelected)));
//        textView.setPadding(AndroidUtilities.dp(0),AndroidUtilities.dp(16),AndroidUtilities.dp(0),AndroidUtilities.dp(16));
//        textView.setEllipsize(TextUtils.TruncateAt.END);
//        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER,8,8,8,8));
//        textView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(runnable != null){
//                    runnable.run();
//                }
//                runnable1.run();
//            }
//        });
//
//        AlertDialog alertDialog = builder.create();
//        baseFragment.showDialog(alertDialog);
//    }
//
//
//
//
////    public  static  void openMessage(String message_link,BaseFragment prentFrgemnt) {
////        if (message_link == null || TextUtils.isEmpty(message_link)) {
////            return;
////        }
////        Browser.openUrl(prentFrgemnt.getParentActivity(),message_link);
////        String url = message_link;
////        Uri data = Uri.parse(url);
////        Log.i("openOfferMessage","openOfferMessage caled => " + message_link);
////        if(data.getScheme().startsWith("http")){
////            String host = data.getHost().toLowerCase();
////            Integer messageId = null;
////            Integer channelId = null;
////            if (host.equals("telegram.me") || host.equals("t.me") || host.equals("telegram.dog")) {
////                String path = data.getPath();
////                 if (path.startsWith("c/")) {
////                        List<String> segments = data.getPathSegments();
////                        if (segments.size() == 3) {
////                            channelId = Utilities.parseInt(segments.get(1));
////                            messageId = Utilities.parseInt(segments.get(2));
////                            if (messageId == 0 || channelId == 0) {
////                                messageId = null;
////                                channelId = null;
////                            }
////                        }
////
////                       if(channelId != null && messageId != null){
////                           Bundle args = new Bundle();
////                           args.putInt("chat_id", channelId);
////                           if (messageId != 0) {
////                               args.putInt("message_id", messageId);
////                           }
////                           ChatActivity fragment = new ChatActivity(args);
////                           prentFrgemnt.presentFragment(fragment);
////
////                       }
////                    }
////
////
////            }
////
////
////        }else if (url.startsWith("tg:openmessage") || url.startsWith("tg://openmessage")) {
////            url = url.replace("tg:openmessage", "tg://telegram.org").replace("tg://openmessage", "tg://telegram.org");
////            data = Uri.parse(url);
////
////            int push_user_id = 0;
////            int push_msg_id = 0;
////
////            String userID = data.getQueryParameter("user_id");
////            String msgID = data.getQueryParameter("message_id");
////            if (userID != null) {
////                try {
////                    push_user_id = Integer.parseInt(userID);
////                } catch (NumberFormatException ignore) {
////                }
////            }
////
////            if (msgID != null) {
////                try {
////                    push_msg_id = Integer.parseInt(msgID);
////                } catch (NumberFormatException ignore) {
////                }
////            }
////
////            Bundle args = new Bundle();
////            args.putInt("user_id", push_user_id);
////            if (push_msg_id != 0) {
////                args.putInt("message_id", push_msg_id);
////            }
////            ChatActivity fragment = new ChatActivity(args);
////            prentFrgemnt.presentFragment(fragment);
////
////        }
////
////    }
//
//
//    static String ignore[] = {"is_visible","title","price","pictures","instock","is_deliverable"};
//    private static boolean ignoreField(String key){
//        boolean has = false;
//        for (String s : ignore) {
//            if ( ShopUtils.isEmpty(s) || key.equals(s)) {
//                has = true;
//                break;
//            }
//        }
//        return has;
//    }
//
//
//    public static void sendProductCoChannel(AccountInstance accountInstance, long dialog_id, ShopDataSerializer.Product product, Map<String,Object> productTags , ArrayList<ProductImageLayout.ImageInput> imageInputs, ShopDataSerializer.Shop shop){
//           if(product == null || shop == null || productTags == null || imageInputs == null){
//               return;
//           }
//
//           HashMap<Object, Object> selectedPhotos = new HashMap<>();
//           ArrayList<Object> selectedPhotosOrder = new ArrayList<>();
//          for(int a = 0; a < imageInputs.size(); a++){
//           ProductImageLayout.ImageInput imageInput = imageInputs.get(a);
//           if(imageInput.bigSize == null || imageInput.smallSize == null || imageInput.fileLocation == null){
//               continue;
//           }
//           MediaController.PhotoEntry photoEntry = new MediaController.PhotoEntry(0,imageInput.pos, 0, imageInput.fileLocation, 0, false, 0, 0, 0);
//           selectedPhotos.put(a,photoEntry);
//           selectedPhotosOrder.add(imageInput.pos);
//           if(!PlusConfig.sendAllPhotos){
//               break;
//           }
//        }
//         ArrayList<TLRPC.MessageEntity> entities = new ArrayList<>();
//         StringBuilder caption = new StringBuilder();
//         caption.append(product.title).append("\n\n");
//         caption.append(ShopUtils.formatCurrency(product.price)).append("\n\n");
//
//         if(!ShopUtils.isEmpty(product.description)){
//             caption.append(product.description).append("\n\n");
//         }
//
//
//        for (Map.Entry<String, Object> entry : productTags.entrySet()) {
//            if(ignoreField(entry.getKey())){
//                continue;
//            }
//            String key = entry.getKey();
//            key =  key.substring(0,1).toUpperCase() + key.substring(1);
//            if(key.contains("_")){
//                key = key.replace("_"," ");
//            }
//            caption.append("✅ ").append(key).append(" ").append(entry.getValue()).append("\n\n");
//        }
//
//        //detail end
//
//         if(!ShopUtils.isEmpty(shop.contact_username)  && PlusConfig.includeUserNameRow){
//             if(PlusConfig.includeEmojiRow){
//                 caption.append("\uD83D\uDC49 ");
//             }
//             caption.append("Contact us ").append("@").append(shop.contact_username).append("\n\n");
//         }
//
//         if(shop.phoneNumbers != null && shop.phoneNumbers.size() > 0  && PlusConfig.includePhoneNumberRow){
//             if(PlusConfig.includeEmojiRow){
//                 caption.append("\uD83D\uDCF1 ");
//             }
//            caption.append("Call us ").append("+").append(shop.phoneNumbers.get(0).phonenumber).append("\n\n");
//         }
//
//
//         if(PlusConfig.showProductLink){
//             if(PlusConfig.includeEmojiRow){
//                 caption.append("\uD83D\uDC47\uD83D\uDC47\uD83D\uDC47").append("\n");
//             }
//             TLRPC.TL_messageEntityTextUrl entity = new TLRPC.TL_messageEntityTextUrl();
//             entity.offset = caption.length();
//             caption.append("Open product").append("");
//             entity.length = "Open product".length();
//             entity.url = ShopUtils.getProductLink(ShopUtils.toClientChannelId(shop.channel_id),product.id);
//             entities.add(entity);
//         }
//
//
//        if (!selectedPhotos.isEmpty()) {
//               ArrayList<SendMessagesHelper.SendingMediaInfo> photos = new ArrayList<>();
//               for (int a = 0; a < selectedPhotosOrder.size(); a++) {
//                   MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) selectedPhotos.get(selectedPhotosOrder.get(a));
//                   SendMessagesHelper.SendingMediaInfo info = new SendMessagesHelper.SendingMediaInfo();
//                   if ((photoEntry != null ? photoEntry.imagePath : null) != null) {
//                       info.path = photoEntry.imagePath;
//                   } else if (photoEntry.path != null) {
//                       info.path = photoEntry.path;
//                   }
//                   info.thumbPath = photoEntry.thumbPath;
//                   info.isVideo = false;
//                   info.entities = null;
//                   info.masks = null;
//                   info.ttl = 0;
//                   info.canDeleteAfter = false;
//                   photos.add(info);
//                   photoEntry.reset();
//               }
//               photos.get(0).caption = caption.toString();
//               photos.get(0).entities = entities;
//               SendMessagesHelper.prepareSendingMedia(accountInstance, photos, dialog_id, null, null, null, false, true, null, true, 0,false);
//               MediaDataController.getInstance(accountInstance.getCurrentAccount()).cleanDraft(dialog_id,0,true);
//
//           }
//
//   }
//
//
//   public static  void vibrate(View view,Activity activity){
//        if (view != null) {
//            Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
//            if (v != null) {
//                v.vibrate(200);
//            }
//            AndroidUtilities.shakeView(view);
//        }
//    }
//
//    public static final String open_product_link = "tg://open?shop_id={shopId}&product_id={productId}";
//
//    public boolean isUserSupported(int account){
//        boolean supoported = false;
//        TLRPC.User user = UserConfig.getInstance(account).getCurrentUser();
//        if (user != null && user.phone != null && user.phone.startsWith("251")) {
//            supoported = true;
//        }
//        return supoported;
//    }
//
//    public static void scale(Rect rect, float factor){
//        float diffHorizontal = (rect.right-rect.left) * (factor-1f);
//        float diffVertical = (rect.bottom-rect.top) * (factor-1f);
//
//        rect.top -= diffVertical/2f;
//        rect.bottom += diffVertical/2f;
//
//        rect.left -= diffHorizontal/2f;
//        rect.right += diffHorizontal/2f;
//    }
//
//    public static void scale(RectF rect, float factor){
//        float diffHorizontal = (rect.right-rect.left) * (factor-1f);
//        float diffVertical = (rect.bottom-rect.top) * (factor-1f);
//
//        rect.top -= diffVertical/2f;
//        rect.bottom += diffVertical/2f;
//
//        rect.left -= diffHorizontal/2f;
//        rect.right += diffHorizontal/2f;
//    }
//
//    public static void processText(BaseFragment fragment, String link){
//        if(fragment == null || link == null || fragment.getParentActivity() == null){
//            return;
//        }
//        Uri data = Uri.parse(link);
//        String url = data.toString();
//
//        if(url.contains("https://t.me/hulugramdonation/") || url.contains("http://t.me/hulugramdonation/")){
//            String uuid = data.getQueryParameter("uuid");
//            if(!TextUtils.isEmpty(uuid)){
//                String finalLinke = "tg://transaction?type=hulufund&id=" + uuid;
//                processText(fragment,finalLinke);
//            }
//        }else if(url.contains("tg://transaction")){
//            data = Uri.parse(url);
//            String uuid =  data.getQueryParameter("id");
//            String type = data.getQueryParameter("type");
//            if(!TextUtils.isEmpty(uuid) && !TextUtils.isEmpty(type)){
//                AlertDialog alertDialog =   new AlertDialog(fragment.getParentActivity(),3);
//                alertDialog.setCanCancel(true);
//                fragment.showDialog(alertDialog);
//                final  int[] reqId ={0};
//                if(Objects.equals(type, "hulufund")){
//                    DonationObject.GetCause req = new DonationObject.GetCause();
//                    req.uuid = uuid;
//                    reqId[0] =ServicesDataController.getInstance(fragment.getCurrentAccount()).sendRequest(req, new ServicesDataController.ResponseCallback() {
//                        @Override
//                        public void onResponse(Object response, APIError apiError) {
//                            alertDialog.dismiss();
//                            if(reqId[0] != 0){
//                                reqId[0] = 0;
//                            }
//                            AndroidUtilities.runOnUIThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if(apiError == null){
//                                        DonationObject.Cause donationCause = (DonationObject.Cause)response;
//                                        fragment.presentFragment(new DonationDetailFragment(donationCause));
//                                    }else{
//                                        AlertsCreator.createSimpleAlert(fragment.getParentActivity(),"Error",apiError.message()).show();
//                                    }
//                                }
//                            });
//
//                        }
//                    });
//                }else{
//                    reqId[0] = ServicesDataController.getInstance(fragment.getCurrentAccount()).getTransactionDetail(type,uuid, (response, apiError) -> {
//                        alertDialog.dismiss();
//                        if(reqId[0] != 0){
//                            reqId[0] = 0;
//                        }
//                        if(apiError == null){
//                            TransactionAlert transactionAlert = new TransactionAlert(fragment.getParentActivity(),(ServicesModel.BaseTransaction) response);
//                            fragment.showDialog(transactionAlert);
//                        }else{
//                            AlertsCreator.createSimpleAlert(fragment.getParentActivity(),"Error",apiError.message()).show();
//                        }
//                    });
//
//                }
//                alertDialog.setOnCancelListener(dialogInterface -> {
//                    if(reqId[0] != 0){
//                        ServicesDataController.getInstance(fragment.getCurrentAccount()).cancelRequest(reqId[0]);
//                        reqId[0] = 0;
//                    }
//                });
//            }
//
//        }else if(url.startsWith("tg://open")){
//            data = Uri.parse(url);
//            if(url.contains("music") || url.contains("miniapp") || url.contains("airtime") || url.contains("marketplace") || url.contains("feed") || url.contains("nearby") || url.contains("scan")){
//
//                if(fragment instanceof ChatActivity){
//                    ChatActivity chatActivity = (ChatActivity)fragment;
//                    if(url.contains("music")){
//                        if (Build.VERSION.SDK_INT >= 23 && fragment.getParentActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                            fragment.getParentActivity().requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, BasePermissionsActivity.REQUEST_CODE_EXTERNAL_STORAGE);
//                            return;
//                        }
//                        fragment.presentFragment(new MusicFilterFragment(null,null));
//                    }else if(url.contains("miniapp")){
//                        if(AppUtilities.isSelfSupported()){
//                            chatActivity.openMiniApp();
//                        }
//                    }else if(url.contains("airtime")){
//                        if(AppUtilities.isSelfSupported()){
//                            chatActivity.openAirtime();
//                        }
//                    }else if(url.contains("marketplace")){
//                        if(AppUtilities.isSelfSupported()){
//                            MarketPlaceActivity.openSelf(fragment);
//                        }
//                    }else if(url.contains("feed")){
//                        FeedFragment.openSelf(fragment);
//                    }else if(url.contains("scan")){
//                        if (fragment.getParentActivity() == null) {
//                            return;
//                        }
//                        if (Build.VERSION.SDK_INT >= 23 && fragment.getParentActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                            fragment.getParentActivity().requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
//                            return;
//                        }
//                        CameraScanActivity.showAsSheet(fragment, true, CameraScanActivity.TYPE_QR, new CameraScanActivity.CameraScanActivityDelegate() {
//                            @Override
//                            public boolean processQr(String text, Runnable onLoadEnd) {
//                                return false;
//                            }
//
//                            @Override
//                            public void didFindQr(String text) {
//                                ShopUtils.processText(fragment, text);
//                            }
//                        });
//                    }else if(url.contains("nearby")){
//                        if (Build.VERSION.SDK_INT >= 23) {
//                            if (fragment.getParentActivity() == null) {
//                                return;
//                            }
//                            if (fragment.getParentActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                                fragment.presentFragment(new HuluchatIntroActivity(HuluchatIntroActivity.ACTION_TYPE_NEARBY_LOCATION_ACCESS, 0));
//                                return;
//                            }
//                        }
//                        boolean enabled = true;
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                            LocationManager lm = (LocationManager) ApplicationLoader.applicationContext.getSystemService(Context.LOCATION_SERVICE);
//                            enabled = lm.isLocationEnabled();
//                        } else if (Build.VERSION.SDK_INT >= 19) {
//                            try {
//                                int mode = Settings.Secure.getInt(ApplicationLoader.applicationContext.getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
//                                enabled = (mode != Settings.Secure.LOCATION_MODE_OFF);
//                            } catch (Throwable e) {
//                                FileLog.e(e);
//                            }
//                        }
//                        if (enabled) {
//                            fragment.presentFragment(new TinderFragment());
//                        } else {
//                            fragment.presentFragment(new HuluchatIntroActivity(HuluchatIntroActivity.ACTION_TYPE_NEARBY_LOCATION_ENABLED, 0));
//                        }
//                    }else if(url.contains("donation")){
//                        if(AppUtilities.isSelfSupported()){
//                           fragment.presentFragment(new DonationsActivity());
//                        }
//                    }
//                }
//            }else if(url.contains("story")){
//                long user_id = Utilities.parseInt(data.getQueryParameter("story"));
//                TLRPC.User user = fragment.getMessagesController().getUser(user_id);
//                if(user != null){
//                    ArrayList<TLRPC.User> userArrayList = new ArrayList<>();
//                    userArrayList.add(user);
//                    Bundle args = new Bundle();
//                    args.putInt("index", 0);
//                    StatusViewerActivity statusViewerActivity = new StatusViewerActivity(args);
//                    statusViewerActivity.setUserArrayList(new ArrayList<>(userArrayList));
//                    fragment.showAsSheet(statusViewerActivity);
//                }
//            }else if(url.contains("shop_id") || url.contains("product_id")){
//                long shop_id = Utilities.parseInt(data.getQueryParameter("shop_id"));
//                int product_id = 0;
//                if(url.contains("product_id")){
//                    product_id = Utilities.parseInt(data.getQueryParameter("product_id"));
//                }
//                if(product_id != 0){
//                    Bundle bundle = new Bundle();
//                    bundle.putLong("chat_id",shop_id);
//                    fragment.presentFragment(new BusinessProfileActivity(bundle));
//                }else{
//                    Bundle bundle = new Bundle();
//                    bundle.putLong("chat_id", shop_id);
//                    bundle.putInt("item_id",product_id);
//                    ProductDetailActivity detailFragment = new ProductDetailActivity(bundle);
//                    fragment.presentFragment(detailFragment);
//                }
//            }
//        }else if(url.startsWith("tg://escrow")) {
//            data = Uri.parse(url);
//            if (url.contains("uuid")) {
//                String uuid = data.getQueryParameter("uuid");
//                final AlertDialog[] alertDialog = {new AlertDialog(fragment.getParentActivity(), 3)};
//                alertDialog[0].setCanCancel(true);
//                fragment.showDialog(alertDialog[0]);
//                final  int[] reqId ={0};
//                reqId[0] = ServicesDataController.getInstance(fragment.getCurrentAccount()).getEscrowConfirmDetail(uuid, (response, apiError) -> {
//                    if(alertDialog[0] != null){
//                        alertDialog[0].dismiss();
//                        alertDialog[0] = null;
//                    }
//                    reqId[0] = 0;
//                    if(apiError == null){
//                        OrderModel orderModel = (OrderModel) response;
//                        EscrowDetailBottomSheet escrowDetailBottomSheet = new EscrowDetailBottomSheet(fragment.getParentActivity(),fragment,EscrowDetailBottomSheet.TYPE_INVOICE_CONFIRM,orderModel,null);
//                        escrowDetailBottomSheet.setEscrowDetailBottomSheetDelegate(new EscrowDetailBottomSheet.EscrowDetailBottomSheetDelegate() {
//                            @Override
//                            public void onProcessEscrow(boolean cancel,String uuid) {
//                                 alertDialog[0] = new AlertDialog(fragment.getParentActivity(),3);
//                                 alertDialog[0].setCanCancel(true);
//                                 alertDialog[0].show();
//                                 reqId[0] = ServicesDataController.getInstance(fragment.getCurrentAccount()).processEscrow(uuid, cancel, (response1, apiError1) -> {
//                                      if(alertDialog[0] != null){
//                                          alertDialog[0].dismiss();
//                                          alertDialog[0] = null;
//                                      }
//                                     reqId[0] = 0;
//
//                                     if(apiError1 == null){
//                                         String message;
//                                         if(cancel){
//                                             message = "Canceled!";
//                                         }else{
//                                             message = "Success!";
//                                         }
//                                         BulletinFactory.of(fragment).createCopyBulletin(message).show();
//                                     }else{
//                                         Context context = fragment.getParentActivity();
//                                         if(fragment.getParentActivity() != null){
//                                             //AlertsCreator.createSimpleAlert(context,"Error", apiError1.message()).show();
//                                         }
//                                     }
//                                 });
//                                 alertDialog[0].setOnCancelListener(dialogInterface -> {
//                                    if(reqId[0] != 0){
//                                        ServicesDataController.getInstance(fragment.getCurrentAccount()).cancelRequest(reqId[0]);
//                                        reqId[0] = 0;
//                                    }
//                                });
//                            }
//                        });
//                        fragment.showDialog(escrowDetailBottomSheet);
//                    }else{
//                        AlertsCreator.createSimpleAlert(fragment.getParentActivity(),"Error",apiError.message()).show();
//                    }
//                });
//                alertDialog[0].setOnCancelListener(dialogInterface -> {
//                    if(reqId[0] != 0){
//                        ServicesDataController.getInstance(fragment.getCurrentAccount()).cancelRequest(reqId[0]);
//                        reqId[0] = 0;
//                    }
//                });
//            }
//        }else if(url.startsWith("tg://user") && url.contains("id")){
//            try {
//                data = Uri.parse(url);
//                long user_id = Long.parseLong(data.getQueryParameter("id"));
//                Bundle args = new Bundle();
//                args.putLong("user_id",user_id);
//                if(MessagesController.getInstance(fragment.getCurrentAccount()).checkCanOpenChat(args,fragment)){
//                    ChatActivity chatActivity = new ChatActivity(args);
//                    fragment.presentFragment(chatActivity);
//                }
//            }catch (Exception ignored){
//
//            }
//        }else if(data.getHost().equals("product.hulugram.org")){
//            try {
//                try {
//                    String path =   data.getPath().replace("/","");
//                    Bundle bundle = new Bundle();
//                    bundle.putInt("item_id",Integer.parseInt(path));
//                    ProductDetailActivity detailFragment = new ProductDetailActivity(bundle);
//                    fragment.presentFragment(detailFragment);
//                }catch (Exception ignore){
//
//                }
//            }catch (Exception ignore){
//
//            }
//        }else if(data.getHost().equals("donation.hulugram.org")){
//            try {
//                try {
//                    String uuid =   data.getPath().replace("/","");
//                    String finalLinke = "tg://transaction?type=hulufund&id=" + uuid;
//                    processText(fragment,finalLinke);
//                }catch (Exception ignore){
//
//                }
//            }catch (Exception ignore){
//
//            }
//        }else if(data.getHost().equals("marketplace.hulugram.org")){
//            try {
//                LogManager.d("host was market: " + data.getHost());
//               List<String> pathes =  data.getPathSegments();
//                LogManager.d("host was path : " + pathes.size());
//
//                if(pathes.size() == 2){
//                   String type = pathes.get(0);
//                   String val = pathes.get(1);
//                    if("shops".equals(type)){
//                       Bundle bundle = new Bundle();
//                       bundle.putLong("chat_id",toClientChannelId(Long.parseLong(val)));
//                       fragment.presentFragment(new BusinessProfileActivity(bundle));
//                       BusinessProfileActivity businessProfileActivity = new BusinessProfileActivity(bundle);
//                       fragment.presentFragment(businessProfileActivity);
//                   }else if("collections".equals(type)){
//                       ShopDataSerializer.Collection collection = new ShopDataSerializer.Collection();
//                       collection.id = Integer.parseInt(val);
//                       collection.description = "Check out this collections";
//                       collection.photo = new ShopDataSerializer.PictureSnip();
//                       collection.photo.photo = null;
//                       collection.title  = "Collections";
//                       collection.is_active = true;
//                       collection.item_count  = 0;
//                       CollectionFragment collectionFragment = new CollectionFragment(collection);
//                       fragment.presentFragment(collectionFragment);
//                   }else if("products".equals(type)){
//                       Bundle bundle = new Bundle();
//                       bundle.putInt("item_id",Integer.parseInt(val));
//                       ProductDetailActivity detailFragment = new ProductDetailActivity(bundle);
//                       fragment.presentFragment(detailFragment);
//                   }else if("categories".equals(type)){
//                       Bundle bundle = new Bundle();
//                       bundle.putString("title",val);
//                       bundle.putString("bus_type", val);
//                       BusinessCategoryFragment productBusinessFragment = new BusinessCategoryFragment(bundle);
//                       fragment.presentFragment(productBusinessFragment);
//
//                   }
//               }
//
//            }catch (Exception exception){
//                LogManager.d("host was path type: expeciton: " + exception.getMessage());
//
//            }
//        }
//
//
//
//    }
//
//    public static void showEscrowConfirmationDetail(BaseFragment baseFragment,
//                                                    EscrowDetailModel escrowDetailModel,
//                                                    String link) {
//        baseFragment.showDialog(ShopEscrowAlertCreator.createEscrowConfirmDetailAlert(baseFragment.getParentActivity(),
//                baseFragment.getCurrentAccount(), escrowDetailModel.item.id,
//                escrowDetailModel.item.price,
//                escrowDetailModel.item.title, escrowDetailModel.item.picture.photo,
//                escrowDetailModel.payment_provider.name,
//                escrowDetailModel.payment_provider.image, false,
//                escrowDetailModel, escrowDetailModel.payment_provider.id,
//                link,
//                new ShopEscrowAlertCreator.OfferDelegate() {
//                    @Override
//                    public void onBuyWithSelectShow() {
//                        ShopEscrowAlertCreator.OfferDelegate.super.onBuyWithSelectShow();
//                    }
//                }));
//    }
//
//    public static  boolean isHuluChatSupportedLink(String text){
//        if(text  == null){
//            return false;
//        }
//        return text.contains("marketplace.hulugram.org") || (text.contains("https://t.me/hulugramdonation/") && text.contains("uuid")) || text.startsWith("tg://user") || text.startsWith("tg://open") || text.startsWith("tg://transaction") || text.startsWith("tg://escrow") || text.contains("product.hulugram.org") || text.contains("donation.hulugram.org");
//
//
//        //
////        try {
////            Uri url = Uri.parse(text);
////            String host =  url.getHost();
////            if(host.equals("product.hulugram.org")){
////                return true;
////            }
////        }catch (Exception ignore){
////
////        }
//    }
//
////
////    public static boolean containInternalLink(MessageObject messageObject){
////        boolean contain = false;
////        if(messageObject != null && messageObject.messageOwner != null && messageObject.messageOwner.message != null &&  messageObject.messageOwner.message.contains("tg://open?")){
////            return true;
////        }
////        return contain;
////    }
//
//    public static String formatProductDetail(List<DetailCell.Item> items){
//        StringBuilder stringBuilder  = new StringBuilder();
//        for(DetailCell.Item item:items){
//            stringBuilder.append(item.key).append(" : ").append(item.value).append("\n");
//        }
//        return stringBuilder.toString();
//    }
//
//    public static String getProductLink(long channel_id,long item_id){
//
//        return open_product_link.replace("{shopId}",String.valueOf(channel_id)).replace("{productId}",String.valueOf(item_id));
//    }
//
//    public static void shareProduct(AccountInstance accountInstance, Map<String,Object> productFull, int dialog_id){
//        if(productFull == null) {
//            return;
//        }
//
//            ArrayList<SendMessagesHelper.SendingMediaInfo> photos = new ArrayList<>();
//            SendMessagesHelper.SendingMediaInfo sendingMediaInfo = new SendMessagesHelper.SendingMediaInfo();
//            sendingMediaInfo.caption = "This is the captin";
//            sendingMediaInfo.isVideo = false;
//            sendingMediaInfo.uri= Uri.parse("https://play-lh.googleusercontent.com/iBYjvYuNq8BB7EEEHktPG1fpX9NiY7Jcyg1iRtQxO442r9CZ8H-X9cLkTjpbORwWDG9d=s180-rw");
//            photos.add(sendingMediaInfo);
//            SendMessagesHelper.prepareSendingMedia(accountInstance,photos,dialog_id,null,null,null,false,false,null,true,0,false);
//
//
//        }
//
//
//    public static String getShopLink(long channel_id){
//        return "tg://open?shop_id=" + channel_id;
//    }
//
//    public static String getUserLink(TLRPC.User user){
//        return "hg://open?user=" + user.username;
//    }
//
//    public static String getChannelLink(TLRPC.Chat chat){
//        return "hg://open?user=" + chat.username;
//    }
//
//
//
//    public static final String UI_CHOOSE_HORIZONTAL = "ui_choose_hor";
//    public static final String UI_INPUT_STRING = "ui_input_str";
//    public static final String UI_INPUT_NUM = "ui_input_num";
//    public static final String UI_INPUT_LOC = "ui_input_loc";
//    public static final String UI_CHOOSE = "ui_choose";
//    public static final String ui_radio_box = "ui_radio_box";
//    public static final String ui_date_chooser = "ui_date_chooser";
//
//    public static final int SHOP_TYPE_CAR = 1;
//    public static final int SHOP_TYPE_HOUSE = 2;
//    public static final int SHOP_TYPE_ELECTRONICS = 3;
//    public static final int SHOP_TYPE_FASHION =  4;
//    public static final int SHOP_TYPE_FURNITURE =  5;
//    public static final int SHOP_TYPE_HEALTH_BEAUTY = 6;
//    public static final int SHOP_TYPE_FOD_AND_BEVERAGE =  7;
//    public static final int SHOP_TYPE_FOD_AND_GENERAL =  8;
//
//    public static String formatReviewData(String date){
//        Instant instant = Instant.parse( date);
//        long epochSec = instant.toEpochMilli();
//        return LocaleController.getInstance().formatterYearMax.format(epochSec);
//    }
//
//    public static String formatLast(String data){
//        try {
//
//            long date = Instant.parse(data).toEpochMilli();
//
//            Calendar rightNow = Calendar.getInstance();
//            int day = rightNow.get(Calendar.DAY_OF_YEAR);
//            int year = rightNow.get(Calendar.YEAR);
//            rightNow.setTimeInMillis(date);
//            int dateDay = rightNow.get(Calendar.DAY_OF_YEAR);
//            int dateYear = rightNow.get(Calendar.YEAR);
//
//            if (dateDay == day && year == dateYear) {
//                int diff = (int) (ConnectionsManager.getInstance(UserConfig.selectedAccount).getCurrentTime() - date / 1000) / 60;
//                if (diff < 1) {
//                    return LocaleController.getString("shopUpdatedJustNow", R.string.shopUpdatedJustNow);
//                } else if (diff < 60) {
//                    return LocaleController.formatPluralString("UpdatedMinutes", diff);
//                }
//                return LocaleController.formatString("ShopUpdatedFormatted", R.string.ShopUpdatedFormatted, LocaleController.formatString("TodayAtFormatted", R.string.TodayAtFormatted, LocaleController.getInstance().formatterDay.format(new Date(date))));
//            } else if (dateDay + 1 == day && year == dateYear) {
//                return LocaleController.formatString("ShopUpdatedFormatted", R.string.ShopUpdatedFormatted, LocaleController.formatString("YesterdayAtFormatted", R.string.YesterdayAtFormatted, LocaleController.getInstance().formatterDay.format(new Date(date))));
//            } else if (Math.abs(System.currentTimeMillis() - date) < 31536000000L) {
//                String format = LocaleController.formatString("formatDateAtTime", R.string.formatDateAtTime, LocaleController.getInstance().formatterDayMonth.format(new Date(date)), LocaleController.getInstance().formatterDay.format(new Date(date)));
//                return LocaleController.formatString("ShopUpdatedFormatted", R.string.ShopUpdatedFormatted, format);
//            } else {
//                String format = LocaleController.formatString("formatDateAtTime", R.string.formatDateAtTime, LocaleController.getInstance().formatterYear.format(new Date(date)), LocaleController.getInstance().formatterDay.format(new Date(date)));
//                return LocaleController.formatString("ShopUpdatedFormatted", R.string.ShopUpdatedFormatted, format);
//            }
//        } catch (Exception e) {
//            FileLog.e(e);
//        }
//        return "LOC_ERR";
//
//
//    }
//
//
//
//
//    public static String formatSort(ShopDataSerializer.Sort  sort){
//        if(sort == null){
//            return "";
//        }
//        return sort.key;
//    }
//
//    public static String formatDuration(int duration) {
//        if (duration <= 0) {
//            return LocaleController.formatPluralString("Seconds", 0);
//        }
//        final int hours = duration / 3600;
//        final int minutes = duration / 60 % 60;
//        final int seconds = duration % 60;
//
//        final StringBuilder stringBuilder = new StringBuilder();
//        if (hours > 0) {
//            stringBuilder.append(LocaleController.formatPluralString("Hours", hours));
//        }
//        if (minutes > 0) {
//            if (stringBuilder.length() > 0) {
//                stringBuilder.append(' ');
//            }
//            stringBuilder.append(LocaleController.formatPluralString("Minutes", minutes));
//        }
//        if (seconds > 0) {
//            if (stringBuilder.length() > 0) {
//                stringBuilder.append(' ');
//            }
//            stringBuilder.append(LocaleController.formatPluralString("Seconds", seconds));
//        }
//        return stringBuilder.toString();
//    }
//
//    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";
//
//
//    public static String formatStatus(String date_str){
//        try {
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
//            Date val = simpleDateFormat.parse(date_str);
//            Date date = new Date();
//        }catch (Exception ignroe){
//
//        }
//
//        return "";
//    }
//
//
//    public static int convertDateToSec(String datStry){
//        try {
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT,Locale.getDefault());
//            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//            Date serverDate = simpleDateFormat.parse(datStry);
//            return  (int)serverDate.toInstant().toEpochMilli()/1000;
//        }catch (Exception ignore){
//
//        }
//     return 0;
//
//    }
//
//    public static File getPathFromBitmap(Bitmap bitmap){
//        if(bitmap == null){
//            return null;
//        }
//        File imagePath = AndroidUtilities.generatePicturePath();
//        try {
//            FileOutputStream out = new FileOutputStream(imagePath);
//            bitmap.compress(Bitmap.CompressFormat.PNG, 80, out);
//            out.flush();
//            out.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return imagePath;
//    }
//
//
//    public static String formatShopDuration(String date){
//
//        Instant serverInstant = Instant.parse(date);
//        Instant localInstant  = Instant.now();
//
//
//
////        Log.i("formatShopDuration","toString = " + instant.toString());
////        Log.i("formatShopDuration","getEpochSecond" + instant.getEpochSecond());
////        Log.i("formatShopDuration","getNano" + instant.getNano());
////        Log.i("formatShopDuration","" + instant.toString());
//
//
//        return "";
//    }
//
//    public static String formatDateTime(String dateString){
//        try {
//            Instant serverInstant = Instant.parse(dateString);
//            long sec = serverInstant.toEpochMilli()/1000;
//            return LocaleController.formatSectionDate(sec);
//
//        }catch (Exception ignore){
//
//        }
//        return "";
//    }
//
//
//
//    public static String formatFullData(String dateString){
//        try {
//            Instant serverInstant = Instant.parse(dateString);
//            long sec = serverInstant.toEpochMilli()/1000;
//            return LocaleController.formatDateTime(sec);
//
//        }catch (Exception ignore){
//
//        }
//        return "";
//    }
//
//    public static String formatStoryDate(String dateString){
//        try {
//            Instant serverInstant = Instant.parse(dateString);
//            int sec = (int)((ConnectionsManager.native_getCurrentTime(UserConfig.selectedAccount)) - (serverInstant.toEpochMilli()/1000));
//            if(sec < 60){
//                return LocaleController.formatPluralString("Seconds", sec) + " ago";
//            }else if (sec < 60 * 60) {
//                return LocaleController.formatPluralString("Minutes", sec / 60) + " ago";
//            }else if (sec < 60 * 60 * 24) {
//                return LocaleController.formatPluralString("Hours", sec / 60 / 60) + " ago";
//            }
//
//        }catch (Exception ignore){
//
//        }
//        return "";
//    }
//
//
//    public static String formatShopDate(String dateString){
//        try {
//
////            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT,Locale.getDefault());
////            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
////
////            Date serverDate = simpleDateFormat.parse(dateString);
////
////
////
////            Date date = new Date();
////            String localDate = simpleDateFormat.format(date);
////            Instant instant = Instant.parse(localDate);
////
//////            Calendar calendar = Calendar.getInstance();
//////            calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
//////            Date localDate = calendar.getTime();
//
//
//            Instant serverInstant = Instant.parse(dateString);
//            int sec = (int)((ConnectionsManager.native_getCurrentTime(UserConfig.selectedAccount)) - (serverInstant.toEpochMilli()/1000));
//            if(sec < 60){
//                return LocaleController.formatPluralString("Seconds", sec) + " ago";
//            }else if (sec < 60 * 60) {
//                return LocaleController.formatPluralString("Minutes", sec / 60) + " ago";
//            }else if (sec < 60 * 60 * 24) {
//                return LocaleController.formatPluralString("Hours", sec / 60 / 60) + " ago";
//            } else if (sec < 60 * 60 * 24 * 7) {
//                return LocaleController.formatPluralString("Days", sec / 60 / 60 / 24) + " ago";
//            } else{
//                int days = sec / 60 / 60 / 24;
//                int weeks = days/7;
//                if(weeks > 4){
//                    if(weeks % 4 == 0){
//                        return weeks/4 + " mon ago";
//                       // return LocaleController.formatPluralString("Months", days / 7) + " ago";
//                    }else{
//                        return weeks/4 + " mon ago";
////                        return String.format("%s %s", LocaleController.formatPluralString("Months", weeks  / 4), LocaleController.formatPluralString("weeks", weeks % 4)) + " ago";
//                    }
//                }else{
//                    return LocaleController.formatPluralString("Weeks", days / 7) + " ago";
//
////                    if (days % 7 == 0) {
////                        return LocaleController.formatPluralString("Weeks", days / 7) + " ago";
////                    }else {
////                        return String.format("%s %s", LocaleController.formatPluralString("Weeks", days / 7), LocaleController.formatPluralString("Days", days % 7)) +  " ago";
////                    }
//                }
//
//
//            }
//
//
//
//
////            long min  = sec/60;
////            if(min < 60){
////                return min + " min ago";
////            }
////            long hour = min/60;
////            if(hour < 24){
////                return hour + " hours ago";
////            }
////            long days = hour/24;
////            if(days < 7){
////                return days + " days ago";
////            }else if(days <= 30){
////                long weeks = days/7;
////                return weeks + " weeks ago";
////            }else if(days <= 365){
////                long month = days / 7 / 12 + 1;
////                return month + " month ago";
////            }else{
////                long year = days / 4 / 12 + 1;
////                return year + " years ago";
////            }
//        }catch (Exception e){
//            Log.i("datetext","sec = " + e.getMessage());
//
//        }
//        return "";
//    }
//
//
//
//    public static String formatShopAbout(ShopDataSerializer.Shop shop){
//        if(shop == null){
//            return "";
//        }
//        return shop.count + " items";
//
//    }
//
//    public static String formatShopRating(int shop){
//
//        SpannableStringBuilder builder = new SpannableStringBuilder();
//        DecimalFormat df = new DecimalFormat("#.#");
//        double averageRating = Double.parseDouble(df.format(shop));
//        for(int a = 0; a < averageRating; a++){
//            builder.append("☆");
//        }
//        builder.append("(").append(String.valueOf(shop)).append(")");
//        return builder.toString();
//
//    }
//
//
//    public static String formatProductOffer(int productId){
//        String name = "https://product.hulugram.org/%s";
//        return String.format(Locale.US,name,productId);
//
//    }
//
//
//    public static String formatShopAboutt(ShopDataSerializer.Shop shop){
//        if(shop == null){
//            return "";
//        }
//
//        SpannableStringBuilder builder = new SpannableStringBuilder();
//        String rating = shop.rating_average + " ";
//        builder.append(rating);
//        builder.append(" ⭐⭐⭐ ");
//
//        builder.append(" ( ").append(String.valueOf(shop.rating_count)).append(")");
//
//        if(!isEmpty(shop.website)){
//            builder.append(" \uD83D\uDD17  ");
//            builder.append(shop.website);
//        }
//
//        builder.append("\n");
//        builder.append("\n");
//
//        builder.append("\uD83C\uDFD9 ").append(shop.city).append("   ").append("\uD83D\uDCC5  joined ").append(formatReviewData(shop.created_at));
//        builder.setSpan(new ForegroundColorSpan(Theme.getColor(Theme.key_chats_secretName)), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        builder.append("\n");
//        builder.append("\n");
//        builder.append("\uD83D\uDCCD ");
//        builder.append(shop.address);
//        builder.append("\n");
//        return builder.toString();
//    }
//
//
//    public static String formatCurrency(String price){
//        return formatCurrency(Double.parseDouble(price));
//    }
//    public static String formatCurrency(double price,String currency){
//        DecimalFormat formatter;
//        if(price == (long) price)
//            formatter = new DecimalFormat("#,###");
//        else
//            formatter = new DecimalFormat("#,###.00");
//        return currency + formatter.format(price);
//    }
//
//    public static String formatCurrency(double price){
//        DecimalFormat formatter;
//        if(price == (long) price)
//            formatter = new DecimalFormat("#,###");
//        else
//            formatter = new DecimalFormat("#,###.00");
//        return "ETB " + formatter.format(price);
//    }
//    public static String formatCurrency(double price,String currency,boolean front){
//        DecimalFormat formatter;
//        if(price == (long) price)
//            formatter = new DecimalFormat("#,###");
//        else
//            formatter = new DecimalFormat("#,###.00");
//        if(front){
//            return String.format("%s" + formatter.format(price),currency);
//        }
//        return String.format(formatter.format(price) + "%s",currency);
//    }
//    public static final int FLAG_TAG_BR = 1;
//    public static final int FLAG_TAG_BOLD = 2;
//    public static final int FLAG_TAG_COLOR = 4;
//    public static final int FLAG_TAG_URL = 8;
//    public static final int FLAG_TAG_ALL = FLAG_TAG_BR | FLAG_TAG_BOLD | FLAG_TAG_URL;
//
//    public static SpannableStringBuilder replaceTags(String str) {
//        return replaceTags(str, FLAG_TAG_ALL);
//    }
//
//    public static SpannableStringBuilder replaceTags(String str, int flag, Object... args) {
//        try {
//            int start;
//            int end;
//            StringBuilder stringBuilder = new StringBuilder(str);
//            if ((flag & FLAG_TAG_BR) != 0) {
//                while ((start = stringBuilder.indexOf("<br>")) != -1) {
//                    stringBuilder.replace(start, start + 4, "\n");
//                }
//                while ((start = stringBuilder.indexOf("<br/>")) != -1) {
//                    stringBuilder.replace(start, start + 5, "\n");
//                }
//            }
//            ArrayList<Integer> bolds = new ArrayList<>();
//            if ((flag & FLAG_TAG_BOLD) != 0) {
//                while ((start = stringBuilder.indexOf("<b>")) != -1) {
//                    stringBuilder.replace(start, start + 3, "");
//                    end = stringBuilder.indexOf("</b>");
//                    if (end == -1) {
//                        end = stringBuilder.indexOf("<b>");
//                    }
//                    stringBuilder.replace(end, end + 4, "");
//                    bolds.add(start);
//                    bolds.add(end);
//                }
//                while ((start = stringBuilder.indexOf("**")) != -1) {
//                    stringBuilder.replace(start, start + 2, "");
//                    end = stringBuilder.indexOf("**");
//                    if (end >= 0) {
//                        stringBuilder.replace(end, end + 2, "");
//                        bolds.add(start);
//                        bolds.add(end);
//                    }
//                }
//            }
//            if ((flag & FLAG_TAG_URL) != 0) {
//                while ((start = stringBuilder.indexOf("**")) != -1) {
//                    stringBuilder.replace(start, start + 2, "");
//                    end = stringBuilder.indexOf("**");
//                    if (end >= 0) {
//                        stringBuilder.replace(end, end + 2, "");
//                        bolds.add(start);
//                        bolds.add(end);
//                    }
//                }
//            }
//
//
//            if ((flag & FLAG_TAG_COLOR) != 0) {
//                while ((start = stringBuilder.indexOf("**")) != -1) {
//                    stringBuilder.replace(start, start + 2, "");
//                    end = stringBuilder.indexOf("**");
//                    if (end >= 0) {
//                        stringBuilder.replace(end, end + 2, "");
//                        bolds.add(start);
//                        bolds.add(end);
//                    }
//                }
//            }
//
//            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(stringBuilder);
//            for (int a = 0; a < bolds.size() / 2; a++) {
//                spannableStringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf")), bolds.get(a * 2), bolds.get(a * 2 + 1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            }
//
//
//            if((flag & FLAG_TAG_COLOR) != 0 && args.length > 0 && args[0] instanceof Integer){
//                int color = (int)args[0];
//                for (int a = 0; a < bolds.size() / 2; a++) {
//                    spannableStringBuilder.setSpan(new ForegroundColorSpan(color), bolds.get(a * 2), bolds.get(a * 2 + 1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//                }
//
//            }
//
//
//            return spannableStringBuilder;
//        } catch (Exception e) {
//            FileLog.e(e);
//        }
//        return new SpannableStringBuilder(str);
//    }
//
//    public static int getIntAlphaColor(int color, float factor){
//        int alpha = Math.round(Color.alpha(color) * factor);
//        int red = Color.red(color);
//        int green = Color.green(color);
//        int blue = Color.blue(color);
//        return Color.argb(alpha, red, green, blue);
//    }
//
//    public static long toBotChannelId(long chat_id){
//        if(BuildVars.DEBUG_PRIVATE_VERSION){
//            return Long.parseLong("-10000" + chat_id);
//        }
//        if(String.valueOf(chat_id).startsWith("-100")){
//            return chat_id;
//        }
//        return Long.parseLong("-100" + chat_id);
//    }
//
//    public static long toClientChannelId(long chat_id){
//        if(BuildVars.DEBUG_PRIVATE_VERSION){
//            if(String.valueOf(chat_id).startsWith("-10000")){
//                return chat_id;
//            }
//            return Long.parseLong(String.valueOf(chat_id).replace("-10000",""));
//
//        }
//        if(!String.valueOf(chat_id).startsWith("-100")){
//            return chat_id;
//        }
//        return Long.parseLong(String.valueOf(chat_id).replace("-100",""));
//    }
//
//
//
//    public static boolean isEmpty(String string){
//        if(TextUtils.isEmpty(string)){
//            return true;
//        }
//        return string.toLowerCase().equals("null");
//    }
//
//    public static String getEmojiForType(int type){
//
//        if(type  == SHOP_TYPE_CAR){
//            return "\uD83D\uDE97";
//        }
//        return "";
//    }
//
//    public static String getDisplayNameForGivenBusiness(int type){
//        if(type == SHOP_TYPE_CAR){
//            return "Car";
//        }
//        return "Business";
//    }
//
//
//    public static Drawable createSimpleSelectorRoundRectStorkeDrawable(int rad, int stroke,int storkeColor,int defaultColor, int pressedColor) {
//
//        GradientDrawable defaultDrawable =  new GradientDrawable();
//        defaultDrawable.setCornerRadius(rad);
//        defaultDrawable.setStroke(stroke,storkeColor);
//        defaultDrawable.setColor(defaultColor);
//
//        GradientDrawable pressedDrawable =  new GradientDrawable();
//        pressedDrawable.setCornerRadius(rad);
//        pressedDrawable.setStroke(stroke,storkeColor);
//        pressedDrawable.setColor(storkeColor);
//
//
//        if (Build.VERSION.SDK_INT >= 21) {
//            ColorStateList colorStateList = new ColorStateList(
//                    new int[][]{StateSet.WILD_CARD},
//                    new int[]{pressedColor}
//            );
//            return new RippleDrawable(colorStateList, defaultDrawable, pressedDrawable);
//        } else {
//            StateListDrawable stateListDrawable = new StateListDrawable();
//            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
//            stateListDrawable.addState(new int[]{android.R.attr.state_selected}, pressedDrawable);
//            stateListDrawable.addState(StateSet.WILD_CARD, defaultDrawable);
//            return stateListDrawable;
//        }
//    }
//
//    public static Drawable createRoundStrokeDrwable(int rad, int stroke, int stroke_color, int fillColor){
//        GradientDrawable shape =  new GradientDrawable();
//        shape.setCornerRadius(rad);
//        shape.setStroke(stroke,stroke_color);
//        shape.setColor(fillColor);
//        return shape;
//    }
//
//    public static Drawable createRoundStrokeDrwable(int rad, int stroke, String stroke_color, int fillColor){
//        GradientDrawable shape =  new GradientDrawable();
//        shape.setCornerRadius(rad);
//        shape.setStroke(stroke,Theme.getColor(stroke_color));
//        shape.setColor(fillColor);
//        return shape;
//    }
//    public static Drawable createRoundStrokeDrwable(int rad, int stroke, String stroke_color, String fillColor){
//        GradientDrawable shape =  new GradientDrawable();
//        shape.setCornerRadius(rad);
//        shape.setStroke(stroke,Theme.getColor(stroke_color));
//        shape.setColor(Theme.getColor(fillColor));
//        return shape;
//    }
//
//    public static Drawable createTopRoundRectDrawable(int rad, int defaultColor) {
//        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, 0, 0, 0, 0}, null, null));
//        defaultDrawable.getPaint().setColor(defaultColor);
//        return defaultDrawable;
//    }
//
//    public static Drawable createBottomRoundRectDrawable(int rad, int defaultColor) {
//        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{0, 0, 0, 0, rad, rad, rad,rad}, null, null));
//        defaultDrawable.getPaint().setColor(defaultColor);
//        return defaultDrawable;
//    }
//
//    public static AlertDialog.Builder createErrorAlert(Context context, APIError apiError){
//        if (context == null || apiError == null) {
//            return null;
//        }
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//
//        EmptyTextProgressView emptyTextProgressView = new EmptyTextProgressView(context);
//        emptyTextProgressView.showTextView();
//        emptyTextProgressView.setText(apiError.message());
//        builder.setView(emptyTextProgressView);
//
//        return builder;
//    }
//
//    public static BottomSheet.Builder createConnectionAlert(Context context,Runnable retry){
//        if (context == null) {
//            return null;
//        }
//        BottomSheet.Builder builder = new BottomSheet.Builder(context, false);
//        builder.setApplyBottomPadding(false);
//
//        ShopsEmptyCell shopsEmptyCell = new ShopsEmptyCell(context);
//        shopsEmptyCell.setType(ShopsEmptyCell.TYPE_RETRY);
//        builder.setCustomView(shopsEmptyCell);
//        shopsEmptyCell.setRetryListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(retry != null){
//                    retry.run();
//                    builder.getDismissRunnable().run();
//                }
//            }
//        });
//        return builder;
//    }
//
//    public static CombinedDrawable createLocBackDrawable(int res){
//        CombinedDrawable menuButton = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(42), res);
//        menuButton.setIconSize(AndroidUtilities.dp(21), AndroidUtilities.dp(21));
//        Theme.setCombinedDrawableColor(menuButton, Theme.getColor(Theme.key_chats_actionBackground), false);
//        Theme.setCombinedDrawableColor(menuButton, Theme.getColor(Theme.key_chats_actionIcon), true);
//        return  menuButton;
//    }
//    public static CombinedDrawable createDetailMenuDrawable(int res){
//        CombinedDrawable menuButton = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(42), res);
//        menuButton.setIconSize(AndroidUtilities.dp(21), AndroidUtilities.dp(21));
//        Theme.setCombinedDrawableColor(menuButton, Theme.getColor(Theme.key_actionBarDefault), false);
//        Theme.setCombinedDrawableColor(menuButton, Theme.getColor(Theme.key_dialogTextBlack), true);
//        return  menuButton;
//    }
//
//    public static Bitmap createTonQR(Context context, String key, Bitmap oldBitmap) {
//        try {
//            HashMap<EncodeHintType, Object> hints = new HashMap<>();
//            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
//            hints.put(EncodeHintType.MARGIN, 0);
//            return new QRCodeWriter().encode(key, BarcodeFormat.QR_CODE, 768, 768, hints, oldBitmap, context,null);
//        } catch (Exception e) {
//            FileLog.e(e);
//        }
//        return null;
//    }
//
//    public static void shareBitmap(Activity activity, View view, String text) {
//        try {
//            ImageView imageView = (ImageView) view;
//            BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
//            File f = AndroidUtilities.getSharingDirectory();
//            f.mkdirs();
//            f = new File(f, "qr.jpg");
//            FileOutputStream outputStream = new FileOutputStream(f.getAbsolutePath());
//            bitmapDrawable.getBitmap().compress(Bitmap.CompressFormat.JPEG, 87, outputStream);
//            outputStream.close();
//
//            Intent intent = new Intent(Intent.ACTION_SEND);
//            intent.setType("image/jpeg");
//            if (!TextUtils.isEmpty(text)) {
//                intent.putExtra(Intent.EXTRA_TEXT, text);
//            }
//            if (Build.VERSION.SDK_INT >= 24) {
//                try {
//                    intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(activity, ApplicationLoader.getApplicationId() + ".provider", f));
//                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                } catch (Exception ignore) {
//                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
//                }
//            } else {
//                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
//            }
//            activity.startActivityForResult(Intent.createChooser(intent, LocaleController.getString("WalletShareQr", R.string.WalletShareQr)), 500);
//        } catch (Exception e) {
//            FileLog.e(e);
//        }
//    }
//
//
//    public interface  PaletHandler{
//        void onPlateLoaded(Palette palette);
//    }
//    public static void createPaletteAsync(Bitmap bitmap,PaletHandler paletHandler) {
//        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
//            public void onGenerated(Palette p) {
//                AndroidUtilities.runOnUIThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(paletHandler != null && p != null){
//                            paletHandler.onPlateLoaded(p);
//                        }
//                    }
//                });
//            }
//        });
//    }
//
//
////    private void showProductSheet(String url) {
////        if (getParentActivity() == null) {
////            return;
////        }
////        Context context = getParentActivity();
////        BottomSheet.Builder builder = new BottomSheet.Builder(context);
////        builder.setApplyBottomPadding(false);
////        builder.setApplyTopPadding(false);
////        builder.setUseFullWidth(false);
////
////
////        FrameLayout frameLayout = new FrameLayout(context);
////
////        TextView titleView = new TextView(context);
////        titleView.setLines(1);
////        titleView.setSingleLine(true);
////        titleView.setText("Product link");
////        titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
////        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
////        titleView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
////        titleView.setEllipsize(TextUtils.TruncateAt.END);
////        titleView.setGravity(Gravity.CENTER_VERTICAL);
////        frameLayout.addView(titleView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 21, 22, 21, 0));
////
////        LinearLayout linearLayout = new LinearLayout(context);
////        linearLayout.setOrientation(LinearLayout.VERTICAL);
////        frameLayout.addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 0, 78, 0, 0));
////
////        TextView descriptionText = new TextView(context);
////        descriptionText.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
////        descriptionText.setGravity(Gravity.CENTER_HORIZONTAL);
////        descriptionText.setLineSpacing(AndroidUtilities.dp(2), 1);
////        descriptionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
////        descriptionText.setText("Scan the qr code to open the prodcut");
////
////        descriptionText.setPadding(AndroidUtilities.dp(32), 0, AndroidUtilities.dp(32), 0);
////        linearLayout.addView(descriptionText, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 0, 0, 0));
////
////        TextView addressValueTextView = new TextView(context);
////
////        ImageView imageView = new ImageView(context);
////        imageView.setImageBitmap(ShopUtils.createTonQR(context, url, null));
////        linearLayout.addView(imageView, LayoutHelper.createLinear(190, 190, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 16, 0, 0));
////        imageView.setOnLongClickListener(v -> {
////            ShopUtils.shareBitmap(getParentActivity(), v, url);
////            return true;
////        });
////
////        ActionBarMenuItem menuItem = new ActionBarMenuItem(context, null, 0, Theme.getColor(Theme.key_dialogTextBlack));
////        menuItem.setLongClickEnabled(false);
////        menuItem.setIcon(R.drawable.ic_ab_other);
////        menuItem.setContentDescription(LocaleController.getString("AccDescrMoreOptions", R.string.AccDescrMoreOptions));
////        menuItem.addSubItem(1, "Copy to clipboard");
////        menuItem.addSubItem(2,"Share Qr Photo");
////        menuItem.setSubMenuOpenSide(2);
////        menuItem.setDelegate(id -> {
////            builder.getDismissRunnable().run();
////            if (id == 1) {
////                AndroidUtilities.addToClipboard(url);
////                Toast.makeText(getParentActivity(), LocaleController.getString("LinkCopied", R.string.LinkCopied), Toast.LENGTH_SHORT).show();
////            } else if (id == 2) {
////                ShopUtils.shareBitmap(getParentActivity(),imageView,url);
////            }
////        });
////        menuItem.setTranslationX(AndroidUtilities.dp(6));
////        menuItem.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor(Theme.key_dialogButtonSelector), 6));
////        frameLayout.addView(menuItem, LayoutHelper.createFrame(48, 48, Gravity.TOP | Gravity.RIGHT, 0, 12, 10, 0));
////        menuItem.setOnClickListener(v -> menuItem.toggleSubMenu());
////
////        addressValueTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
////        addressValueTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmono.ttf"));
////        addressValueTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
////        StringBuilder stringBuilder = new StringBuilder("");
////        stringBuilder.insert(stringBuilder.length() / 2, '\n');
////        addressValueTextView.setText(stringBuilder);
////        //linearLayout.addView(addressValueTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 0));
////        addressValueTextView.setOnLongClickListener(v -> {
////            AndroidUtilities.addToClipboard(url);
////            Toast.makeText(getParentActivity(), LocaleController.getString("WalletTransactionAddressCopied", R.string.WalletTransactionAddressCopied), Toast.LENGTH_SHORT).show();
////            return true;
////        });
////
////        TextView buttonTextView = new TextView(context);
////        buttonTextView.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
////        buttonTextView.setGravity(Gravity.CENTER);
////        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
////        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
////        buttonTextView.setText("Share");
////        buttonTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
////        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_featuredStickers_addButton), Theme.getColor(Theme.key_featuredStickers_addButtonPressed)));
////        linearLayout.addView(buttonTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 42, Gravity.LEFT | Gravity.TOP, 16, 20, 16, 16));
////        buttonTextView.setOnClickListener(v -> AndroidUtilities.openSharing(this, url));
////
////        ScrollView scrollView = new ScrollView(context);
////        scrollView.setVerticalScrollBarEnabled(false);
////        scrollView.addView(frameLayout, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));
////        if (Build.VERSION.SDK_INT >= 21) {
////            scrollView.setNestedScrollingEnabled(true);
////        }
////
////        builder.setCustomView(scrollView);
////        BottomSheet bottomSheet = builder.create();
////        bottomSheet.setCanDismissWithSwipe(false);
////        showDialog(bottomSheet);
////    }
////
////    private void showProductDetail(String detail){
////        if (getParentActivity() == null) {
////            return;
////        }
////        Context context = getParentActivity();
////        BottomSheet.Builder builder = new BottomSheet.Builder(context);
////        builder.setApplyBottomPadding(false);
////        builder.setApplyTopPadding(false);
////        builder.setUseFullWidth(false);
////
////        FrameLayout frameLayout = new FrameLayout(context);
////
////        TextView titleView = new TextView(context);
////        titleView.setLines(1);
////        titleView.setSingleLine(true);
////        titleView.setText("Product Descripation");
////        titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
////        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
////        titleView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
////        titleView.setEllipsize(TextUtils.TruncateAt.END);
////        titleView.setGravity(Gravity.CENTER_VERTICAL);
////        frameLayout.addView(titleView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 21, 22, 21, 0));
////
////
////        LinearLayout linearLayout = new LinearLayout(context);
////        linearLayout.setOrientation(LinearLayout.VERTICAL);
////        frameLayout.addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 0, 40, 0, 0));
////
////
////        AboutLinkCell aboutLinkCell = new AboutLinkCell(context,this){
////            @Override
////            protected void didPressUrl(String url) {
////                if (url.startsWith("@")) {
////                    getMessagesController().openByUserName(url.substring(1), ProductDetailActivity.this, 0);
////                } else if (url.startsWith("#")) {
////                    DialogsActivity fragment = new DialogsActivity(null);
////                    fragment.setSearchString(url);
////                    presentFragment(fragment,true);
////                } else{
////                    Browser.openUrl(context,url);
////                }
////
////            }
////        };
////        aboutLinkCell.setText(detail,true);
////        linearLayout.addView(aboutLinkCell,LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 8, 8, 0));
////
////        ScrollView scrollView = new ScrollView(context);
////        scrollView.setVerticalScrollBarEnabled(false);
////        scrollView.addView(frameLayout, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));
////        if (Build.VERSION.SDK_INT >= 21) {
////            scrollView.setNestedScrollingEnabled(true);
////        }
////
////        builder.setCustomView(scrollView);
////        BottomSheet bottomSheet = builder.create();
////        bottomSheet.setCanDismissWithSwipe(false);
////        showDialog(bottomSheet);
////
////    }
//
//}
