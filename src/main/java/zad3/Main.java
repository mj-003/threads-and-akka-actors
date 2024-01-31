package zad3;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Main {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("wine-production-system");
        final ActorRef wineProduction = system.actorOf(WineProduction.props(), "wine-production");

        wineProduction.tell(new WineProduction.StartProduction(), ActorRef.noSender());

        system.terminate();
    }
}

