#!/bin/bash

echo "INFO: This scripts asumes you have installed jmeter. If not, please run install_jmeter.sh first."
echo "====================================================================================================="
echo "/!\ WARNING: These tests can only be run once per day. To run them more frequently, please register on the Google Play Console and API Ninjas to obtain your own Google Safe Browsing and Profanity Ninjas API keys."
echo "====================================================================================================="
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
  ./gradlew bootRun --args='--spring.profiles.active=dev-async' > /dev/null 2>&1 &
  echo "Initializing the application...."
  sleep 10
  echo "===================================================================================================="
  echo "Running load test 1: 10000 users shorting 10000 different links with QR and branded at the same time"
  echo "===================================================================================================="
  cd apache-jmeter-5.6.3/bin
  ./jmeter -n -t ../../Load_test.jmx -l ../../results.jtl
  docker-compose down
  docker-compose up -d
  echo "Initializing kafka...."
  sleep 10
  echo "========================================================================================================================="
  echo "Running load test 2: 2500 users shorting 2500 different links and then accessing them uniformly distributed in 5 minutes" 
  echo "=========================================================================================================================="
  ./jmeter -n -t ../../Load_test_v2.jmx -l ../../results2.jtl
  # echo "Despues"
  # echo $GOOGLE_SAFE_BROWSING_API_KEY
  # echo $NINJA_PROFANITY_FILTER_API_KEY
  echo "Load tests complete. Results are saved in load_tests.jtl."
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

./jmeter -g ../../results.jtl -o ../../load_test_report
./jmeter -g ../../results2.jtl -o ../../load_test_report2
echo "Report1 generated on load_test_report folder and report2 generated on load_test_report2."