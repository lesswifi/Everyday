package compsci290.edu.duke.myeveryday.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yx78 on 4/16/17.
 */

public class Constants {
    public final static int JOURNALS = 1;
    public final static int TAGS = 2;
    public final static int ANALYTICS = 3;
    public final static int ATLAS = 4;
    public final static int LOGOUT = 5;
    public final static int DELETE = 6;

    public static final HashMap<Integer, String> CURRENT_NAME;
    static
    {
        CURRENT_NAME = new HashMap<Integer, String>();
        CURRENT_NAME.put(1, "Journals");
        CURRENT_NAME.put(2, "Tags");
        CURRENT_NAME.put(3, "Analytics");
        CURRENT_NAME.put(4, "Atlas");
    }


    public static final String SERIALIZED_CATEGORY = "serialized_category";
    public static final String DEFAULT_CATEGORY = "General";

    public static final String NOTE_CLOUD_END_POINT = "/notes";
    public static final String CATEGORY_CLOUD_END_POINT = "/categories";
    public static final String USERS_CLOUD_END_POINT = "/users/";
    public static final String SERIALZED_CATEGORY = "serialized_category";

    public final static String SELECTED_CATEGORY_ID = "selected_category_id";
    public static final String SERIALIZED_NOTE = "serialized_note";


    public final static String MIME_TYPE_IMAGE_EXT = ".jpg";

}
