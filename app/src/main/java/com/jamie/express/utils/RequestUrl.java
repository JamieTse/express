package com.jamie.express.utils;

/**
 * Created by jamie on 2016/3/16.
 */
public class RequestUrl {

    /**
     * 服务器地址
     */
    public static final String SERVER_ROOT = "http://192.168.56.1/app_express";

    //public static final String SERVER_ROOT = "http://jamieexpress.applinzi.com";

    public static final String IMG_URL = SERVER_ROOT + "/Public/upload/";

    //public static final String IMG_URL = "http://jamieexpress-public.stor.sinaapp.com/upload/";

    public static final String LOGIN_URL = SERVER_ROOT + "/index.php/Login/do_login";

    public static final String SET_ALIAS_URL = SERVER_ROOT + "/index.php/Login/set_alias";

    public static final String GET_EXPRESSAGES_URL = SERVER_ROOT + "/index.php/Expressages/get_expressages";

    //参数expressage_id
    public static final String GET_SINGLE_EXPRESSAGE_URL = SERVER_ROOT + "/index.php/Expressages/get_single";

    //参数addr,deadline,description,from_user,reward,substance
    public static final String POST_URL = SERVER_ROOT + "/index.php/Expressages/do_post";

    //参数page,userid
    public static final String GET_POST_RECORD_URL = SERVER_ROOT + "/index.php/Expressages/post_record";

    public static final String GET_ALL_TAGS_URL = SERVER_ROOT + "/index.php/Tags/get_all_tags";

    //参数expressage_id
    public static final String GET_EXPRESSAGE_TAGS_URL = SERVER_ROOT + "/index.php/Tags/get_expressage_tags";

    //参数user_id
    public static final String GET_USER_TAGS_URL = SERVER_ROOT + "/index.php/Tags/get_user_tags";

    //参数label_ids,tags,msg,expressage_id,user_id,name,icon
    public static final String PUSH_BY_TAGS_URL = SERVER_ROOT + "/index.php/Tags/push_by_tags";

    //参数user_id,label_ids
    public static final String SET_USER_TAGS_URL = SERVER_ROOT + "/index.php/Tags/set_user_tags";

    //参数page,user_id
    public static final String GET_USER_APPLYS_URL = SERVER_ROOT + "/index.php/Applys/get_user_applys";

    //参数apply_id
    public static final String DISABLE_MISSION_URL = SERVER_ROOT + "/index.php/Applys/disable_mission";

    //参数apply_id
    public static final String EXPIRE_MISSION_URL = SERVER_ROOT + "/index.php/Applys/expire_mission";

    //参数apply_id
    public static final String DONE_MISSION_URL = SERVER_ROOT + "/index.php/Applys/done_mission";

    //参数user_id,page
    public static final String TO_COMMENTS_URL = SERVER_ROOT + "/index.php/Applys/to_comments";

    //参数applyer,applyer_name,applyer_icon,owner,owner_name,expressage_id
    public static final String DO_HELP_URL = SERVER_ROOT + "/index.php/Applys/do_help";

    //参数apply_id
    public static final String GET_APPLY_URL = SERVER_ROOT + "/index.php/Applys/get_single_apply";

    //参数page,user_id
    public static final String GET_USER_CREDITS_URL = SERVER_ROOT + "/index.php/Credits/get_user_credits";

    //参数from_user,to_user,apply_id,expressage_id,type(1正常,2取消,3过时),remark,content
    public static final String DO_CREDIT_URL = SERVER_ROOT + "/index.php/Credits/do_credit";

    //to_id,to_name,msg,expressage_id,apply_id,create_id,create_name,create_icon
    public static final String PUSH_PHONE_URL = SERVER_ROOT + "/index.php/My/push_phone";

    public static final String PUSH_APPLY_URL = SERVER_ROOT + "/index.php/My/push_apply";

    //to_id,to_name,msg,expressage_id,apply_id,create_id,create_name,create_icon
    public static final String PUSH_MISSION_DONE_URL = SERVER_ROOT + "/index.php/My/push_mission_done";

    //to_id,to_name,msg,expressage_id,apply_id,create_id,create_name,create_icon
    public static final String PUSH_CREDIT_DONE_URL = SERVER_ROOT + "/index.php/My/push_credit_done";

    //to_id,to_name,msg,expressage_id,apply_id,create_id,create_name,create_icon
    public static final String PUSH_DISABLED_URL = SERVER_ROOT + "/index.php/My/push_disabled";

    public static final String PUSH_EXPIRED_URL = SERVER_ROOT + "/index.php/My/push_expired";

    public static final String UPLOAD_ICON_URL = SERVER_ROOT + "/index.php/My/upload_icon";

    //参数page,user_id
    public static final String GET_USER_MESSAGES_URL = SERVER_ROOT + "/index.php/Messages/get_user_messages";

}
