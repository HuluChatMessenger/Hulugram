package plus.utils.cache;


import static plus.utils.cache.CacheFragment.TYPE_ANIMATED_STICKERS_CACHE;
import static plus.utils.cache.CacheFragment.TYPE_DOCUMENTS;
import static plus.utils.cache.CacheFragment.TYPE_MUSIC;
import static plus.utils.cache.CacheFragment.TYPE_PHOTOS;
import static plus.utils.cache.CacheFragment.TYPE_VIDEOS;
import static plus.utils.cache.CacheFragment.TYPE_VOICE;

import android.app.Activity;
import android.os.Bundle;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ProfileActivity;

import java.util.ArrayList;
import java.util.HashSet;

public class CacheDialogManager {

    public Activity parentActivity;
    private final BaseFragment baseFragment;
    private final long dialog_id;
    public Runnable doneRunnable;
    private BottomSheet bottomSheet;
    private  CacheFragment.DialogFileEntities dialogFileEntities;
    private boolean loadingDialogEntity;
    private boolean canceled;

    public CacheDialogManager(BaseFragment fragment,long dialog_id,Runnable doneRunnable){
        this.baseFragment =fragment;
        this.dialog_id = dialog_id;
        this.doneRunnable = doneRunnable;
        parentActivity = fragment.getParentActivity();
    }
    public  void loadDialogEntity(){
        if(loadingDialogEntity){
            return;
        }
        loadingDialogEntity = true;
        baseFragment.getFileLoader().getFileDatabase().getQueue().postRunnable(() -> {
            CacheFragment.DialogFileEntities dialogFileEntities = new CacheFragment.DialogFileEntities(dialog_id);
            ArrayList<CacheModel.FileInfo> fileInfos = new ArrayList<>();
            baseFragment.getFileLoader().getFileDatabase().getFileMetaDataForDialogId(dialog_id, fileInfos);
            if(canceled){
                loadingDialogEntity = false;
                AndroidUtilities.runOnUIThread(doneRunnable);
                return;
            }
            for(int a = 0; a < fileInfos.size(); a++){
                CacheModel.FileInfo info = fileInfos.get(a);
                dialogFileEntities.totalSize += info.size;
                int addToType = info.type;
                String fileName = info.file.getName().toLowerCase();
                if (fileName.endsWith(".mp3") || fileName.endsWith(".m4a") ) {
                    addToType = TYPE_MUSIC;
                }
                dialogFileEntities.addFile(info, addToType);
            }
            dialogFileEntities.filesCount = fileInfos.size();
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    loadingDialogEntity = false;
                    doneRunnable.run();
                    showClearCacheDialog(dialogFileEntities);
                }
            });

        });
    }

    private void showClearCacheDialog(CacheFragment.DialogFileEntities entities) {

        if (parentActivity == null || baseFragment == null) {
            return;
        }
        bottomSheet = new plus.utils.cache.DilogCacheBottomSheet(baseFragment, entities, entities.createCacheModel(), new DilogCacheBottomSheet.Delegate() {
            @Override
            public void onAvatarClick() {
                bottomSheet.dismiss();
                Bundle args = new Bundle();
                if (entities.dialogId > 0) {
                    args.putLong("user_id", entities.dialogId);
                } else {
                    args.putLong("chat_id", -entities.dialogId);
                }
                baseFragment.presentFragment(new ProfileActivity(args, null));
            }

            @Override
            public void cleanupDialogFiles(CacheFragment.DialogFileEntities entities, plus.utils.cache.StorageDiagramView.ClearViewData[] clearViewData, CacheModel cacheModel) {
                 dialogFileEntities = entities;
                 CacheDialogManager.this.cleanupDialogFiles( clearViewData, cacheModel);
            }


        });
        baseFragment.showDialog(bottomSheet);
    }

    private void cleanupDialogFiles( plus.utils.cache.StorageDiagramView.ClearViewData[] clearViewData, CacheModel dialogCacheModel) {
        final AlertDialog progressDialog = new AlertDialog(parentActivity, AlertDialog.ALERT_TYPE_SPINNER);
        progressDialog.setCanCancel(false);
        progressDialog.showDelayed(500);

        HashSet<CacheModel.FileInfo> filesToRemove = new HashSet<>();
        for (int a = 0; a < 7; a++) {
            if (clearViewData != null) {
                if (clearViewData[a] == null || !clearViewData[a].clear) {
                    continue;
                }
            }
            CacheFragment.FileEntities entitiesToDelete = dialogFileEntities.entitiesByType.get(a);
            if (entitiesToDelete == null) {
                continue;
            }
            filesToRemove.addAll(entitiesToDelete.files);
            dialogFileEntities.totalSize -= entitiesToDelete.totalSize;
            dialogFileEntities.entitiesByType.delete(a);
            if (TYPE_PHOTOS == a) {
            } else if (a == TYPE_VIDEOS) {
            } else if (a == TYPE_DOCUMENTS) {
            } else if (a == TYPE_MUSIC) {
            } else if (a == TYPE_VOICE) {
            } else if (a == TYPE_ANIMATED_STICKERS_CACHE) {
            }
        }
        if (dialogFileEntities.entitiesByType.size() == 0) {
           // cacheModel.remove(dialogEntities);
        }
        if (dialogCacheModel != null) {
            for (CacheModel.FileInfo fileInfo : dialogCacheModel.selectedFiles) {
                if (!filesToRemove.contains(fileInfo)) {
                    filesToRemove.add(fileInfo);
                    dialogFileEntities.removeFile(fileInfo);
                    if (fileInfo.type == TYPE_PHOTOS) {
                    } else if (fileInfo.type == TYPE_VIDEOS) {
                    } else if (fileInfo.size == TYPE_DOCUMENTS) {
                    } else if (fileInfo.size == TYPE_MUSIC) {
                    } else if (fileInfo.size == TYPE_VOICE) {
                    }
                }
            }
        }
        for (CacheModel.FileInfo fileInfo : filesToRemove) {
           // this.cacheModel.onFileDeleted(fileInfo);
        }


//        cacheRemovedTooltip.setInfoText(LocaleController.formatString("CacheWasCleared", R.string.CacheWasCleared, AndroidUtilities.formatFileSize(totalSizeBefore - totalSize)));
//        cacheRemovedTooltip.showWithAction(0, UndoView.ACTION_CACHE_WAS_CLEARED, null, null);

        ArrayList<CacheModel.FileInfo> fileInfos = new ArrayList<>(filesToRemove);
        baseFragment.getFileLoader().getFileDatabase().removeFiless(fileInfos);
        baseFragment.getFileLoader().cancelLoadAllFiles();
        baseFragment.getFileLoader().getFileLoaderQueue().postRunnable(() -> {
            for (int i = 0; i < fileInfos.size(); i++) {
                fileInfos.get(i).file.delete();
            }

            AndroidUtilities.runOnUIThread(() -> {
                FileLoader.getInstance(baseFragment.getCurrentAccount()).checkCurrentDownloadsFiles();
                try {
                    progressDialog.dismiss();
                } catch (Exception e) {
                    FileLog.e(e);
                }
            });
        });
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;

    }

}
