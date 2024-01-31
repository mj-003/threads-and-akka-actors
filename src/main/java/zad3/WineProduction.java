package zad3;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class WineProduction extends AbstractActor {
    private final ActorRef pressing;
    private final ActorRef filtration;
    private final ActorRef fermentation;
    private final ActorRef bottling;
    private final ActorRef warehouse;

    public WineProduction() {
        pressing = getContext().actorOf(Pressing.props(), "pressing");
        filtration = getContext().actorOf(Filtration.props(), "filtration");
        fermentation = getContext().actorOf(Fermentation.props(), "fermentation");
        bottling = getContext().actorOf(Bottling.props(), "bottling");
        warehouse = getContext().actorOf(Warehouse.props(), "warehouse");
    }

    static public Props props() {
        return Props.create(WineProduction.class, WineProduction::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartProduction.class, msg -> pressing.tell(new ProductionMessage(true, 0), getSelf()))
                .match(ProductionMessage.class, msg ->
                {
                    if (msg.isSuccess())
                    {
                        if (msg.getProduced() == 0) {
                            fermentation.tell(new ProductionMessage(true, 0), getSelf());
                        } else {
                            warehouse.tell(msg, getSelf());
                        }
                    } else {
                        System.out.println("Wine production failed.");
                        getContext().stop(getSelf());
                    }
                })
                .build();
    }

    static class StartProduction {
    }
}

