package zad3_;

import akka.actor.ActorSystem;
import akka.actor.ActorRef;
import akka.actor.DeadLetter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class WineProductionSystem {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("WineProductionSystem");

        final int timeScale = 150;

        ActorRef warehouse = system.actorOf(Warehouse.props(), "warehouse");
        ActorRef pressing = system.actorOf(Pressing.props(1, timeScale), "pressing");
        ActorRef fermentation = system.actorOf(Fermentation.props(10, timeScale), "fermentation");
        ActorRef filtration = system.actorOf(Filtration.props(10, timeScale), "filtration");
        ActorRef bottling = system.actorOf(Bottling.props(1, timeScale), "bottling");


        warehouse.tell(new Warehouse.AddProduct("grapes", 200), ActorRef.noSender());
        warehouse.tell(new Warehouse.AddProduct("grape juice", 0), ActorRef.noSender());
        warehouse.tell(new Warehouse.AddProduct("sugar", 100), ActorRef.noSender());

        pressing.tell(new Pressing.StartProcessing(), ActorRef.noSender());


        system.scheduler().scheduleOnce(
                Duration.create(1, "minutes"),
                () -> {
                    Timeout timeout = new Timeout(Duration.create(5, "seconds"));
                    Future<Object> future = Patterns.ask(warehouse, new Warehouse.GetInventory(), timeout);

                    try
                    {
                        Warehouse.InventoryState result = (Warehouse.InventoryState) Await.result(future, timeout.duration());
                        result.inventory.forEach((key, value) -> System.out.println(key + ": " + value));
                    }
                    catch (Exception e)
                    {
                        System.out.println("Failed to get inventory state: " + e.getMessage());
                    }
                    system.terminate();
                },
                system.dispatcher()
        );

    }
}
