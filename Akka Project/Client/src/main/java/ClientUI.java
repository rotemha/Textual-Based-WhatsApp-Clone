
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.Serializable;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static akka.actor.TypedActor.self;

public class ClientUI {

    private static String[] input = null;
    // create the actor system for the client
    private static final ActorSystem clientSystem = ActorSystem.create("WhatsAppClientSystem");
    private static ActorRef client = null;
    private static String username = null;
    private static Double timeInSeconds = Double.valueOf("0");
    private static boolean exit = false;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (!exit) {
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
                case "exit":
                    exit = true;
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
//            // create the client actor (under the given username)
            try {
                client = clientSystem.actorOf(Props.create(Client.class), username);
                client.tell(new Messages.Connect(username), ActorRef.noSender());
            } catch (Exception ignored) {
            }
        }
    }

    private static void userDisconnect() {
        try {
            client.tell(new Messages.Disconnect(username), ActorRef.noSender());
            clientSystem.stop(client);
        } catch (Exception ignored) {
        }
    }

    private static void userSendText() {
        if (input.length != 4) {
            System.out.println("Invalid command");
        } else {
            String target = input[2];
            String text = input[3];
            try {
                client.tell(new Messages.SendTextToUser(username, target, text), ActorRef.noSender());
            } catch (Exception ignored) {
            }
        }
    }

    private static void userSendFile() {
        if (input.length != 4) {
            System.out.println("Invalid command");
        } else {
            String target = input[2];
            String sourcefilePath = input[3];
            try {
                client.tell(new Messages.SendFileToUser(username, target, sourcefilePath), ActorRef.noSender());
            } catch (Exception ignored) {
            }
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
            String groupname = input[2];
            try {
                client.tell(new Messages.CreateGroup(username, groupname), ActorRef.noSender());
            } catch (Exception ignored) {
            }
        }
    }

    private static void groupLeave() {
        if (input.length != 3) {
            System.out.println("Invalid command");
        } else {
            String groupname = input[2];
            try {
                client.tell(new Messages.LeaveGroup(username, groupname), ActorRef.noSender());
            } catch (Exception ignored) {
            }
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
            String groupname = input[3];
            String text = input[4];
            try {
                client.tell(new Messages.SendTextToUser(username, groupname, text), ActorRef.noSender());
            } catch (Exception ignored) {
            }
        }
    }

    private static void groupSendFile() {
        if (input.length != 5) {
            System.out.println("Invalid command");
        } else {
            String groupname = input[3];
            String sourcefilePath = input[4];
            try {
                client.tell(new Messages.SendFileToGroup(username, groupname, sourcefilePath), ActorRef.noSender());
            } catch (Exception ignored) {
            }
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
            String groupname = input[3];
            String target = input[4];
            try {
                client.tell(new Messages.InviteUserToGroup(username, groupname, target), ActorRef.noSender());
            } catch (Exception ignored) {
            }
        }
    }

    private static void groupUserRemove() {
        if (input.length != 5) {
            System.out.println("Invalid command");
        } else {
            String groupname = input[3];
            String target = input[4];
            try {
                client.tell(new Messages.RemoveUserFromGroup(username, groupname, target), ActorRef.noSender());
            } catch (Exception ignored) {
            }
        }
    }

    private static void groupUserMute() {
        if (input.length != 6) {
            System.out.println("Invalid command");
        } else {
            String groupname = input[3];
            String target = input[4];
            timeInSeconds = Double.valueOf(input[5]);
            try {
                client.tell(new Messages.MuteUserInGroup(username, groupname, target, timeInSeconds), ActorRef.noSender());
            } catch (Exception ignored) {
            }
        }
    }

    private static void groupUserUnmute() {
        if (input.length != 5) {
            System.out.println("Invalid command");
        } else {
            String groupname = input[3];
            String target = input[4];
            try {
                client.tell(new Messages.UnmuteUserInGroup(username, groupname, target), ActorRef.noSender());
            } catch (Exception ignored) {
            }
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
            String groupname = input[3];
            String target = input[4];
            try {
                client.tell(new Messages.PromoteCoAdminInGroup(username, groupname, target), ActorRef.noSender());
            } catch (Exception ignored) {
            }
        }
    }

    private static void groupCoAdminRemove() {
        if (input.length != 5) {
            System.out.println("Invalid command");
        } else {
            String groupname = input[3];
            String target = input[4];
            try {
                client.tell(new Messages.DemoteCoAdminInGroup(username, groupname, target), ActorRef.noSender());
            } catch (Exception ignored) {
            }
        }
    }

    private static boolean isConnected() {
        return client != null;
    }

}
