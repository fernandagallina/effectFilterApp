package fernandagallina.filtermypicapp;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fernandagallina.filtermypicapp.effect.EffectContent;
import fernandagallina.filtermypicapp.effect.GLToolbox;
import fernandagallina.filtermypicapp.effect.MyItemRecyclerViewAdapter;
import fernandagallina.filtermypicapp.effect.TextureRender;

/**
 * Created by fernanda on 04/10/16.
 */

public class EffectFragment extends Fragment implements GLSurfaceView.Renderer{

    private EffectContext mEffectContext;
    private Effect mEffect;
    private boolean mInitialized = false;
    private int[] mTextures = new int[2];
    private TextureRender mTexRenderer = new TextureRender();

    @InjectView(R.id.image_preview)
    GLSurfaceView mEffectView;

    @InjectView(R.id.list)
    RecyclerView recyclerView;

    String stringUri;
    Bitmap bitmap;

    // TODO: Customize parameters
    private int mColumnCount = 1;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";

    private OnListFragmentInteractionListener mListener;
    private int mCurrentEffect;
    private int mImageWidth;
    private int mImageHeight;

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

            //imagePreview.setImageBitmap(bitmap);
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEffectView.setEGLContextClientVersion(2);
        mEffectView.setRenderer(this);
        mEffectView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mCurrentEffect = 0;
    }

    private void loadTextures() {
        GLES20.glGenTextures(2, mTextures, 0);

        mImageWidth = bitmap.getWidth();
        mImageHeight = bitmap.getHeight();
        mTexRenderer.updateTextureSize(mImageWidth, mImageHeight);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        GLToolbox.initTexParams();
    }

    private void initEffect() {
        EffectFactory effectFactory = mEffectContext.getFactory();
        if (mEffect != null) {
            mEffect.release();
        }
        // Initialize the correct effect based on the selected menu/action item
        switch (mCurrentEffect) {

            case 0:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_AUTOFIX);
                break;

            case R.drawable.duotone:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_DUOTONE);
                mEffect.setParameter("first_color", Color.YELLOW);
                mEffect.setParameter("second_color", Color.DKGRAY);
                break;

            case R.drawable.lomoish:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_LOMOISH);
                break;

            default:
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = new OnListFragmentInteractionListener() {
                @Override
                public void onListFragmentInteraction(EffectContent.EffectItem item) {
                    switch (item.imageResource) {
                        case R.drawable.duotone:
                            mCurrentEffect = R.drawable.duotone;
                            break;

                        case R.drawable.lomoish:
                            mCurrentEffect = R.drawable.lomoish;
                            break;

                        default:
                            break;
                    }
                    mEffectView.requestRender();
                }
            };
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    private void applyEffect() {
        mEffect.apply(mTextures[0], mImageWidth, mImageHeight, mTextures[1]);
    }

    private void renderResult() {
        if (mCurrentEffect != R.id.none) {
            // if no effect is chosen, just render the original bitmap
            mTexRenderer.renderTexture(mTextures[1]);
        } else {
            // render the result of applyEffect()
            mTexRenderer.renderTexture(mTextures[0]);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (mTexRenderer != null) {
            mTexRenderer.updateViewSize(width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (!mInitialized) {
            //Only need to do this once
            mEffectContext = EffectContext.createWithCurrentGlContext();
            mTexRenderer.init();
            loadTextures();
            mInitialized = true;
        }
        if (mCurrentEffect != R.id.none) {
            //if an effect is chosen initialize it and apply it to the texture
            initEffect();
            applyEffect();
        }
        renderResult();
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(EffectContent.EffectItem item);
    }
}
