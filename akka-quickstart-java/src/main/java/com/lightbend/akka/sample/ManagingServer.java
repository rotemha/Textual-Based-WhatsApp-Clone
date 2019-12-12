package com.lightbend.akka.sample;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ManagingServer extends AbstractActor {

    // key: userA
    // value: userA's actorRef
    private ConcurrentHashMap<String, ActorRef> usersList = new ConcurrentHashMap<>();

    // concurrent list of group names
    private ConcurrentLinkedQueue<String> groupsList = new ConcurrentLinkedQueue<>();

    // key: groupA's name
    // value: groupA's Users (admin is part of groupA's users)
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> groupAllUsers = new ConcurrentHashMap<>();

    // key: groupA's name
    // value: groupA's admin
    private ConcurrentHashMap<String, String> groupAdmins = new ConcurrentHashMap<>();

    // key: groupA's name
    // value: groupA's list of coAdmins (not including the admin himself)
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> groupCoAdmins = new ConcurrentHashMap<>();

    // key: groupA's name
    // value: groupA's list of mutedUsers
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> groupMutedUsers = new ConcurrentHashMap<>();

    // key: groupA's name
    // value: groupA's list of regularUsers
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> groupRegularUsers = new ConcurrentHashMap<>();


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Messages.Connect.class, this::onConnect)
                .match(Messages.Disconnect.class, this::onDisconnect)
                .match(Messages.CreateGroup.class, this::onCreateGroup)
                .match(Messages.LeaveGroup.class, this::onLeaveGroup)
                .match(Messages.SendTextToGroup.class, this::onSendTextToGroup)
                .match(Messages.SendFileToGroup.class, this::onSendFileToGroup)
                .match(Messages.InviteUserToGroup.class, this::onInviteUserToGroup)
                .match(Messages.RemoveUserFromGroup.class, this::onRemoveUserFromGroup)
                .match(Messages.MuteUserInGroup.class, this::onMuteUserInGroup)
                .match(Messages.UnmuteUserInGroup.class, this::onUnmuteUserInGroup)
                .match(Messages.PromoteCoAdminInGroup.class, this::onPromoteCoAdminInGroup)
                .match(Messages.DemoteCoAdminInGroup.class, this::onDemoteCoAdminInGroup)
                .build();
    }


    private void onConnect(Messages.Connect message) {
        // need to check if the server is down (from the client's side) and act accordingly
        // (how can we know if the server is down? how much time should we wait for an ack/error?)
        if (usersList.containsKey(message.username)) {
            getSender().tell(new Messages.ServerReply(message.username + " is in use!"), ActorRef.noSender());
        } else {
            usersList.put(message.username, getSender());
            getSender().tell(new Messages.ServerReply(message.username + " has connected successfully!"), ActorRef.noSender());
        }
    }

    private void onDisconnect(Messages.Disconnect message) {
        // need to check if the server is down (from the client's side) and act accordingly
        // (how can we know if the server is down? how much time should we wait for an ack/error?)
        if (usersList.containsKey(message.username)) {
            leaveAllGroups(message.username);
            usersList.remove(message.username);
            getSender().tell(message.username + " has been disconnected successfully!", ActorRef.noSender());
        } else {
            // I ADDED THIS ERROR
            getSender().tell(new Messages.ServerReply(message.username + " does not exist!"), ActorRef.noSender());
            // I ADDED THIS ERROR
        }
    }

    private void onCreateGroup(Messages.CreateGroup message) {
        if (groupsList.contains(message.groupname)) {
            getSender().tell(message.groupname + " already exists!", ActorRef.noSender());
        } else {
            // add groupname to the list of groups
            groupsList.add(message.groupname);
            // set username as the group's admin
            groupAdmins.put(message.groupname, message.username);
            // initialize the list of users
            groupAllUsers.put(message.groupname, new ConcurrentLinkedQueue<>());
            // add username to the list of users in message.groupname
            groupAllUsers.get(message.groupname).add(message.username);
            // initialize the list of coAdmins
            groupCoAdmins.put(message.groupname, new ConcurrentLinkedQueue<>());
            getSender().tell(new Messages.ServerReply(message.groupname + " created successfully!"), ActorRef.noSender());
        }
    }

    private void onLeaveGroup(Messages.LeaveGroup message) {
        if (!groupAllUsers.get(message.groupname).contains(message.username)) {
            getSender().tell(new Messages.ServerReply(message.username + " is not in " + message.groupname + "!"), ActorRef.noSender());
        } else {
            // remove user from the group
            groupAllUsers.get(message.groupname).remove(message.username);
            groupRegularUsers.get(message.groupname).remove(message.username);
            groupMutedUsers.get(message.groupname).remove(message.username);

            // if the user is a coAdmin then remove this privilege
            groupCoAdmins.get(message.groupname).remove(message.username);

            // BROADCAST TO GROUP
            broadcastFromServer(message.groupname, message.username + " has left " + message.groupname + "!");

            // if the user is the admin of the group then we delete the group
            if (groupAdmins.get(message.groupname).equals(message.username)) {
                broadcastFromServer(message.groupname, message.groupname + " admin has closed " + message.groupname + "!");
                deleteGroup(message.groupname);
            }
        }
    }

    private void onSendTextToGroup(Messages.SendTextToGroup message) {
        // successful text send
        if (validMessageToGroup(message.groupname, message.username)) {
            broadcastFromUser(message.groupname, message.username, message.text);
        }
    }

    private void onSendFileToGroup(Messages.SendFileToGroup message) {
        // successful file send
        if (validMessageToGroup(message.groupname, message.username)) {
//            if (sourceFilePath is not valid) {
//                getSender().tell(new Messages.ServerReply(message.sourceFilePath + " does not exists!"), ActorRef.noSender());
//            } else {
            /////////////////////////////// NEEDS CHANGING ///////////////////////////////
            broadcastFromUser(message.groupname, message.username, message.sourceFilePath);
            /////////////////////////////// NEEDS CHANGING ///////////////////////////////
//            }
        }
    }

    private void onInviteUserToGroup(Messages.InviteUserToGroup message) {
        if (validInviteRemovalMuteUnmuteInGroup(message.groupname, message.username, message.target)) {
            // target is already in group
            if (groupAllUsers.get(message.groupname).contains(message.target)) {
                getSender().tell(new Messages.ServerReply(message.target + " is already in " + message.groupname + "!"), ActorRef.noSender());
            } else {
                usersList.get(message.target).tell(new Messages.InvitePending(message.username, message.groupname, message.target, "You have been invited to " + message.groupname + ", Accept?"), usersList.get(message.username));
            }
        }
    }

    private void onRemoveUserFromGroup(Messages.RemoveUserFromGroup message) {
        if (validInviteRemovalMuteUnmuteInGroup(message.groupname, message.username, message.target)) {
            // target is not in group
            if (!groupAllUsers.get(message.groupname).contains(message.target)) {
                getSender().tell(new Messages.ServerReply(message.target + " is not in " + message.groupname + "!"), ActorRef.noSender());
            }
            // target is the group's admin
            else if (groupAdmins.get(message.groupname).contains(message.target)) {
                getSender().tell(new Messages.ServerReply(message.username + " is an admin thus can't be removed!"), ActorRef.noSender());
            }
            // target removed
            else {
//                getSender().tell(new Messages.LeaveGroup(message.target, message.groupname), ActorRef.noSender());
                // remove user from the group
                groupAllUsers.get(message.groupname).remove(message.target);
                groupRegularUsers.get(message.groupname).remove(message.target);
                groupMutedUsers.get(message.groupname).remove(message.target);
                // if the user is a coAdmin then remove this privilege
                groupCoAdmins.get(message.groupname).remove(message.target);

                usersList.get(message.target).tell(new Messages.ServerReply("You have been removed from " + message.groupname + " by " + message.username + "!"), ActorRef.noSender());
            }
        }
    }

    private void onMuteUserInGroup(Messages.MuteUserInGroup message) {
        if (validInviteRemovalMuteUnmuteInGroup(message.groupname, message.username, message.target)) {
            // target is a co-admin and username is a co-admin
            if (groupCoAdmins.get(message.groupname).contains(message.target) && groupCoAdmins.get(message.groupname).contains(message.username)) {
                getSender().tell(new Messages.ServerReply(message.username + " is a co-admin thus can't mute the target co-admin" + message.target + "!"), ActorRef.noSender());
            }
            // already muted user
            else if (groupMutedUsers.get(message.groupname).containsKey(message.username)) {
                groupMutedUsers.get(message.groupname).replace(message.username, message.timeout);
                usersList.get(message.target).tell(new Messages.ServerReply("You have been muted for " + message.timeout + " in " + message.groupname + " by " + message.username + "!"), ActorRef.noSender());
            }
            // target is the group's admin
            else if (groupAdmins.get(message.groupname).contains(message.target)) {
                getSender().tell(new Messages.ServerReply(message.target + " is the admin thus can't be muted!"), ActorRef.noSender());
            }
            // target muted
            else {
                groupCoAdmins.get(message.groupname).remove(message.target);
                groupRegularUsers.get(message.groupname).remove(message.target);
                groupMutedUsers.get(message.groupname).put(message.target, message.timeout);
                usersList.get(message.target).tell(new Messages.ServerReply("You have been muted for " + message.timeout + " in " + message.groupname + " by " + message.username + "!"), ActorRef.noSender());
                /////////////////////////////// NEEDS CHANGING ///////////////////////////////
                // support automatic unmute
                /////////////////////////////// NEEDS CHANGING ///////////////////////////////
            }

        }
    }

    private void onUnmuteUserInGroup(Messages.UnmuteUserInGroup message) {
        if (validInviteRemovalMuteUnmuteInGroup(message.groupname, message.username, message.target)) {
            if (groupMutedUsers.get(message.groupname).containsKey(message.username)) {
                // target unmuted
                groupMutedUsers.get(message.groupname).remove(message.username);
                groupRegularUsers.put(message.groupname, new ConcurrentLinkedQueue<>());
                groupRegularUsers.get(message.groupname).add(message.target);
                usersList.get(message.target).tell(new Messages.ServerReply("You have been unmuted in " + message.groupname + " by " + message.username + "!"), ActorRef.noSender());
            } else {
                getSender().tell(new Messages.ServerReply(message.target + " is not muted !"), ActorRef.noSender());
            }
        }
    }

    private void onPromoteCoAdminInGroup(Messages.PromoteCoAdminInGroup message) {
        // username is the admin
        if (validPromoteDemoteInGroup(message.groupname, message.username, message.target)) {
            // target is the admin
            if (groupAdmins.get(message.groupname).contains(message.target)) {
                getSender().tell(new Messages.ServerReply(message.target + " is an admin thus can't be promoted!"), ActorRef.noSender());
            }
            // target is a co-admin
            else if (groupCoAdmins.get(message.groupname).contains(message.target)) {
                getSender().tell(new Messages.ServerReply(message.target + " is a co-admin thus can't be promoted!"), ActorRef.noSender());
            } else {
                // target is a regular user
                if (groupRegularUsers.get(message.groupname).contains(message.target)) {
                    groupRegularUsers.get(message.groupname).remove(message.target);
                    groupCoAdmins.get(message.groupname).add(message.target);
                }
                // target is a muted user
                else if (groupMutedUsers.get(message.groupname).containsKey(message.target)) {
                    groupMutedUsers.get(message.groupname).remove(message.target);
                    groupCoAdmins.get(message.groupname).add(message.target);
                }
                usersList.get(message.target).tell(new Messages.ServerReply("You have been promoted to co-admin in " + message.groupname + "!"), ActorRef.noSender());
            }
        }
    }

    private void onDemoteCoAdminInGroup(Messages.DemoteCoAdminInGroup message) {
        // username is the admin
        if (validPromoteDemoteInGroup(message.groupname, message.username, message.target)) {
            // target is the admin
            if (groupAdmins.get(message.groupname).contains(message.target)) {
                getSender().tell(new Messages.ServerReply(message.target + " is an admin thus can't be demoted!"), ActorRef.noSender());
            }
            // target is a regular user
            else if (groupRegularUsers.get(message.groupname).contains(message.target)) {
                getSender().tell(new Messages.ServerReply(message.target + " is a user thus can't be demoted!"), ActorRef.noSender());
            }
            // target is a muted user
            else if (groupMutedUsers.get(message.groupname).containsKey(message.target)) {
                getSender().tell(new Messages.ServerReply(message.target + " is a muted user thus can't be demoted!"), ActorRef.noSender());
            }
            // target is a co-admin
            else if (groupCoAdmins.get(message.groupname).contains(message.target)) {
                groupCoAdmins.get(message.groupname).remove(message.target);
                groupRegularUsers.get(message.groupname).add(message.target);
                // target demoted
                usersList.get(message.target).tell(new Messages.ServerReply("You have been promoted to user in " + message.groupname + "!"), ActorRef.noSender());
            }

        }
    }

    private void deleteGroup(String groupname) {
        groupsList.remove(groupname);
        groupAllUsers.remove(groupname);
        groupMutedUsers.remove(groupname);
        groupRegularUsers.remove(groupname);
        groupCoAdmins.remove(groupname);
        groupAdmins.remove(groupname);
    }

    private void leaveAllGroups(String username) {
        for (String groupname : groupsList) {
            if (groupAllUsers.get(groupname).contains(username)) {
                onLeaveGroup(new Messages.LeaveGroup(username, groupname));
            }
        }
    }

    private void broadcastFromServer(String groupname, String message) {
        for (String username : groupAllUsers.get(groupname)) {
            usersList.get(username).tell(new Messages.ServerReply(message), ActorRef.noSender());
        }
    }

    private void broadcastFromUser(String groupname, String sender, String message) {
        for (String username : groupAllUsers.get(groupname)) {
            usersList.get(username).tell(new Messages.ServerReply(message), usersList.get(sender));
        }
    }

    private boolean validMessageToGroup(String groupname, String username) {
        // group does not exist
        if (!groupsList.contains(groupname)) {
            getSender().tell(new Messages.ServerReply(groupname + " does not exists!"), ActorRef.noSender());
            return false;
        }
        // user is not in group
        else if (!groupAllUsers.get(groupname).contains(username)) {
            getSender().tell(new Messages.ServerReply("You are not part of " + groupname + "!"), ActorRef.noSender());
            return false;
        }
        // user is muted in group
        else if (!groupMutedUsers.get(groupname).containsKey(username)) {
            getSender().tell(new Messages.ServerReply("You are muted for " + groupMutedUsers.get(groupname).get(username) + "in" + groupname + "!"), ActorRef.noSender());
            return false;
        } else {
            return true;
        }
    }

    private boolean validInviteRemovalMuteUnmuteInGroup(String groupname, String username, String target) {
        // group does not exist
        if (!groupsList.contains(groupname)) {
            getSender().tell(new Messages.ServerReply(groupname + " does not exists!"), ActorRef.noSender());
            return false;
        }
        // user is not admin/co-admin in group
        else if (!groupCoAdmins.get(groupname).contains(username) && !groupAdmins.get(groupname).contains(username)) {
            getSender().tell(new Messages.ServerReply("You are neither an admin nor a co-admin of " + groupname + "!"), ActorRef.noSender());
            return false;
        }
        // target doesn't exist
        else if (!groupAllUsers.get(groupname).contains(target)) {
            getSender().tell(new Messages.ServerReply(target + " does not exists!"), ActorRef.noSender());
            return false;
        } else {
            return true;
        }
    }

    private boolean validPromoteDemoteInGroup(String groupname, String username, String target) {
        // group does not exist
        if (!groupsList.contains(groupname)) {
            getSender().tell(new Messages.ServerReply(groupname + " does not exists!"), ActorRef.noSender());
            return false;
        }
        // user is not admin/co-admin in group
        else if (!groupAdmins.get(groupname).contains(username)) {
            getSender().tell(new Messages.ServerReply("You are not an admin of " + groupname + "!"), ActorRef.noSender());
            return false;
        }
        // target doesn't exist
        else if (!groupAllUsers.get(groupname).contains(target)) {
            getSender().tell(new Messages.ServerReply(target + " does not exists!"), ActorRef.noSender());
            return false;
        } else {
            return true;
        }
    }

}
