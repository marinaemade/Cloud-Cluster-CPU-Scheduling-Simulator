import java.util.ArrayList;
import java.util.List;

public class ACSScheduler {

    //list of processes that have been demoted from Tier 1
    private final List<Process> tier2Queue= new ArrayList<>();

    // position the chosen process had in Tier 2 (so the table can still show it there)
    private int selectedIndex = 0;

    // one label per time unit (P1, CS, Idle) - used by the GUI Gantt chart
    private final List<String> timeline = new ArrayList<>();

    //Called when a process consumes its full Round Robin quantum
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
        selectedIndex = tier2Queue.indexOf(selected);
        tier2Queue.remove(selected);
        return selected ;
    }

    // Runs one Tier 1 Round Robin turn.
    // An unfinished process is demoted after consuming its full quantum.
    public int executeTier1Turn(
            List<Process> tier1Queue,
            int quantum,
            int currentTime,
            int cs,
            Process lastExecuted,
            List<Process> allProcesses) {

        if (quantum <= 0) {
            throw new IllegalArgumentException(
                    "Quantum must be greater than 0.");
        }
        if (cs < 0) {
            throw new IllegalArgumentException(
                    "Context switch time cannot be negative.");
        }

        // Add processes that arrived at the current time.
        admitArrivalsAtTick(currentTime, allProcesses, tier1Queue);

        if (tier1Queue.isEmpty()) {
            return currentTime;
        }

        // Take the process at the front of Tier 1 (it stays in the queue until it really gets the CPU)
        Process p = tier1Queue.get(0);
        boolean segmentStart = true; // true until the first row of this turn is printed

        // Charge context-switch time when switching between processes.
        if (lastExecuted != null && lastExecuted != p) {
            for (int i = 0; i < cs; i++) {
                boolean arrived = admitArrivalsAtTick(currentTime, allProcesses, tier1Queue);
                if (segmentStart) {
                    logRow(currentTime, "CS", true, contextSwitchNote(lastExecuted, p), true, tier1Queue);
                    segmentStart = false;
                } else {
                    logRow(currentTime, "CS", false, "", arrived, tier1Queue);
                }
                currentTime++;
            }
        }

        // after a context switch the process leaves Tier 1 now and starts running
        if (!segmentStart) {
            tier1Queue.remove(p);
        }
        p.markStarted(currentTime);

        // Run for the quantum or until the process finishes.
                int runTime = Math.min(quantum, p.remainingTime);
                boolean firstRunRow = true;
                boolean leftTier1AfterCS = !segmentStart; // Tier 1 changes when p leaves it after a context switch

        for (int i = 0; i < runTime; i++) {
            boolean arrived = admitArrivalsAtTick(currentTime, allProcesses, tier1Queue);
            logRow(currentTime, p.id, firstRunRow, "",
                arrived || segmentStart || (leftTier1AfterCS && firstRunRow), tier1Queue);
            if (segmentStart) {
                tier1Queue.remove(p); // it was still shown in Tier 1 on its first row
                segmentStart = false;
            }
            firstRunRow = false;

            p.executeOneUnit();
            currentTime++;
        }

        if (p.isCompleted()) {
            p.markCompleted(currentTime);
        } else {
            // The assignment demotes unfinished processes after a full quantum.
            MoveTotier2(p, currentTime);
        }

        return currentTime;
    }

    public int executeProcess(Process p ,int currentTime,
        int cs,
        Process lastExecuted,
        List<Process> allProcesses,
        List<Process> tier1Queue )
    {
        // p stays visible in Tier 2 on its first row of the table
        tier2Queue.add(Math.min(selectedIndex, tier2Queue.size()), p);
        boolean segmentStart = true;

        if (lastExecuted != null && lastExecuted != p) {
            for (int i = 0; i < cs; i++) {
                boolean arrived = admitArrivalsAtTick(currentTime, allProcesses, tier1Queue);
                if (segmentStart) {
                    logRow(currentTime, "CS", true, "Context switch loads " + p.id, true, tier1Queue);
                    segmentStart = false;
                    tier2Queue.remove(p);
                } else {
                    logRow(currentTime, "CS", false, "", arrived, tier1Queue);
                }
                currentTime++;
            }
        }
        p.markStarted(currentTime);

        // since it is non preemitive so it should wait untill it finishes
        boolean firstRunRow = true;
        while (!p.isCompleted()) {
            boolean arrived = admitArrivalsAtTick(currentTime, allProcesses, tier1Queue);
            logRow(currentTime, p.id, firstRunRow, "", arrived || segmentStart, tier1Queue);
            if (segmentStart) {
                tier2Queue.remove(p);
                segmentStart = false;
            }
            firstRunRow = false;

            p.executeOneUnit();
            currentTime++;
        }

        p.markCompleted(currentTime);
        return currentTime ;
    }

    // loops to make the cpu clocking make sure that the next process enter if there is no current process
    // returns true if a new process entered Tier 1 at this time
    public boolean admitArrivalsAtTick(int currentTime, List<Process> allProcesses, List<Process> tier1Queue) {
        boolean admitted = false;
        for (Process candidate : allProcesses) {
            if (candidate.arrivalTime == currentTime
                    && candidate.firstStartTime == -1   // only brand-new processes
                    && !candidate.isCompleted()
                    && !tier1Queue.contains(candidate)
                    && !tier2Queue.contains(candidate)) {
                tier1Queue.add(candidate);
                admitted = true;
            }
        }
        return admitted;
    }

    // ---------------- table output (Time | Process | Tier) ----------------

    private String contextSwitchNote(Process from, Process to) {
        if (from.isCompleted()) {
            return "Context switch loads " + to.id;
        }
        return "Context switch saves " + from.id + " and loads " + to.id;
    }

    private String queueText(List<Process> queue) {
        if (queue.isEmpty()) {
            return "[Empty]";
        }
        String text = "[";
        for (int i = 0; i < queue.size(); i++) {
            Process p = queue.get(i);
            text += p.id + " (remaining: " + p.remainingTime + ", Priority: " + p.currentPriority + ")";
            if (i < queue.size() - 1) {
                text += ", ";
            }
        }
        return text + "]";
    }

    // prints one row of the table (one time unit)
    // who      = P1 / CS / Idle (always saved for the Gantt chart)
    // showWho  = print the name only on the first row of a block, like a merged cell
    // showTiers= print the Tier 1 / Tier 2 snapshot (only when something changed)
    private void logRow(int time, String who, boolean showWho, String note,
                        boolean showTiers, List<Process> tier1Queue) {

        // Priorities are NOT re-aged here, the aged value
        // is calculated only when a Tier 2 process is selected (SelectNextTier2Process)
        timeline.add(who);

        String prefix = String.format("%-6d %-9s ", time, showWho ? who : "");
        String indent = "                 ";

        List<String> lines = new ArrayList<>();
        if (!note.isEmpty()) {
            lines.add(note);
        }
        if (showTiers) {
            lines.add("Tier 1: " + queueText(tier1Queue));
            if (!tier2Queue.isEmpty()) {
                lines.add("Tier 2: " + queueText(tier2Queue));
            }
        }

        if (lines.isEmpty()) {
            System.out.println(prefix);
            return;
        }
        System.out.println(prefix + lines.get(0));
        for (int i = 1; i < lines.size(); i++) {
            System.out.println(indent + lines.get(i));
        }
    }

    // CPU idle row (name and snapshot are shown only on the first idle time unit)
    public void logIdle(int time, boolean first, List<Process> tier1Queue) {
        logRow(time, "Idle", first, "", first, tier1Queue);
    }

    // last row: everything finished
    public void logFinal(int time) {
        System.out.println(String.format("%-6d %-9s ", time, "") + "Tier 1: [Empty]");
        System.out.println("                 Tier 2: [Empty]");
    }

    public List<String> getTimeline() {
        return timeline;
    }

    public boolean isTier2Empty() {
        return tier2Queue.isEmpty();
    }

    public List<Process> getTier2Queue() {
        return tier2Queue;
    }
public void reset() {
    tier2Queue.clear();
    timeline.clear();
    selectedIndex = 0;
}
}