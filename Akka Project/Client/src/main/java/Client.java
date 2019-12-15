
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

    public ActorSelection server = getContext().actorSelection("akka.tcp://Server@127.0.0.1:2552/user/Server");


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Messages.Connect.class, this::onConnect)
                .build();
    }

    private void onConnect(Messages.Connect message) throws Exception {
        Object result = ask(server,  message);
        if(result != null)
        {
            System.out.println(result);
        }
        else{
            System.out.println("server is offline! try again later!");
        }
    }

    private static Object ask(ActorSelection actorRef, Serializable Message) {
        final Timeout timeout = new Timeout(Duration.create(2, TimeUnit.SECONDS));
        Future<Object> rt = Patterns.ask(actorRef, Message, timeout);
        Object result = null;
        try {
            result = Await.result(rt, timeout.duration());
        } catch (Exception e) {
        }
        return result;
    }

    private static Object ask(ActorRef actorRef, Serializable Message) {
        final Timeout timeout = new Timeout(Duration.create(2, TimeUnit.SECONDS));
        Future<Object> rt = Patterns.ask(actorRef, Message, timeout);
        Object result = null;
        try {
            result = Await.result(rt, timeout.duration());
            System.out.println("the result is " + result);
        } catch (Exception e) {
        }
        return result;
    }

}