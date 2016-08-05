package klim.mobile.android.testvk;


import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment implements AdapterView.OnItemClickListener, LoaderCallbacks<Cursor> {

    private VkUser vkUser;
    private DbManager db;
    private cursorAdapter adapter;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DbManager(getActivity());
        db.open();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_friends, container, false);
        vkUser = VkUser.getInstance();
        ListView friendList = (ListView) v.findViewById(R.id.friend_list);

        adapter = new cursorAdapter(getActivity(),null,0);

        friendList.setAdapter(adapter);

        ((MainActivity) getActivity()).getSupportLoaderManager().initLoader(0,null,this);
        friendList.setOnItemClickListener(this);


        return v;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = db.getData((int) id);
        int user_id =0;
        if (cursor.moveToFirst()){
            user_id = cursor.getInt(cursor.getColumnIndex("user_id"));
        }
        cursor.close();
        GalleryFragment gf = new GalleryFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("user_id", user_id);
        gf.setArguments(bundle);
        ((MainActivity) getActivity()).nextFragment(gf);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        dbCursorLoader dbLoader = new dbCursorLoader(getActivity(),db);
        return dbLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    static class dbCursorLoader extends CursorLoader {
        DbManager db;
        public dbCursorLoader(Context context, DbManager db){
            super(context);
            this.db = db;
        }

        @Override
        public Cursor loadInBackground() {
            return db.getAllData();
        }
    }

    private class cursorAdapter extends CursorAdapter{
        private LayoutInflater cursorInflater;

        public cursorAdapter(Context context, Cursor cursor, int flags) {
            super(context, cursor, flags);
            cursorInflater = LayoutInflater.from(context);
        }

        public void bindView(View view, Context context, Cursor cursor) {

            TextView textName = (TextView) view.findViewById(R.id.friend_name);
            TextView textStatus = (TextView) view.findViewById(R.id.friend_status);
            ImageView imgFoto = (ImageView) view.findViewById(R.id.friend_picture);

            String name = cursor.getString( cursor.getColumnIndex( DbManager.COLUMN_NAME ) );
            String status = cursor.getString(cursor.getColumnIndex( DbManager.COLUMN_STATUS ));
            byte[] img = cursor.getBlob(cursor.getColumnIndex( DbManager.COLUMN_PHOTO ));
            textName.setText(name);
            textStatus.setText(status);
            imgFoto.setImageBitmap(DbManager.getImage(img));
        }

        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return cursorInflater.inflate(R.layout.item_friend, null);
        }
    }

}
