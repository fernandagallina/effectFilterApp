package fernandagallina.filtermypicapp.effect;

import java.util.ArrayList;
import java.util.List;

import fernandagallina.filtermypicapp.R;

/**
 * Created by fernanda on 05/10/16.
 */

public class EffectContent {

    static String[] titleEffect = {
            "Lomoish",
            "Duotone",
            "GrayScale",
            "Posterize",
            "Tint",
            "Vignette",
            "FishEye",
            "Negative",
            "Sepia"
    };

    static Integer[] imageId = {
            R.drawable.lomoish,
            R.drawable.duotone,
            R.drawable.gray,
            R.drawable.posterize,
            R.drawable.tint,
            R.drawable.vignette,
            R.drawable.fisheye,
            R.drawable.negative,
            R.drawable.sepia
    };

    public static final List<EffectItem> ITEMS = new ArrayList<>();

    private static final int COUNT = 8;

    static {
        // Add some sample items.
        for (int i = 0; i <= COUNT-1; i++) {
            addItem(createEffectItem(titleEffect[i], imageId[i]));
        }
    }

    private static void addItem(EffectItem item) {
        ITEMS.add(item);
    }

    private static EffectItem createEffectItem(String title, Integer image) {
        return new EffectItem(title, image);
    }
    public static class EffectItem {
        public final String title;
        public final Integer imageResource;

        public EffectItem(String title, Integer image) {
            this.title = title;
            this.imageResource = image;
        }

    }
}
