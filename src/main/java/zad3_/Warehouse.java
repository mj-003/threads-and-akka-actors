package zad3_;

import akka.actor.AbstractActor;
import akka.actor.Props;

import java.util.concurrent.ConcurrentHashMap;

public class Warehouse extends AbstractActor
{
    static public Props props()
    {
        return Props.create(Warehouse.class, Warehouse::new);
    }

    public record AddProduct(String name, int quantity) {}
    public record TakeProduct(String name, int quantity) {}
    public record ProductAdded(String name, int quantity) {}
    public record ProductNotAvailable(String name, int quantity) {}
    static public class GetInventory {}
    private final ConcurrentHashMap<String, Integer> inventory = new ConcurrentHashMap<>();

    static public class InventoryState {
        public final ConcurrentHashMap<String, Integer> inventory;
        public InventoryState(ConcurrentHashMap<String, Integer> inventory) {
            this.inventory = inventory;
        }
    }

    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                .match(AddProduct.class, this::addProduct)
                .match(TakeProduct.class, this::takeProduct)
                .match(GetInventory.class, this::sendInventoryState)
                .build();
    }

    private void addProduct(AddProduct command)
    {
        inventory.merge(command.name, command.quantity, Integer::sum);
        getSender().tell(new ProductAdded(command.name, command.quantity), getSelf());
    }

    private void takeProduct(TakeProduct command) {
        int quantityBefore = inventory.getOrDefault(command.name, 0);
        // System.out.println("Warehouse: " + command.name + " available before taking: " + quantityBefore);

        if (quantityBefore >= command.quantity) {
            inventory.put(command.name, quantityBefore - command.quantity);
            // System.out.println("Warehouse: " + command.name + " taken: " + command.quantity);
            getSender().tell(new ProductTaken(command.name, command.quantity), getSelf());
        } else {
            System.out.println("Warehouse: Not enough " + command.name + " available.");
            System.out.println("=========" + quantityBefore + " < " + command.quantity + "=========");
            getSender().tell(new ProductNotAvailable(command.name, command.quantity), getSelf());
        }

        int quantityAfter = inventory.getOrDefault(command.name, 0);
        // System.out.println("Warehouse: " + command.name + " available after taking: " + quantityAfter);
    }



    private void sendInventoryState(GetInventory command)
    {
        getSender().tell(new InventoryState(inventory), getSelf());
    }

    static public class ProductTaken
    {
        public final String name;
        public final int quantity;
        public ProductTaken(String name, int quantity) {
            this.name = name;
            this.quantity = quantity;
        }
    }
}
