public class RealTimeDb {
    Context ctx;
    private static RealTimeDb realTimeDb;
    public final DatabaseReference dbRef;
    private final FirebaseDatabase database;
    private final MyValueEventListener valueEventListener;


    private RealTimeDb(Context ctx) {
        this.ctx = ctx;
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference(Constants.realTimeDb_refence).child(Constants.Users);
        String chatList = Constants.ChatList;
        valueEventListener = new MyValueEventListener(ctx);
    }

    public static RealTimeDb getInstance(Context ctx) {
        if (realTimeDb == null) {
            synchronized ("a") {
                if (realTimeDb == null) {
                    realTimeDb=new RealTimeDb(ctx);
                }
            }
        }
        return realTimeDb;
    }


    //test data
    public void sendStingTodb(String content) {
        DatabaseReference message  = database.getReference("message");
        message.setValue("hellow Firebase db");
        message.addValueEventListener(valueEventListener);
    }


    /**
     * set user info
     * @param userid
     * @param user
     * @return RealTimeDb
     */
    public RealTimeDb savaUserInfo(String userid, User user) {
        getUserRef(userid).setValue(user);
        return realTimeDb;
    }

    /**
     * set user info change listener
     * @param dataChange
     */
    public void setOnUserDataChagne(UserDataChange dataChange) {
        valueEventListener.dataChangelistener = dataChange;
    }

    /**
     * set messenge info change listener
     * @param messageDataChange
     */
    public void setOnMessageDataChange( MessageDataChange messageDataChange) {
        valueEventListener.messageDataChange = messageDataChange;
    }

    /**
     * set massenge list change listner
     * @param chatListDataChange
     */
    public void setOnChatListDataChange(ChatListDataChange chatListDataChange) {
        valueEventListener.chatListDataChange = chatListDataChange;
    }

    /**
     * 
     * @param dataChange
     */
    public RealTimeDb getUserData(UserDataChange dataChange) {
        setOnUserDataChagne(dataChange);
        return realTimeDb;
    }

    /**
     * get messenge list info reference
     * @param userid
     * @return
     */
    public DatabaseReference getChatListRef(String userid) {
        DatabaseReference dbRef = database.getReference(Constants.ChatList).child(userid);
        dbRef.addValueEventListener(valueEventListener);
        return dbRef;
    }

    /**
     * get user info reference
     * dbRef.addValueEventListener
     * @param userid
     * @return
     */
    public DatabaseReference getUserRef(String userid) {
        DatabaseReference dbRef = database.getReference(Constants.Users).child(userid);
        dbRef.addValueEventListener(valueEventListener);
        return dbRef;
    }

    /**
     * reference messenge info
     * @param messageid
     * @return
     */
    public DatabaseReference getMessageRef(String messageid) {
        String messages = Constants.Messages;
        DatabaseReference dbRef = database.getReference(Constants.Messages).child(messageid);
        dbRef.addValueEventListener(valueEventListener);
        return dbRef;
    }

    /**
     * update info
     * @param messageid
     */
    public void updataSession(String messageid,  SessionBean sessionBean,int position) {
        DatabaseReference  reference=  getMessageRef(messageid);

        Map<String, Object> map = new HashMap<>();
        map.put(position+"", sessionBean);
        reference.updateChildren(map);

    }

    public void updataChatList(String username){
        DatabaseReference reference = database.getReference(Constants.ChatList).child(username);
        ArrayList<ChatListBean> list= new ArrayList<>();

    }



}
