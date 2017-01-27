package re.jcg.playmusicexporter.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
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
    private static PowerManager m_powerManager;

    public static void startExport(Context pContext) {
        m_powerManager = (PowerManager) pContext.getSystemService(POWER_SERVICE);
        Intent lIntent = new Intent(pContext, ExportAllService.class);
        lIntent.setAction(ACTION_EXPORT);
        pContext.startService(lIntent);
        Log.i(TAG, "Intent sent!");
    }

    public ExportAllService()
    {
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
        PowerManager.WakeLock CPULock = m_powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ExportAllService");
        CPULock.acquire();
        List<Album> lAlba = lAlbumDataSource.getAll();
        for (Album lAlbum : lAlba) {
            for (MusicTrack lTrack : lAlbum.getMusicTrackList()) {
                if (lTrack.isOfflineAvailable()) {
                    String lPath = MusicPathBuilder.Build(lTrack, lExportStructure);
                    try {
                        if (lPlayMusicManager.exportMusicTrack(lTrack, lUri, lPath, PlayMusicExporterPreferences.getFileOverwritePreference())) {
                            Log.i(TAG, "Exported Music Track: " + getStringForTrack(lTrack));
                        } else {
                            Log.i(TAG, "Failed to export Music Track: " + getStringForTrack(lTrack));
                        }
      
                    } catch (IllegalArgumentException e) {
                        if (e.getMessage().contains("Invalid URI:")) {
                            /*
                            TODO: Make it impossible to reach this point
                            This happens when the user has not yet set the export path.

                             */
                            Log.i(TAG, "Automatic export failed, because the URI is invalid.");
                        } else throw e;
                    }
                    finally
                    {
                        if ( CPULock.isHeld())
                        {
                            CPULock.release();
                        }
                    }
                }
            }
        }
        if ( CPULock.isHeld())
        {
            CPULock.release();
        }
    }

    private String getStringForTrack(MusicTrack pTrack) {
        return pTrack.getAlbumArtist() + " - " + pTrack.getAlbum() + " - " + pTrack.getTitle();
    }
}

