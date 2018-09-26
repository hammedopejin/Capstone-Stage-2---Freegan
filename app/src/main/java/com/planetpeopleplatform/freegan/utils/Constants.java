package com.planetpeopleplatform.freegan.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Constants {

    //Firebase Reference
    public static FirebaseDatabase database;
    public static FirebaseDatabase getDatabase() {
        if (database == null) {
            database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
        }
        return database;
    }
    public static DatabaseReference firebase = getDatabase().getReference();

    public static FirebaseStorage storage = FirebaseStorage.getInstance();
    public static StorageReference storageRef = storage.getReferenceFromUrl("gs://freegan-eabd2.appspot.com");

    // The Constant EXTRA_DATA.
    public static final String EXTRA_DATA = "extraData";

    //User
    public static final String kOBJECTID = "objectId";
    public static final String kUSER = "users";
    public static final String kCREATEDAT = "createdAt";
    public static final String kUPDATEDAT = "updatedAt";
    public static final String kEMAIL = "email";
    public static final String kFACEBOOK = "facebook";
    public static final String kLOGINMETHOD = "loginMethod";
    public static final String kPUSHID = "pushId";
    public static final String kFIRSTNAME = "firstname";
    public static final String kLASTNAME = "lastname";
    public static final String kFULLNAME = "fullname";
    public static final String kUSERNAME = "userName";
    public static final String kUSERIMAGEURL = "userImgUrl";
    public static final String kCURRENTUSER = "currentUser";
    public static final String kLONGITUDE = "longitude";
    public static final String kLATITUDE = "latitude";
    public static final String kBLOCKEDUSER = "blockedUsers";

    //posts
    public static final String kPOST = "posts";
    public static final String kLIKE = "likes";
    public static final String kPOSTUSEROBJECTID = "postUserObjectId";
    public static final String kPOSTID = "postId";
    public static final String kPOSTER = "poster";
    public static final String kPOSTERNAME = "posterName";
    public static final String kPROFILEIMAGEURL = "profileImgUrl";
    public static final String kIMAGEURL = "imageUrl";
    public static final String kPOSTDATE = "postDate";
    public static final String kDESCRIPTION = "description";
    public static final String kPOSTLOCATION = "posts_location";

    //recent
    public static final String kRECENT = "recent";
    public static final String kCHATROOMID = "chatRoomID";
    public static final String kUSERID = "userId";
    public static final String kDATE = "date";
    public static final String kPRIVATE = "private";
    public static final String kGROUP = "group";
    public static final String kGROUPID = "groupId";
    public static final String kRECENTID = "recentId";
    public static final String kMEMBERS = "members";
    public static final String kDISCRIPTION = "discription";
    public static final String kLASTMESSAGE = "lastMessage";
    public static final String kCOUNTER = "counter";
    public static final String kTYPE = "type";
    public static final String kWITHUSERUSERNAME = "withUserUserName";
    public static final String kWITHUSERUSERID = "withUserUserID";
    public static final String kOWNERID = "ownerID";

    public static final String kNAME = "name";
    public static final String kSENDERID = "senderId";
    public static final String kSENDERNAME = "senderName";
    public static final String kTHUMBNAIL = "thumbnail";
    public static final String kCURRENTUSERID = "currentUserUid";

    //Message
    public static final String kSTATUS = "status";
    public static final String kMESSAGE = "message";
    public static final String kMESSAGEID = "messageId";
    public static final String kREPORTMESSAGE = "reportMessages";


    //Friends
    public static final String kFRIEND = "friends";
    public static final String kFRIENDID = "friendId";

    //message types
    public static final String kPICTURE = "picture";
    public static final String kTEXT = "text";
    public static final String kVIDEO = "video";
    public static final String kAUDIO = "audio";
    public static final String kLOCATION = "location";

    //Delete Dialog
    public static final String kTITLE = "title";
    public static final String kKEY= "key";
    public static final String kCHILDREF = "childRef";
    public static final String kPOSITION = "position";
    public static final String kFLAG = "flag";

    //Intent
    public static final String kBUNDLE = "bundle";
    public static final String kPOSTERID = "posterId";

    //SharedPreferences
    public static final String kSORT_BY = "sortBy";

    //JSON KEY
    public static final String JSONARRAYKEY = "uniqueArrays";

    //Report
    public static final String kREPORTDATE = "reportDate";

    //FirebaseMessagingService
    public static final String kINSTANCEID = "instanceId";

}
