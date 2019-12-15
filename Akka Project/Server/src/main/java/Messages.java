

import java.io.Serializable;

public class Messages {

    public static final class Connect implements Serializable {

        public String username;

        public Connect(String username) {
            this.username = username;
        }

    }

    public static final class Disconnect implements Serializable {

        public String username;

        public Disconnect(String username) {
            this.username = username;

        }

    }

    public static final class SendTextToUser implements Serializable {

        public String username;
        public String target;
        public String text;

        public SendTextToUser(String username, String target, String text) {
            this.username = username;
            this.target = target;
            this.text = text;
        }

    }

    public static final class SendFileToUser implements Serializable {

        public String username;
        public String target;
        public String sourceFilePath;

        public SendFileToUser(String username, String target, String sourceFilePath) {
            this.username = username;
            this.target = target;
            this.sourceFilePath = sourceFilePath;
        }

    }

    public static final class CreateGroup implements Serializable {

        public String username;
        public String groupname;

        public CreateGroup(String username, String groupname) {
            this.username = username;
            this.groupname = groupname;
        }

    }

    public static final class LeaveGroup implements Serializable {

        public String username;
        public String groupname;

        public LeaveGroup(String username, String groupname) {
            this.username = username;
            this.groupname = groupname;
        }

    }

    public static final class SendTextToGroup implements Serializable {

        public String username;
        public String groupname;
        public String text;

        public SendTextToGroup(String username, String groupname, String text) {
            this.username = username;
            this.groupname = groupname;
            this.text = text;
        }

    }

    public static final class SendFileToGroup implements Serializable {

        public String username;
        public String groupname;
        public String sourceFilePath;

        public SendFileToGroup(String username, String groupname, String sourceFilePath) {
            this.username = username;
            this.groupname = groupname;
            this.sourceFilePath = sourceFilePath;
        }

    }

    public static final class InviteUserToGroup implements Serializable {

        public String username;
        public String groupname;
        public String target;

        public InviteUserToGroup(String username, String groupname, String target) {
            this.username = username;
            this.groupname = groupname;
            this.target = target;
        }

    }

    public static final class InvitePending implements Serializable {

        public String username;
        public String groupname;
        public String target;
        public String message;

        public InvitePending(String username, String groupname, String target, String message) {
            this.username = username;
            this.groupname = groupname;
            this.target = target;
            this.message = message;
        }

    }

    public static final class InviteAnswer implements Serializable {

        public String username;
        public String groupname;
        public String answer;

        public InviteAnswer(String username, String groupname, String answer) {
            this.username = username;
            this.groupname = groupname;
            this.answer = answer;
        }

    }

    public static final class RemoveUserFromGroup implements Serializable {

        public String username;
        public String groupname;
        public String target;

        public RemoveUserFromGroup(String username, String groupname, String target) {
            this.username = username;
            this.groupname = groupname;
            this.target = target;
        }

    }

    public static final class MuteUserInGroup implements Serializable {

        public String username;
        public String groupname;
        public String target;
        public double timeout;

        public MuteUserInGroup(String username, String groupname, String target, double timeout) {
            this.username = username;
            this.groupname = groupname;
            this.target = target;
            this.timeout = timeout;
        }

    }

    public static final class UnmuteUserInGroup implements Serializable {

        public String username;
        public String groupname;
        public String target;

        public UnmuteUserInGroup(String username, String groupname, String target) {
            this.username = username;
            this.groupname = groupname;
            this.target = target;
        }

    }

    public static final class PromoteCoAdminInGroup implements Serializable {

        public String username;
        public String groupname;
        public String target;

        public PromoteCoAdminInGroup(String username, String groupname, String target) {
            this.username = username;
            this.groupname = groupname;
            this.target = target;
        }

    }

    public static final class DemoteCoAdminInGroup implements Serializable {

        public String username;
        public String groupname;
        public String target;

        public DemoteCoAdminInGroup(String username, String groupname, String target) {
            this.username = username;
            this.groupname = groupname;
            this.target = target;
        }

    }


    /////////////////////////////////////// SERVER REPLY ///////////////////////////////////////

//    public static final class ServerReply implements Serializable {
//
//        public String serverReply;
//
//        public ServerReply(String serverReply) {
//            this.serverReply = serverReply;
//        }
//
//    }



}
