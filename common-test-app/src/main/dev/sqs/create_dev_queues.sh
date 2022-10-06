echo "Development SQS queues run."

curl --retry 120 --retry-delay 1 --retry-max-time 300 --max-time 4 --connect-timeout 2 "http://localhost:4576/?Action=CreateQueue&QueueName=ta-dev-fifo-queue.fifo&Attribute.1.Name=FifoQueue&Attribute.1.Value=true&Attribute.2.Name=ContentBasedDeduplication&Attribute.2.Value=true"
echo ""
curl --retry 120 --retry-delay 1 --retry-max-time 300 --max-time 4 --connect-timeout 2 "http://localhost:4576/?Action=CreateQueue&QueueName=ta-dev-first-queue"
echo ""
curl --retry 120 --retry-delay 1 --retry-max-time 300 --max-time 4 --connect-timeout 2 "http://localhost:4576/?Action=CreateQueue&QueueName=ta-dev-second-queue"
echo ""

echo "Development SQS queues created."