echo "Test SQS queues run."

curl --retry 120 --retry-delay 1 --retry-max-time 300 --max-time 4 --connect-timeout 2 "http://localhost:4576/?Action=CreateQueue&QueueName=ta-test-fifo-queue.fifo&Attribute.1.Name=FifoQueue&Attribute.1.Value=true&Attribute.2.Name=ContentBasedDeduplication&Attribute.2.Value=true"
echo ""
curl --retry 120 --retry-delay 1 --retry-max-time 300 --max-time 4 --connect-timeout 2 "http://localhost:4576/?Action=CreateQueue&QueueName=ta-test-first-queue"
echo ""
curl --retry 120 --retry-delay 1 --retry-max-time 300 --max-time 4 --connect-timeout 2 "http://localhost:4576/?Action=CreateQueue&QueueName=ta-test-second-queue"
echo ""

echo "Test SQS queues created."