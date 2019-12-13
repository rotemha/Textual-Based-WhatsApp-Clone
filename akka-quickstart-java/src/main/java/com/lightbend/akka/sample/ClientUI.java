package com.lightbend.akka.sample;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.util.Scanner;

public class ClientUI {

    private static String[] input = null;
    private static final ActorSystem actorSystem = ActorSystem.create("WhatsApp");
    private static ActorRef actorRef = null;
    private static String username = null;
    private static String groupname = null;
    private static String target = null;
    private static String text = null;
    private static String sourcefilePath = null;
    private static Double timeInSeconds = null;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Enter command");
            input = scanner.nextLine().split(" ");
            if (input.length < 1) {
                continue;
            }
            switch (input[0]) {
                case "/user":
                    userCommand();
                    break;
                case "/group":
                    groupCommand();
                    break;
                default:
                    System.out.println("Invalid command");
                    break;
            }
        }
    }

    private static void userCommand() {
        if (input.length < 2) {
            System.out.println("Invalid command");
        } else {
            switch (input[1]) {
                case "connect":
                    userConnect();
                    break;
                case "disconnect":
                    userDisconnect();
                    break;
                case "text":
                    userSendText();
                    break;
                case "file":
                    userSendFile();
                    break;
                default:
                    System.out.println("Invalid command");
                    break;
            }
        }
    }

    private static void userConnect() {
        if (input.length != 3) {
            System.out.println("Invalid command");
        } else {
            username = input[2];
            actorRef = actorSystem.actorOf(Props.create(Client.class), username);
            actorRef.tell(new Messages.Connect(username), ActorRef.noSender());
        }
    }

    private static void userDisconnect() {
        if (isConnected()){
            
        }
    }

    private static void userSendText() {
        if (input.length != 4) {
            System.out.println("Invalid command");
        } else {
            target = input[2];
            text = input[3];
        }
    }

    private static void userSendFile() {
        if (input.length != 4) {
            System.out.println("Invalid command");
        } else {
            target = input[2];
            sourcefilePath = input[3];
        }
    }

////////////////////////////////////////////////

    private static void groupCommand() {
        if (input.length < 2) {
            System.out.println("Invalid command");
        } else {
            switch (input[1]) {
                case "create":
                    groupCreate();
                    break;
                case "leave":
                    groupLeave();
                    break;
                case "send":
                    groupSend();
                    break;
                case "user":
                    groupUser();
                    break;
                case "coadmin":
                    groupCoAdmin();
                    break;
                default:
                    System.out.println("Invalid command");
                    break;
            }
        }
    }

    private static void groupCreate() {
        if (input.length != 3) {
            System.out.println("Invalid command");
        } else {
            groupname = input[2];
        }
    }

    private static void groupLeave() {
        if (input.length != 3) {
            System.out.println("Invalid command");
        } else {
            groupname = input[2];
        }
    }

    private static void groupSend() {
        if (input.length < 3) {
            System.out.println("Invalid command");
        } else {
            switch (input[2]) {
                case "text":
                    groupSendText();
                    break;
                case "file":
                    groupSendFile();
                    break;
                default:
                    System.out.println("Invalid command");
                    break;
            }
        }
    }

    private static void groupSendText() {
        if (input.length != 5) {
            System.out.println("Invalid command");
        } else {
            groupname = input[3];
            text = input[4];
        }
    }

    private static void groupSendFile() {
        if (input.length != 5) {
            System.out.println("Invalid command");
        } else {
            groupname = input[3];
            sourcefilePath = input[4];
        }
    }

    private static void groupUser() {
        if (input.length != 3) {
            System.out.println("Invalid command");
        } else {
            switch (input[2]) {
                case "invite":
                    groupUserInvite();
                    break;
                case "remove":
                    groupUserRemove();
                    break;
                case "mute":
                    groupUserMute();
                    break;
                case "unmute":
                    groupUserUnmute();
                    break;
                default:
                    System.out.println("Invalid command");
                    break;
            }
        }
    }

    private static void groupUserInvite() {
        if (input.length != 5) {
            System.out.println("Invalid command");
        } else {
            groupname = input[3];
            target = input[4];
        }
    }

    private static void groupUserRemove() {
        if (input.length != 5) {
            System.out.println("Invalid command");
        } else {
            groupname = input[3];
            target = input[4];
        }
    }

    private static void groupUserMute() {
        if (input.length != 6) {
            System.out.println("Invalid command");
        } else {
            groupname = input[3];
            target = input[4];
            timeInSeconds = Double.valueOf(input[5]);
        }
    }

    private static void groupUserUnmute() {
        if (input.length != 5) {
            System.out.println("Invalid command");
        } else {
            groupname = input[3];
            target = input[4];
        }
    }

    private static void groupCoAdmin() {
        if (input.length != 3) {
            System.out.println("Invalid command");
        } else {
            switch (input[2]) {
                case "add":
                    groupCoAdminAdd();
                    break;
                case "remove":
                    groupCoAdminRemove();
                    break;
                default:
                    System.out.println("Invalid command");
                    break;
            }
        }
    }

    private static void groupCoAdminAdd() {
        if (input.length != 5) {
            System.out.println("Invalid command");
        } else {
            groupname = input[3];
            target = input[4];
        }
    }

    private static void groupCoAdminRemove() {
        if (input.length != 5) {
            System.out.println("Invalid command");
        } else {
            groupname = input[3];
            target = input[4];
        }
    }

    private static boolean isConnected(){
        return actorRef != null;
    }
}
