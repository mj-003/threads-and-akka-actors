package zad3_;

import akka.actor.*;

import java.util.Random;

public class Bottling extends AbstractActor
{
    static public Props props(int slots, int timeScale)
    {
        return Props.create(Bottling.class, () -> new Bottling(slots, timeScale));
    }

    static public class StartBottling {}

    private int availableSlots;
    private final int timeScale;
    private final Random random = new Random();
    private final double failureProbability = 0.05;
    boolean isFinished = false;

    public Bottling(int slots, int timeScale)
    {
        this.availableSlots = slots;
        this.timeScale = timeScale;
    }

    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                .match(StartBottling.class, this::startBottling)
                .match(Warehouse.ProductTaken.class, this::productTaken)
                .match(Warehouse.ProductNotAvailable.class, this::productNotAvailable)
                .match(Warehouse.ProductAdded.class, this::productAdded)
                .match(Filtration.StatusResponse.class, this::handlePreviousActorStatus)
                .match(Filtration.CheckStatus.class, this::checkStatus)
                .build();
    }


    /* ------------- START BOTTLING ------------- */
    private void startBottling(StartBottling command)
    {
        if (!isFinished) {
            if (availableSlots > 0)
            {
                getContext().getSystem().actorSelection("/user/warehouse").tell(new Warehouse.TakeProduct("filtered wine", 75), getSelf());
            } else {
                System.out.println("Bottling: no available slots");
            }
        }
    }


    /* ------------- TAKE PRODUCT ------------- */
    private void productTaken(Warehouse.ProductTaken command)
    {
        if (command.name.equals("filtered wine"))
        {
            boolean isSuccess = random.nextDouble() >= failureProbability;
            if (isSuccess)
            {
                getContext().getSystem().scheduler().scheduleOnce(
                        java.time.Duration.ofMinutes(5 / timeScale),
                        () -> {
                            getContext().getSystem().actorSelection("/user/warehouse")
                                    .tell(new Warehouse.AddProduct("bottled wine", 1), getSelf());

                            availableSlots++;
                            self().tell(new StartBottling(), self());
                        },
                        getContext().getDispatcher()
                );
            }

            else
            {
                System.out.println("Bottling process failed for " + command.quantity + " of " + command.name);
                self().tell(new StartBottling(), self());
            }

        }
    }


    /* ------------- PRODUCT ADDED ------------- */
    private void productAdded(Warehouse.ProductAdded command)
    {
        System.out.println("Bottling: " + command + " added");
    }


    /* ------------- PRODUCT NOT AVAILABLE ------------- */
    private void productNotAvailable(Warehouse.ProductNotAvailable productNotAvailable)
    {
        System.out.println("Bottling: " + productNotAvailable + " not available");
        ActorSelection pressing = getContext().getSystem().actorSelection("/user/pressing");
        pressing.tell(new Pressing.CheckStatus(), getSelf());
    }


    /* ------------- OTHER ------------- */
    private void handlePreviousActorStatus(Filtration.StatusResponse statusResponse)
    {
        if (statusResponse.isFinished()) {
            isFinished = true;
            // getContext().stop(getSelf());
        }
    }

    private void checkStatus(Filtration.CheckStatus command)
    {
        getSender().tell(new Filtration.StatusResponse(isFinished), getSelf());
    }

}
