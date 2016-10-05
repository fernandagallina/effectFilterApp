package fernandagallina.filtermypicapp.effect;

import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import fernandagallina.filtermypicapp.R;

/**
 * Created by fernanda on 05/10/16.
 */

public class EffectContent {

    static String[] titleEffect = {
            "Lomoish",
            "Duotone"
    };

    static Integer[] imageId = {
            R.drawable.lomoish,
            R.drawable.duotone
    };

    public static final List<EffectItem> ITEMS = new ArrayList<>();

    private static final int COUNT = 2;

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
