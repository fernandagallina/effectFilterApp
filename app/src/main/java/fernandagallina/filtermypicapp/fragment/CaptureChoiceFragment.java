package fernandagallina.filtermypicapp.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fernandagallina.filtermypicapp.ImageSource;
import fernandagallina.filtermypicapp.MainActivity;
import fernandagallina.filtermypicapp.R;

/**
 * Created by fernanda on 03/10/16.
 */

public class CaptureChoiceFragment extends Fragment{

    @InjectView(R.id.camera_button)
    ImageButton cameraButton;

    @InjectView(R.id.galeria_button)
    ImageButton galeriaButton;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_capturechoice, container, false);
        ButterKnife.inject(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).transitionFragments(ImageSource.CAMERA);
            }
        });

        galeriaButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).transitionFragments(ImageSource.GALERIA);
            }
        });
    }
}
