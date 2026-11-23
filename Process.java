public class Process{
    String id;
    int arrivalTime;
    int burstTime;
    int remainingTime;
    int priority;
    int firstStartTime;
    int completionTime;
    int currentPriority;
    int tier2EntryTime;


    public Process(String id, int arrivalTime,
                int burstTime, int priority) {

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

    public int getWaitingTime() {
        return getTurnaroundTime() - burstTime;
    }

    public int getResponseTime() {
       return firstStartTime - arrivalTime;
    }

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
}