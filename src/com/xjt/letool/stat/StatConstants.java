
package com.xjt.letool.stat;

public class StatConstants {

    public static final String UMENG_APP_RELEASE_KEY = "539289e956240b9f6902fa3c";
    public static final String UMENG_APP_DEBUG_KEY = "538f2c0456240ba4a0059794";

    public static final String UMENG_TEST_CHANNEL_ID = "com-xjt-letool-umeng-test-id";

    //
    public static final String EVENT_KEY_PHOTO_LONG_PRESSED = "picture_lp"; // 照片界面长安
    public static final String EVENT_KEY_VIDEO_LONG_PRESSED = "video_lp"; // 视频界面长安
    public static final String EVENT_KEY_GALLERY_LONG_PRESSED = "gallery_lp"; // 图库或者视频集界面长安
    public static final String EVENT_KEY_SELECT_OK = "select_ok"; // 选择完成确认
    public static final String EVENT_KEY_PHOTO_DELETE = "ph_delete"; // 小图界面删除
    public static final String EVENT_KEY_VIDEO_DELETE = "video_delete"; // 视频界面删除

    public static final String EVENT_KEY_CLICK_PHOTO = "clk_photo"; // 点击照片
    public static final String EVENT_KEY_CLICK_PICTURE = "clk_pic"; // 点击图片
    public static final String EVENT_KEY_CLICK_VIDEO = "clk_video"; // 点击录像
    public static final String EVENT_KEY_CLICK_MOVIE = "clk_movie"; // 点击视频
    //
    public static final String EVENT_KEY_SLIDE_MENU = "slid_menu";// 点击侧面栏抽屉进入侧面栏
    public static final String EVENT_KEY_HARD_MENU_MENU = "hard_slid_menu"; // 从硬件菜单进入侧面栏
    public static final String EVENT_KEY_SLIDE_MENU_PICTURE = "sl_picture"; // 侧边栏进入图片
    public static final String EVENT_KEY_SLIDE_MENU_VIDEO = "sl_video"; // 侧边栏进入视频
    public static final String EVENT_KEY_SLIDE_MENU_SETTING = "sl_setting"; // 侧边栏进入设置
    public static final String EVENT_KEY_SLIDE_MENU_EXIT = "sl_exit"; // 侧边栏退出
    //
    public static final String EVENT_KEY_FULL_IMAGE_SHARE = "full_share"; //大图分享
    public static final String EVENT_KEY_FULL_IMAGE_SHARE_OK = "full_share_ok"; //大图分享
    public static final String EVENT_KEY_FULL_IMAGE_DETAIL = "full_detail"; // 大图详情
    public static final String EVENT_KEY_FULL_IMAGE_DELETE_OK = "full_delete_ok"; // 大图删除一次完成
    public static final String EVENT_KEY_FULL_IMAGE_DOUBLE_CLICK = "double_click"; //大图双击
    public static final String EVENT_KEY_FULL_IMAGE_FilM_MODE = "film_mode"; // 电影模式
    //
    public static final String EVENT_KEY_CAMERA_SRC_SETTING = "camera_src_set"; // 照片目录设置
    public static final String EVENT_KEY_CAMERA_SRC_SETTING_OK = "camera_src_set_ok"; // 照片目录设置后保存
    public static final String EVENT_KEY_CLEAR_CAHCE = "clear_cache"; // 清理缓存
    public static final String EVENT_KEY_UPDATE_CHECK = "update_check"; // 更新检查
    public static final String EVENT_KEY_QQ_ADD = "qq_add"; // 加QQ
    public static final String EVENT_KEY_QQ_GROUP_ADD = "qq_g_add"; // 加QQ群
}
