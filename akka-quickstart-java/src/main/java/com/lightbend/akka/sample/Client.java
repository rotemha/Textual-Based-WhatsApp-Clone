package com.lightbend.akka.sample;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;

public class Client extends AbstractActor {

    public ActorSelection server = getContext().actorSelection("akka.tcp://WhatsApp@127.0.0.1:2552/user/WhatsApp");


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Messages.Connect.class, this::onConnect)
                .build();
    }

    private void onConnect(Messages.Connect message) throws Exception {
        final Timeout timeout = new Timeout(Duration.create(2, TimeUnit.SECONDS));
        Future<Object> rt = Patterns.ask(server, message, timeout);
        Object result = Await.result(rt, timeout.duration());
        System.out.println("the result is " + result);
        System.out.println(result);
    }
}
