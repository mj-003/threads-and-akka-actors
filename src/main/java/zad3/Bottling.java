package zad3;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class Bottling extends AbstractActor {
    private final int resourcesRequired = 1;
    private final int successRate = 0;
    private final int processingTime = 5;

    static public Props props() {
        return Props.create(Bottling.class, Bottling::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ProductionMessage.class, msg -> {
                    Thread.sleep(processingTime);
                    if (Math.random() * 100 < successRate) {
                        System.out.println("Bottling successfully completed.");
                        sender().tell(new ProductionMessage(true, 1), getSelf());
                    } else {
                        System.out.println("Bottling failed.");
                        sender().tell(new ProductionMessage(false, 0), getSelf());
                    }
                })
                .build();
    }
}
