from kafka import KafkaProducer, KafkaConsumer
import json
import logging
from typing import Dict, Any

logger = logging.getLogger(__name__)

class KafkaService:
    def __init__(self, bootstrap_servers: str = 'kafka1:29092'):
        logger.info(f"Initializing KafkaService with bootstrap_servers={bootstrap_servers}")
        try:
            self.producer = KafkaProducer(
                bootstrap_servers=bootstrap_servers,
                value_serializer=lambda v: json.dumps(v).encode('utf-8'),
                retries=3,
                max_block_ms=5000
            )
            logger.info("KafkaProducer initialized successfully")
        except Exception as e:
            logger.error(f"Failed to initialize KafkaProducer: {e}")
            raise

        try:
            self.consumer = KafkaConsumer(
                'content_processing',
                bootstrap_servers=bootstrap_servers,
                value_deserializer=lambda x: json.loads(x.decode('utf-8')),
                auto_offset_reset='earliest',
                group_id='rag_processor',
                max_poll_interval_ms=900000,  # 15 minutes
                session_timeout_ms=60000,     # 60 seconds
                heartbeat_interval_ms=20000,  # 20 seconds
                max_poll_records=1            # Process one message at a time
            )
            logger.info("KafkaConsumer initialized successfully")
        except Exception as e:
            logger.error(f"Failed to initialize KafkaConsumer: {e}")
            raise

        self.topic = 'content_processing'

    def send_content(self, content_data: Dict[str, Any]):
        try:
            self.producer.send(self.topic, content_data)
            self.producer.flush(timeout=5.0)
            logger.info("Content sent to Kafka")
        except Exception as e:
            logger.error(f"Error sending to Kafka: {e}")
            raise

    def consume_content(self):
        logger.info("Starting to consume messages from Kafka")
        try:
            while True:
                logger.debug("Polling Kafka consumer")
                messages = self.consumer.poll(timeout_ms=1000, max_records=1)
                for tp, msgs in messages.items():
                    for message in msgs:
                        logger.info(f"Consumed message: {message.value}")
                        yield message.value
                self.consumer.commit()
        except Exception as e:
            logger.error(f"Error consuming Kafka messages: {e}")
            raise