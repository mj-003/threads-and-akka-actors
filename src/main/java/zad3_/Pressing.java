package zad3_;

import akka.actor.AbstractActor;
import akka.actor.Props;

import java.util.Random;

public class Pressing extends AbstractActor
{
    static public Props props(int slots, int timeScale)
    {
        return Props.create(Pressing.class, () -> new Pressing(slots, timeScale));
    }

    static public class StartProcessing {}

    private int availableSlots;
    private final int timeScale;
    private final Random random = new Random();
    double failureProbability = 0.05;
    boolean isFinished;

    public Pressing(int slots, int timeScale)
    {
        this.availableSlots = slots;
        this.timeScale = timeScale;
    }

    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                .match(StartProcessing.class, this::startProcessing)
                .match(Warehouse.ProductTaken.class, this::productTaken)
                .match(Warehouse.ProductNotAvailable.class, this::productNotAvailable)
                .match(Warehouse.ProductAdded.class, this::productAdded)
                .match(CheckStatus.class, this::checkStatus)
                .build();
    }

    private void startProcessing(StartProcessing command)
    {
        if (!isFinished) {
            if (availableSlots > 0)
            {
                System.out.println("START PRESSING");
                getContext().getSystem().actorSelection("/user/warehouse")
                        .tell(new Warehouse.TakeProduct("grapes", 15), getSelf());
                availableSlots--;
            } else {
                System.out.println("PRESSING NO SLOTS");
                startProcessing();
            }
        }
    }

    private void productTaken(Warehouse.ProductTaken command)
    {
        if (command.name.equals("grapes")) {
            boolean isSuccess = random.nextDouble() >= failureProbability;

            if (isSuccess) {
                scheduleGrapeJuiceProduction();
            } else {
                handlePressingFailure(command);
            }
        }
    }

    private void scheduleGrapeJuiceProduction()
    {
        getContext().getSystem().scheduler().scheduleOnce(
                java.time.Duration.ofHours(1 / timeScale),
                () -> {
                    addGrapeJuiceToWarehouse();
                    startProcessing();
                    availableSlots++;
                    startFermentation();
                },
                getContext().getDispatcher()
        );
    }

    private void addGrapeJuiceToWarehouse()
    {
        Warehouse.AddProduct grapeJuice = new Warehouse.AddProduct("grape juice", 10);
        getContext().getSystem().actorSelection("/user/warehouse").tell(grapeJuice, getSelf());
        // System.out.println("Pressing: grape juice added");
    }

    private void startProcessing()
    {
        self().tell(new StartProcessing(), self());
        // System.out.println("Pressing: start processing");
    }

    private void startFermentation()
    {
        getContext().getSystem().actorSelection("/user/fermentation").tell(new Fermentation.StartFermentation(), getSelf());
    }
    private void productNotAvailable(Warehouse.ProductNotAvailable command)
    {
        isFinished = true;
        System.out.println("---------- PRESSING STOPPED ----------");
    }
    private void handlePressingFailure(Warehouse.ProductTaken command)
    {
        System.out.println("Pressing failed for " + command.quantity + " of " + command.name);
        availableSlots++;
        startProcessing();
    }

    private void productAdded(Warehouse.ProductAdded command)
    {
        System.out.println("Pressing: " + command + " added");
    }

    static public class CheckStatus {}

    public record StatusResponse(boolean isFinished) {
    }

    private void checkStatus(CheckStatus command)
    {
        getSender().tell(new StatusResponse(isFinished), getSelf());
    }
}
