package fernandagallina.filtermypicapp;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by fernanda on 04/10/16.
 */

public class EffectFragment extends Fragment {

    @InjectView(R.id.image_preview)
    ImageView imagePreview;

    @InjectView(R.id.list)
    RecyclerView recyclerView;

    String stringUri;
    Bitmap bitmap;

    // TODO: Customize parameters
    private int mColumnCount = 1;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";

    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EffectFragment() {
    }

    public static EffectFragment newInstance(String stringUri) {
        EffectFragment effectFragment = new EffectFragment();

        Bundle args = new Bundle();
        args.putString("image", stringUri);
        effectFragment.setArguments(args);

        return effectFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            stringUri = getArguments().getString("image");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_effect, container, false);
        ButterKnife.inject(this, view);

        Uri uri = Uri.parse(stringUri);
        getActivity().getContentResolver().notifyChange(uri, null);
        ContentResolver cr = getActivity().getContentResolver();
        try {
            bitmap = android.provider.MediaStore.Images.Media
                            .getBitmap(cr, uri);
            imagePreview.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), mColumnCount, LinearLayoutManager.HORIZONTAL, false));
        }
        recyclerView.setAdapter(new MyItemRecyclerViewAdapter(EffectContent.ITEMS, mListener));
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(EffectContent.EffectItem item);
    }
}
