package plus.utils;

import android.graphics.Color;
import android.text.TextUtils;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.checkerframework.checker.units.qual.A;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Adapters.DrawerLayoutAdapter;

import java.util.ArrayList;

public class AppUtilities {

    public static final int MARKETPLACE = 1;
    public static final int HULU_TAXI = 2;
    public static final int SCAN = 3;
    public static final int WALLET = 4;
    public static final int FEED = 5;
    public static final int NEW_CHAT = 6;
    public static final int MUSIC =  7;
    public static final int GAMES =  8;
    public static final int AIRTIME = 9;
    public static final int BILL = 10;
    public static final int DONATION = 11;
    public static final int PLUS_APP_ALL= 12;
    public static final int SAVED_MESSAGE= 13;
    public static final int CLEAR_CACHE= 14;
    public static final int ONLINE_USERS= 15;
    public static final int PLUS_APP_NOTIFICATIONS= 16;
    public static final int PEOPLE_NEAR_BY =  17;
    public static final int LIVE_SCORE =  18;
    public static final int STATUS =  19;
    public static final int BOOKMARK =  20;
    public static final int HIDDEN_CHAT= 21;
    public static final int DRAFT= 22;

    public static final int SEND_MONEY =  23;
    public static final int THEME =  24;
    public static final int USSD =  245;
    public static final int CHATGPT =  246;



    public static class PlusApp {

        public String name;
        public int id;
        public int res;
        public int color;
        public boolean animated;
        public boolean standAloneImage;



        public PlusApp(String name, int id, int res, int color) {
            this.name = name;
            this.id = id;
            this.res = res;
            this.color = color;
        }
    }
    public static  ArrayList<PlusApp> apps = new ArrayList<>();
    public static  ArrayList<PlusApp> features = new ArrayList<>();

    public static boolean appsLoaded;
    static {
        loadApps();
    }
    public static void loadApps(){
        if(appsLoaded){
            return;
        }
        apps = getPlusApps();
        features =getFeatureApps();
        appsLoaded =true;
    }

    public static final int COLOR_MARKETPLACE= 0xffF44336;
    public static final int COLOR_AIRTIME = 0xfffbc02d;
    public static final int COLOR_SCAN = 0xff465775;
    public static final int COLOR_DONATION = 0xff50C878;
    public static final int COLOR_BILL = 0xff2196f3;
    public static final int COLOR_FEED = 0xfff57c00;
    public static final int COLOR_MUSIC = 0xff2196f3;
    public static final int COLOR_TAXI = 0xff50C878;
    public static final int COLOR_NEARBY = 0xffFD3A73;



    private static ArrayList<PlusApp> getPlusApps(){

        ArrayList<PlusApp> plusApps = new ArrayList<>();


        PlusApp app;
        if(ChatUtils.isSupportedUser()){

            app = new PlusApp(LocaleController.getString("Scan",R.string.Scan), SCAN, R.drawable.msg_mini_qr, COLOR_SCAN);
            plusApps.add(app);
            app =   new PlusApp(LocaleController.getString("Marketplace",R.string.Marketplace), MARKETPLACE, R.drawable.ic_shop,COLOR_MARKETPLACE );
            plusApps.add(app);
        }


//        if(BuildConfig.DEBUG){
//            app = new PlusApp("Status", STATUS, R.drawable.ic_status, COLOR_DONATION);
//            plusApps.add(app);
//        }




        app = new PlusApp(LocaleController.getString("Feed",R.string.Feed), FEED, R.drawable.ic_feed, COLOR_FEED);
        plusApps.add(app);


        app = new PlusApp(LocaleController.getString("Music",R.string.Music), MUSIC, R.drawable.msg_voice_headphones_solar, COLOR_MUSIC);
        plusApps.add(app);


        app = new PlusApp(LocaleController.getString("PeopleNearby",R.string.PeopleNearby), PEOPLE_NEAR_BY, R.drawable.msg_nearby, COLOR_NEARBY);
        plusApps.add(app);

        app = new PlusApp(LocaleController.getString("Theme",R.string.Theme), THEME, R.drawable.msg_theme, Theme.getColor(Theme.key_avatar_background2Green));
        plusApps.add(app);

        app = new PlusApp(LocaleController.getString("AttachGame",R.string.AttachGame), GAMES, R.drawable.filter_game_solar, Theme.getColor(Theme.key_avatar_backgroundPink));
        plusApps.add(app);
//
        if(ChatUtils.isSupportedUser()){
//            app = new PlusApp("USSD", USSD, R.drawable.msg_filled_general, Theme.getColor(Theme.key_avatar_background2Blue));
//            plusApps.add(app);

            app = new PlusApp(LocaleController.getString("AirTime",R.string.AirTime), AIRTIME, R.drawable.air_time, Theme.getColor(Theme.key_avatar_background2Green));
            plusApps.add(app);

        }

//        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
//        String theme_channel = firebaseRemoteConfig.getStreing("theme_channel");
//        if(!TextUtils.isEmpty(theme_channel)){
//
//      //  }
//        String game_channel = firebaseRemoteConfig.getString("game_channel");
//        if(!TextUtils.isEmpty(game_channel)){
//
//        }

//
//
        if(BuildConfig.DEBUG){
            app = new PlusApp("Samri AI", CHATGPT, R.drawable.ic_samri_round, Color.TRANSPARENT);
            app.standAloneImage = true;
            plusApps.add(app);
        }



        return plusApps;
    }


    private static ArrayList<PlusApp> getFeatureApps(){

        ArrayList<PlusApp> plusApps = new ArrayList<>();
////        PlusApp app =   new PlusApp(LocaleController.getString("NewChat",R.string.NewChat), 1, R.drawable.floating_pencil, COLOR_DONATION);
////        plusApps.add(app);
//
//         PlusApp  app =   new PlusApp(LocaleController.getString("HuluBookmark",R.string.HuluBookmark), BOOKMARK, R.drawable.ic_bookmark, COLOR_DONATION);
//         plusApps.add(app);
//
//
//        app =   new PlusApp(LocaleController.getString("Online",R.string.Online), ONLINE_USERS, R.drawable.filter_online_filled, COLOR_DONATION);
//        plusApps.add(app);
//
//
//        app =   new PlusApp(LocaleController.getString("HiddenChat",R.string.HiddenChat), HIDDEN_CHAT, R.drawable.ime_filter_icon_lock_filled, COLOR_DONATION);
//        plusApps.add(app);
//
//        app =   new PlusApp(LocaleController.getString("Draft",R.string.Draft), DRAFT, R.drawable.ime_multi_panel_recent_actions, COLOR_DONATION);
//        plusApps.add(app);
//
////        app =   new PlusApp("Video", 1, R.drawable.msg_videocall, COLOR_DONATION);
////        plusApps.add(app);



        return plusApps;
    }

    public static boolean isTestUser(TLRPC.User user){
        return user != null && user.phone != null && user.phone.equals("9996627933");
    }

    public static boolean isUserSupported(TLRPC.User user) {
        return user != null&&  user.phone != null &&  (user.phone.startsWith("251") ||  user.phone.equals("9996627933"));
    }

    public static boolean isSelfSupported() {
        return isUserSupported(UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser());
    }

    public static boolean isBlockedUser(TLRPC.User user){
        //enable for only iran and uzbek users
        return user != null&&  user.phone != null &&  (user.phone.startsWith("998") ||  user.phone.equals("98"));

    }

}
