echo "TA development run."

cd $(dirname $0)

sh create_test_queues.sh
sh create_dev_queues.sh

echo "TA development started."