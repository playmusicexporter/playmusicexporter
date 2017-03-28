package re.jcg.playmusicexporter.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

import java.util.Optional;

import de.arcus.framework.superuser.SuperUser;
import re.jcg.playmusicexporter.R;
import re.jcg.playmusicexporter.settings.PlayMusicExporterPreferences;

public class Intro extends AppIntro {
    private static final String TAG = "PME_Intro";
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    Fragment welcome;
    Fragment warning;
    Fragment storage;
    Fragment superuser;
    Fragment finish;

    private void initFragments() {
        welcome = AppIntroFragment.newInstance(
                "Welcome!",
                "This is the Play Music Exporter. It can export songs from Play Music " +
                        "and save them as MP3 files where you want them to be.",
                R.drawable.ic_launcher_transparent,
                Color.parseColor("#ef6c00"));
        warning = AppIntroFragment.newInstance(
                "Warning!",
                "You are responsible for what you do with this app. Depending on where you live " +
                        "it might be illegal to use this app. We discourage piracy of music " +
                        "and other intellectual property. Sharing music you exported with " +
                        "this tool might be a very bad idea, Google could put an invisible " +
                        "watermark on the music, so that people can trace the MP3s back to " +
                        "the owner of the Google account that was used.",
                R.drawable.ic_warning_white,
                Color.parseColor("#ef6c00"));
        storage = AppIntroFragment.newInstance(
                "We need access to your storage.",
                "We need to access the external storage, " +
                        "for copying the Play Music database to a folder," +
                        "where we have the right to work with it. " +
                        "We also need access to the external storage," +
                        "to finish up the MP3s, from encrypted without ID3 tags," +
                        "to decrypted with ID3 tags, before we save them to your export path.",
                R.drawable.ic_folder_white,
                Color.parseColor("#ef6c00"));
        superuser = AppIntroFragment.newInstance(
                "We need root access.",
                "Some of the files we need to access are in the private folders of Play Music. " +
                        "Android prevents apps from accessing the private folders " +
                        "of other apps, but luckily, you can circumvent this protection " +
                        "with root access. Without root access this app can't do anything.",
                R.drawable.ic_superuser,
                Color.parseColor("#ef6c00"));
        finish = AppIntroFragment.newInstance(
                "Tutorial finished!",
                "One note: Should you revoke any of these permission, the tutorial will be " +
                        "shown again on the next launch.",
                R.drawable.ic_launcher_transparent,
                Color.parseColor("#ef6c00"));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) getSupportActionBar().hide();
        showSkipButton(false);

        initFragments();

        addSlide(welcome);
        addSlide(warning);
        addSlide(storage);
        addSlide(superuser);
        addSlide(finish);

        pager.setPagingEnabled(true);
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        logSlideChanged(oldFragment, newFragment);
        if (warning.equals(oldFragment) && storage.equals(newFragment)) {
            promptAcceptWarning();
        } else if (storage.equals(oldFragment) && superuser.equals(newFragment)) {
            requestStoragePermission();
        } else if (superuser.equals(oldFragment) && finish.equals(newFragment)) {
            SuperUser.askForPermissionInBackground(granted -> {
                if (!granted) {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(this);
                    builder.setTitle(R.string.dialog_superuser_access_denied_title);
                    builder.setMessage(R.string.dialog_superuser_access_denied);
                    builder.setCancelable(false);
                    builder.setPositiveButton(R.string.text_okay, (dialog, which)
                            -> pager.setCurrentItem(pager.getCurrentItem() - 1));
                    builder.show();
                }
            });
        }
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Shows a warning and close the app
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(this);
                    builder.setTitle(R.string.dialog_storage_access_denied_title);
                    builder.setMessage(R.string.dialog_storage_access_denied);
                    builder.setCancelable(false);
                    builder.setPositiveButton(R.string.text_okay, (dialog, which)
                            -> pager.setCurrentItem(pager.getCurrentItem() - 1));
                    builder.show();

                }
            }
        }
    }

    private void promptAcceptWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Understood?");
        builder.setMessage("Have you read and understood this?");
        builder.setCancelable(false);
        builder.setNegativeButton("No", ((dialog, which)
                -> pager.setCurrentItem(pager.getCurrentItem() - 1)));
        builder.setPositiveButton("Yes", (((dialog, which) -> dialog.dismiss())));
        builder.show();
    }

    private void logSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Optional.ofNullable requires API level 24, and I won't do manual null checks.
            Log.i(TAG, "Fragment switched from {" +
                    Optional.ofNullable(oldFragment).map(Fragment::toString).orElse("") +
                    "} to {" +
                    Optional.ofNullable(newFragment).map(Fragment::toString).orElse("") +
                    "}.");
        }
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        PlayMusicExporterPreferences.init(this);
        PlayMusicExporterPreferences.setSetupDone(true);
        startActivity(new Intent(this, MusicContainerListActivity.class));
    }
}
