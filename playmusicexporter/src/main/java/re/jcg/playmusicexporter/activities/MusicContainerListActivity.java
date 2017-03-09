/*
 * Copyright (c) 2015 David Schulte
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package re.jcg.playmusicexporter.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import de.arcus.framework.logger.Logger;
import de.arcus.framework.crashhandler.CrashHandler;
import de.arcus.playmusiclib.exceptions.CouldNotOpenDatabaseException;
import de.arcus.playmusiclib.exceptions.NoSuperUserException;
import re.jcg.playmusicexporter.R;
import re.jcg.playmusicexporter.fragments.MusicTrackListFragment;
import re.jcg.playmusicexporter.fragments.MusicContainerListFragment;
import re.jcg.playmusicexporter.fragments.NavigationDrawerFragment;
import re.jcg.playmusicexporter.items.SelectedTrackList;
import de.arcus.playmusiclib.PlayMusicManager;
import de.arcus.playmusiclib.datasources.AlbumDataSource;
import de.arcus.playmusiclib.datasources.ArtistDataSource;
import de.arcus.playmusiclib.datasources.PlaylistDataSource;
import de.arcus.playmusiclib.enums.ID3v2Version;
import de.arcus.playmusiclib.items.MusicTrackList;
import re.jcg.playmusicexporter.settings.PlayMusicExporterPreferences;

/**
 * An activity representing a list of Tracks. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MusicTrackListActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link MusicContainerListFragment} and the item details
 * (if present) is a {@link MusicTrackListFragment}.
 * <p/>
 * This activity also implements the required
 * {@link MusicContainerListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class MusicContainerListActivity extends AppCompatActivity
        implements MusicContainerListFragment.Callbacks
        , NavigationDrawerFragment.NavigationDrawerCallbacks,
        SearchView.OnQueryTextListener {

    @Override
    public void onViewTypeChanged(NavigationDrawerFragment.ViewType viewType) {
        mViewType = viewType;
        loadList();
    }

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private PlayMusicManager mPlayMusicManager;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private String mSearchKeyword;

    private NavigationDrawerFragment.ViewType mViewType;

    private SearchView mSearchView;

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Adds the crash handler to this class
        CrashHandler.addCrashHandler(this);

        PlayMusicExporterPreferences.init(this);
        if (!PlayMusicExporterPreferences.getSetupDone()) {
            startActivity(new Intent(this, Intro.class));
            finish();
        } else {

            setContentView(R.layout.activity_track_list);


            Logger.getInstance().logVerbose("Activity", "onCreate(" + this.getLocalClassName() + ")");

            // Setup ActionBar
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(R.string.app_name);
            }


            mNavigationDrawerFragment = (NavigationDrawerFragment)
                    getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

            mNavigationDrawerFragment.setOnListViewChanged(this);

            // Set up the drawer.
            mNavigationDrawerFragment.setUp(
                    R.id.navigation_drawer,
                    (DrawerLayout) findViewById(R.id.drawer_layout));

            if (findViewById(R.id.track_detail_container) != null) {
                // The detail container view will be present only in the
                // large-screen layouts (res/values-large and
                // res/values-sw600dp). If this view is present, then the
                // activity should be in two-pane mode.
                mTwoPane = true;

                // In two-pane mode, list items should be given the
                // 'activated' state when touched.
                ((MusicContainerListFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_main))
                        .setActivateOnItemClick(true);
            }

            boolean waitForPermissions = false;

            // Check file system permissions
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                waitForPermissions = true;
            }

            if (!waitForPermissions)
                loadPlayMusicExporter();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Continue loading
                    loadPlayMusicExporter();
                } else {
                    // Shows a warning and close the app
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(this);
                    builder.setTitle(R.string.dialog_storage_access_denied_title);
                    builder.setMessage(R.string.dialog_storage_access_denied);
                    builder.setCancelable(false);
                    builder.setPositiveButton(R.string.text_okay, (dialog, which) -> MusicContainerListActivity.this.finish());
                    builder.show();

                }
            }
        }
    }
    /**
     * Loads the PlayMusicExporter lib and shows the list
     */
    private void loadPlayMusicExporter()
    {
        // Gets the running instance
        mPlayMusicManager = PlayMusicManager.getInstance();

        // Create a new instance
        if (mPlayMusicManager == null) {
            mPlayMusicManager = new PlayMusicManager(this);

            try {
                // Simple play ground
                mPlayMusicManager.startUp();
                mPlayMusicManager.setOfflineOnly(true);

                // Setup ID3
                mPlayMusicManager.setID3Enable(true);
                mPlayMusicManager.setID3EnableArtwork(true);
                mPlayMusicManager.setID3EnableFallback(true);
                mPlayMusicManager.setID3v2Version(ID3v2Version.ID3v23);
                mPlayMusicManager.setID3ArtworkFormat(Bitmap.CompressFormat.PNG);
                mPlayMusicManager.setID3ArtworkMaximumSize(PlayMusicExporterPreferences.getAlbumArtSize());

            } catch (Exception e) {
                Logger.getInstance().logError("SetupPlayMusicExporter", e.toString());
            }
        }

        // Loads the list
        mViewType = mNavigationDrawerFragment.getViewType();
        loadList();

        // Setup the selection list for this activity
        SelectedTrackList.getInstance().setupActionMode(this);
    }

    /**
     * Update all view lists
     */
    public void updateLists() {
        Fragment fragmentList = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_main);

        // Album view
        if (fragmentList instanceof MusicContainerListFragment) {
            // Gets the music list fragment
            MusicContainerListFragment musicTrackListFragment = (MusicContainerListFragment) fragmentList;

            // Update the view
            musicTrackListFragment.updateListView();
        }
    }

    /**
     * Loads the music list with the current view type
     */
    private void loadList() {
        // Manager is not loaded
        if (mPlayMusicManager == null) return;

        // Gets the music list fragment
        MusicContainerListFragment musicTrackListFragment = (MusicContainerListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_main);

        switch(mViewType) {
            case Album:
                // Load all albums to the list
                AlbumDataSource dataSourceAlbum = new AlbumDataSource(mPlayMusicManager);
                dataSourceAlbum.setOfflineOnly(true);
                dataSourceAlbum.setSerchKey(mSearchKeyword);
                musicTrackListFragment.setMusicTrackList(dataSourceAlbum.getAll());
                break;
            case Artist:
                // Load all artists to the list
                ArtistDataSource dataSourceArtist = new ArtistDataSource(mPlayMusicManager);
                dataSourceArtist.setOfflineOnly(true);
                dataSourceArtist.setSerchKey(mSearchKeyword);
                musicTrackListFragment.setMusicTrackList(dataSourceArtist.getAll());
                break;
            case Playlist:
                // Load all playlists to the list
                PlaylistDataSource dataSourcePlaylist = new PlaylistDataSource(mPlayMusicManager);
                dataSourcePlaylist.setOfflineOnly(true);
                dataSourcePlaylist.setSerchKey(mSearchKeyword);
                musicTrackListFragment.setMusicTrackList(dataSourcePlaylist.getAll());
                break;
            case Rated:
                // Load all created albums to the list
                AlbumDataSource dataSourceRatedAlbum = new AlbumDataSource(mPlayMusicManager);
                dataSourceRatedAlbum.setOfflineOnly(true);
                dataSourceRatedAlbum.setRatedOnly(true);
                dataSourceRatedAlbum.setSerchKey(mSearchKeyword);
                musicTrackListFragment.setMusicTrackList(dataSourceRatedAlbum.getAll());
                break;
        }
    }

    /**
     * Callback method from {@link MusicContainerListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(MusicTrackList musicTrackList) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(MusicTrackListFragment.ARG_MUSIC_TRACK_LIST_ID, musicTrackList.getMusicTrackListID());
            arguments.putString(MusicTrackListFragment.ARG_MUSIC_TRACK_LIST_TYPE, musicTrackList.getMusicTrackListType());
            MusicTrackListFragment fragment = new MusicTrackListFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.track_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, MusicTrackListActivity.class);
            detailIntent.putExtra(MusicTrackListFragment.ARG_MUSIC_TRACK_LIST_ID, musicTrackList.getMusicTrackListID());
            detailIntent.putExtra(MusicTrackListFragment.ARG_MUSIC_TRACK_LIST_TYPE, musicTrackList.getMusicTrackListType());
            startActivity(detailIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.music_track_list, menu);

        MenuItem itemRefreshLibrary = menu.findItem(R.id.action_refresh);
        itemRefreshLibrary.setOnMenuItemClickListener(item ->
        {
            try
            {
                mPlayMusicManager.reloadDatabase();
                mPlayMusicManager = null;
                loadPlayMusicExporter();
	            Toast.makeText( this, R.string.database_reloaded, Toast.LENGTH_SHORT).show();
            }
            catch (NoSuperUserException | CouldNotOpenDatabaseException e)
            {
	            Toast.makeText( this, R.string.dialog_superuser_access_denied_title, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            return true;
        });

        // Finds the search item and create the search view
        MenuItem itemSearch = menu. findItem(R.id.action_search);
        mSearchView = (SearchView)MenuItemCompat.getActionView(itemSearch);

        if (mSearchView != null) {
            // Sets the search listener
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setIconifiedByDefault(true);

            // Hack to change the default placeholder
            EditText searchEditText = (EditText) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            searchEditText.setHint(R.string.search);
        }

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String keyword) {
        mSearchView.clearFocus();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String keyword) {
        mSearchKeyword = keyword;
        loadList();

        return false;
    }
}
