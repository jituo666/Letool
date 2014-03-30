package com.xjt.letool.data;


public final class DataSourceType {
    public static final int TYPE_NOT_CATEGORIZED = 0;
    public static final int TYPE_LOCAL = 1;
    public static final int TYPE_PICASA = 2;
    public static final int TYPE_CAMERA = 3;

//    private static final MediaPath PICASA_ROOT = MediaPath.fromString("/picasa");
//    private static final MediaPath LOCAL_ROOT = MediaPath.fromString("/local");
//
    public static int identifySourceType(MediaSet set) {
//        if (set == null) {
//            return TYPE_NOT_CATEGORIZED;
//        }
//
//        MediaPath path = set.getPath();
//        if (MediaSetUtils.isCameraSource(path)) return TYPE_CAMERA;
//
//        MediaPath prefix = path.getPrefixPath();
//
//        if (prefix == PICASA_ROOT) return TYPE_PICASA;
//        if (prefix == LOCAL_ROOT) return TYPE_LOCAL;

        return TYPE_NOT_CATEGORIZED;
    }
}
