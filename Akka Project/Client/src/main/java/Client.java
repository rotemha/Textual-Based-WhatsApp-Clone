
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
                .match(Object.class, this::onMessage)
                .build();
    }

    private void onMessage(Object message) {
        Object result = ask(whatsAppManagingServer, (Serializable) message);
        if (result != null) {
            System.out.println(result);
        } else {
            System.out.println("server is offline! try again later!");
        }
    }

    private static Object ask(ActorSelection actorRef, Serializable Message) {
        final Timeout timeout = new Timeout(Duration.create(2, TimeUnit.SECONDS));
        Future<Object> answer = Patterns.ask(actorRef, Message, timeout);
        try {
            return Await.result(answer, timeout.duration());
        } catch (Exception e) {
        }
        return null;
    }


}