package zad3;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

enum ResourceType {
    GRAPES,
    SUGAR,
    WATER,
    BOTTLES,
    WINE
}

class ResourceRequest {
    private final ResourceType resourceType;
    private final int quantity;

    public ResourceRequest(ResourceType resourceType, int quantity) {
        this.resourceType = resourceType;
        this.quantity = quantity;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public int getQuantity() {
        return quantity;
    }
}

class ResourceState {
    private final Map<ResourceType, Integer> resourcesState;

    public ResourceState(Map<ResourceType, Integer> resourcesState) {
        this.resourcesState = resourcesState;
    }

    public Map<ResourceType, Integer> getResourcesState() {
        return resourcesState;
    }
}

class WarehouseActor extends AbstractActor {
    private final Map<ResourceType, Integer> resources = new HashMap<>();

    public WarehouseActor(Map<ResourceType, Integer> initialResources) {
        this.resources.putAll(initialResources);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ResourceRequest.class, request -> {
                    ResourceType requestedType = request.getResourceType();
                    int requestedQuantity = request.getQuantity();
                    if (resources.containsKey(requestedType) && resources.get(requestedType) >= requestedQuantity) {
                        getSender().tell(new ResourceState(Collections.singletonMap(requestedType, requestedQuantity)), getSelf());
                    } else {
                        System.out.println("Brak wystarczającej ilości zasobów w magazynie: " + requestedType);
                    }
                })
                .build();
    }
}

class PressingActor extends AbstractActor {
    private final Map<ResourceType, Integer> requiredResources = new HashMap<>() {{
        put(ResourceType.GRAPES, 15);
    }};

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ResourceState.class, state -> {
                    if (state.getResourcesState().getOrDefault(ResourceType.GRAPES, 0) >= requiredResources.get(ResourceType.GRAPES)) {
                        getContext().getSystem().actorSelection("/user/warehouseActor").tell(new ResourceRequest(ResourceType.GRAPES, requiredResources.get(ResourceType.GRAPES)), getSelf());
                    } else {
                        System.out.println(state.getResourcesState().getOrDefault(ResourceType.GRAPES, 0));
                        System.out.println("Brak wystarczającej ilości surowców do tłoczenia wina.");
                    }
                })
                .build();
    }
}

class FermentationActor extends AbstractActor {
    private final Map<ResourceType, Integer> requiredResources = new HashMap<>() {{
        put(ResourceType.WINE, 15);
        put(ResourceType.WATER, 8);
        put(ResourceType.SUGAR, 2);
    }};

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ResourceState.class, state -> {
                    getContext().getSystem().actorSelection("/user/bottlingActor").tell(new ResourceRequest(ResourceType.WINE, requiredResources.get(ResourceType.WINE)), getSelf());
                })
                .build();
    }
}

class BottlingActor extends AbstractActor {
    private final Map<ResourceType, Integer> requiredResources = new HashMap<>() {{
        put(ResourceType.WINE, 25);
    }};

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ResourceState.class, state -> {
                    getContext().getSystem().actorSelection("/user/warehouseActor").tell(new ResourceRequest(ResourceType.BOTTLES, requiredResources.get(ResourceType.BOTTLES)), getSelf());
                })
                .build();
    }
}

public class WineProductionExample {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("WineProductionSystem");

        // Początkowe ilości surowców w magazynie
        Map<ResourceType, Integer> initialResources = new HashMap<>();
        initialResources.put(ResourceType.GRAPES, 1000); // Przykładowa początkowa ilość winogron
        initialResources.put(ResourceType.SUGAR, 1000); // Przykładowa początkowa ilość cukru
        initialResources.put(ResourceType.WATER, 1000); // Przykładowa początkowa ilość wody
        initialResources.put(ResourceType.BOTTLES, 1000); // Przykładowa początkowa ilość butelek

        // Tworzenie aktora magazynu
        ActorRef warehouseActor = system.actorOf(Props.create(WarehouseActor.class, initialResources), "warehouseActor");

        // Tworzenie aktorów procesów
        ActorRef pressingActor = system.actorOf(Props.create(PressingActor.class), "pressingActor");
        ActorRef fermentationActor = system.actorOf(Props.create(FermentationActor.class), "fermentationActor");
        ActorRef bottlingActor = system.actorOf(Props.create(BottlingActor.class), "bottlingActor");

        // Rozpoczęcie procesu produkcji wina
        pressingActor.tell(new ResourceState(new HashMap<>()), ActorRef.noSender());

        // Zakończenie systemu aktorów
        system.terminate();
    }
}
