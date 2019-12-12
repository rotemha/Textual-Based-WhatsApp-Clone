package com.lightbend.akka.sample;

public class Messages {

    public static final class Connect {

        public String username;

        public Connect(String username) {
            this.username = username;
        }

    }

    public static final class Disconnect {

        public String username;

        public Disconnect(String username) {
            this.username = username;

        }

    }

    public static final class SendTextToUser {

        public String username;
        public String target;
        public String text;

        public SendTextToUser(String username, String target, String text) {
            this.username = username;
            this.target = target;
            this.text = text;
        }

    }

    public static final class SendFileToUser {

        public String username;
        public String target;
        public String sourceFilePath;

        public SendFileToUser(String username, String target, String sourceFilePath) {
            this.username = username;
            this.target = target;
            this.sourceFilePath = sourceFilePath;
        }

    }

    public static final class CreateGroup {

        public String username;
        public String groupname;

        public CreateGroup(String username, String groupname) {
            this.username = username;
            this.groupname = groupname;
        }

    }

    public static final class LeaveGroup {

        public String username;
        public String groupname;

        public LeaveGroup(String username, String groupname) {
            this.username = username;
            this.groupname = groupname;
        }

    }

    public static final class SendTextToGroup {

        public String username;
        public String groupname;
        public String text;

        public SendTextToGroup(String username, String groupname, String text) {
            this.username = username;
            this.groupname = groupname;
            this.text = text;
        }

    }

    public static final class SendFileToGroup {

        public String username;
        public String groupname;
        public String sourceFilePath;

        public SendFileToGroup(String username, String groupname, String sourceFilePath) {
            this.username = username;
            this.groupname = groupname;
            this.sourceFilePath = sourceFilePath;
        }

    }

    public static final class InviteUserToGroup {

        public String username;
        public String groupname;
        public String target;

        public InviteUserToGroup(String username, String groupname, String target) {
            this.username = username;
            this.groupname = groupname;
            this.target = target;
        }

    }

    public static final class InvitePending {

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

    public static final class InviteAnswer {

        public String username;
        public String groupname;
        public String target;
        public String answer;

        public InviteAnswer(String username, String groupname, String target, String answer) {
            this.username = username;
            this.groupname = groupname;
            this.target = target;
            this.answer = answer;
        }

    }

    public static final class RemoveUserFromGroup {

        public String username;
        public String groupname;
        public String target;

        public RemoveUserFromGroup(String username, String groupname, String target) {
            this.username = username;
            this.groupname = groupname;
            this.target = target;
        }

    }

    public static final class MuteUserInGroup {

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

    public static final class UnmuteUserInGroup {

        public String username;
        public String groupname;
        public String target;

        public UnmuteUserInGroup(String username, String groupname, String target) {
            this.username = username;
            this.groupname = groupname;
            this.target = target;
        }

    }

    public static final class PromoteCoAdminInGroup {

        public String username;
        public String groupname;
        public String target;

        public PromoteCoAdminInGroup(String username, String groupname, String target) {
            this.username = username;
            this.groupname = groupname;
            this.target = target;
        }

    }

    public static final class DemoteCoAdminInGroup {

        public String username;
        public String groupname;
        public String target;

        public DemoteCoAdminInGroup(String username, String groupname, String target) {
            this.username = username;
            this.groupname = groupname;
            this.target = target;
        }

    }


    /////////////////////////////////////// SERVER REPLIES ///////////////////////////////////////

    public static final class ServerReply {

        public String serverReply;

        public ServerReply(String serverReply) {
            this.serverReply = serverReply;
        }

    }


}
