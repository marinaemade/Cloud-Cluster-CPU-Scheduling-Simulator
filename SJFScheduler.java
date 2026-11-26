import java.util.ArrayList;
import java.util.List;

public class SJFScheduler {

    private List<Process> processes;
    private int contextSwitchTime;
    private int currentTime;

    private List<String> timeline = new ArrayList<>();

    public SJFScheduler(List<Process> processes,int contextSwitchTime) {

        if (processes == null) {
        throw new IllegalArgumentException("Process list cannot be null.");
    }

    if (contextSwitchTime < 0) {
        throw new IllegalArgumentException("Context switch time cannot be negative.");
    }

        this.processes = processes;
        this.contextSwitchTime = contextSwitchTime;
        this.currentTime = 0;
    }

    private Process findShortestProcess(){
        Process shortest = null;

        for (Process p : processes) {

            if (p.arrivalTime <= currentTime && p.remainingTime > 0) {
                if (shortest == null || p.remainingTime < shortest.remainingTime 
                    || (p.remainingTime == shortest.remainingTime && p.arrivalTime < shortest.arrivalTime) 
                    || (p.remainingTime == shortest.remainingTime &&
                        p.arrivalTime == shortest.arrivalTime &&
                        p.id.compareTo(shortest.id) < 0)) {
                    shortest = p;
                }
            }
        }
        return shortest;
    }

    public void run(){
        int completedProcesses = 0;
        Process previousProcess = null;

        while(completedProcesses< processes.size()){

            Process shortest = findShortestProcess();

            if(shortest == null){
                timeline.add("Idle");
                currentTime++;
                previousProcess = null;
                continue;
            }

            if(previousProcess !=null && previousProcess !=shortest){
                for(int i=0; i< contextSwitchTime; i++){
                    timeline.add("CS");
                    currentTime++;
                }
                shortest = findShortestProcess();
            }

            shortest.markStarted(currentTime);
            timeline.add(shortest.id);
            shortest.executeOneUnit();

            currentTime++;
            
            previousProcess =shortest;

            if(shortest.isCompleted()){
                shortest.markCompleted(currentTime);
                completedProcesses++;
            }
        }
    }

    public void printTimeline() {

        System.out.println("SJF Execution Timeline:");

        for (int i = 0; i < timeline.size(); i++) {

            System.out.println(
                "Time " + i + " - " + (i + 1) + ": " + timeline.get(i)
            );
        }
    }

    public void printResults() {

        if (processes.isEmpty()) {
            System.out.println("No processes to display.");
            return;
        }

        double totalWaitingTime = 0;
        double totalTurnaroundTime = 0;

        System.out.println("\nSJF Scheduling Results:");

        System.out.printf(
            "%-10s %-15s %-15s %-15s %-15s%n",
            "Process", "Completion", "Waiting", "Turnaround", "Response"
        );

        for (Process p : processes) {

            int completionTime = p.completionTime;
            int waitingTime = p.getWaitingTime();
            int turnaroundTime = p.getTurnaroundTime();
            int responseTime = p.getResponseTime();

            totalWaitingTime += waitingTime;
            totalTurnaroundTime += turnaroundTime;


            System.out.printf(
                "%-10s %-15d %-15d %-15d %-15d%n",
                p.id,
                completionTime,
                waitingTime,
                turnaroundTime,
                responseTime
            );
        }

        double averageWaitingTime = totalWaitingTime / processes.size();
        double averageTurnaroundTime = totalTurnaroundTime / processes.size();

        System.out.printf(
            "\nAverage Waiting Time: %.2f%n",
            averageWaitingTime
        );

        System.out.printf(
            "Average Turnaround Time: %.2f%n",
            averageTurnaroundTime
        );
    }

    public void reset() {

        currentTime = 0;

        timeline.clear();

        for (Process p : processes) {
            p.reset();
        }
    }

    
}
