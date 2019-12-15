
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

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
                .match(Messages.InviteAnswer.class, this::onInviteAnswer)
                .match(Messages.RemoveUserFromGroup.class, this::onRemoveUserFromGroup)
                .match(Messages.MuteUserInGroup.class, this::onMuteUserInGroup)
                .match(Messages.UnmuteUserInGroup.class, this::onUnmuteUserInGroup)
                .match(Messages.PromoteCoAdminInGroup.class, this::onPromoteCoAdminInGroup)
                .match(Messages.DemoteCoAdminInGroup.class, this::onDemoteCoAdminInGroup)
                .build();
    }


    private void onConnect(Messages.Connect message) {
        System.out.println("Received Connect from " + message.username);
        // need to check if the server is down (from the client's side) and act accordingly
        // (how can we know if the server is down? how much time should we wait for an ack/error?)
        if (isUserInSystem(message.username)) {
            getSender().tell(message.username + " is in use!", ActorRef.noSender());
        } else {
            addUserToSystem(message.username);
            getSender().tell(message.username + " has connected successfully!", ActorRef.noSender());
        }
    }

    private void onDisconnect(Messages.Disconnect message) {
        System.out.println("Received Disconnect from " + message.username);
        // need to check if the server is down (from the client's side) and act accordingly
        // (how can we know if the server is down? how much time should we wait for an ack/error?)
        if (isUserInSystem(message.username)) {
            getSender().tell(message.username + " has been disconnected successfully!", ActorRef.noSender());
            leaveAllGroups(message.username);
            removeUserFromSystem(message.username);
        } else {
            // I ADDED THIS ERROR
            getSender().tell(message.username + " is not connected!", ActorRef.noSender());
            // I ADDED THIS ERROR
        }
    }

    private void onCreateGroup(Messages.CreateGroup message) {
        if (isGroupInSystem(message.groupname)) {
            getSender().tell(message.groupname + " already exists!", ActorRef.noSender());
        } else {
            // add groupname to the list of groups
            addGroupToSystem(message.groupname);
            // set username as the group's admin
            groupAdmins.put(message.groupname, message.username);
            // initialize the list of users
            groupAllUsers.put(message.groupname, new ConcurrentLinkedQueue<>());
            // add username to the list of users in message.groupname
            groupAllUsers.get(message.groupname).add(message.username);

            // initialize the list of mutedUsers
            groupMutedUsers.put(message.groupname, new ConcurrentHashMap<>());
            // initialize the list of regularUsers
            groupRegularUsers.put(message.groupname, new ConcurrentLinkedQueue<>());
            // initialize the list of coAdmins
            groupCoAdmins.put(message.groupname, new ConcurrentLinkedQueue<>());
            getSender().tell(message.groupname + " created successfully!", ActorRef.noSender());
        }
    }

    private void onLeaveGroup(Messages.LeaveGroup message) {
        if (!isUserInGroup(message.groupname, message.username)) {
            getSender().tell(message.username + " is not in " + message.groupname + "!", ActorRef.noSender());
        } else {

            // BROADCAST TO GROUP
            broadcastFromServer(message.groupname, message.username + " has left " + message.groupname + "!");

            // if the user is the admin of the group then we delete the group
            if (isAdminInGroup(message.groupname, message.username)) {
                broadcastFromServer(message.groupname, message.groupname + " admin has closed " + message.groupname + "!");
                deleteGroupFromSystem(message.groupname);
            }

            // remove user from the group
            if (isUserInGroup(message.groupname, message.username)) {
                groupAllUsers.get(message.groupname).remove(message.username);
            }
            removeRegularUserFromGroup(message.groupname, message.username);
            removeMutedUserFromGroup(message.groupname, message.username);

            // if the user is a coAdmin then remove this privilege
            removeCoAdminFromGroup(message.groupname, message.username);

        }
    }

    private void onSendTextToGroup(Messages.SendTextToGroup message) {
        if (validMessageToGroup(message.groupname, message.username)) {
            // successful text send
            broadcastFromUser(message.groupname, message.username, message.text);
        }
    }

    private void onSendFileToGroup(Messages.SendFileToGroup message) {
        if (validMessageToGroup(message.groupname, message.username)) {
//            if (sourceFilePath is not valid) {
//                getSender().tell(message.sourceFilePath + " does not exists!"), ActorRef.noSender());
//            } else {
            /////////////////////////////// NEEDS CHANGING ///////////////////////////////
            // successful file send
            broadcastFromUser(message.groupname, message.username, message.sourceFilePath);
            /////////////////////////////// NEEDS CHANGING ///////////////////////////////
//            }
        }
    }

    private void onInviteUserToGroup(Messages.InviteUserToGroup message) {
        if (validInviteRemovalMuteUnmuteInGroup(message.groupname, message.username, message.target)) {
            // target is already in group
            if (isUserInGroup(message.groupname, message.target)) {
                getSender().tell(message.target + " is already in " + message.groupname + "!", ActorRef.noSender());
            } else {
                if (isUserInSystem(message.username) && isUserInSystem(message.target)) {
                    getUserRef(message.target).tell(new Messages.InvitePending(message.username, message.groupname, message.target, "You have been invited to " + message.groupname + ", Accept?"), getUserRef(message.username));
                }
            }
        }
    }

    private void onInviteAnswer(Messages.InviteAnswer message) {
        // group does not exist
        if (!isGroupInSystem(message.groupname)) {
            getSender().tell(message.groupname + " does not exists!", ActorRef.noSender());
        }
        // username doesn't exist
        else if (!isUserInSystem(message.username)) {
            getSender().tell(message.username + " does not exists!", ActorRef.noSender());
        }
        // username is already in group
        else if (isUserInGroup(message.groupname, message.username)) {
            getSender().tell(message.username + " is already in " + message.groupname + "!", ActorRef.noSender());
        } else if (message.answer.equals("Yes")) {
            if (isUserInSystem(message.username)) {
                getUserRef(message.username).tell(message.username + "Welcome to " + message.groupname + "!", ActorRef.noSender());
            }
        }

    }

    private void onRemoveUserFromGroup(Messages.RemoveUserFromGroup message) {
        if (validInviteRemovalMuteUnmuteInGroup(message.groupname, message.username, message.target)) {
            // target is not in group
            if (!isUserInGroup(message.groupname, message.target)) {
                getSender().tell(message.target + " is not in " + message.groupname + "!", ActorRef.noSender());
            }
            // target is the group's admin
            else if (isAdminInGroup(message.groupname, message.target)) {
                getSender().tell(message.username + " is an admin thus can't be removed!", ActorRef.noSender());
            }
            // target removed
            else {
//                getSender().tell(new Messages.LeaveGroup(message.target, message.groupname), ActorRef.noSender());
                // remove user from the group
                if (isUserInGroup(message.groupname, message.target)) {
                    groupAllUsers.get(message.groupname).remove(message.target);
                }
                removeRegularUserFromGroup(message.groupname, message.target);
                removeMutedUserFromGroup(message.groupname, message.target);
                // if the user is a coAdmin then remove this privilege
                removeCoAdminFromGroup(message.groupname, message.target);

                if (isUserInSystem(message.target)) {
                    getUserRef(message.target).tell("You have been removed from " + message.groupname + " by " + message.username + "!", ActorRef.noSender());
                }
            }
        }
    }

    private void onMuteUserInGroup(Messages.MuteUserInGroup message) {
        if (validInviteRemovalMuteUnmuteInGroup(message.groupname, message.username, message.target)) {
            // target is a co-admin and username is a co-admin
            if (isCoAdminInGroup(message.groupname, message.target) && isCoAdminInGroup(message.groupname, message.username)) {
                getSender().tell(message.username + " is a co-admin thus can't mute the target co-admin" + message.target + "!", ActorRef.noSender());
            }
            // already muted user
            else if (isMutedUserInGroup(message.groupname, message.username)) {
                groupMutedUsers.get(message.groupname).replace(message.username, message.timeout);
                if (isUserInSystem(message.target)) {
                    getUserRef(message.target).tell("You have been muted for " + message.timeout + " in " + message.groupname + " by " + message.username + "!", ActorRef.noSender());
                }
            }
            // target is the group's admin
            else if (isAdminInGroup(message.groupname, message.target)) {
                getSender().tell(message.target + " is the admin thus can't be muted!", ActorRef.noSender());
            }
            // target muted
            else {
                removeCoAdminFromGroup(message.groupname, message.target);
                removeRegularUserFromGroup(message.groupname, message.target);
                if (isMutedUserInGroup(message.groupname, message.username)) {
                    addMutedUserToGroup(message.groupname, message.target, message.timeout);
                }
                if (isUserInSystem(message.target)) {
                    getUserRef(message.target).tell("You have been muted for " + message.timeout + " in " + message.groupname + " by " + message.username + "!", ActorRef.noSender());
                }
                /////////////////////////////// NEEDS CHANGING ///////////////////////////////
                // support automatic unmute
                /////////////////////////////// NEEDS CHANGING ///////////////////////////////
            }

        }
    }

    private void onUnmuteUserInGroup(Messages.UnmuteUserInGroup message) {
        if (validInviteRemovalMuteUnmuteInGroup(message.groupname, message.username, message.target)) {
            if (isMutedUserInGroup(message.groupname, message.username)) {
                // target unmuted
                removeMutedUserFromGroup(message.groupname, message.username);
                groupRegularUsers.put(message.groupname, new ConcurrentLinkedQueue<>());
                addRegularUserToGroup(message.groupname, message.target);
                if (isUserInSystem(message.target))
                    getUserRef(message.target).tell("You have been unmuted in " + message.groupname + " by " + message.username + "!", ActorRef.noSender());
            } else {
                getSender().tell(message.target + " is not muted !", ActorRef.noSender());
            }
        }
    }

    private void onPromoteCoAdminInGroup(Messages.PromoteCoAdminInGroup message) {
        // username is the admin
        if (validPromoteDemoteInGroup(message.groupname, message.username, message.target)) {
            // target is the admin
            if (isAdminInGroup(message.groupname, message.target)) {
                getSender().tell(message.target + " is an admin thus can't be promoted!", ActorRef.noSender());
            }
            // target is a co-admin
            else if (isCoAdminInGroup(message.groupname, message.target)) {
                getSender().tell(message.target + " is a co-admin thus can't be promoted!", ActorRef.noSender());
            } else {
                // target is a regular user
                if (isRegularUserInGroup(message.groupname, message.target)) {
                    removeRegularUserFromGroup(message.groupname, message.target);
                    addCoAdminToGroup(message.groupname, message.target);
                }
                // target is a muted user
                else if (isMutedUserInGroup(message.groupname, message.target)) {
                    removeMutedUserFromGroup(message.groupname, message.target);
                    addCoAdminToGroup(message.groupname, message.target);
                }
                if (isUserInSystem(message.target)) {
                    getUserRef(message.target).tell("You have been promoted to co-admin in " + message.groupname + "!", ActorRef.noSender());
                }
            }
        }
    }

    private void onDemoteCoAdminInGroup(Messages.DemoteCoAdminInGroup message) {
        // username is the admin
        if (validPromoteDemoteInGroup(message.groupname, message.username, message.target)) {
            // target is the admin
            if (isAdminInGroup(message.groupname, message.target)) {
                getSender().tell(message.target + " is an admin thus can't be demoted!", ActorRef.noSender());
            }
            // target is a regular user
            else if (isRegularUserInGroup(message.groupname, message.target)) {
                getSender().tell(message.target + " is a user thus can't be demoted!", ActorRef.noSender());
            }
            // target is a muted user
            else if (isMutedUserInGroup(message.groupname, message.target)) {
                getSender().tell(message.target + " is a muted user thus can't be demoted!", ActorRef.noSender());
            }
            // target is a co-admin
            else if (isCoAdminInGroup(message.groupname, message.target)) {
                removeCoAdminFromGroup(message.groupname, message.target);
                addRegularUserToGroup(message.groupname, message.target);
                // target demoted
                if (isUserInSystem(message.target)) {
                    getUserRef(message.target).tell("You have been promoted to user in " + message.groupname + "!", ActorRef.noSender());
                }
            }

        }
    }

    private void leaveAllGroups(String username) {
        for (String groupname : groupsList) {
            if (isUserInGroup(groupname, username)) {
                onLeaveGroup(new Messages.LeaveGroup(username, groupname));
            }
        }
    }

    private void broadcastFromServer(String groupname, String message) {
        if (isGroupInSystem(groupname)) {
            for (String username : groupAllUsers.get(groupname)) {
                System.out.println("username: " + username);
                getUserRef(username).tell(message, ActorRef.noSender());
                System.out.println("SENT");
            }
        }
    }

    private void broadcastFromUser(String groupname, String sender, String message) {
        if (isUserInSystem(sender) && isGroupInSystem(groupname)) {
            for (String username : groupAllUsers.get(groupname)) {
                getUserRef(username).tell(message, getUserRef(sender));
            }
        }
    }

    private boolean validMessageToGroup(String groupname, String username) {
        // group does not exist
        if (!isGroupInSystem(groupname)) {
            getSender().tell(groupname + " does not exists!", ActorRef.noSender());
            return false;
        }
        // user is not in group
        else if (!isUserInGroup(groupname, username)) {
            getSender().tell("You are not part of " + groupname + "!", ActorRef.noSender());
            return false;
        }
        // user is muted in group
        else if (isMutedUserInGroup(groupname, username)) {
            getSender().tell("You are muted for " + groupMutedUsers.get(groupname).get(username) + "in" + groupname + "!", ActorRef.noSender());
            return false;
        } else {
            return true;
        }
    }

    private boolean validInviteRemovalMuteUnmuteInGroup(String groupname, String username, String target) {
        // group does not exist
        if (!isGroupInSystem(groupname)) {
            getSender().tell(groupname + " does not exists!", ActorRef.noSender());
            return false;
        }
        // user is not admin/co-admin in group
        else if (!isCoAdminInGroup(groupname, username) && !isAdminInGroup(groupname, username)) {
            getSender().tell("You are neither an admin nor a co-admin of " + groupname + "!", ActorRef.noSender());
            return false;
        }
        // target doesn't exist
        else if (!isUserInGroup(groupname, target)) {
            getSender().tell(target + " does not exists!", ActorRef.noSender());
            return false;
        } else {
            return true;
        }
    }

    private boolean validPromoteDemoteInGroup(String groupname, String username, String target) {
        // group does not exist
        if (!isGroupInSystem(groupname)) {
            getSender().tell(groupname + " does not exists!", ActorRef.noSender());
            return false;
        }
        // user is not admin/co-admin in group
        else if (!isAdminInGroup(groupname, username)) {
            getSender().tell("You are not an admin of " + groupname + "!", ActorRef.noSender());
            return false;
        }
        // target doesn't exist
        else if (!isUserInGroup(groupname, target)) {
            getSender().tell(target + " does not exists!", ActorRef.noSender());
            return false;
        } else {
            return true;
        }
    }

    private boolean isUserInSystem(String username) {
        return usersList.containsKey(username);
    }

    private ActorRef getUserRef(String username) {
        return usersList.get(username);
    }

    private void addUserToSystem(String username) {
        usersList.put(username, getSender());
    }

    private void removeUserFromSystem(String username) {
        usersList.remove(username);
    }

    private boolean isGroupInSystem(String groupname) {
        return groupsList.contains(groupname);
    }

    private void addGroupToSystem(String groupname) {
        groupsList.add(groupname);
    }

    private void deleteGroupFromSystem(String groupname) {
        groupsList.remove(groupname);
        groupAllUsers.remove(groupname);
        groupMutedUsers.remove(groupname);
        groupRegularUsers.remove(groupname);
        groupCoAdmins.remove(groupname);
        groupAdmins.remove(groupname);
    }

    private boolean isUserInGroup(String groupname, String username) {
        if (groupAllUsers.containsKey(groupname)) {
            return groupAllUsers.get(groupname).contains(username);
        } else {
            return false;
        }
    }

    private boolean isMutedUserInGroup(String groupname, String username) {
        if (groupMutedUsers.containsKey(groupname)) {
            return groupMutedUsers.get(groupname).containsKey(username);
        } else {
            return false;
        }
    }

    private void addMutedUserToGroup(String groupname, String username, Double timeout) {
        if (groupMutedUsers.containsKey(groupname)) {
            groupMutedUsers.get(groupname).put(username, timeout);
        }
    }

    private void removeMutedUserFromGroup(String groupname, String username) {
        if (groupMutedUsers.containsKey(groupname)) {
            groupMutedUsers.get(groupname).remove(username);
        }
    }

    private boolean isRegularUserInGroup(String groupname, String username) {
        if (groupRegularUsers.containsKey(groupname)) {
            return groupRegularUsers.get(groupname).contains(username);
        } else {
            return false;
        }
    }

    private void addRegularUserToGroup(String groupname, String username) {
        if (groupRegularUsers.containsKey(groupname)) {
            groupRegularUsers.get(groupname).add(username);
        }
    }

    private void removeRegularUserFromGroup(String groupname, String username) {
        if (groupRegularUsers.containsKey(groupname)) {
            groupRegularUsers.get(groupname).remove(username);
        }
    }

    private boolean isCoAdminInGroup(String groupname, String username) {
        if (groupCoAdmins.containsKey(groupname)) {
            return groupCoAdmins.get(groupname).contains(username);
        } else {
            return false;
        }
    }

    private void addCoAdminToGroup(String groupname, String username) {
        if (groupCoAdmins.containsKey(groupname)) {
            groupCoAdmins.get(groupname).add(username);
        }
    }

    private void removeCoAdminFromGroup(String groupname, String username) {
        if (groupCoAdmins.containsKey(groupname)) {
            groupCoAdmins.get(groupname).remove(username);
        }
    }

    private boolean isAdminInGroup(String groupname, String username) {
        if (groupAdmins.containsKey(groupname)) {
            return groupAdmins.get(groupname).equals(username);
        } else {
            return false;
        }
    }


}
