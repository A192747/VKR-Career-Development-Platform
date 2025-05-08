from confluent_kafka import Producer, Consumer, KafkaError
import json
import logging
from typing import Dict, Any

logger = logging.getLogger(__name__)

class KafkaService:
    def __init__(self, bootstrap_servers: str = 'kafka1:29092'):
        logger.info(f"Initializing KafkaService with bootstrap_servers={bootstrap_servers}")
        try:
            self.producer = Producer({
                'bootstrap.servers': bootstrap_servers,
                'retries': 3,
                'max.block.ms': 5000
            })
            logger.info("KafkaProducer initialized successfully")
        except Exception as e:
            logger.error(f"Failed to initialize KafkaProducer: {e}")
            raise

        try:
            self.consumer = Consumer({
                'bootstrap.servers': bootstrap_servers,
                'group.id': 'rag_processor',
                'auto.offset.reset': 'earliest',
                'session.timeout.ms': 60000,
                'heartbeat.interval.ms': 20000,
                'max.poll.interval.ms': 900000
            })
            self.consumer.subscribe(['content_processing'])
            logger.info("KafkaConsumer initialized successfully")
        except Exception as e:
            logger.error(f"Failed to initialize KafkaConsumer: {e}")
            raise

        self.topic = 'content_processing'

    def send_content(self, content_data: Dict[str, Any]):
        try:
            self.producer.produce(self.topic, json.dumps(content_data).encode('utf-8'))
            self.producer.flush(timeout=5.0)
            logger.info("Content sent to Kafka")
        except Exception as e:
            logger.error(f"Error sending to Kafka: {e}")
            raise

    def consume_content(self):
        logger.info("Starting to consume messages from Kafka")
        try:
            while True:
                msg = self.consumer.poll(timeout=1.0)
                if msg is None:
                    continue
                if msg.error():
                    logger.error(f"Kafka error: {msg.error()}")
                    continue
                logger.info(f"Consumed message: {msg.value().decode('utf-8')}")
                yield json.loads(msg.value().decode('utf-8'))
                self.consumer.commit()
        except Exception as e:
            logger.error(f"Error consuming Kafka messages: {e}")
            raise