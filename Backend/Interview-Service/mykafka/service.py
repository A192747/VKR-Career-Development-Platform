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
                value_serializer=lambda v: json.dumps(v).encode('utf-8')
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
                group_id='rag_processor'
            )
            logger.info("KafkaConsumer initialized successfully")
        except Exception as e:
            logger.error(f"Failed to initialize KafkaConsumer: {e}")
            raise

        self.topic = 'content_processing'

    def send_content(self, content_data: Dict[str, Any]):
        try:
            self.producer.send(self.topic, content_data)
            self.producer.flush()
            logger.info("Content sent to Kafka")
        except Exception as e:
            logger.error(f"Error sending to Kafka: {e}")
            raise

    def consume_content(self):
        logger.info("Starting to consume messages from Kafka")
        try:
            for message in self.consumer:
                logger.info(f"Consumed message: {message.value}")
                yield message.value
        except Exception as e:
            logger.error(f"Error consuming Kafka messages: {e}")
            raise
