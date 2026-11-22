
# Cloud-Cluster CPU Scheduling Simulator

A Java-based Operating System simulator that implements and compares CPU scheduling algorithms for mixed workloads, including CPU-intensive batch jobs and latency-sensitive interactive tasks.

## Project Overview

This project simulates CPU scheduling in a cloud-cluster environment using two algorithms:

1. **Preemptive Shortest Job First (SJF):** Always executes the process with the shortest remaining burst time.
2. **Adaptive Container Scheduler (ACS):** A custom multi-tier scheduling algorithm designed to balance responsiveness and CPU utilization.

## Scheduling Algorithms

### 1. Preemptive SJF
- Selects the process with the shortest remaining execution time.
- Preempts the running process when a shorter job arrives.
- Accounts for context switching overhead.

### 2. Adaptive Container Scheduler (ACS)

ACS consists of three tiers:

| Tier | Scheduling Strategy | Description |
|---|---|---|
| Tier 1 | Round Robin | All new processes enter this queue and receive a fixed time quantum. |
| Tier 2 | Non-Preemptive Priority | Processes that use their entire quantum are demoted to this queue and execute until completion. |
| Tier 3 | Aging | Gradually improves the priority of waiting processes to prevent starvation. |

## Performance Metrics

The simulator calculates and displays:

- Waiting Time (WT)
- Turnaround Time (TAT)
- Response Time (RT)
- Average Waiting Time
- Average Turnaround Time

It also generates heuristic demotion matrices showing the scheduler's state at each time step, including process execution and queue contents.

## Test Cases

The project includes the two test cases provided in the assignment, with different:

- Process arrival times
- Burst times
- Initial priorities
- Time quantum (Q)
- Context switching time (CS)

Additional test cases can be used to evaluate scheduling behavior under different workloads.

## Technologies Used

- **Java** – Scheduling algorithms and simulation logic.
- **Object-Oriented Programming (OOP)** – Project structure and implementation.
- **Git & GitHub** – Version control and collaboration.

## Course Information

- **University:** Cairo University
- **Faculty:** Faculty of Computers and Artificial Intelligence
- **Course:** CS241 – Operating System 1
- **Assignment:** Assignment 2 – Cloud-Cluster CPU Scheduling
