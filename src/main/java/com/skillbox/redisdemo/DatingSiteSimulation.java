package com.skillbox.redisdemo;

import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
import org.redisson.config.Config;

import java.util.Random;

import static java.lang.System.out;

public class DatingSiteSimulation {

    private RedissonClient redisson;


    private RList<Integer> userQueue;

    private final static String QUEUE_KEY = "USER_QUEUE";
    private final static int TOTAL_USERS = 20;
    private final static int PAID_SERVICE_CHANCE = 10; // 1 in 10 chance

    void init() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        try {
            redisson = Redisson.create(config);
        } catch (RedisConnectionException exc) {
            out.println("Не удалось подключиться к Redis");
            out.println(exc.getMessage());
        }

        userQueue = redisson.getList(QUEUE_KEY);
        userQueue.clear();

        for (int i = 1; i <= TOTAL_USERS; i++) {
            userQueue.add(i);
        }
    }

    void shutdown() {
        redisson.shutdown();
    }

    void runSimulation() throws InterruptedException {
        Random random = new Random();

        while (true) {
            int user = userQueue.remove(0);
            out.println("— На главной странице показываем пользователя " + user);
            userQueue.add(user);

            if (random.nextInt(PAID_SERVICE_CHANCE) == 0) {
                int paidUser = userQueue.remove(random.nextInt(TOTAL_USERS));
                out.println("> Пользователь " + paidUser + " оплатил платную услугу");
                userQueue.add(0, paidUser);
            }

            Thread.sleep(1000);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        DatingSiteSimulation simulation = new DatingSiteSimulation();
        simulation.init();
        simulation.runSimulation();
        simulation.shutdown();
    }
}
