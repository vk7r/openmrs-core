#!/bin/bash

# Number of iterations to calculate mean
iterations=50
total_time=0

for i in $(seq 1 $iterations); do
  echo "Iteration $i: Restarting container..."

  # Restart the container
  docker-compose restart

  # Measure the time it takes to recover
  start_time=$(date +%s)
  
  # Replace this with your health check command or URL to check readiness
  while ! curl -s http://localhost:8080/openmrs; do
    sleep 1
  done

  end_time=$(date +%s)
  recovery_time=$((end_time - start_time))
  
  echo "Recovery time for iteration $i: $recovery_time seconds"
  total_time=$((total_time + recovery_time))
done

mean_recovery_time=$((total_time / iterations))
echo "Mean recovery time: $mean_recovery_time seconds"
