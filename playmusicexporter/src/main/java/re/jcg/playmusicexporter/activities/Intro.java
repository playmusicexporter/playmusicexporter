package re.jcg.playmusicexporter.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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
    Fragment error;
    Fragment finish;

    private void initFragments() {
        int color = ContextCompat.getColor(this, R.color.application_main);
        welcome = AppIntroFragment.newInstance(
                getString(R.string.intro_welcome_title),
                getString(R.string.intro_welcome_description),
                R.drawable.ic_launcher_transparent,
                color);
        warning = AppIntroFragment.newInstance(
                getString(R.string.intro_warning_title),
                getString(R.string.intro_warning_description),
                R.drawable.ic_warning_white,
                color);
        storage = AppIntroFragment.newInstance(
                getString(R.string.intro_storage_title),
                getString(R.string.intro_storage_description),
                R.drawable.ic_folder_white,
                color);
        superuser = AppIntroFragment.newInstance(
                getString(R.string.intro_superuser_title),
                getString(R.string.intro_superuser_description),
                R.drawable.ic_superuser,
                color);
        error = AppIntroFragment.newInstance(
                getString(R.string.intro_error_title),
                getString(R.string.intro_error_description),
                R.drawable.ic_error_white,
                color);
        finish = AppIntroFragment.newInstance(
                getString(R.string.intro_finish_title),
                getString(R.string.intro_finish_description),
                R.drawable.ic_launcher_transparent,
                color);
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
        addSlide(error);
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
        } else if (superuser.equals(oldFragment) && error.equals(newFragment)) {
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
        } else if (error.equals(oldFragment) && finish.equals(newFragment)) {
            promptEnableErrorReporting();
        }
    }

    private void promptEnableErrorReporting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Dialog.OnClickListener enable = (dialog, which) -> {
            PlayMusicExporterPreferences.setReportStats(true);
            dialog.dismiss();
        };
        Dialog.OnClickListener disable = (dialog, which) -> {
            PlayMusicExporterPreferences.setReportStats(false);
            dialog.dismiss();
        };
        builder.setTitle(R.string.error_alert_dialog_title);
        builder.setMessage(R.string.error_alert_dialog_message);
        builder.setCancelable(false);
        builder.setNegativeButton(R.string.no, disable);
        builder.setNeutralButton(R.string.whatever, enable);
        builder.setPositiveButton(R.string.yes, enable);
        builder.show();
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
        builder.setTitle(R.string.warning_alert_dialog_title);
        builder.setMessage(R.string.warning_alert_dialog_message);
        builder.setCancelable(false);
        builder.setNegativeButton(getString(R.string.no), ((dialog, which)
                -> pager.setCurrentItem(pager.getCurrentItem() - 1)));
        builder.setPositiveButton(getString(R.string.yes), (((dialog, which) -> dialog.dismiss())));
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
