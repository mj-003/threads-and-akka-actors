package zad3_;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Props;

import java.util.Random;

public class Fermentation extends AbstractActor
{
    static public Props props(int slots, int timeScale)
    {
        return Props.create(Fermentation.class, () -> new Fermentation(slots, timeScale));
    }

    static public class StartFermentation {}
    private int availableSlots;
    private final int timeScale;
    private final Random random = new Random();
    double failureProbability = 0.05;
    boolean sugarTaken = false;
    boolean grapeJuiceTaken = false;
    boolean isFinished = false;

    public Fermentation(int slots, int timeScale)
    {
        this.availableSlots = slots;
        this.timeScale = timeScale;
    }

    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                .match(StartFermentation.class, this::startFermentation)
                .match(Warehouse.ProductTaken.class, this::productTaken)
                .match(Warehouse.ProductNotAvailable.class, this::productNotAvailable)
                .match(Warehouse.ProductAdded.class, this::productAdded)
                .match(Pressing.StatusResponse.class, this::handlePreviousActorStatus)
                .match(Pressing.CheckStatus.class, this::checkStatus)
                .build();
    }



    /* ------------- START FERMENTATION ------------- */
    private void startFermentation(StartFermentation command)
    {
        if (!isFinished) {
            if (availableSlots > 0) {
                getContext().getSystem().actorSelection("/user/warehouse")
                        .tell(new Warehouse.TakeProduct("grape juice", 5), getSelf());
                getContext().getSystem().actorSelection("/user/warehouse")
                        .tell(new Warehouse.TakeProduct("sugar", 2), getSelf());
                availableSlots--;
            } else {
                System.out.println("Fermentation: no available slots");
            }
            sugarTaken = false;
            grapeJuiceTaken = false;
        }
    }



    /* ------------- PRODUCT TAKEN ------------- */
    private void productTaken(Warehouse.ProductTaken command)
    {
        if (command.name.equals("grape juice"))
        {
            grapeJuiceTaken = true;
        }

        if (command.name.equals("sugar"))
        {
            sugarTaken = true;
        }

        boolean isSuccess = random.nextDouble() >= failureProbability;

        if (isSuccess && sugarTaken && grapeJuiceTaken)
        {
            scheduleWineProduction();
        }

        else
        {
            handleFermentationFailure(command.quantity, command.name);
        }
    }

    private void scheduleWineProduction()
    {
        getContext().getSystem().scheduler().scheduleOnce(
                java.time.Duration.ofHours(14 / timeScale),
                () -> {
                    addWineToWarehouse();
                    getContext().getSystem().actorSelection("/user/filtration").tell(new Filtration.StartFiltration(), getSelf());
                    availableSlots++;
                    startFermentation();
                },
                getContext().getDispatcher()
        );
    }

    private void addWineToWarehouse()
    {
        Warehouse.AddProduct wine = new Warehouse.AddProduct("wine", 10);
        getContext().getSystem().actorSelection("/user/warehouse").tell(wine, getSelf());
    }

    private void startFermentation()
    {
        self().tell(new StartFermentation(), self());
    }

    private void handleFermentationFailure(int quantity, String productName)
    {
        availableSlots++;
        self().tell(new StartFermentation(), self());

        getContext().getSystem().scheduler().scheduleOnce(
                java.time.Duration.ofSeconds(5),
                () -> {
                    startFermentation();
                    self().tell(new StartFermentation(), self());
                },
                getContext().getDispatcher()
        );
    }


    /* ------------- PRODUCT ADDED ------------- */
    private void productAdded(Warehouse.ProductAdded command)
    {
        System.out.println("Fermentation: " + command + " added");
    }


    /* ------------- PRODUCT NOT AVAILABLE ------------- */
    private void productNotAvailable(Warehouse.ProductNotAvailable productNotAvailable)
    {
        System.out.println("Fermentation: " + productNotAvailable + " not available");
        ActorSelection pressing = getContext().getSystem().actorSelection("/user/pressing");
        pressing.tell(new Pressing.CheckStatus(), getSelf());
    }


    /* ------------- OTHER ------------- */
    private void handlePreviousActorStatus(Pressing.StatusResponse statusResponse)
    {
        if (statusResponse.isFinished()) {
            isFinished = true;
            // getContext().stop(getSelf());
        }
    }

    static public class CheckStatus {}

    public record StatusResponse(boolean isFinished) {}
    private void checkStatus(Pressing.CheckStatus command)
    {
        getSender().tell(new Pressing.StatusResponse(isFinished), getSelf());
    }

}
