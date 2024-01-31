package zad3;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class Fermentation extends AbstractActor {
    private final int resourcesRequired = 15;
    private final int successRate = 5;
    private final int processingTime = 14;
    private int availableResources;

    public Fermentation() {
        this.availableResources = resourcesRequired;
    }

    static public Props props() {
        return Props.create(Fermentation.class, Fermentation::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ProductionMessage.class, msg -> {
                    if (availableResources >= resourcesRequired) {
                        Thread.sleep(processingTime);
                        if (Math.random() * 100 < successRate) {
                            availableResources -= resourcesRequired;
                            System.out.println("Fermentation successfully completed.");
                            sender().tell(new ProductionMessage(true, resourcesRequired), getSelf());
                        } else {
                            System.out.println("Fermentation failed.");
                            sender().tell(new ProductionMessage(false, 0), getSelf());
                        }
                    } else {
                        System.out.println("Insufficient resources for fermentation.");
                        sender().tell(new ProductionMessage(false, 0), getSelf());
                    }
                })
                .build();
    }
}
