package org.example.senderservice.observer;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataBaseObserverImpl implements DataBaseObserver {
    @Async
    @Scheduled(cron = "${database.observer.schedule:0 0/1 * * * *}")
    @Override
    public void watch() {
        System.out.println("Observe DB");
    }
}
