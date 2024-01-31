package zad3_;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Props;

import java.util.Random;

public class Filtration extends AbstractActor
{
    static public Props props(int slots, int timeScale)
    {
        return Props.create(Filtration.class, () -> new Filtration(slots, timeScale));
    }

    static public class StartFiltration {}
    private int availableSlots;
    private final int timeScale;
    private final Random random = new Random();
    private final double failureProbability = 0.05;
    boolean isFinished = false;

    public Filtration(int slots, int timeScale)
    {
        this.availableSlots = slots;
        this.timeScale = timeScale;
    }

    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                .match(StartFiltration.class, this::startFiltration)
                .match(Warehouse.ProductTaken.class, this::productTaken)
                .match(Warehouse.ProductNotAvailable.class, this::productNotAvailable)
                .match(Warehouse.ProductAdded.class, this::productAdded)
                .match(Fermentation.StatusResponse.class, this::handlePreviousActorStatus)
                .match(Fermentation.CheckStatus.class, this::checkStatus)
                .build();
    }


    /* ------------- START FILTRATION ------------- */
    private void startFiltration(StartFiltration command)
    {
        if (!isFinished)
        {
            if (availableSlots > 0)
            {
                getContext().getSystem().actorSelection("/user/warehouse").tell(new Warehouse.TakeProduct("wine", 25), getSelf());
                availableSlots--;
            } else {
                System.out.println("Filtration: no available slots");
            }
        }
    }


    /* ------------- TAKE PRODUCT ------------- */
    public void productTaken(Warehouse.ProductTaken command)
    {
        if (command.name.equals("wine"))
        {
            boolean isSuccess = random.nextDouble() >= failureProbability;
            if (isSuccess)
            {
                getContext().getSystem().scheduler().scheduleOnce(
                        java.time.Duration.ofHours(12 / timeScale),
                        () -> {
                            getContext().getSystem().actorSelection("/user/warehouse")
                                    .tell(new Warehouse.AddProduct("filtered wine", 24), getSelf());

                            getContext().getSystem().actorSelection("/user/bottling")
                                    .tell(new Bottling.StartBottling(), getSelf());

                            availableSlots++;
                            self().tell(new StartFiltration(), self());
                        },
                        getContext().getDispatcher()
                );
            }

            else
            {
                System.out.println("Filtration failed for " + command.quantity + " of " + command.name);
                availableSlots++;
                self().tell(new StartFiltration(), self());
            }
        }
    }


    /* ------------- PRODUCT ADDED ------------- */
    private void productAdded(Warehouse.ProductAdded command)
    {
        System.out.println("Filtration: " + command + " added");
    }


    /* ------------- PRODUCT NOT AVAILABLE ------------- */
    private void productNotAvailable(Warehouse.ProductNotAvailable productNotAvailable)
    {
        System.out.println("Filtration: " + productNotAvailable + " not available");
        ActorSelection fermentation = getContext().getSystem().actorSelection("/user/fermentation");
        fermentation.tell(new Fermentation.CheckStatus(), getSelf());
    }


    /* ------------- OTHER ------------- */
    private void handlePreviousActorStatus(Fermentation.StatusResponse statusResponse)
    {
        if (statusResponse.isFinished())
        {
            isFinished = true;
            // getContext().stop(getSelf());
        }
    }

    static public class CheckStatus {}

    public record StatusResponse(boolean isFinished) {}

    private void checkStatus(Fermentation.CheckStatus command)
    {
        getSender().tell(new Fermentation.StatusResponse(isFinished), getSelf());
    }
}
