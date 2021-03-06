package fernandagallina.filtermypicapp.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fernandagallina.filtermypicapp.R;
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

    @InjectView(R.id.save_button)
    FloatingActionButton saveButton;

    @InjectView(R.id.share)
    FloatingActionButton shareButton;

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

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_effect, container, false);
        ButterKnife.inject(this, view);

        recyclerView.setNestedScrollingEnabled(false);

        Uri uri = Uri.parse(stringUri);
        getActivity().getContentResolver().notifyChange(uri, null);
        ContentResolver cr = getActivity().getContentResolver();
        try {
            bitmap = android.provider.MediaStore.Images.Media
                            .getBitmap(cr, uri);
            bitmap = getResizedBitmap(bitmap, 640);

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
        mEffectView.setEGLConfigChooser(8, 8, 8, 8, 8, 0);
        mEffectView.setRenderer(this);
        mEffectView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mEffectView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mCurrentEffect = 0;

        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                saveImage();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                share();
            }
        });
    }

    public void share() {
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_STREAM, getImageUri());
        startActivity(Intent.createChooser(shareIntent, "Share image using"));
    }

    public Uri getImageUri() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void saveImage() {
        FileOutputStream out;
        File file = new File(Environment.getExternalStorageDirectory(),  "effectPic.png");
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

            out.flush();
            out.close();

            MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
            Toast.makeText(getActivity(), "File Saved", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Save Failed", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Save Failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl)
            throws OutOfMemoryError {
        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);

        try {
            gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
            int offset1, offset2;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    int texturePixel = bitmapBuffer[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            return null;
        }

        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
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

            case R.drawable.x:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_ROTATE);
                mEffect.setParameter("angle", 0);
                break;

            case R.drawable.warm:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_TEMPERATURE);
                mEffect.setParameter("scale", 1f);
                break;

            case R.drawable.cool:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_TEMPERATURE);
                mEffect.setParameter("scale", 0f);
                break;

            case R.drawable.duotone:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_DUOTONE);
                mEffect.setParameter("first_color", Color.GREEN);
                mEffect.setParameter("second_color", Color.BLUE);
                break;

            case R.drawable.duotoneredblue:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_DUOTONE);
                mEffect.setParameter("first_color", Color.RED);
                mEffect.setParameter("second_color", Color.BLUE);
                break;

            case R.drawable.duotonepinkyellow:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_DUOTONE);
                mEffect.setParameter("first_color", Color.MAGENTA);
                mEffect.setParameter("second_color", Color.YELLOW);
                break;

            case R.drawable.lomoish:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_LOMOISH);
                break;

            case R.drawable.gray:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_GRAYSCALE);
                break;

            case R.drawable.negative:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_NEGATIVE);
                break;

            case R.drawable.posterize:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_POSTERIZE);
                break;

            case R.drawable.crossprocess:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_CROSSPROCESS);
                break;

            case R.drawable.documentary:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_DOCUMENTARY);
                break;

            case R.drawable.fisheye:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_FISHEYE);
                mEffect.setParameter("scale", .9f);
                break;

            case R.drawable.tint:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_TINT);
                mEffect.setParameter("tint", Color.MAGENTA);
                break;

            case R.drawable.vignette:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_VIGNETTE);
                mEffect.setParameter("scale", .7f);
                break;

            case R.drawable.sepia:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_SEPIA);
                break;

            case R.drawable.grain:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_GRAIN);
                mEffect.setParameter("strength", .85f);
                break;

            case R.drawable.horizontal:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_FLIP);
                mEffect.setParameter("horizontal", true);
                break;

            case R.drawable.vertical:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_FLIP);
                mEffect.setParameter("vertical", true);
                break;

            case R.drawable.sharpen:
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_STRAIGHTEN);
                mEffect.setParameter("angle", -40f);
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

                    mCurrentEffect = item.imageResource;

                    mEffectView.requestRender();
                }
            };
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnListFragmentInteractionListener) {
            mListener = new OnListFragmentInteractionListener() {
                @Override
                public void onListFragmentInteraction(EffectContent.EffectItem item) {

                    mCurrentEffect = item.imageResource;

                    mEffectView.requestRender();
                }
            };
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    private void applyEffect() {
        if(mEffect != null)
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
        bitmap = createBitmapFromGLSurface(0, 0, mEffectView.getWidth(), mEffectView.getHeight(), gl);
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(EffectContent.EffectItem item);
    }
}
