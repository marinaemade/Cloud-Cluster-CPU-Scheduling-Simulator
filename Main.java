import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Main {

    // each row: {arrival, burst, priority}
    static int[][] testCase1 = { {0, 4, 2}, {1, 7, 3}, {3, 2, 1} };
    static int[][] testCase2 = { {0, 5, 4}, {1, 6, 2}, {4, 4, 3} };

    public static void main(String[] args) {

        runTestCase("Test Case 1", testCase1, 3, 0);
        runTestCase("Test Case 2", testCase2, 3, 1);

        // Data for the report graphs
        runSweep("Test Case 1", testCase1);
        runSweep("Test Case 2", testCase2);
    }

    static List<Process> createProcesses(int[][] data) {
        List<Process> list = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            list.add(new Process("P" + (i + 1), data[i][0], data[i][1], data[i][2]));
        }
        return list;
    }

    static void runTestCase(String name, int[][] data, int q, int cs) {
        System.out.println("==================================================");
        System.out.println(name + "  (Q = " + q + ", CS = " + cs + ")");
        System.out.println("==================================================");

        // SJF
        SJFScheduler sjf = new SJFScheduler(createProcesses(data), cs);
        sjf.run();
        sjf.printTimeline();
        sjf.printResults();

        // ACS
        System.out.println();
        ACSSimulator acs = new ACSSimulator(createProcesses(data), q, cs);
        acs.run();
        acs.printResults();
        System.out.println();
    }

    // Tries different Q and CS values and prints a table (copy it into Excel for graphs)
    static void runSweep(String name, int[][] data) {
        System.out.println("==================================================");
        System.out.println(name + " - effect of Q and CS");
        System.out.println("==================================================");
        System.out.printf("%-4s %-4s %-12s %-12s %-12s %-12s%n",
                "Q", "CS", "ACS AvgWT", "ACS AvgTAT", "SJF AvgWT", "SJF AvgTAT");

        int[] quantums = {1, 2, 3, 4, 5};
        int[] switchTimes = {0, 1, 2, 3};

        PrintStream original = System.out;

        for (int cs : switchTimes) {
            for (int q : quantums) {

                // hide the step-by-step logs while sweeping
                System.setOut(new PrintStream(new OutputStream() {
                    public void write(int b) { }
                }));

                List<Process> acsList = createProcesses(data);
                ACSSimulator acs = new ACSSimulator(acsList, q, cs);
                acs.run();

                List<Process> sjfList = createProcesses(data);
                SJFScheduler sjf = new SJFScheduler(sjfList, cs);
                sjf.run();

                System.setOut(original);

                System.out.printf("%-4d %-4d %-12.2f %-12.2f %-12.2f %-12.2f%n",
                        q, cs,
                        acs.getAverageWaitingTime(), acs.getAverageTurnaroundTime(),
                        average(sjfList, true), average(sjfList, false));
            }
        }
        System.out.println();
    }

    static double average(List<Process> list, boolean waiting) {
        double total = 0;
        for (Process p : list) {
            total += waiting ? p.getWaitingTime() : p.getTurnaroundTime();
        }
        return total / list.size();
    }
}