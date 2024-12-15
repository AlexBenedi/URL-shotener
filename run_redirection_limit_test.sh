#!/bin/bash

echo "INFO: This scripts asumes you have installed jmeter. If not, please run install_jmeter.sh first."
echo "Would you like to continue? (y/n)"
read -r response

# echo "Antes"
# echo $GOOGLE_SAFE_BROWSING_API_KEY
# echo $NINJA_PROFANITY_FILTER_API_KEY

# Load the environment variables to restore them after the tests
old_GSB_api_key=$GOOGLE_SAFE_BROWSING_API_KEY
old_Profanity_api_key=$NINJA_PROFANITY_FILTER_API_KEY

# Load the test API keys -> must be changed each time the test is run
export GOOGLE_SAFE_BROWSING_API_KEY=$GOOGLE_SAFE_BROWSING_API_KEY_LOAD_TEST
export NINJA_PROFANITY_FILTER_API_KEY=$NINJA_PROFANITY_FILTER_API_KEY_LOAD_TEST

if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
  echo "Running load tests..."
  docker-compose down
  docker-compose up -d
  echo "Initializing kafka...."
  sleep 10
  ./gradlew bootRun > /dev/null 2>&1 &
  echo "Initializing the application...."
  sleep 10
  echo "===================================================================================================="
  echo "Running load test 1: 10000 users shorting 10000 different links with QR and branded at the same time"
  echo "Redirection Limit is on, so only 6 test will pass                                                   "
  echo "===================================================================================================="
  cd apache-jmeter-5.6.3/bin
  ./jmeter -n -t ../../Load_test.jmx -l ../../results3.jtl
  # echo "Despues"
  # echo $GOOGLE_SAFE_BROWSING_API_KEY
  # echo $NINJA_PROFANITY_FILTER_API_KEY
  echo "Load tests complete. Results are saved in results3.jtl."
else
  echo "Load tests cancelled."
fi

#echo "Al final"
export GOOGLE_SAFE_BROWSING_API_KEY=$old_GSB_api_key
export NINJA_PROFANITY_FILTER_API_KEY=$old_Profanity_api_key
#echo $GOOGLE_SAFE_BROWSING_API_KEY
#echo $NINJA_PROFANITY_FILTER_API_KEY

#Option to stop the background process
echo "Would you like to stop the background process? (y/n)"
read -r stop_response
if [[ "$stop_response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
  # Find and kill the process listening on port 8080
  pid=$(lsof -t -i:8080)
  if [ -n "$pid" ]; then
    kill $pid
    echo "Process listening on port 8080 stopped."
  else
    echo "No process found listening on port 8080."
  fi
else
  echo "Background process continues running."
fi

./jmeter -g ../../results3.jtl -o ../../load_test_report3
echo "Report3 generated on load_test_report3"