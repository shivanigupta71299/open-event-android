package org.fossasia.openevent.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.fossasia.openevent.R;
import org.fossasia.openevent.adapters.SessionsListAdapter;
import org.fossasia.openevent.api.Urls;
import org.fossasia.openevent.data.Session;
import org.fossasia.openevent.dbutils.DbSingleton;
import org.fossasia.openevent.widget.DialogFactory;

import java.text.ParseException;
import java.util.ArrayList;

import butterknife.BindView;
import timber.log.Timber;

/**
 * User: manan
 * Date: 22-05-2015
 */
public class BookmarksFragment extends BaseFragment {

    private final String FRAGMENT_TAG = "FTAG";
    SessionsListAdapter sessionsListAdapter;

    @BindView(R.id.list_bookmarks) RecyclerView bookmarkedTracks;

    View view;
    ArrayList<Integer> bookmarkedIds;

    @Override
    public void onResume() {
        super.onResume();
        if (sessionsListAdapter != null) {
            try {
                DbSingleton dbSingleton = DbSingleton.getInstance();
                bookmarkedIds = dbSingleton.getBookmarkIds();
                sessionsListAdapter.clear();
                for (int i = 0; i < bookmarkedIds.size(); i++) {
                    Integer id = bookmarkedIds.get(i);
                    Session session = dbSingleton.getSessionById(id);
                    sessionsListAdapter.addItem(i, session);
                }
                sessionsListAdapter.notifyDataSetChanged();

            } catch (ParseException e) {
                Timber.e("Parsing Error Occurred at BookmarksFragment::onResume.");
            }
        }
        if (!bookmarkedIds.isEmpty()) {
            bookmarkedTracks.setVisibility(View.VISIBLE);
        } else {
            DialogFactory.createSimpleActionDialog(getActivity(), R.string.bookmarks, R.string.empty_list, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.content_frame, new TracksFragment(), FRAGMENT_TAG).commit();
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.menu_tracks);
                }
            }).show();
            bookmarkedTracks.setVisibility(View.GONE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.i("Bookmarks Fragment create view");
        setHasOptionsMenu(true);

        View view = super.onCreateView(inflater, container, savedInstanceState);

        final DbSingleton dbSingleton = DbSingleton.getInstance();

        try {
            bookmarkedIds = dbSingleton.getBookmarkIds();

        } catch (ParseException e) {
            Timber.e("Parsing Error Occurred at BookmarksFragment::onCreateView.");
        }
        bookmarkedTracks.setVisibility(View.VISIBLE);
        sessionsListAdapter = new SessionsListAdapter(getContext(), new ArrayList<Session>());
        for (int i = 0; i < bookmarkedIds.size(); i++) {
            Integer id = bookmarkedIds.get(i);
            Session session = dbSingleton.getSessionById(id);
            sessionsListAdapter.addItem(i, session);
        }

        bookmarkedTracks.setAdapter(sessionsListAdapter);
        bookmarkedTracks.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        return view;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_bookmarks;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share_bookmarks_url:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, Urls.WEB_APP_URL_BASIC + Urls.BOOKMARKS);
                intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share_links);
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_links)));
                break;
            default:
                //do nothing
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_bookmarks, menu);
    }
}