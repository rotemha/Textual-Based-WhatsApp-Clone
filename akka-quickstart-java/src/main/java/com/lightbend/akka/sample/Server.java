package com.lightbend.akka.sample;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class Server
{
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("WhatsApp");
        system.actorOf(Props.create(ManagingServer.class), "ManagingServer");
    }
}