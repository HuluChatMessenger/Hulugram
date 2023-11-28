package plus.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Patterns;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.core.graphics.ColorUtils;

import org.json.JSONObject;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.Bitmaps;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ForegroundDetector;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ShareAlert;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

import plus.PlusConfig;
import plus.cells.ButtonCell;
import plus.cells.CheckBoxChatCell;
import plus.components.AnimatedInfoView;
import plus.helpers.LogManager;
import plus.marketplace.BusinessProfileActivity;
import plus.marketplace.ProductDetailActivity;
import plus.marketplace.utils.ShopUtils;
import plus.net.ServicesDataController;
import plus.net.ServicesModel;

public class HulugramUtils {


    public static int getRandomNumber(int limit){
        return new Random().nextInt(limit);
    }

    public static <T> T chooseRandom(List<T> list){
        return list.get(getRandomNumber(list.size()));
    }


    public static boolean needShowPasscode(boolean reset) {
        boolean wasInBackground = ForegroundDetector.getInstance().isWasInBackground(reset);
        if (reset) {
            ForegroundDetector.getInstance().resetBackgroundVar();
        }
        int uptime = (int) (SystemClock.elapsedRealtime() / 1000);
        if (BuildVars.LOGS_ENABLED && reset && PlusConfig.passcodeHash.length() > 0) {
            FileLog.d("wasInBackground = " + wasInBackground + " appLocked = " + PlusConfig.appLocked + " autoLockIn = " + PlusConfig.autoLockIn + " lastPauseTime = " + PlusConfig.lastPauseTime + " uptime = " + uptime);
        }
        return PlusConfig.passcodeHash.length() > 0 && wasInBackground &&
                (PlusConfig.appLocked ||
                        PlusConfig.autoLockIn != 0 && PlusConfig.lastPauseTime != 0 && !PlusConfig.appLocked && (PlusConfig.lastPauseTime + PlusConfig.autoLockIn) <= uptime ||
                        uptime + 5 < PlusConfig.lastPauseTime);
    }


    public static String capitalize(String name){
        String capitalizeString = "";
        if(!name.trim().equals("")){
            capitalizeString = name.substring(0,1).toUpperCase() + name.substring(1);
        }
        return capitalizeString;
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

    public static final String TWITTER_PACKAGE= "com.twitter.android";
    public static final String WHATSAPP_PACKAGE= "com.whatsapp";
    public static final String TIK_TALK_PACKAGE= "com.zhiliaoapp.musically";
    public static final String INSTAGRAM_PACKAGE= "com.instagram.android";

    public static String getExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        String ext = null;
        if (idx != -1) {
            ext = fileName.substring(idx + 1);
        }
        if (ext == null) {
            ext = "";
        }
        ext = ext.toLowerCase();
        return ext;
    }
    public static void shareWithApp(int opation,Activity activity,String text,ArrayList<String> paths){
        LogManager.d(String.format("shareWithApp( pack = %s, text = %s, path = %s)",opation,text,paths.toString()));
         PackageManager pm= activity.getPackageManager();
        String appName = "";

        try {

           String pack = "";
           if(opation == 214){
               pack = WHATSAPP_PACKAGE;
               appName = "WhatsApp";
           }else if(opation == 215){
               pack = INSTAGRAM_PACKAGE;
               appName = "Instagram";
           }else if(opation == 216){
               pack = TWITTER_PACKAGE;
               appName = "Twitter";
           }else if(opation == 217){
               pack = TIK_TALK_PACKAGE;
               appName = "TikTok";
           }

           PackageInfo info= pm.getPackageInfo(pack, PackageManager.GET_META_DATA);
           Intent  waIntent = new Intent();

           String type = "";
           ArrayList<Uri> uriArrayList = new ArrayList<>();

           if(paths.size() > 0){
               String ext = getExtension(paths.get(0));
               for (int a = 0; a < paths.size(); a++) {
                   String imageUri = paths.get(a);
                   Uri fileUri = FileProvider.getUriForFile(activity, ApplicationLoader.getApplicationId()  + ".provider", new File(imageUri));
                   uriArrayList.add(fileUri);
//                   if (ext.contains("mp4")) {
//
//                   } else {
//                       uriArrayList.add(Uri.parse(imageUri));
//                   }
               }
               if (ext.contains("mp4")) {
                   type = "video/mp4";
               } else if (ext.contains("jpg") || ext.contains("jpeg") || ext.contains("gif")) {
                   type = "image/*";
               } else {
                   type = "text/plain";
               }
           }

           if(!TextUtils.isEmpty(text) && opation != 217){
               waIntent.putExtra(Intent.EXTRA_TEXT, text);
           }
           if(uriArrayList.size() > 0){
               if(uriArrayList.size() > 1){
                   waIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                   waIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriArrayList);
               }else{
                   waIntent.setAction(Intent.ACTION_SEND);
                   waIntent.putExtra(Intent.EXTRA_STREAM,uriArrayList.get(0));

               }
           }
           waIntent.setType(type);
           waIntent.setPackage(pack);
           waIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
           activity.startActivity(Intent.createChooser(waIntent, "Share with"));
       } catch (PackageManager.NameNotFoundException e) {
           Toast.makeText(ApplicationLoader.applicationContext, appName + " not Installed", Toast.LENGTH_SHORT).show();
       }catch ( Exception e){
          //  LogManager.e(e,true,true,"share.log",e.getMessage());
        }
   }

    public static Bitmap loadBitmapFromUrl(String path, float maxWidth, float maxHeight, boolean useMaxScale) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        InputStream inputStream = null;
        try {
             inputStream = new URL(path).openConnection().getInputStream();
        }catch (Exception ignored){

        }
        if(inputStream == null){
            return null;
        }
        float photoW = bmOptions.outWidth;
        float photoH = bmOptions.outHeight;
        float scaleFactor = useMaxScale ? Math.max(photoW / maxWidth, photoH / maxHeight) : Math.min(photoW / maxWidth, photoH / maxHeight);
        if (scaleFactor < 1) {
            scaleFactor = 1;
        }
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = (int) scaleFactor;
        if (bmOptions.inSampleSize % 2 != 0) {
            int sample = 1;
            while (sample * 2 < bmOptions.inSampleSize) {
                sample *= 2;
            }
            bmOptions.inSampleSize = sample;
        }
        bmOptions.inPurgeable = Build.VERSION.SDK_INT < 21;

        Matrix matrix = null;

        scaleFactor /= bmOptions.inSampleSize;
        if (scaleFactor > 1) {
            matrix = new Matrix();
            matrix.postScale(1.0f / scaleFactor, 1.0f / scaleFactor);
        }

        Bitmap b = null;
        try {
            b = BitmapFactory.decodeStream(inputStream, null,bmOptions);
            if (b != null) {
                if (bmOptions.inPurgeable) {
                    Utilities.pinBitmap(b);
                }
                Bitmap newBitmap = Bitmaps.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                if (newBitmap != b) {
                    b.recycle();
                    b = newBitmap;
                }
            }
        } catch (Throwable e) {
            FileLog.e(e);
            ImageLoader.getInstance().clearMemory();
        }
        return b;
    }

    private static final Hashtable<String, Typeface> typefaceCache = new Hashtable<>();

    public static void showToast(Context context, CharSequence text, int duration) {
//        Toast toast = Toast.makeText(context, text, duration);
//        ViewGroup viewGroup  = (ViewGroup) toast.getView();
//        if(viewGroup != null){
//            viewGroup.getChildAt(0).sett
//        }
//        ((TextView) ((ViewGroup) toast.getView()).getChildAt(0)).s
//        toast.show();
    }


    public static boolean IsValidUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            return URLUtil.isValidUrl(urlString) && Patterns.WEB_URL.matcher(urlString).matches();
        } catch (MalformedURLException ignored) {
        }
        return false;
    }


    private static TextToSpeech textToSpeech;
    private static boolean textToSpeechInit;


    public static final String hulugram_official = "huluchat_official";
    public static final String hulugram_offical_global = "hulugramupdate";

    public static int hulugram_caht_id = 100;


