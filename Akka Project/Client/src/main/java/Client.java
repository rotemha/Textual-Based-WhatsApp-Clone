
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class Client extends AbstractActor {

    public ActorSelection whatsAppManagingServer = getContext().actorSelection("akka.tcp://WhatsAppServerSystem@127.0.0.1:3553/user/WhatsAppManagingServer");


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
        String result = ask(whatsAppManagingServer, message);
        printing(result);
    }

    private void onDisconnect(Messages.Disconnect message) {
        String result = ask(whatsAppManagingServer, message);
        printing(result);
    }

    private void onCreateGroup(Messages.CreateGroup message) {
        String result = ask(whatsAppManagingServer, message);
        printing(result);
    }

    private void onLeaveGroup(Messages.LeaveGroup message) {
        String result = ask(whatsAppManagingServer, message);
        printing(result);
    }

    private void onSendTextToGroup(Messages.SendTextToGroup message) {
        String result = ask(whatsAppManagingServer, message);
        printing(result);
    }

    private void onSendFileToGroup(Messages.SendFileToGroup message) {
        String result = ask(whatsAppManagingServer, message);
        printing(result);
    }

    private void onInviteUserToGroup(Messages.InviteUserToGroup message) {
        String result = ask(whatsAppManagingServer, message);
        printing(result);
    }

    private void onInviteAnswer(Messages.InviteAnswer message) {
        String result = ask(whatsAppManagingServer, message);
        printing(result);
    }

    private void onRemoveUserFromGroup(Messages.RemoveUserFromGroup message) {
        String result = ask(whatsAppManagingServer, message);
        printing(result);
    }

    private void onMuteUserInGroup(Messages.MuteUserInGroup message) {
        String result = ask(whatsAppManagingServer, message);
        printing(result);
    }

    private void onUnmuteUserInGroup(Messages.UnmuteUserInGroup message) {
        String result = ask(whatsAppManagingServer, message);
        printing(result);
    }

    private void onPromoteCoAdminInGroup(Messages.PromoteCoAdminInGroup message) {
        String result = ask(whatsAppManagingServer, message);
        printing(result);
    }

    private void onDemoteCoAdminInGroup(Messages.DemoteCoAdminInGroup message) {
        String result = ask(whatsAppManagingServer, message);
        printing(result);
    }

    private static void printing(String result) {
        if (result != null) {
            System.out.println(result);
        } else {
            System.out.println("server is offline! try again later!");
        }
    }

    public String ask(ActorSelection actorRef, Serializable Message) {
        final Timeout timeout = new Timeout(Duration.create(2, TimeUnit.SECONDS));
        Future<Object> answer = Patterns.ask(actorRef, Message, timeout);
        System.out.println(this.self());
        try {
            return (String) Await.result(answer, timeout.duration());
        } catch (Exception ignored) {
        }
//        actorRef.tell(Message, getSender());
        return null;
    }


}