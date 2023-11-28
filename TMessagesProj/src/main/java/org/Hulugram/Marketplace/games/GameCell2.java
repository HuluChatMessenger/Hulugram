package plus.games;/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */


import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DocumentObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.SvgHelper;
import org.telegram.messenger.WebFile;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.Locale;

public class GameCell2 extends FrameLayout{

    private BackupImageView linkImageView;
    private TextView textView;
    private TLRPC.BotInlineResult inlineResult;
    private TLRPC.User bot;
    private FrameLayout container;
    private MentionGameCell mentionCell;
    private View selector;
    private TLRPC.PhotoSize currentPhotoObject;
    private TLRPC.Photo photoAttach;

    public GameCell2(@NonNull Context context) {
        super(context);
        int imageHeight = (int) (getItemSize(2)/AndroidUtilities.density);

        container = new FrameLayout(context);
        addView(container, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        linkImageView = new BackupImageView(context);
        linkImageView.getImageReceiver().setNeedsQualityThumb(true);
        linkImageView.setRoundRadius(AndroidUtilities.dp(6));
        linkImageView.getImageReceiver().setShouldGenerateQualityThumb(true);
        container.addView(linkImageView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT,Gravity.TOP|Gravity.LEFT,6,6,6, 8 + 8 + 6+ 20 + 36));

        textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        textView.setMaxLines(2);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        container.addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 12, 8 + 6 + imageHeight, 0, 4));


        mentionCell = new MentionGameCell(context,null);
        container.addView(mentionCell, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 32, Gravity.TOP | Gravity.LEFT, 4,  + 8 + 16 + 6+ 20 + imageHeight, 0, 4));


        selector = new View(context);
        selector.setBackgroundDrawable(Theme.getSelectorDrawable(false));
        addView(selector, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int itemWidth;
        itemWidth = getItemSize(3);

        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec((true ? 0 : AndroidUtilities.dp(2)) + itemWidth + AndroidUtilities.dp(32 + 16 +  6 + 8 + 20), MeasureSpec.EXACTLY));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (Build.VERSION.SDK_INT >= 21) {
            selector.drawableHotspotChanged(event.getX(), event.getY());
        }
        return super.onTouchEvent(event);
    }
    private final static int DOCUMENT_ATTACH_TYPE_NONE = 0;
    private final static int DOCUMENT_ATTACH_TYPE_DOCUMENT = 1;
    private final static int DOCUMENT_ATTACH_TYPE_GIF = 2;
    private final static int DOCUMENT_ATTACH_TYPE_AUDIO = 3;
    private final static int DOCUMENT_ATTACH_TYPE_VIDEO = 4;
    private final static int DOCUMENT_ATTACH_TYPE_MUSIC = 5;
    private final static int DOCUMENT_ATTACH_TYPE_STICKER = 6;
    private final static int DOCUMENT_ATTACH_TYPE_PHOTO = 7;
    private final static int DOCUMENT_ATTACH_TYPE_GEO = 8;

    private Object parentObject;
    private boolean isForceGif;
    private TLRPC.Document documentAttach;
    private boolean mediaWebpage;
    private int documentAttachType;
    public void setInlineResult(TLRPC.BotInlineResult inlineResult, TLRPC.User bot,boolean forceGif) {
        this.inlineResult = inlineResult;
        this.bot = bot;
        isForceGif =forceGif;
        parentObject = inlineResult = inlineResult;
        if (inlineResult != null) {
            documentAttach = inlineResult.document;
            photoAttach = inlineResult.photo;
        } else {
            documentAttach = null;
            photoAttach = null;
        }
        textView.setText("game title");
        textView.setText(inlineResult.title);
        if (forceGif) {
            documentAttachType = DOCUMENT_ATTACH_TYPE_GIF;
        }

        TLRPC.PhotoSize currentPhotoObjectThumb = null;
        ArrayList<TLRPC.PhotoSize> photoThumbs = null;
        TLRPC.TL_webDocument webDocument = null;
        String urlLocation = null;
        WebFile webFile = null;
        if (inlineResult.photo != null) {
            currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(photoThumbs, AndroidUtilities.getPhotoSize(), true);
            currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(photoThumbs, 80);
            if (currentPhotoObjectThumb == currentPhotoObject) {
                currentPhotoObjectThumb = null;
            }
        }

        String ext = null;
        if (documentAttach != null) {
            if (isForceGif || MessageObject.isGifDocument(documentAttach)) {
                currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(documentAttach.thumbs, 90);
            } else if (MessageObject.isStickerDocument(documentAttach) || MessageObject.isAnimatedStickerDocument(documentAttach, true)) {
                currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(documentAttach.thumbs, 90);
                ext = "webp";
            } else {
                if (documentAttachType != DOCUMENT_ATTACH_TYPE_MUSIC && documentAttachType != DOCUMENT_ATTACH_TYPE_AUDIO) {
                    currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(documentAttach.thumbs, 90);
                }
            }
        } else if (inlineResult != null && inlineResult.photo != null) {
            currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(photoThumbs, AndroidUtilities.getPhotoSize(), true);
            currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(photoThumbs, 80);
            if (currentPhotoObjectThumb == currentPhotoObject) {
                currentPhotoObjectThumb = null;
            }
        }

        if (inlineResult.content instanceof TLRPC.TL_webDocument) {
            if (inlineResult.type != null) {
                if (inlineResult.type.startsWith("gif")) {
                    if (inlineResult.thumb instanceof TLRPC.TL_webDocument && "video/mp4".equals(inlineResult.thumb.mime_type)) {
                        webDocument = (TLRPC.TL_webDocument) inlineResult.thumb;
                    } else {
                        webDocument = (TLRPC.TL_webDocument) inlineResult.content;
                    }
                    //documentAttachType = DOCUMENT_ATTACH_TYPE_GIF;
                } else if (inlineResult.type.equals("photo")) {
                    if (inlineResult.thumb instanceof TLRPC.TL_webDocument) {
                        webDocument = (TLRPC.TL_webDocument) inlineResult.thumb;
                    } else {
                        webDocument = (TLRPC.TL_webDocument) inlineResult.content;
                    }
                }
            }
        }

        if (inlineResult.photo != null) {
            photoThumbs = new ArrayList<>(inlineResult.photo.sizes);
        }

        int width;
        int w = 0;
        int h = 0;
        if (w == 0 || h == 0) {
            if (currentPhotoObject != null) {
                if (currentPhotoObjectThumb != null) {
                    currentPhotoObjectThumb.size = -1;
                }
                w = currentPhotoObject.w;
                h = currentPhotoObject.h;
            } else if (inlineResult != null) {
                int[] result = MessageObject.getInlineResultWidthAndHeight(inlineResult);
                w = result[0];
                h = result[1];
            }
        }
        if (w == 0 || h == 0) {
            w = h = AndroidUtilities.dp(80);
        }
        if (documentAttach != null || currentPhotoObject != null || webFile != null || urlLocation != null) {
            String currentPhotoFilter;
            String currentPhotoFilterThumb = "52_52_b";

            if (mediaWebpage) {
                width = (int) (w / (h / (float) AndroidUtilities.dp(80)));
                if (documentAttachType == DOCUMENT_ATTACH_TYPE_GIF) {
                    currentPhotoFilterThumb = currentPhotoFilter = String.format(Locale.US, "%d_%d_b", (int) (width / AndroidUtilities.density), 80);
                } else {
                    currentPhotoFilter = String.format(Locale.US, "%d_%d", (int) (width / AndroidUtilities.density), 80);
                    currentPhotoFilterThumb = currentPhotoFilter + "_b";
                }
            } else {
                currentPhotoFilter = "52_52";
            }
            linkImageView.setAspectFit(true);

            if (documentAttachType == DOCUMENT_ATTACH_TYPE_GIF) {
                if (documentAttach != null) {
                    TLRPC.VideoSize thumb = MessageObject.getDocumentVideoThumb(documentAttach);
                    if (thumb != null) {
                       // linkImageView.setImage(ImageLocation.getForDocument(thumb, documentAttach), "100_100", ImageLocation.getForDocument(currentPhotoObject, documentAttach), currentPhotoFilter, -1, ext, parentObject, 1);
                    } else {
                        ImageLocation location = ImageLocation.getForDocument(documentAttach);
                        if (isForceGif) {
                            location.imageType = FileLoader.IMAGE_TYPE_ANIMATION;
                        }
                     //   linkImageView.setImage(location, "100_100", ImageLocation.getForDocument(currentPhotoObject, documentAttach), currentPhotoFilter, documentAttach.size, ext, parentObject, 0);
                    }
                } else if (webFile != null) {
                    //linkImageView.setImage(ImageLocation.getForWebFile(webFile), "100_100", ImageLocation.getForPhoto(currentPhotoObject, photoAttach), currentPhotoFilter, ext, parentObject, 1);
                } else {
                   // linkImageView.setImage(ImageLocation.getForPath(urlLocation), "100_100", ImageLocation.getForPhoto(currentPhotoObject, photoAttach), currentPhotoFilter, ext, parentObject, 1);
                }
            } else {
                if (currentPhotoObject != null) {
                    SvgHelper.SvgDrawable svgThumb = DocumentObject.getSvgThumb(documentAttach, Theme.key_windowBackgroundGray, 1.0f);
                    if (MessageObject.canAutoplayAnimatedSticker(documentAttach)) {
                        if (svgThumb != null) {
                          //  linkImageView.setImage(ImageLocation.getForDocument(documentAttach), "80_80", svgThumb, currentPhotoObject.size, ext, parentObject, 0);
                        } else {
                           // linkImageView.setImage(ImageLocation.getForDocument(documentAttach), "80_80", ImageLocation.getForDocument(currentPhotoObject, documentAttach), currentPhotoFilterThumb, currentPhotoObject.size, ext, parentObject, 0);
                        }
                    } else {
                        if (documentAttach != null) {
                            if (svgThumb != null) {
                                //linkImageView.setImage(ImageLocation.getForDocument(currentPhotoObject, documentAttach), currentPhotoFilter, svgThumb, currentPhotoObject.size, ext, parentObject, 0);
                            } else {
                              //  linkImageView.setImage(ImageLocation.getForDocument(currentPhotoObject, documentAttach), currentPhotoFilter, ImageLocation.getForPhoto(currentPhotoObjectThumb, photoAttach), currentPhotoFilterThumb, currentPhotoObject.size, ext, parentObject, 0);
                            }
                        } else {
                            //linkImageView.setImage(ImageLocation.getForPhoto(currentPhotoObject, photoAttach), currentPhotoFilter, ImageLocation.getForPhoto(currentPhotoObjectThumb, photoAttach), currentPhotoFilterThumb, currentPhotoObject.size, ext, parentObject, 0); }
                    }}
                } else if (webFile != null) {
                   // linkImageView.setImage(ImageLocation.getForWebFile(webFile), currentPhotoFilter, ImageLocation.getForPhoto(currentPhotoObjectThumb, photoAttach), currentPhotoFilterThumb, -1, ext, parentObject, 1);
                } else {
                   // linkImageView.setImage(ImageLocation.getForPath(urlLocation), currentPhotoFilter, ImageLocation.getForPhoto(currentPhotoObjectThumb, photoAttach), currentPhotoFilterThumb, -1, ext, parentObject, 1);
                }
            }

        }

    }

    public static int getItemSize(int itemsCount) {
        final int itemWidth;
        if (AndroidUtilities.isTablet()) {
            itemWidth = (AndroidUtilities.dp(490) - (itemsCount - 1) * AndroidUtilities.dp(2)) / itemsCount;
        } else {
            itemWidth = (AndroidUtilities.displaySize.x - (itemsCount - 1) * AndroidUtilities.dp(2)) / itemsCount;
        }
        return itemWidth;
    }


}
