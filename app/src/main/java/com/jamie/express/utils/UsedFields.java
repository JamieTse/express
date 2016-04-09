package com.jamie.express.utils;

/**
 * Created by jamie on 2016/3/16.
 */
public class UsedFields {

    public static final String ID = "id";
    //public static final String IMG_PREFIX = "thumb_";

    public static final class DBUserInfo {
        //public static final String ID = "id";
        public static final String USER_ID = "user_id";
        public static final String PASSWORD = "password";
        public static final String USER_NAME = "name";
        public static final String NICK = "nickname";
        public static final String SEX = "sex";
        public static final String ICON = "icon";
        public static final String ADDR = "address";
        public static final String TEL = "tel";
        public static final String QQ = "QQ";
        public static final String YEAR = "year";
        public static final String MAJOR = "major";
        public static final String ALIAS = "alias";
        public static final String _DEPARTMENT = "department_name";
        public static final String _COLLEGE = "college_name";
        public static final String _SCHOOL = "school_name";
        public static final String _PROVINCE = "province_name";
        public static final String _CITY = "city_name";
    }

    public static final class DBExpressage {
        public static final String EXPRESSAGE_ID = "expressage_id";
        public static final String FROM_USER = "from_user";
        public static final String ADDR = "address";
        public static final String SUBSTANCE = "substance";
        public static final String DEADLINE = "deadline";
        public static final String REWARD = "reward";
        public static final String DESCRIPTION = "description";
        public static final String APPLY_ID = "apply_id";
        public static final String CREATE_TIME = "create_time";
        public static final String _USER_NAME = "user_name";
        public static final String _USER_ICON = "user_icon";
        public static final String _USER_ADDR = "user_address";
        public static final String _EX_MISSION_DONE = "mission_done";
        public static final String _EX_EXPIRED = "expired";
        public static final String _APPLY_DISABLED = "disabled";
        public static final String _APPLY_CREDIT = "credit_id";
        public static final String _APPLY_TIME = "apply_time";
//        public static final String _APPLYER_NAME = "applyer_name";
//        public static final String _APPLYER_ICON = "applyer_icon";
        public static final String _LABEL_IDS = "label_ids";
    }

    public static final class DBApplyRecord {
        public static final String APPLY_ID = "apply_id";
        public static final String EX_ID = "expressage_id";
        public static final String EX_OWNER = "owner";
        public static final String EX_APPLYER = "applyer";
        public static final String DISABLED = "disabled";
        public static final String EXPIRED = "expired";
        public static final String MISSION_DONE = "mission_done";
        public static final String CREDIT_ID = "credit_id";
        public static final String CREATE_TIME = "create_time";
        public static final String _OWNER_NAME = "owner_name";
        public static final String _OWNER_ICON = "owner_icon";
        public static final String _APPLYER_NAME = "applyer_name";
        public static final String _APPLYER_ICON = "applyer_icon";
        public static final String _EX_ADDR = "expressage_address";
        public static final String _EX_SUBSTANCE = "substance";
        public static final String _EX_REWARD = "reward";
        public static final String _EX_DEADLINE = "deadline";
        public static final String _EX_DESCRIPTION = "description";
    }

    public static final class DBCreditRecord {
        public static final String CREDIT_ID = "credit_id";
        public static final String FROM_USER = "from_user";
        public static final String TO_USER = "to_user";
        public static final String APPLY_ID = "apply_id";
        public static final String EX_ID = "expressage_id";
        public static final String TYPE = "type";
        public static final String REMARK = "remark";
        public static final String CONTENT = "content";
        public static final String CREATE_TIME = "create_time";
        public static final String _FROM_NAME = "from_name";
        public static final String _FROM_ICON = "from_icon";
    }

    public static final class DBUserLabel {
        public static final String USER_LABEL_ID = "user_label_id";
        public static final String USER_ID = "user_id";
        public static final String LABEL_ID = "label_id";
        public static final String LABEL_TITLE = "label_title";
    }

    public static final class DBExpressageLabel {
        public static final String EX_LABEL_ID = "expressage_label_id";
        public static final String EX_ID = "expressage_id";
        public static final String LABEL_ID = "label_id";
        public static final String LABEL_TITLE = "label_title";
    }

    public static final class DBPush {
        public static final String PUSH_ID = "push_id";
        public static final String SENDNO = "sendno";
        public static final String TYPE = "type";
        public static final String TYPE_EXPLAIN = "type_explain";
        public static final String MSG = "msg";
        public static final String TO_NAME = "to_name";
        public static final String TO_ID = "to_id";
        public static final String IS_DEAL = "is_deal";
        public static final String CREATE_ID = "create_id";
        public static final String CREATE_NAME = "create_name";
        public static final String CREATE_ICON = "create_icon";
        public static final String APPLY_ID = "apply_id";
        public static final String EX_ID = "expressage_id";
        public static final String CREATE_TIME = "create_time";
        public static final String _EX_ADDR = "expressage_address";
        public static final String _EX_SUBSTANCE = "expressage_substance";
        public static final String _EX_REWARD = "expressage_reward";
    }

}
