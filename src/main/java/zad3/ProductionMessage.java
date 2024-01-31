package zad3;

public class ProductionMessage {
    private final boolean success;
    private final int processedResources;

    public ProductionMessage(boolean success, int processedResources) {
        this.success = success;
        this.processedResources = processedResources;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getProduced() {
        return processedResources;
    }
}

