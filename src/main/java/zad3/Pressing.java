package zad3;
import akka.actor.AbstractActor;
import akka.actor.Props;

public class Pressing extends AbstractActor {
    private final int resourcesRequired = 15;
    private final int successRate = 0;
    private final int processingTime = 12;
    private int availableResources;

    public Pressing() {
        this.availableResources = resourcesRequired;
    }

    static public Props props() {
        return Props.create(Pressing.class, Pressing::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ProductionMessage.class, msg -> {
                    if (availableResources >= resourcesRequired) {
                        Thread.sleep(processingTime);
                        if (Math.random() * 100 < successRate) {
                            availableResources -= resourcesRequired;
                            System.out.println("Pressing successfully completed.");
                            sender().tell(new ProductionMessage(true, 10), getSelf()); // Przykładowa wartość dla drugiego argumentu
                        } else {
                            System.out.println("Pressing failed.");
                            sender().tell(new ProductionMessage(false, 0), getSelf()); // Przykładowa wartość dla drugiego argumentu
                        }
                    } else {
                        System.out.println("Insufficient resources for pressing.");
                        sender().tell(new ProductionMessage(false, 0), getSelf()); // Przykładowa wartość dla drugiego argumentu
                    }
                })
                .build();
    }
}
