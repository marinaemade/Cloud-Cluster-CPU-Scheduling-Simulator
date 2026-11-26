public class Process{
    String id;  //Process name P1, P2,..
    int arrivalTime;
    int burstTime;  //Total CPU time the process needs
    int remainingTime;
    int priority;

    int firstStartTime;
    int completionTime;

    int currentPriority; // Used by ACS: the priority after applying aging
    int tier2EntryTime; // The time this process entered Tier 2


    public Process(String id, int arrivalTime,
        int burstTime, int priority) {
            if (id == null ) {
            throw new IllegalArgumentException("Process ID cannot be empty.");
        }
        if (arrivalTime < 0 || burstTime <= 0 || priority <= 0) {
            throw new IllegalArgumentException(
                "Arrival must be >= 0, burst and priority must be > 0"
            );
        }


        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
        this.firstStartTime = -1;
        this.completionTime = -1;
        this.currentPriority = priority;
        this.tier2EntryTime = -1;
    }

    public int getTurnaroundTime() {
        return completionTime - arrivalTime;
    }

    // Includes time spent waiting and context-switching
    public int getWaitingTime() {
        return getTurnaroundTime() - burstTime;
    }

    public int getResponseTime() {
       return firstStartTime - arrivalTime;
    }

    // Record only the first time the process gets the CPU
    public void markStarted(int currentTime) {
        if (firstStartTime == -1) {
            firstStartTime = currentTime;
        }
    }

    public void markCompleted(int currentTime) {
        completionTime = currentTime;
    }


    public boolean isCompleted() {
        return remainingTime == 0;
    }

    public void executeOneUnit() {
        if (remainingTime > 0) {
            remainingTime--;
        }
    }

    public void reset() {
        remainingTime = burstTime;
        firstStartTime = -1;
        completionTime = -1;
        currentPriority = priority;
        tier2EntryTime = -1;
    }
    public int getResponseTime() {
    return firstStartTime == -1 ? -1 : firstStartTime - arrivalTime;
}
}