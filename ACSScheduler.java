import java.util.ArrayList;
import java.util.List;
public class ACSScheduler {
private final List<Process> tier2Queue= new ArrayList<>();

//Called by when a process consumes its full Round Robin quantum
public void MoveTotier2(Process p ,int currentTime){
    p.tier2EntryTime = currentTime;
        p.currentPriority = p.priority;
        tier2Queue.add(p);
}
//Calculates how many total time units the process has been sitting idle in Tier 2
// the higher time spent the greater priority become -4 units spent waiting in Tier 2 improves priority by 1
public int getActualPriorty(Process p, int currentTime){
int waitingTime = currentTime -p.tier2EntryTime;
int agingBoost = waitingTime/4;
p.currentPriority=Math.max(1,p.priority - agingBoost); // to avoid -ve values be at least 1 
    return p.currentPriority;
}
//selects the next process based on current priority 
public Process SelectNextTier2Process(int currentTime){
    if(tier2Queue.isEmpty()){
        return null;
    }
    Process selected =null;
    int HighestPriority =Integer.MAX_VALUE;
    for(Process p : tier2Queue){
        int ActualPriority =getActualPriorty(p, currentTime);
        if(ActualPriority < HighestPriority){
            HighestPriority=ActualPriority;
            selected =p;
        }//tie breaker when 2 have the same priority the priority goes to the first come
          else if(ActualPriority==HighestPriority){
            if (p.tier2EntryTime < selected.tier2EntryTime) {
                    selected = p;
                }
        
    } }
    tier2Queue.remove(selected);
    return selected ;
}
public int executeProcess(Process p ,int currentTime,
            int cs,
            Process lastExecuted,
            List<Process> allProcesses,
            List<Process> tier1Queue ){

if (lastExecuted != null && lastExecuted != p && cs > 0) {
            for (int i = 0; i < cs; i++) {
                admitArrivalsAtTick(currentTime, allProcesses, tier1Queue);
                currentTime++;
            }
            p.markStarted(currentTime); }

       // since it is non preemitive so it should wait untill it finishes
        while (!p.isCompleted()) {
            admitArrivalsAtTick(currentTime, allProcesses, tier1Queue);

            // Refresh aged priorities of waiting Tier 2 processes before logging
            for (Process waiting : tier2Queue) {
                getActualPriorty(waiting, currentTime);
            }
            logTimeStep(currentTime, p, tier1Queue);

            p.executeOneUnit();
            currentTime++;}

p.markCompleted(currentTime);
        return currentTime ;
          
}
// loops to make the cpu clocking make sure that the next process enter if there is no current process
private void admitArrivalsAtTick(int currentTime, List<Process> allProcesses, List<Process> tier1Queue) {
        for (Process candidate : allProcesses) {
            if (candidate.arrivalTime == currentTime
                    && !candidate.isCompleted()
                    && !tier1Queue.contains(candidate)
                    && !tier2Queue.contains(candidate)) {
                tier1Queue.add(candidate);
            }
        }
    }
    //build snapchots of the cpu executing 
    //
private void logTimeStep(int time, Process running, List<Process> tier1Queue) {
        StringBuilder sb = new StringBuilder();
        sb.append("T=").append(time).append(" | Running: ").append(running.id);
        sb.append(" | Tier 1: [");
        for (int i = 0; i < tier1Queue.size(); i++) {
            Process t1 = tier1Queue.get(i);
            sb.append(t1.id).append("(rem:").append(t1.remainingTime)
              .append(", prio:").append(t1.currentPriority).append(")");
            if (i < tier1Queue.size() - 1) sb.append(", ");
        }
        sb.append("] | Tier 2: [");
        for (int i = 0; i < tier2Queue.size(); i++) {
            Process t2 = tier2Queue.get(i);
            sb.append(t2.id).append("(rem:").append(t2.remainingTime)
              .append(", prio:").append(t2.currentPriority).append(")");
            if (i < tier2Queue.size() - 1) sb.append(", ");
        }
        sb.append("]");
        System.out.println(sb.toString());
    }

    public boolean isTier2Empty() {
        return tier2Queue.isEmpty();
    }

    public List<Process> getTier2Queue() {
        return tier2Queue;
    }

}

