import java.util.ArrayList;
import java.util.List;

public class ACSSimulator {

    private List<Process> processes;
    private int quantum;
    private int contextSwitchTime;
    private ACSScheduler scheduler = new ACSScheduler();

    public ACSSimulator(List<Process> processes, int quantum, int contextSwitchTime) {
        if (processes == null) {
            throw new IllegalArgumentException("Process list cannot be null.");
        }
        if (quantum <= 0) {
            throw new IllegalArgumentException("Quantum must be greater than 0.");
        }
        if (contextSwitchTime < 0) {
            throw new IllegalArgumentException("Context switch time cannot be negative.");
        }
        this.processes = processes;
        this.quantum = quantum;
        this.contextSwitchTime = contextSwitchTime;
    }

    public void run() {
        List<Process> tier1Queue = new ArrayList<>();
        int currentTime = 0;
        int completed = 0;
        Process lastExecuted = null;
        boolean wasIdle = false;

        System.out.println("ACS Execution Table (Q=" + quantum + ", CS=" + contextSwitchTime + "):");
        System.out.printf("%-6s %-9s %s%n", "Time", "Process", "Tier");

        while (completed < processes.size()) {

            scheduler.admitArrivalsAtTick(currentTime, processes, tier1Queue);

            if (!tier1Queue.isEmpty()) {
                // Tier 1: Round Robin turn
                Process p = tier1Queue.get(0);
                currentTime = scheduler.executeTier1Turn(tier1Queue, quantum,
                        currentTime, contextSwitchTime, lastExecuted, processes);
                lastExecuted = p;
                wasIdle = false;
                if (p.isCompleted()) {
                    completed++;
                }
            } else if (!scheduler.isTier2Empty()) {
                // Tier 2: non-preemptive priority with aging
                Process p = scheduler.SelectNextTier2Process(currentTime);
                currentTime = scheduler.executeProcess(p, currentTime,
                        contextSwitchTime, lastExecuted, processes, tier1Queue);
                lastExecuted = p;
                wasIdle = false;
                completed++;
            } else {
                // Nothing ready: CPU is idle
                scheduler.logIdle(currentTime, !wasIdle, tier1Queue);
                wasIdle = true;
                currentTime++;
                lastExecuted = null;
            }
        }

        scheduler.logFinal(currentTime);
    }

    public void printResults() {
        System.out.println("\nACS Scheduling Results:");
        System.out.printf("%-10s %-14s %-12s %-16s %-14s %-10s%n",
                "Process", "Arrival Time", "Finish Time", "Turnaround Time", "Waiting Time", "Response");

        double totalWaiting = 0;
        double totalTurnaround = 0;
        String turnaroundText = "";
        String waitingText = "";

        for (int i = 0; i < processes.size(); i++) {
            Process p = processes.get(i);
            System.out.printf("%-10s %-14d %-12d %-16d %-14d %-10d%n",
                    p.id, p.arrivalTime, p.completionTime,
                    p.getTurnaroundTime(), p.getWaitingTime(), p.getResponseTime());
            totalWaiting += p.getWaitingTime();
            totalTurnaround += p.getTurnaroundTime();

            if (i > 0) {
                turnaroundText += " + ";
                waitingText += " + ";
            }
            turnaroundText += p.getTurnaroundTime();
            waitingText += p.getWaitingTime();
        }

        int n = processes.size();
        System.out.printf("Average Turnaround Time: (%s)/%d = %.2f time units%n",
                turnaroundText, n, totalTurnaround / n);
        System.out.printf("Average Waiting Time: (%s)/%d = %.2f time units%n",
                waitingText, n, totalWaiting / n);
    }

    public List<String> getTimeline() {
        return scheduler.getTimeline();
    }

    public double getAverageWaitingTime() {
        double total = 0;
        for (Process p : processes) {
            total += p.getWaitingTime();
        }
        return total / processes.size();
    }

    public double getAverageTurnaroundTime() {
        double total = 0;
        for (Process p : processes) {
            total += p.getTurnaroundTime();
        }
        return total / processes.size();
    }
}