//    public static final int ID_WALLET = 1;
//    public static final int ID_FEED = 2;
//    public static final int ID_MARKETPLACE = 3;
//    public static final int ID_MUSIC = 4;
//    public static final int ID_AIR_TIME = 4;
//    public static final int ID_BILL = 4;
//    public static final int ID_TAXI = 4;
//    public static final int ID_SCAN = 4;




    public static String getApkShareMessage(){
        StringBuilder builder = new StringBuilder();
        builder.append("Check out  'Hulugram'\n");
        builder.append(BuildVars.PLAYSTORE_APP_URL);
        return builder.toString();
    }



    //plus
//    public static void showContactFilter(Context context,BaseFragment baseFragment,Runnable runnable){
//        ArrayList<String> arrayList = new ArrayList<>();
//        ArrayList<String> desc = new ArrayList<>();
//        ArrayList<Integer> types = new ArrayList<>();
//
//        arrayList.add("All");
//        desc.add("All of your contact");
//        types.add(ContactsAdapter.FILTER_ALL);
//
//        arrayList.add("Online");
//        desc.add("contact's that are online right now");
//        types.add(ContactsAdapter.FILTER_ONLINE);
//
//        arrayList.add("Mutual");
//        desc.add("Contacts who have your phone number");
//        types.add(ContactsAdapter.FILTER_MUTUAL);
//
//        arrayList.add("UnMutual");
//        desc.add("Contacts who don't have your phone number");
//        types.add(ContactsAdapter.FILTER_UN_MUTUAL);
//
//        arrayList.add("Blocked");
//        desc.add("Contacts who are blocked by you");
//        types.add(ContactsAdapter.FILTER_BLOCKED);
//
//        arrayList.add("Probably Blocked By");
//        desc.add("Contacts who  may or may not blocked you");
//        types.add(ContactsAdapter.FILTER_BLOCKED_BY);
//
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("Select Filter");
//        final LinearLayout linearLayout = new LinearLayout(context);
//        linearLayout.setOrientation(LinearLayout.VERTICAL);
//        builder.setView(linearLayout);
//
//        for (int a = 0; a < arrayList.size(); a++) {
//            RadioButtonCell cell = new RadioButtonCell(context);
//            cell.setPadding(AndroidUtilities.dp(4), 0, AndroidUtilities.dp(4), 0);
//            cell.setTag(a);
//            cell.setTextAndValue(arrayList.get(a),desc.get(a),false,SharedConfig.contactFilterType == types.get(a));
//            linearLayout.addView(cell);
//            cell.setOnClickListener(v -> {
//                Integer which = (Integer) v.getTag();
//                SharedConfig.setContactFilterType(types.get(which));
//
//                builder.getDismissRunnable().run();
//                if(runnable != null){
//                    runnable.run();
//                }
//            });
//        }
//        baseFragment.showDialog(builder.create());
//    }
    //

    public static boolean isUserProbBlockedBy(TLRPC.User user){
        if (user == null || user.self) {
            return false;
        }
        return user.status == null || user.status.expires == 0 || user instanceof TLRPC.TL_userEmpty;
    }


    public static boolean isOnline(TLRPC.User user,int currentAccount) {
        if (user == null || user.self) {
            return false;
        }
        if (user.status != null && user.status.expires <= 0) {
            if (MessagesController.getInstance(currentAccount).onlinePrivacy.containsKey(user.id)) {
                return true;
            }
        }
        if (user.status != null && user.status.expires > ConnectionsManager.getInstance(currentAccount).getCurrentTime()) {
            return true;
        }
        return false;
    }
