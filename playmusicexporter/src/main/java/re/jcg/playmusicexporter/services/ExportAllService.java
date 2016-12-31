package re.jcg.playmusicexporter.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import java.util.List;

import re.jcg.playmusicexporter.settings.PlayMusicExporterPreferences;
import re.jcg.playmusicexporter.utils.MusicPathBuilder;
import de.arcus.playmusiclib.PlayMusicManager;
import de.arcus.playmusiclib.datasources.AlbumDataSource;
import de.arcus.playmusiclib.exceptions.CouldNotOpenDatabaseException;
import de.arcus.playmusiclib.exceptions.NoSuperUserException;
import de.arcus.playmusiclib.exceptions.PlayMusicNotFoundException;
import de.arcus.playmusiclib.items.Album;
import de.arcus.playmusiclib.items.MusicTrack;


public class ExportAllService extends IntentService {
    public static final String TAG = "AutoGPME_ExportService";
    public static final String ACTION_EXPORT = "re.jcg.playmusicexporter.action.EXPORT";
    public static final String ACTION_SET_EXPORT_JOB = "re.jcg.playmusicexporter.action.SET_EXPORT_JOB";

    public static void startExport(Context pContext) {
        Intent lIntent = new Intent(pContext, ExportAllService.class);
        lIntent.setAction(ACTION_EXPORT);
        pContext.startService(lIntent);
        Log.i(TAG, "Intent sent!");
    }

    public ExportAllService() {
        super("AutoGPME-ExportService");
    }

    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Received intent: " + intent.getAction());
        switch (intent.getAction()) {
            case ACTION_EXPORT:
                export();
                break;
            case ACTION_SET_EXPORT_JOB:
                ExportAllJob.scheduleExport(this);
                break;
        }
    }

    private void export() {
        PlayMusicExporterPreferences.init(this);
        PlayMusicManager lPlayMusicManager = new PlayMusicManager(this);

        try {
            lPlayMusicManager.startUp();
        } catch (PlayMusicNotFoundException | NoSuperUserException | CouldNotOpenDatabaseException e) {
            e.printStackTrace();
        }
        Uri lUri = PlayMusicExporterPreferences.getConditionedAutoExportPath();
        String lExportStructure = PlayMusicExporterPreferences.getConditionedAutoExportStructure();
        Log.i(TAG, lUri.toString());
        AlbumDataSource lAlbumDataSource = new AlbumDataSource(lPlayMusicManager);
        lAlbumDataSource.setOfflineOnly(true);
        List<Album> lAlba = lAlbumDataSource.getAll();
        for (Album lAlbum : lAlba) {
            for (MusicTrack lTrack : lAlbum.getMusicTrackList()) {
                if (lTrack.isOfflineAvailable()) {
                    String lPath = MusicPathBuilder.Build(lTrack, lExportStructure);
                    if (!isAlreadyThere(lUri, lPath)) {
                        if (lPlayMusicManager.exportMusicTrack(lTrack, lUri, lPath)) {
                            Log.i(TAG, "Exported Music Track: " + getStringForTrack(lTrack));
                        } else {
                            Log.i(TAG, "Failed to export Music Track: " + getStringForTrack(lTrack));
                        }
                    } else {
                        Log.i(TAG, lPath + " already exists.");
                    }
                }
            }
        }
    }

    private boolean isAlreadyThere(Uri pUri, String pPath) {
        DocumentFile lDocumentFile = DocumentFile.fromTreeUri(this, pUri);
        for (String lDisplayName: pPath.split("/")) {
            if (lDocumentFile.findFile(lDisplayName) != null) {
                lDocumentFile = lDocumentFile.findFile(lDisplayName);
            } else {
                Log.i(TAG, pPath + " does not exist yet.");
                return false;
            }
        }
        return true;
    }

    private String getStringForTrack(MusicTrack pTrack) {
        return pTrack.getAlbumArtist() + " - " + pTrack.getAlbum() + " - " + pTrack.getTitle();
    }
}

