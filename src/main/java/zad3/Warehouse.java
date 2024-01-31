package zad3;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class Warehouse extends AbstractActor {
    private int totalProduced = 0;

    static public Props props() {
        return Props.create(Warehouse.class, Warehouse::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ProductionMessage.class, msg -> {
                    if (msg.isSuccess()) {
                        totalProduced += msg.getProduced();
                        System.out.println("Warehouse received " + msg.getProduced() + " units.");
                    } else {
                        System.out.println("Warehouse received failed production message.");
                    }
                })
                .build();
    }
}