//
//    public static void loadMiniApps(){
//        Utilities.stageQueue.postRunnable(new Runnable() {
//            @Override
//            public void run() {
//                AndroidUtilities.runOnUIThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didMiniAppsLoaded,getDefuaultMiniApps());
//                    }
//                });
//            }
//        });
//    }

//
//    public static ArrayList<MiniAppsManageFragment.MiniApp> getMiniApps(){
//        SharedPreferences sharedPreferences = PlusConfig.getPlusSharedPref();
//        String miniApps = sharedPreferences.getString("miniAppss", "");
//        if(TextUtils.isEmpty(miniApps)){
//            return  HulugramUtils.getDefuaultMiniApps();
//        }else{
//             ArrayList<MiniAppsManageFragment.MiniApp> miniAppArrayListfinal = new ArrayList<>();
//            String[] apps = miniApps.split("||");
//            for (int a = 0; a < apps.length; a++) {
//                String app = apps[a];
//                String[] sub = app.split("_");
//                MiniAppsManageFragment.MiniApp miniApp = new MiniAppsManageFragment.MiniApp();
//                if (sub.length == 3) {
//                    int id = Integer.parseInt(sub[0]);
//                    int pos = Integer.parseInt(sub[1]);
//                    int enabled = Integer.parseInt(sub[2]);
//                    miniApp.enabled = enabled == 1;
//                    miniApp.order = pos;
//                    miniApp.id = id;
//                    if (id == HulugramUtils.ID_WALLET) {
//                        miniApp.title = LocaleController.getString("Wallet", R.string.Wallet);
//                        miniApp.icon = R.drawable.wallet_mini;
//                    } else if (id == HulugramUtils.ID_FEED) {
//                        miniApp.title = LocaleController.getString("HUluFeed", R.string.HUluFeed);
//                        miniApp.icon = R.drawable.feed_small;
//                    } else if (id == HulugramUtils.ID_MARKETPLACE) {
//                        miniApp.title = LocaleController.getString("Marketplace", R.string.Marketplace);
//                        miniApp.icon = R.drawable.feed_small;
//                    } else if (id == HulugramUtils.ID_MUSIC) {
//                        miniApp.title = LocaleController.getString("AttachMusic", R.string.AttachMusic);
//                        miniApp.icon = R.drawable.music_small;
//                    }
//                }
//                Collections.sort(miniAppArrayListfinal, new Comparator<MiniAppsManageFragment.MiniApp>() {
//                    @Override
//                    public int compare(MiniAppsManageFragment.MiniApp miniApp, MiniAppsManageFragment.MiniApp t1) {
//                        return Integer.compare(miniApp.order,t1.order);
//                    }
//                });
//                miniAppArrayListfinal.add(miniApp);
//            }
//            return miniAppArrayListfinal;
//        }
//    }
//
//    public static ArrayList<MiniAppsManageFragment.MiniApp> getDefuaultMiniApps(){
//        ArrayList<MiniAppsManageFragment.MiniApp> miniAppArrayList = new ArrayList<>();
//        int order = 0;
//        int color = Theme.getColor(Theme.key_chats_archivePinBackground);
//
//        MiniAppsManageFragment.MiniApp miniApp = new MiniAppsManageFragment.MiniApp();
//        miniApp.id = ID_WALLET;
//        miniApp.enabled = true;
//        miniApp.title = LocaleController.getString("Wallet",R.string.Wallet);
//        miniApp.order = order++;
//        miniApp.icon  = R.drawable.wallet_mini;
////        if(PlusUtilities.isSelfSupported()){
////            miniAppArrayList.add(miniApp);
////        }
//
////        Theme.getColor(Theme.key_avatar_backgroundGreen);
//
//       // int color   = Theme.getColor(Theme.key_avatar_backgroundGreen);
//        miniApp = new MiniAppsManageFragment.MiniApp();
//        miniApp.id = ID_MARKETPLACE;
//        miniApp.enabled = true;
//        miniApp.order = order++;
//        miniApp.title = LocaleController.getString("Marketplace",R.string.Marketplace);
//        miniApp.icon  = R.drawable.store_small;
//        miniApp.color = color;
//        if(PlusUtilities.isSelfSupported()){
//            miniAppArrayList.add(miniApp);
//        }
//
//        miniApp = new MiniAppsManageFragment.MiniApp();
//        miniApp.id = ID_FEED;
//        miniApp.enabled = true;
//        miniApp.order = order++;
//        miniApp.title = LocaleController.getString("HUluFeed",R.string.HUluFeed);
//        miniApp.icon  = R.drawable.feed_small;
//        miniApp.color = color;
//        miniAppArrayList.add(miniApp);
//
//        miniApp = new MiniAppsManageFragment.MiniApp();
//        miniApp.id = ID_MUSIC;
//        miniApp.enabled = true;
//        miniApp.order = order++;
//        miniApp.title = LocaleController.getString("AttachMusic",R.string.AttachMusic);
//        miniApp.color = color;
//        miniApp.icon  = R.drawable.music_small;
//        miniAppArrayList.add(miniApp);
//
//
//        return miniAppArrayList;
//
//    }
//


    public static void openTelegram(Activity activity){
        if(activity == null){
            return;
        }
        Intent intent = ApplicationLoader.applicationContext.getPackageManager().getLaunchIntentForPackage("org.telegram.messenger");
        if (intent != null) {
            activity.startActivity(intent);
        } else {
            // Telegram app is not installed
            // You can redirect the user to the Play Store to download Telegram
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=org.telegram.messenger"));
            activity.startActivity(intent);
        }
    }

    public static Typeface getRoboBoldTypeface(){
        return AndroidUtilities.getTypeface("fonts/roboto_bold.ttf");
    }

    public static Typeface getRobotoMediumTypeface(){
        return AndroidUtilities.getTypeface("fonts/rmedium.ttf");
    }

    public static Typeface getRobotoItalicTypeface() {
        return AndroidUtilities.getTypeface("fonts/ritalic.ttf");
    }


    public static  void showToast(Context context, TLRPC.User user,String text){
        Toast toast = Toast.makeText(ApplicationLoader.applicationContext, text , Toast.LENGTH_LONG);
        if(PlusConfig.notification_toastNotiOnBottom){
            toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
        }else{
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
        }
        toast.setMargin(0,AndroidUtilities.dp(14));
        FrameLayout frameLayout = new FrameLayout(context);
        BackupImageView imageView = new BackupImageView(context);
        AvatarDrawable avatarDrawable = new AvatarDrawable(user);
        avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
        imageView.setRoundRadius(AndroidUtilities.dp(10));
        imageView.setImage(ImageLocation.getForUser(user, ImageLocation.TYPE_SMALL), "20_20", avatarDrawable, user);
        frameLayout.addView(imageView,LayoutHelper.createFrame(20,20,Gravity.LEFT|Gravity.TOP,8,4,8,8));

        TextView nameTextView = new TextView(context);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        nameTextView.setLines(1);
        nameTextView.setTextColor(Color.WHITE);
        nameTextView.setText(text );
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setGravity(Gravity.LEFT);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);

        frameLayout.addView(nameTextView,LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.TOP, 28 + 8,4,8,8));
        frameLayout.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(12), AppUtils.getTransparentColor(Color.BLACK,0.5f)));
        toast.setView(frameLayout);
        toast.show();
    }

    public static void triggerRebirth(Context context) {
        Intent mStartActivity = new Intent(context, LaunchActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE);
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }



    public static void showIncomingNotification(){

    }

    public static void parseActionNotification(JSONObject extraObject,long sentTime,long receiveTime,boolean notify){
        try {
            long to_user_id = extraObject.getLong("telegramID");
            int currentAccount;
            int account = UserConfig.selectedAccount;
            for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                if (UserConfig.getInstance(a).getClientUserId() == to_user_id) {
                    account = a;
                    break;
                }
            }
            final int accountFinal = currentAccount = account;
            if (!UserConfig.getInstance(currentAccount).isClientActivated()) {
                return;
            }
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if(notify){
                        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didReceivedPushNotification,extraObject.toString());
                    }
                    try {
                       // NotificationDataManager.getInstance(currentAccount).saveNotification(extraObject.getString("action"),extraObject,sentTime,receiveTime,accountFinal);
                    }catch (Exception ignore){

                    }


                }
            },1000);
        }catch (Exception e){

        }

    }


    private static  Bitmap getRoundAvatarBitmap(TLObject userOrChat) {
        Bitmap bitmap = null;
        try {
            if (userOrChat instanceof TLRPC.User) {
                TLRPC.User user = (TLRPC.User) userOrChat;
                if (user.photo != null && user.photo.photo_small != null) {
                    BitmapDrawable img = ImageLoader.getInstance().getImageFromMemory(user.photo.photo_small, null, "50_50");
                    if (img != null) {
                        bitmap = img.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
                    } else {
                        try {
                            BitmapFactory.Options opts = new BitmapFactory.Options();
                            opts.inMutable = true;
                            bitmap = BitmapFactory.decodeFile(FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(user.photo.photo_small, true).toString(), opts);
                        } catch (Throwable e) {
                            FileLog.e(e);
                        }
                    }
                }
            } else {
                TLRPC.Chat chat = (TLRPC.Chat) userOrChat;
                if (chat.photo != null && chat.photo.photo_small != null) {
                    BitmapDrawable img = ImageLoader.getInstance().getImageFromMemory(chat.photo.photo_small, null, "50_50");
                    if (img != null) {
                        bitmap = img.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
                    } else {
                        try {
                            BitmapFactory.Options opts = new BitmapFactory.Options();
                            opts.inMutable = true;
                            bitmap = BitmapFactory.decodeFile(FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(chat.photo.photo_small, true).toString(), opts);
                        } catch (Throwable e) {
                            FileLog.e(e);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
        if (bitmap == null) {
            Theme.createDialogsResources(ApplicationLoader.applicationContext);
            AvatarDrawable placeholder;
            if (userOrChat instanceof TLRPC.User) {
                placeholder = new AvatarDrawable((TLRPC.User) userOrChat);
            } else {
                placeholder = new AvatarDrawable((TLRPC.Chat) userOrChat);
            }
            bitmap = Bitmap.createBitmap(AndroidUtilities.dp(42), AndroidUtilities.dp(42), Bitmap.Config.ARGB_8888);
            placeholder.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            placeholder.draw(new Canvas(bitmap));
        }

        Canvas canvas = new Canvas(bitmap);
        Path circlePath = new Path();
        circlePath.addCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, Path.Direction.CW);
        circlePath.toggleInverseFillType();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPath(circlePath, paint);
        return bitmap;
    }

    public static void sendRxnToUser(AccountInstance accountInstance, long user_id, String message,String path){
        ArrayList<SendMessagesHelper.SendingMediaInfo> photos = new ArrayList<>();
        SendMessagesHelper.SendingMediaInfo info = new SendMessagesHelper.SendingMediaInfo();
        info.path = path;
        info.isVideo = false;
        info.entities = null;
        info.masks = null;
        info.ttl = 0;
        info.thumbPath = null;
        info.canDeleteAfter = false;
        photos.add(info);
        photos.get(0).caption = message;
        photos.get(0).entities = null;

        SendMessagesHelper.prepareSendingMedia(accountInstance, photos, user_id, null, null, null, false, true, null, true, 0,false,null);

    }

    public static File getPathFromBitmap(Bitmap bitmap){
        if(bitmap == null){
            return null;
        }
        File imagePath = AndroidUtilities.generatePicturePath();
        try {
            FileOutputStream out = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imagePath;
    }


    public static String formatStoryLink(long user_id){
        return "tg://open?story=" + user_id;
    }

    public static void checkQrCode(BaseFragment baseFragment,String text){
        if(baseFragment == null){
            return;
        }
        try {
            Long shop_id =null;
            Integer product_id = null;
            Uri data = Uri.parse(text);
            String url = data.toString();
            if(url.startsWith("tg://open")){
                data = Uri.parse(url);
                shop_id = Utilities.parseLong(data.getQueryParameter("shop_id"));
                if(url.contains("product_id")){
                    product_id = Utilities.parseInt(data.getQueryParameter("product_id"));
                }
                if(product_id == null){
                    Bundle bundle = new Bundle();
                    bundle.putLong("chat_id",shop_id);
                    baseFragment.presentFragment(new BusinessProfileActivity(bundle));
                }else{
                    Bundle bundle = new Bundle();
                    bundle.putLong("chat_id", shop_id);
                    bundle.putInt("item_id",product_id);
                    ProductDetailActivity detailFragment = new ProductDetailActivity(bundle);
                    baseFragment.presentFragment(detailFragment);
                }

            }else if(url.startsWith("hg://open")){
                data = Uri.parse(url);
                if(url.contains("user")){
                    String username = data.getQueryParameter("user");
                    baseFragment.getMessagesController().openByUserName(username, baseFragment,0);
                }
            }
        }catch (Exception e){

        }
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


    public static boolean isLight(int color) {
        int red   = Color.red(color);
        int green = Color.green(color);
        int blue  = Color.blue(color);

        float[] hsl = new float[3];
        ColorUtils.RGBToHSL(red, green, blue, hsl);
        return hsl[2] >= 0.5f;
    }
    public static void init(){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("default_config4",MODE_PRIVATE);
        if( preferences.getBoolean("first_time",true)){
            SharedPreferences pref = MessagesController.getGlobalMainSettings();
            pref.edit().putString("theme", null).commit();
            preferences.edit().putBoolean("first_time",false).commit();
            SharedPreferences themeConfig = ApplicationLoader.applicationContext.getSharedPreferences("themeconfig", MODE_PRIVATE);
            themeConfig.edit().putString("lastDayTheme", null).commit();
        }
    }

    public static void copyPartOfText(BaseFragment baseFragment, StringBuilder message) {
        if (baseFragment == null || baseFragment.getParentActivity() == null) {
            return;
        }

        Context context = baseFragment.getParentActivity();

        BottomSheet.Builder builder = new BottomSheet.Builder(context);
        FrameLayout frameLayout = new FrameLayout(context);

        TextView titleView = new TextView(context);
        titleView.setText(LocaleController.getString("CopyPieceOfText", R.string.CopyPieceOfText));
        titleView.setTextSize(16);
        titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlue2));
        frameLayout.addView(titleView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP,20,5,0,0));

        TextView textView = new TextView(context);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView.setTextIsSelectable(true);
        textView.setTextSize(16);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setTextIsSelectable(true);
        textView.setPadding(16,16,16,16);
        textView.setText(message);

        TextView closeview = new TextView(context);
        closeview.setPadding(AndroidUtilities.dp(15),AndroidUtilities.dp(15),AndroidUtilities.dp(15),AndroidUtilities.dp(15));
        closeview.setTextSize(16);
        closeview.setBackgroundDrawable(Theme.createSimpleSelectorRoundRectDrawable(0,0,Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon)));
        closeview.setTextColor(Theme.getColor(Theme.key_dialogTextBlue2));
        closeview.setText(LocaleController.getString("Close",R.string.Close));
        closeview.setOnClickListener(v -> {
            builder.getDismissRunnable().run();
        });

        frameLayout.addView(textView,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.TOP,20,30,20,50));
        frameLayout.addView(closeview,LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT,LayoutHelper.WRAP_CONTENT,Gravity.BOTTOM|Gravity.CENTER,0,0,0,0));

        builder.setCustomView(frameLayout);
        baseFragment.showDialog(builder.create());
    }


    public static void joinToCommunity(BaseFragment fragment, ArrayList<TLRPC.Chat> chats,Runnable runnable) {
        if (fragment == null || fragment.getParentActivity() == null || chats == null || chats.isEmpty()){
            return;
        }
        if(true){
            showChannelJoinAlert(fragment,chats,runnable);
            return;
        }
        final int account = fragment.getCurrentAccount();
        final Context context = fragment.getParentActivity();
        final FrameLayout frameLayout = new FrameLayout(context);

        final String title;
        final String message;
        title = "Do you want to follow Hulugram news?";
        message = "Then join to our channel!";

        TextView messageTextView = new TextView(context);
        messageTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        messageTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
        messageTextView.setText(AndroidUtilities.replaceTags(message));

        BackupImageView imageView = new BackupImageView(context);
        imageView.setRoundRadius(AndroidUtilities.dp(20));
        imageView.setImageResource(R.mipmap.ic_launcher);
        frameLayout.addView(imageView, LayoutHelper.createFrame(40, 40, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 22, 5, 22, 0));

        TextView textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuItem));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView.setLines(1);
        textView.setMaxLines(1);
        textView.setSingleLine(true);
        textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setText(title);
        frameLayout.addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 21 : 76), 11, (LocaleController.isRTL ? 76 : 21), 0));
        frameLayout.addView(messageTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 24, 57, 24, 9));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        boolean[] checked = new boolean[chats.size()];
        Arrays.fill(checked, true);
        for(int a = 0 ; a < chats.size();a++){
            CheckBoxChatCell checkBoxCell = new CheckBoxChatCell(context,true);
            checkBoxCell.setUser(chats.get(a),checked[a],false);
            checkBoxCell.setTag(a);
            checkBoxCell.setBackground(Theme.getSelectorDrawable(true));
            checkBoxCell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   int pos = (int)v.getTag();
                    checked[pos] = !checked[pos];
                    ((CheckBoxChatCell)v).setChecked( checked[pos],true);
                }
            });
            linearLayout.addView(checkBoxCell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));
        }
        CheckBoxCell dontAskCheckBox = new CheckBoxCell(context,3);
        dontAskCheckBox.setText("Don't ask again!","",false,false);
        linearLayout.addView(dontAskCheckBox,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT,21,0,0,0));

        dontAskCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CheckBoxCell)v).setChecked(!dontAskCheckBox.isChecked(),true);
                PlusConfig.dontAskToJoinCom =dontAskCheckBox.isChecked();
                PlusConfig.editor.putBoolean("dontAskToJoinCom",PlusConfig.dontAskToJoinCom).commit();
                SharedConfig.saveConfig();

            }
        });

        frameLayout.addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 0, 57 + 40 + 8, 0, 9));


        AlertDialog dialog = new AlertDialog.Builder(context).setView(frameLayout)
                .setPositiveButton(LocaleController.getString("ChannelJoin", R.string.ChannelJoin), (dialogInterface, i) -> {

                    for(int a = 0 ; a < chats.size(); a++){
                        if(!checked[a])continue;
                         TLRPC.Chat chat = chats.get(a);
                         TLRPC.TL_channels_joinChannel req = new TLRPC.TL_channels_joinChannel();
                         req.channel = MessagesController.getInstance(account).getInputChannel(chat.id);
                        ConnectionsManager.getInstance(account).sendRequest(req, new RequestDelegate() {
                        @Override
                        public void run(TLObject response, TLRPC.TL_error error) {

                        }
                    });
                    }


                })
                .setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PlusConfig.askToJoinCount++;
                        PlusConfig.editor.putInt("askToJoinCount",PlusConfig.askToJoinCount).apply();
                    }
                })
                .create();
        fragment.showDialog(dialog);
    }

    public static void showChannelJoinAlert(BaseFragment baseFragment, ArrayList<TLRPC.Chat> chats,Runnable runnable){
        if(baseFragment == null || baseFragment.getParentActivity() == null){
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(baseFragment.getParentActivity());
        builder.setTopAnimation(R.raw.wallet_congrats, Theme.getColor(Theme.key_dialogBackground));
        builder.setTitle("Follow Hulugram");
        builder.setMessage("Join our channel to get latest news and update!");
        Runnable dismissRunnble = builder.getDismissRunnable();

        LinearLayout linearLayout = new LinearLayout(baseFragment.getParentActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        boolean[] checked = new boolean[chats.size()];
        Arrays.fill(checked, true);
        for(int a = 0 ; a < chats.size();a++){
            CheckBoxChatCell checkBoxCell = new CheckBoxChatCell(baseFragment.getParentActivity(),true);
            checkBoxCell.setUser(chats.get(a),checked[a],false);
            checkBoxCell.setTag(a);
            checkBoxCell.setOnClickListener(v -> {
                int pos = (int)v.getTag();
                 checked[pos] = !checked[pos];
                ((CheckBoxChatCell)v).setChecked( checked[pos],true);
            });
            linearLayout.addView(checkBoxCell,LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT,LayoutHelper.WRAP_CONTENT,Gravity.CENTER_HORIZONTAL));
        }

        CheckBoxCell dontAskCheckBox = new CheckBoxCell(baseFragment.getParentActivity(),3);
        dontAskCheckBox.setText("Don't ask again!","",false,false);
        dontAskCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CheckBoxCell)v).setChecked(!dontAskCheckBox.isChecked(),true);
                PlusConfig.dontAskToJoinCom =dontAskCheckBox.isChecked();
                PlusConfig.editor.putBoolean("dontAskToJoinCom",PlusConfig.dontAskToJoinCom).commit();

          }});
        linearLayout.addView(dontAskCheckBox,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.CENTER_VERTICAL,21,0,0,0));

        ButtonCell buttonCell = new ButtonCell(baseFragment.getParentActivity());
        buttonCell.setText("Join".toUpperCase());
        linearLayout.addView(buttonCell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.CENTER,0,0,0,0));
        buttonCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int a = 0 ; a < chats.size(); a++){
                    if(!checked[a])continue;
                    TLRPC.Chat chat = chats.get(a);
                    TLRPC.TL_channels_joinChannel req = new TLRPC.TL_channels_joinChannel();
                    req.channel = MessagesController.getInstance(UserConfig.selectedAccount).getInputChannel(chat.id);
                    ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, new RequestDelegate() {
                        @Override
                        public void run(TLObject response, TLRPC.TL_error error) {


                        }
                    });
                }
                dismissRunnble.run();
            }
        });

        builder.setView(linearLayout);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                PlusConfig.dontAskToJoinCom = dontAskCheckBox.isChecked();
                PlusConfig.editor.putBoolean("dontAskToJoinCom",PlusConfig.dontAskToJoinCom).commit();
                runnable.run();
            }
        });
        baseFragment.showDialog(builder.create());
    }


    public static void showShareApp(BaseFragment baseFragment){
        if(baseFragment == null || baseFragment.getParentActivity() == null){
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(baseFragment.getParentActivity());
        builder.setTopImage(R.mipmap.ic_launcher_round,0);
        builder.setTitle("Share Hulugram!");
        builder.setMessage("");
        Runnable dismissRunnble = builder.getDismissRunnable();

        LinearLayout linearLayout = new LinearLayout(baseFragment.getParentActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        CheckBoxCell dontAskCheckBox = new CheckBoxCell(baseFragment.getParentActivity(),3);
        dontAskCheckBox.setText("Don't ask again!","",false,false);
        dontAskCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CheckBoxCell)v).setChecked(!dontAskCheckBox.isChecked(),true);
                PlusConfig.dontAskToShareApp =dontAskCheckBox.isChecked();
                PlusConfig.editor.putBoolean("dontAskToShareApp",PlusConfig.dontAskToShareApp).commit();

            }});
        linearLayout.addView(dontAskCheckBox,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.CENTER_VERTICAL,21,0,0,0));

        ButtonCell buttonCell = new ButtonCell(baseFragment.getParentActivity());
        buttonCell.setText("Share".toUpperCase());
        linearLayout.addView(buttonCell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.CENTER,0,0,0,0));
        buttonCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 ShareAlert shareAlert =   ShareAlert.createShareAlert(baseFragment.getContext(),null, BuildVars.PLAYSTORE_APP_URL,false, BuildVars.PLAYSTORE_APP_URL,false);
                baseFragment.showDialog(shareAlert);
                dismissRunnble.run();
            }
        });

        builder.setView(linearLayout);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                PlusConfig.dontAskToShareApp = dontAskCheckBox.isChecked();
                PlusConfig.editor.putBoolean("dontAskToShareApp",PlusConfig.dontAskToShareApp).commit();
            }
        });
        baseFragment.showDialog(builder.create());
    }


    public static void showComingSoonAlert(BaseFragment baseFragment,String title,String desc,String button){
        if(baseFragment == null || baseFragment.getParentActivity() == null){
            return;
        }
        Context context = baseFragment.getParentActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        Runnable runnable1 = builder.getDismissRunnable();
        FrameLayout frameLayout = new FrameLayout(context){
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.getSize(AndroidUtilities.dp(240 + 80)));
            }
        };
        builder.setView(frameLayout);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        frameLayout.addView(linearLayout,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT));

        AnimatedInfoView animatedInfoView = new AnimatedInfoView(context,null,AnimatedInfoView.STICKER_TYPE_STAR);
        animatedInfoView.title.setText(title);
        animatedInfoView.subtitle.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        animatedInfoView.subtitle.setText(desc);
        linearLayout.addView(animatedInfoView,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.CENTER_HORIZONTAL));

        TextView  nameTextView = new TextView(context);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setText("⭐⭐⭐⭐⭐");
        nameTextView.setGravity(Gravity.CENTER);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        linearLayout.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 16));

        TextView textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_passport_authorizeText));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        textView.setGravity(Gravity.CENTER);
        textView.setMaxLines(1);
        textView.setText(button);
        textView.setSingleLine();
        textView.setBackgroundDrawable(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(32),Theme.getColor(Theme.key_passport_authorizeBackground), Theme.getColor(Theme.key_passport_authorizeBackgroundSelected)));
        textView.setPadding(AndroidUtilities.dp(0),AndroidUtilities.dp(16),AndroidUtilities.dp(0),AndroidUtilities.dp(16));
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER,8,8,8,8));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        AlertDialog alertDialog = builder.create();
        baseFragment.showDialog(alertDialog);
    }

    public static void showNotSupportedYetAlert(BaseFragment baseFragment){
        if(baseFragment == null || baseFragment.getParentActivity() == null){
            return;
        }
        Context context = baseFragment.getParentActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        Runnable runnable1 = builder.getDismissRunnable();
        FrameLayout frameLayout = new FrameLayout(context){
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.getSize(AndroidUtilities.dp(240 + 80)));
            }
        };
        builder.setView(frameLayout);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        frameLayout.addView(linearLayout,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT));

        AnimatedInfoView animatedInfoView = new AnimatedInfoView(context,null,AnimatedInfoView.STICKER_TYPE_NOT_FOUND);
        animatedInfoView.title.setText("Not Supported!");
        animatedInfoView.subtitle.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        animatedInfoView.subtitle.setText("Your location is not supported yet!");
        linearLayout.addView(animatedInfoView,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.CENTER_HORIZONTAL));

        TextView  nameTextView = new TextView(context);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setText("");
        nameTextView.setGravity(Gravity.CENTER);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        linearLayout.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 16));

        TextView textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_passport_authorizeText));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        textView.setGravity(Gravity.CENTER);
        textView.setMaxLines(1);
        textView.setText("Continue".toUpperCase());
        textView.setSingleLine();
        textView.setBackgroundDrawable(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(32),Theme.getColor(Theme.key_passport_authorizeBackground), Theme.getColor(Theme.key_passport_authorizeBackgroundSelected)));
        textView.setPadding(AndroidUtilities.dp(0),AndroidUtilities.dp(16),AndroidUtilities.dp(0),AndroidUtilities.dp(16));
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER,8,8,8,8));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runnable1.run();
            }
        });

        AlertDialog alertDialog = builder.create();
        baseFragment.showDialog(alertDialog);
    }




    private static SparseArray<AnimatorSet> animatorSetSparseArray = new SparseArray<>();

    public static void  animateViewsVisibility(ArrayList<View> views,boolean show){
        animateViewsVisibility(views,show,220,0.0f,0.0f,0.0f);
    }

    public static void  animateViewsVisibility(View view,boolean show){
        ArrayList<View> views = new ArrayList<>();
        views.add(view);
        animateViewsVisibility(views,show,220,0.0f,0.0f,0.0f);
    }

    public static void animateViewsVisibility(ArrayList<View> views,boolean show,int duration,float fromAlpha,float fromScaleX,float fromScaleY){
        if(views == null || views.size() == 0){
            return;
        }
        ArrayList<View> finalViews = new ArrayList<>();
        for(View v:views){
            if(show && v.getTag() != null || !show && v.getTag() == null){
                v.setTag(show?null:1);
                if(show){
                    v.setVisibility(View.VISIBLE);
                }
                finalViews.add(v);
            }
        }
        if(finalViews.isEmpty()){
            return;
        }

        final AnimatorSet[] shadowAnimation = {new AnimatorSet()};
        for(View v:finalViews){
            shadowAnimation[0].playTogether(ObjectAnimator.ofFloat(v, View.ALPHA, show ? 1.0f : fromAlpha));
            shadowAnimation[0].playTogether(ObjectAnimator.ofFloat(v, View.SCALE_Y, show ? 1.0f : fromScaleY));
            shadowAnimation[0].playTogether(ObjectAnimator.ofFloat(v, View.SCALE_X, show ? 1.0f : fromScaleX));
        }
        shadowAnimation[0].setDuration(duration);
        shadowAnimation[0].addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (shadowAnimation[0] != null && shadowAnimation[0].equals(animation)) {
                    if (!show) {
                        for(View v:views){
                            v.setVisibility(View.INVISIBLE);
                        }
                    }
                    shadowAnimation[0] = null;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (shadowAnimation[0] != null && shadowAnimation[0].equals(animation)) {
                    shadowAnimation[0] = null;
                }
            }
        });
        shadowAnimation[0].start();
    }

    public static void animateAlpha(ArrayList<View> views,boolean show,int duration,float fromAlpha,Runnable onAnimationEnd){
        if(views == null || views.size() == 0){
            return;
        }
        ArrayList<View> finalViews = new ArrayList<>();
        for(View v:views){
            if(show && v.getTag() != null || !show && v.getTag() == null){
                v.setTag(show?null:1);
                if(show){
                    v.setVisibility(View.VISIBLE);
                }
                finalViews.add(v);
            }
        }
        if(finalViews.isEmpty()){
            return;
        }

        final AnimatorSet[] shadowAnimation = {new AnimatorSet()};
        for(View v:finalViews){
            shadowAnimation[0].playTogether(ObjectAnimator.ofFloat(v, View.ALPHA, show ? 1.0f : fromAlpha));
        }
        shadowAnimation[0].setDuration(duration);
        shadowAnimation[0].addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (shadowAnimation[0] != null && shadowAnimation[0].equals(animation)) {
                    if (!show) {
                        for(View v:views){
                            v.setVisibility(View.INVISIBLE);
                        }
                    }
                    shadowAnimation[0] = null;
                }
                onAnimationEnd.run();

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (shadowAnimation[0] != null && shadowAnimation[0].equals(animation)) {
                    shadowAnimation[0] = null;
                }
                onAnimationEnd.run();
            }
        });

        shadowAnimation[0].start();
    }


    public static void showTabSettingAlert(BaseFragment baseFragment){
        if(baseFragment == null || baseFragment.getParentActivity() == null){
            return;
        }
        Context context = baseFragment.getParentActivity();
        //tab mode, tab size,


    }
    public static void showSideMenuSettingAlert(BaseFragment baseFragment){

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static AlertDialog.Builder createReadNotificationPermissionDialog(Activity activity,Runnable onContinueCalled, Runnable cancelRunnable, Theme.ResourcesProvider resourcesProvider) {
        if (activity == null || Build.VERSION.SDK_INT < 29) {
            return null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, resourcesProvider);
        builder.setTitle("Notification Permission");
        float aspectRatio = 354f / 936f;
        builder.setTopViewAspectRatio(aspectRatio);
        builder.setMessage(AndroidUtilities.replaceTags(LocaleController.getString(R.string.PermissionReadNotifications)));
        builder.setPositiveButton(LocaleController.getString(R.string.Continue), (dialog, which) -> {
            if (!isNotificationServiceEnabled()) {
                onContinueCalled.run();
                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                activity.startActivity(intent);

                return;
            }
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), ((dialog, which) -> cancelRunnable.run()));
        return builder;
    }

    //plus
    private static boolean isNotificationServiceEnabled() {
        String packageName =ApplicationLoader.applicationContext.getPackageName();
        String flat = Settings.Secure.getString(ApplicationLoader.applicationContext.getContentResolver(), "enabled_notification_listeners");
        if (flat != null) {
            return flat.contains(packageName);
        }
        return false;
    }

    public static String getTransactionText(ServicesModel.BaseTransaction transaction){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Transaction Type: ").append("Airtime").append("\n");
        stringBuilder.append("Transaction Amount: ").append(transaction.amount).append("\n");
        stringBuilder.append("Transaction Created: ").append(ShopUtils.formatFullData(transaction.createdAt)).append("\n");
        stringBuilder.append("Transaction Committed: ").append(ShopUtils.formatFullData(transaction.updatedAt)).append("\n");
        stringBuilder.append("Transaction Status: ").append(transaction.status_text).append("\n");
        if(!ShopUtils.isEmpty(transaction.phone_number)){
            stringBuilder.append("Recipient PhoneNumber: ").append(transaction.phone_number).append("\n");
        }

        if(transaction instanceof ServicesModel.AirTimeTransaction){
            ServicesModel.AirTimeTransaction airTimeTransaction = (ServicesModel.AirTimeTransaction) transaction;
            if(airTimeTransaction.receiverUser != null) {
                String value = ContactsController.formatName(airTimeTransaction.tgUser.first_name,airTimeTransaction.tgUser.last_name);
                stringBuilder.append("Transaction sent to:").append(value).append("\n");
            }
        }else if(transaction instanceof ServicesModel.DSTVTransactions){
            ServicesModel.DSTVTransactions dstvTransactions = (ServicesModel.DSTVTransactions) transaction;
            stringBuilder.append("Smart Card Number: ").append(dstvTransactions.smart_card_number).append("\n");
            stringBuilder.append("Months: ").append(dstvTransactions.months).append("\n");
            stringBuilder.append("Package : ").append(dstvTransactions._package).append("\n");
        }else if(transaction instanceof ServicesModel.AirlineTransactions){
            ServicesModel.AirlineTransactions airlineTransactions = (ServicesModel.AirlineTransactions) transaction;
            stringBuilder.append("Reservation Number: ").append(airlineTransactions.reservation_number).append("\n");

        }else if(transaction instanceof ServicesModel.CanalPLusTransactions){
            ServicesModel.CanalPLusTransactions canalPLusTransactions = (ServicesModel.CanalPLusTransactions) transaction;
            stringBuilder.append("Months: ").append(canalPLusTransactions.months).append("\n");
            stringBuilder.append("Card Renewal Number: ").append(canalPLusTransactions.card_renewal_number).append("\n");
        }
        stringBuilder.append("Transaction Link: ").append(ServicesDataController.getTransLink(transaction)).append("\n");
        return stringBuilder.toString();
    }

}
